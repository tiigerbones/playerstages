package tiigerpaws.playerstages.api;

import net.minecraft.advancement.Advancement;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Objects;

public class AdvancementCondition extends UnlockCondition {
    private final Identifier advancementId;

    public AdvancementCondition(Identifier advancementId, int count) {
        super("advancement", count);
        this.advancementId = advancementId;
    }

    @Override
    public boolean isMet(ServerPlayerEntity player, Identifier target, int achievedCount) {
        if (!target.equals(advancementId)) return false;
        Advancement advancement = Objects.requireNonNull(player.getServer()).getAdvancementLoader().get(advancementId);
        return advancement != null && player.getAdvancementTracker().getProgress(advancement).isDone();
    }

    public Identifier getAdvancementId() { return advancementId; }
}