package tiigerpaws.playerstages.stages;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import tiigerpaws.playerstages.config.StageConfig;
import tiigerpaws.playerstages.stages.StageData.UnlockCondition;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.server.ServerAdvancementLoader;

public class UnlockConditionHandler {
    public static void init() {
        registerItemPickupListener();
        registerAdvancementListener();
    }

    private static void registerItemPickupListener() {
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (world.isClient || !(player instanceof ServerPlayerEntity serverPlayer)) return;

            world.getEntitiesByClass(ItemEntity.class, player.getBoundingBox().expand(2.0), itemEntity -> true)
                    .forEach(itemEntity -> {
                        Item item = itemEntity.getStack().getItem();
                        Identifier itemId = Registries.ITEM.getId(item);
                        checkUnlockConditions(serverPlayer, itemId, itemEntity.getStack().getCount(), "item_pickup");
                    });
        });
    }

    private static void registerAdvancementListener() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            ServerAdvancementLoader loader = server.getAdvancementLoader();
            for (Advancement advancement : loader.getAdvancements()) {
                AdvancementProgress progress = player.getAdvancementTracker().getProgress(advancement);
                if (progress.isDone()) {
                    checkUnlockConditions(player, advancement.getId(), 1, "advancement");
                }
            }
        });
    }

    private static void checkUnlockConditions(ServerPlayerEntity player, Identifier targetId, int count, String conditionType) {
        String currentStageId = StageManager.getPlayerStage(player);
        StageData currentStage = StageConfig.getStage(currentStageId);

        for (StageData stage : StageConfig.getAllStages().values()) {
            if (stage.getLevel() <= currentStage.getLevel()) continue; // Only check higher-level stages

            for (UnlockCondition condition : stage.getUnlockConditions()) {
                if (condition.type().equals(conditionType) && condition.target().equals(targetId)) {
                    if (conditionType.equals("item_pickup") && count >= condition.count()) {
                        currentStage.setLocked(false); // Unlock current stage
                        if (!currentStage.isFinal()) {
                            String nextStageId = StageManager.getNextStage(currentStageId);
                            StageManager.setPlayerStage(player, nextStageId);
                            player.sendMessage(Text.literal("Unlocked stage: " + stage.getName()), false);
                        } else {
                            player.sendMessage(Text.literal("Stage " + currentStage.getName() + " fully unlocked!"), false);
                        }
                        break;
                    } else if (conditionType.equals("advancement")) {
                        currentStage.setLocked(false); // Unlock current stage
                        if (!currentStage.isFinal()) {
                            String nextStageId = StageManager.getNextStage(currentStageId);
                            StageManager.setPlayerStage(player, nextStageId);
                            player.sendMessage(Text.literal("Unlocked stage: " + stage.getName()), false);
                        } else {
                            player.sendMessage(Text.literal("Stage " + currentStage.getName() + " fully unlocked!"), false);
                        }
                        break;
                    }
                }
            }
        }
    }
}