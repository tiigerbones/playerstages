package tiigerpaws.playerstages.events;

import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import tiigerpaws.playerstages.config.RestrictionConfig;
import tiigerpaws.playerstages.config.StageConfig;
import tiigerpaws.playerstages.stages.StageManager;
import net.minecraft.server.world.ServerWorld;

public class StageEventHandler {
    public static void init() {
        // Block breaking restrictions
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            if (player instanceof ServerPlayerEntity serverPlayer && !world.isClient && !serverPlayer.isCreative()) {
                String stageId = StageManager.getPlayerStage(serverPlayer);
                // Check block restriction
                Identifier blockId = Registries.BLOCK.getId(world.getBlockState(pos).getBlock());
                if (StageConfig.isRestricted(stageId, blockId, "block")) {
                    serverPlayer.sendMessage(Text.literal("You are not experienced enough to break this block!")
                            .styled(style -> style.withColor(0xFF5555).withItalic(true)), true);
                    return ActionResult.success(applyRestriction(serverPlayer, null, world));
                }
                // Check tool restriction
                ItemStack stack = player.getStackInHand(hand);
                Identifier itemId = Registries.ITEM.getId(stack.getItem());
                if (StageConfig.isRestricted(stageId, itemId, "item")) {
                    serverPlayer.sendMessage(Text.literal("You are not experienced enough to use this tool!")
                            .styled(style -> style.withColor(0xFF5555).withItalic(true)), true);
                    return ActionResult.success(applyRestriction(serverPlayer, stack, world));
                }
            }
            return ActionResult.PASS;
        });

        // Item use restrictions
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (player instanceof ServerPlayerEntity serverPlayer && !world.isClient && !serverPlayer.isCreative()) {
                String stageId = StageManager.getPlayerStage(serverPlayer);
                ItemStack stack = player.getStackInHand(hand);
                Identifier itemId = Registries.ITEM.getId(stack.getItem());
                if (StageConfig.isRestricted(stageId, itemId, "item")) {
                    serverPlayer.sendMessage(Text.literal("You are not experienced enough to use this item!")
                            .styled(style -> style.withColor(0xFF5555).withItalic(true)), true);
                    return TypedActionResult.fail(applyRestriction(serverPlayer, stack, world) ? ItemStack.EMPTY : stack);
                }
            }
            return TypedActionResult.pass(player.getStackInHand(hand));
        });

        // Block use restrictions
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (player instanceof ServerPlayerEntity serverPlayer && !world.isClient && !serverPlayer.isCreative()) {
                String stageId = StageManager.getPlayerStage(serverPlayer);
                ItemStack stack = player.getStackInHand(hand);
                Identifier itemId = Registries.ITEM.getId(stack.getItem());
                if (StageConfig.isRestricted(stageId, itemId, "item")) {
                    serverPlayer.sendMessage(Text.literal("You are not experienced enough to use this tool!")
                            .styled(style -> style.withColor(0xFF5555).withItalic(true)), true);
                    return applyRestriction(serverPlayer, stack, world) ? ActionResult.FAIL : ActionResult.PASS;
                }
                // Check block placement restriction
                if (RestrictionConfig.isBlockPlacementRestricted()) {
                    Identifier blockId = Registries.BLOCK.getId(world.getBlockState(hitResult.getBlockPos()).getBlock());
                    if (StageConfig.isRestricted(stageId, blockId, "block")) {
                        serverPlayer.sendMessage(Text.literal("You are not experienced enough to place this block!")
                                .styled(style -> style.withColor(0xFF5555).withItalic(true)), true);
                        return applyRestriction(serverPlayer, stack, world) ? ActionResult.FAIL : ActionResult.PASS;
                    }
                }
            }
            return ActionResult.PASS;
        });

        // Attack restrictions with restricted items
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (player instanceof ServerPlayerEntity serverPlayer && !world.isClient && !serverPlayer.isCreative()) {
                String stageId = StageManager.getPlayerStage(serverPlayer);
                ItemStack stack = player.getStackInHand(hand);
                Identifier itemId = Registries.ITEM.getId(stack.getItem());
                if (StageConfig.isRestricted(stageId, itemId, "item")) {
                    serverPlayer.sendMessage(Text.literal("You are not experienced enough to use this item!")
                            .styled(style -> style.withColor(0xFF5555).withItalic(true)), true);
                    return applyRestriction(serverPlayer, stack, world) ? ActionResult.FAIL : ActionResult.PASS;
                }
            }
            return ActionResult.PASS;
        });

        // Dimension restriction
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            if (!player.isCreative()) {
                String stageId = StageManager.getPlayerStage(player);
                Identifier dimensionId = player.getWorld().getRegistryKey().getValue();
                if (StageConfig.isRestricted(stageId, dimensionId, "dimension")) {
                    player.sendMessage(Text.literal("You are not experienced enough to enter this dimension!")
                            .styled(style -> style.withColor(0xFF5555).withItalic(true)), true);
                    // Teleport back to overworld
                    ServerWorld overworld = server.getWorld(World.OVERWORLD);
                    player.teleport(overworld, player.getX(), player.getY(), player.getZ(), player.getYaw(), player.getPitch());
                }
            }
        });

        // Crafting restriction
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (player instanceof ServerPlayerEntity serverPlayer && !world.isClient && !serverPlayer.isCreative()) {
                String stageId = StageManager.getPlayerStage(serverPlayer);
                ItemStack stack = player.getStackInHand(hand);
                Identifier recipeId = Registries.ITEM.getId(stack.getItem());
                if (StageConfig.isRestricted(stageId, recipeId, "recipe")) {
                    serverPlayer.sendMessage(Text.literal("You are not experienced enough to craft this item!")
                            .styled(style -> style.withColor(0xFF5555).withItalic(true)), true);
                    return TypedActionResult.fail(stack);
                }
            }
            return TypedActionResult.pass(player.getStackInHand(hand));
        });

        // Creative mode handling
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            if (!player.isCreative()) {
                StageManager.restorePlayerStage(player);
            }
        });
    }

    private static boolean applyRestriction(ServerPlayerEntity player, ItemStack stack, World world) {
        boolean actionTaken = false;
        if (RestrictionConfig.isRestrictionDrop() && stack != null) {
            ItemEntity itemEntity = new ItemEntity(world, player.getX(), player.getY() + 0.5, player.getZ(), stack.copy());
            itemEntity.setPickupDelay(40);
            world.spawnEntity(itemEntity);
            stack.setCount(0);
            actionTaken = true;
        }
        if (RestrictionConfig.isRestrictionDamage()) {
            player.damage(player.getDamageSources().generic(), 1.0F); // Half a heart
            actionTaken = true;
        }
        if (RestrictionConfig.isReduceEffectiveness()) {
            // Note: Full implementation requires mixins to modify tool/weapon effectiveness
            actionTaken = true;
        }
        return actionTaken;
    }
}