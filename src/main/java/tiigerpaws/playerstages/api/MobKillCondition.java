package tiigerpaws.playerstages.api;

import net.minecraft.entity.EntityType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class MobKillCondition extends UnlockCondition {
    private final Identifier entityType;

    public MobKillCondition(Identifier entityType, int count) {
        super("mob_kill", count);
        this.entityType = entityType;
    }

    @Override
    public boolean isMet(ServerPlayerEntity player, Identifier target, int achievedCount) {
        return target.equals(entityType) && achievedCount >= count;
    }

    public Identifier getEntityType() { return entityType; }
}