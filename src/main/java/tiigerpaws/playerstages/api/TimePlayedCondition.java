package tiigerpaws.playerstages.api;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class TimePlayedCondition extends UnlockCondition {
    private final long ticks;

    public TimePlayedCondition(long ticks) {
        super("time_played", 1);
        this.ticks = ticks;
    }

    @Override
    public boolean isMet(ServerPlayerEntity player, Identifier target, int achievedCount) {
        return player.getServer().getTicks() >= ticks;
    }

    public long getTicks() { return ticks; }
}