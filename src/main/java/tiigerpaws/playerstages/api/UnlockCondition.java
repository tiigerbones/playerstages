package tiigerpaws.playerstages.api;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public abstract class UnlockCondition {
    protected final String type;
    protected final int count;

    public UnlockCondition(String type, int count) {
        this.type = type;
        this.count = count;
    }

    public abstract boolean isMet(ServerPlayerEntity player, Identifier target, int achievedCount);

    public String getType() { return type; }
    public int getCount() { return count; }
}