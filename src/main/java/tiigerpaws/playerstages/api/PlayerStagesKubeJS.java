package tiigerpaws.playerstages.api;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import tiigerpaws.playerstages.events.UnlockConditionHandler;

public class PlayerStagesKubeJS {
    public static void triggerCustomEvent(ServerPlayerEntity player, String event) {
        UnlockConditionHandler.checkUnlockConditions(player, new Identifier("kubejs", event), 1, "custom_event");
    }
}