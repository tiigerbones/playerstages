package tiigerpaws.playerstages.stages;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import tiigerpaws.playerstages.PlayerStages;
import tiigerpaws.playerstages.api.StageData;
import tiigerpaws.playerstages.config.ConfigLoader;
import tiigerpaws.playerstages.data.PlayerStageData;

public class StageManager {
    public static String getPlayerStage(PlayerEntity player) {
        if (player instanceof ServerPlayerEntity serverPlayer) {
            if (serverPlayer.isCreative()) {
                PlayerStages.LOGGER.info("Player {} is in creative mode, unlocking all stages.", serverPlayer.getName().getString());
                for (StageData stage : ConfigLoader.getAllStages().values()) {
                    stage.setLocked(false);
                }
                return getHighestStageId();
            }
            PlayerStageData data = PlayerStageData.get(serverPlayer);
            String stage = data.getStage();
            if (stage == null || stage.isEmpty() || ConfigLoader.getStage(stage) == null) {
                PlayerStages.LOGGER.info("No valid stage for player {}. Assigning level 1 stage.", serverPlayer.getName().getString());
                for (StageData stageData : ConfigLoader.getAllStages().values()) {
                    if (stageData.getLevel() == 1) {
                        setPlayerStage(serverPlayer, stageData.getStageId());
                        stageData.setLocked(true);
                        PlayerStages.LOGGER.info("Assigned player {} to stage {} (Locked: true)", serverPlayer.getName().getString(), stageData.getStageId());
                        return stageData.getStageId();
                    }
                }
                PlayerStages.LOGGER.warn("No level 1 stage found, using default_stage for player {}.", serverPlayer.getName().getString());
                setPlayerStage(serverPlayer, "default_stage");
                return "default_stage";
            }
            PlayerStages.LOGGER.debug("Player {} is in stage {} (Locked: {})", serverPlayer.getName().getString(), stage, ConfigLoader.getStage(stage).isLocked());
            return stage;
        }
        return "default_stage";
    }

    public static void setPlayerStage(ServerPlayerEntity player, String stageId) {
        PlayerStageData data = PlayerStageData.get(player);
        data.setStage(stageId);
        PlayerStages.LOGGER.info("Set stage for player {} to {}", player.getName().getString(), stageId);
    }

    public static void resetPlayerStage(ServerPlayerEntity player) {
        for (StageData stage : ConfigLoader.getAllStages().values()) {
            if (stage.getLevel() == 1) {
                setPlayerStage(player, stage.getStageId());
                stage.setLocked(true);
                PlayerStages.LOGGER.info("Reset player {} to stage {} (Locked: true)", player.getName().getString(), stage.getStageId());
                return;
            }
        }
        setPlayerStage(player, "default_stage");
        PlayerStages.LOGGER.warn("No level 1 stage found, reset player {} to default_stage.", player.getName().getString());
    }

    public static String getNextStage(String currentStageId) {
        StageData currentStage = ConfigLoader.getStage(currentStageId);
        if (currentStage == null || currentStage.isFinal()) {
            PlayerStages.LOGGER.debug("Stage {} is null or final, no next stage.", currentStageId);
            return currentStageId;
        }
        int currentLevel = currentStage.getLevel();
        for (StageData stage : ConfigLoader.getAllStages().values()) {
            if (stage.getLevel() == currentLevel + 1) {
                PlayerStages.LOGGER.debug("Found next stage: {} for current stage {}", stage.getStageId(), currentStageId);
                return stage.getStageId();
            }
        }
        PlayerStages.LOGGER.debug("No next stage found for {}", currentStageId);
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
        PlayerStages.LOGGER.debug("Highest stage ID: {}", highestStageId);
        return highestStageId;
    }

    public static void restorePlayerStage(ServerPlayerEntity player) {
        PlayerStageData data = PlayerStageData.get(player);
        String stageId = data.getStage();
        StageData stage = ConfigLoader.getStage(stageId);
        if (stage != null) {
            stage.setLocked(true);
            PlayerStages.LOGGER.info("Restored player {} to stage {} (Locked: true)", player.getName().getString(), stageId);
        } else {
            resetPlayerStage(player);
        }
        // Re-lock all higher stages
        for (StageData otherStage : ConfigLoader.getAllStages().values()) {
            if (otherStage.getLevel() > (stage != null ? stage.getLevel() : 0)) {
                otherStage.setLocked(true);
            }
        }
    }
}