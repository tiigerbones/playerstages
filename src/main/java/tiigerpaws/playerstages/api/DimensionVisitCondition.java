package tiigerpaws.playerstages.api;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class DimensionVisitCondition extends UnlockCondition {
    private final Identifier dimensionId;

    public DimensionVisitCondition(Identifier dimensionId, int count) {
        super("dimension_visit", count);
        this.dimensionId = dimensionId;
    }

    @Override
    public boolean isMet(ServerPlayerEntity player, Identifier target, int achievedCount) {
        return target.equals(dimensionId) && achievedCount >= count;
    }

    public Identifier getDimensionId() { return dimensionId; }
}