package tiigerpaws.playerstages.api;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class BlockBreakCondition extends UnlockCondition {
    private final Identifier block;

    public BlockBreakCondition(Identifier block, int count) {
        super("block_break", count);
        this.block = block;
    }

    @Override
    public boolean isMet(ServerPlayerEntity player, Identifier target, int achievedCount) {
        return target.equals(block) && achievedCount >= count;
    }

    public Identifier getBlock() { return block; }
}