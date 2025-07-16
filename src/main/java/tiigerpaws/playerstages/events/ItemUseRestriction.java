package tiigerpaws.playerstages.events;

import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.TypedActionResult;
import tiigerpaws.playerstages.PlayerStages;
import tiigerpaws.playerstages.api.PlayerStageManager;
import tiigerpaws.playerstages.config.ConfigLoader;
import net.minecraft.registry.Registries;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class ItemUseRestriction {
    public static void register() {
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (player instanceof ServerPlayerEntity serverPlayer && !world.isClient && !serverPlayer.isCreative()) {
                String stageId = PlayerStageManager.getStage(serverPlayer);
                ItemStack stack = player.getStackInHand(hand);
                Identifier itemId = Registries.ITEM.getId(stack.getItem());
                if (ConfigLoader.isRestricted(stageId, itemId, "item")) {
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