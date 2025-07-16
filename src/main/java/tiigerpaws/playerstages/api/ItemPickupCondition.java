package tiigerpaws.playerstages.api;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class ItemPickupCondition extends UnlockCondition {
    private final Identifier item;

    public ItemPickupCondition(Identifier item, int count) {
        super("item_pickup", count);
        this.item = item;
    }

    @Override
    public boolean isMet(ServerPlayerEntity player, Identifier target, int achievedCount) {
        return target.equals(item) && achievedCount >= count;
    }

    public Identifier getItem() { return item; }
}