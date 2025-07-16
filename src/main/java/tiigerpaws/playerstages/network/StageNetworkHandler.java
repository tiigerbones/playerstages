package tiigerpaws.playerstages.network;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import tiigerpaws.playerstages.PlayerStages;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import tiigerpaws.playerstages.api.PlayerStageManager;

public class StageNetworkHandler {
    private static final Identifier STAGE_SYNC_PACKET = PlayerStages.id("stage_sync");

    public static void init() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            String stageId = PlayerStageManager.getStage(player);
            sendStageUpdate(player, stageId);
        });
    }

    public static void sendStageUpdate(ServerPlayerEntity player, String stageId) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        new StageSyncPacket(stageId).write(buf);
        ServerPlayNetworking.send(player, STAGE_SYNC_PACKET, buf);
    }
}