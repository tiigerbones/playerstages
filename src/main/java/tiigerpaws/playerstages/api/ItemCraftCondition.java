package tiigerpaws.playerstages.api;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class ItemCraftCondition extends UnlockCondition {
    private final Identifier item;

    public ItemCraftCondition(Identifier item, int count) {
        super("item_craft", count);
        this.item = item;
    }

    @Override
    public boolean isMet(ServerPlayerEntity player, Identifier target, int achievedCount) {
        return target.equals(item) && achievedCount >= count;
    }

    public Identifier getItem() { return item; }
}