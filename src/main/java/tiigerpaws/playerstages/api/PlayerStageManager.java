package tiigerpaws.playerstages.api;

import tiigerpaws.playerstages.PlayerStages;
import tiigerpaws.playerstages.config.ConfigLoader;
import tiigerpaws.playerstages.data.PlayerStageData;
import tiigerpaws.playerstages.network.StageNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class PlayerStageManager {
    public static String getStage(ServerPlayerEntity player) {
        if (player.isCreative()) {
            PlayerStages.LOGGER.info("Player {} in creative mode, unlocking all stages.", player.getName().getString());
            for (StageData stage : ConfigLoader.getAllStages().values()) {
                stage.setLocked(false);
            }
            return getHighestStageId();
        }
        PlayerStageData data = PlayerStageData.get(player);
        String stageId = data.getStage();
        if (stageId == null || stageId.isEmpty() || ConfigLoader.getStage(stageId) == null) {
            PlayerStages.LOGGER.info("No valid stage for {}. Assigning level 1.", player.getName().getString());
            for (StageData stage : ConfigLoader.getAllStages().values()) {
                if (stage.getLevel() == 1) {
                    setStage(player, stage.getStageId());
                    stage.setLocked(true);
                    return stage.getStageId();
                }
            }
            setStage(player, "default_stage");
            return "default_stage";
        }
        return stageId;
    }

    public static void setStage(ServerPlayerEntity player, String stageId) {
        PlayerStageData data = PlayerStageData.get(player);
        data.setStage(stageId);
        PlayerStages.LOGGER.info("Set stage for {} to {}", player.getName().getString(), stageId);
        StageNetworkHandler.sendStageUpdate(player, stageId);
    }

    public static void advanceStage(ServerPlayerEntity player) {
        String currentStageId = getStage(player);
        String nextStageId = getNextStage(currentStageId);
        if (!nextStageId.equals(currentStageId)) {
            setStage(player, nextStageId);
            StageData nextStage = ConfigLoader.getStage(nextStageId);
            player.sendMessage(Text.literal("Advanced to stage: " + (nextStage != null ? nextStage.getName() : nextStageId)), false);
        }
    }

    public static boolean hasStage(ServerPlayerEntity player, String stageId) {
        return getStage(player).equals(stageId);
    }

    public static void resetStage(ServerPlayerEntity player) {
        for (StageData stage : ConfigLoader.getAllStages().values()) {
            if (stage.getLevel() == 1) {
                setStage(player, stage.getStageId());
                stage.setLocked(true);
                PlayerStages.LOGGER.info("Reset player {} to stage {}", player.getName().getString(), stage.getStageId());
                return;
            }
        }
        setStage(player, "default_stage");
        PlayerStages.LOGGER.warn("No level 1 stage found, reset player {} to default_stage.", player.getName().getString());
    }

    public static String getNextStage(String currentStageId) {
        StageData current = ConfigLoader.getStage(currentStageId);
        if (current == null || current.isFinal()) return currentStageId;
        int currentLevel = current.getLevel();
        for (StageData stage : ConfigLoader.getAllStages().values()) {
            if (stage.getLevel() == currentLevel + 1) return stage.getStageId();
        }
        return currentStageId;
    }

    public static String getHighestStageId() {
        String highestStageId = "default_stage";
        int maxLevel = 0;
        for (StageData stage : ConfigLoader.getAllStages().values()) {
            if (stage.getLevel() > maxLevel) {
                maxLevel = stage.getLevel();
                highestStageId = stage.getStageId();
            }
        }
        return highestStageId;
    }
}