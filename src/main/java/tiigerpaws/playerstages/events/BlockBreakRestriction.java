package tiigerpaws.playerstages.events;

import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import tiigerpaws.playerstages.PlayerStages;
import tiigerpaws.playerstages.config.StageConfig;
import tiigerpaws.playerstages.stages.StageManager;
import net.minecraft.registry.Registries;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class BlockBreakRestriction {
    public static void register() {
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            if (player instanceof ServerPlayerEntity serverPlayer && !world.isClient && !serverPlayer.isCreative()) {
                String stageId = StageManager.getPlayerStage(serverPlayer);
                Identifier blockId = Registries.BLOCK.getId(world.getBlockState(pos).getBlock());
                if (StageConfig.isRestricted(stageId, blockId, "block")) {
                    PlayerStages.LOGGER.debug("Restricted block {} for player {} in stage {}", blockId, serverPlayer.getName().getString(), stageId);
                    serverPlayer.sendMessage(Text.literal("You are not experienced enough to break this block!")
                            .styled(style -> style.withColor(0xFF5555).withItalic(true)), true);
                    RestrictionHandler.applyRestrictions(serverPlayer, null, world);
                    return ActionResult.FAIL;
                }
                ItemStack stack = player.getStackInHand(hand);
                Identifier itemId = Registries.ITEM.getId(stack.getItem());
                if (StageConfig.isRestricted(stageId, itemId, "item")) {
                    PlayerStages.LOGGER.debug("Restricted tool {} for player {} in stage {}", itemId, serverPlayer.getName().getString(), stageId);
                    serverPlayer.sendMessage(Text.literal("You are not experienced enough to use this tool!")
                            .styled(style -> style.withColor(0xFF5555).withItalic(true)), true);
                    return RestrictionHandler.applyRestrictions(serverPlayer, stack, world) ? ActionResult.FAIL : ActionResult.PASS;
                }
            }
            return ActionResult.PASS;
        });
    }
}