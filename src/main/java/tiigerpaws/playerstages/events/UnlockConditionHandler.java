package tiigerpaws.playerstages.events;

import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import tiigerpaws.playerstages.PlayerStages;
import tiigerpaws.playerstages.api.PlayerStageManager;
import tiigerpaws.playerstages.api.StageData;
import tiigerpaws.playerstages.api.UnlockCondition;
import tiigerpaws.playerstages.config.ConfigLoader;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.advancement.Advancement;
import net.minecraft.entity.ItemEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.server.world.ServerWorld;

public class UnlockConditionHandler {
    public static void init() {
        registerItemPickupListener();
        registerAdvancementListener();
        registerMobKillListener();
        registerDimensionVisitListener();
        registerBlockPlaceListener();
        registerBlockBreakListener();
        registerItemCraftListener();
    }

    private static void registerItemPickupListener() {
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (world.isClient || !(player instanceof ServerPlayerEntity serverPlayer)) return;
            world.getEntitiesByClass(ItemEntity.class, player.getBoundingBox().expand(2.0), itemEntity -> true)
                    .forEach(itemEntity -> {
                        Identifier itemId = Registries.ITEM.getId(itemEntity.getStack().getItem());
                        checkUnlockConditions(serverPlayer, itemId, itemEntity.getStack().getCount(), "item_pickup");
                    });
        });
    }

    private static void registerAdvancementListener() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            for (Advancement advancement : server.getAdvancementLoader().getAdvancements()) {
                if (player.getAdvancementTracker().getProgress(advancement).isDone()) {
                    checkUnlockConditions(player, advancement.getId(), 1, "advancement");
                }
            }
        });
    }

    private static void registerMobKillListener() {
        ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.register((world, killer, killed) -> {
            if (killer instanceof ServerPlayerEntity player) {
                Identifier entityId = Registries.ENTITY_TYPE.getId(killed.getType());
                checkUnlockConditions(player, entityId, 1, "mob_kill");
            }
        });
    }

    private static void registerDimensionVisitListener() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            Identifier dimensionId = player.getWorld().getRegistryKey().getValue();
            checkUnlockConditions(player, dimensionId, 1, "dimension_visit");
        });
    }

    private static void registerBlockPlaceListener() {
        // Use Fabric API event for block placement
        // Example: UseBlockCallback.EVENT
    }

    private static void registerBlockBreakListener() {
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (world.isClient || !(player instanceof ServerPlayerEntity serverPlayer)) return;
            Identifier blockId = Registries.BLOCK.getId(state.getBlock());
            checkUnlockConditions(serverPlayer, blockId, 1, "block_break");
        });
    }

    private static void registerItemCraftListener() {
        // Use Fabric API event for crafting (e.g., CraftingResultCallback)
    }

    public static void checkUnlockConditions(ServerPlayerEntity player, Identifier targetId, int count, String conditionType) {
        String currentStageId = PlayerStageManager.getStage(player);
        StageData currentStage = ConfigLoader.getStage(currentStageId);
        if (currentStage == null) return;

        for (StageData stage : ConfigLoader.getAllStages().values()) {
            if (stage.getLevel() <= currentStage.getLevel()) continue;
            for (UnlockCondition condition : stage.getUnlockConditions()) {
                if (condition.getType().equals(conditionType) && condition.isMet(player, targetId, count)) {
                    currentStage.setLocked(false);
                    if (!currentStage.isFinal()) {
                        String nextStageId = PlayerStageManager.getNextStage(currentStageId);
                        PlayerStageManager.setStage(player, nextStageId);
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