package tiigerpaws.playerstages.events;

import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import tiigerpaws.playerstages.PlayerStages;
import tiigerpaws.playerstages.config.StageConfig;
import tiigerpaws.playerstages.stages.StageManager;
import net.minecraft.registry.Registries;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class ItemUseRestriction {
    public static void register() {
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (player instanceof ServerPlayerEntity serverPlayer && !world.isClient && !serverPlayer.isCreative()) {
                String stageId = StageManager.getPlayerStage(serverPlayer);
                ItemStack stack = player.getStackInHand(hand);
                Identifier itemId = Registries.ITEM.getId(stack.getItem());
                if (StageConfig.isRestricted(stageId, itemId, "item")) {
                    PlayerStages.LOGGER.debug("Restricted item use {} for player {} in stage {}", itemId, serverPlayer.getName().getString(), stageId);
                    serverPlayer.sendMessage(Text.literal("You are not experienced enough to use this item!")
                            .styled(style -> style.withColor(0xFF5555).withItalic(true)), true);
                    return TypedActionResult.fail(RestrictionHandler.applyRestrictions(serverPlayer, stack, world) ? ItemStack.EMPTY : stack);
                }
            }
            return TypedActionResult.pass(player.getStackInHand(hand));
        });
    }
}