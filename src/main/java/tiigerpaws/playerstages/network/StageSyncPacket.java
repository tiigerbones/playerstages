package tiigerpaws.playerstages.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class StageSyncPacket {
    private final String stageId;

    public StageSyncPacket(String stageId) {
        this.stageId = stageId;
    }

    public void write(PacketByteBuf buf) {
        buf.writeString(stageId);
    }

    public static StageSyncPacket read(PacketByteBuf buf) {
        return new StageSyncPacket(buf.readString());
    }

    public String getStageId() { return stageId; }
}