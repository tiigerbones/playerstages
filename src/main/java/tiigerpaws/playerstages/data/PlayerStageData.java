package tiigerpaws.playerstages.data;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import tiigerpaws.playerstages.PlayerStages;

public class PlayerStageData extends PersistentState {
    private static final String NBT_KEY = "playerstages";
    private String stageId = "";

    public static PlayerStageData get(ServerPlayerEntity player) {
        ServerWorld world = player.getServerWorld();
        return world.getPersistentStateManager().getOrCreate(
                PlayerStageData::new,
                PlayerStageData::new,
                PlayerStages.MOD_ID + "_" + player.getUuidAsString()
        );
    }

    public PlayerStageData() {
    }

    public PlayerStageData(NbtCompound nbt) {
        this.stageId = nbt.getString("stage_id");
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        nbt.putString("stage_id", stageId);
        return nbt;
    }

    public String getStage() {
        return stageId;
    }

    public void setStage(String stageId) {
        this.stageId = stageId;
        markDirty();
    }
}