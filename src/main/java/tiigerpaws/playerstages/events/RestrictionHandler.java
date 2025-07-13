package tiigerpaws.playerstages.events;

import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import tiigerpaws.playerstages.PlayerStages;
import tiigerpaws.playerstages.config.RestrictionConfig;
import net.minecraft.registry.Registries;

public class RestrictionHandler {
    public static boolean applyRestrictions(ServerPlayerEntity player, ItemStack stack, World world) {
        boolean actionTaken = false;
        if (RestrictionConfig.isRestrictionDrop() && stack != null && !stack.isEmpty()) {
            ItemEntity itemEntity = new ItemEntity(world, player.getX(), player.getY() + 0.5, player.getZ(), stack.copy());
            itemEntity.setPickupDelay(40);
            world.spawnEntity(itemEntity);
            stack.setCount(0);
            PlayerStages.LOGGER.debug("Dropped item {} for player {}", Registries.ITEM.getId(stack.getItem()), player.getName().getString());
            actionTaken = true;
        }
        if (RestrictionConfig.isRestrictionDamage()) {
            player.damage(player.getDamageSources().magic(), 1.0F); // Half a heart
            PlayerStages.LOGGER.debug("Applied damage to player {}", player.getName().getString());
            actionTaken = true;
        }
        if (RestrictionConfig.isReduceEffectiveness()) {
            // Note: Full implementation requires mixins to modify tool/weapon effectiveness
            PlayerStages.LOGGER.debug("Reduce effectiveness applied for player {} (mixin required)", player.getName().getString());
            actionTaken = true;
        }
        return actionTaken;
    }
}