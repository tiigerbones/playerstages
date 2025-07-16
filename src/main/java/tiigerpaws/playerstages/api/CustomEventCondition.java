package tiigerpaws.playerstages.api;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class CustomEventCondition extends UnlockCondition {
    private final String event;

    public CustomEventCondition(String event) {
        super("custom_event", 1);
        this.event = event;
    }

    @Override
    public boolean isMet(ServerPlayerEntity player, Identifier target, int achievedCount) {
        return target.toString().equals(event);
    }

    public String getEvent() { return event; }
}