package tiigerpaws.playerstages.config;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import tiigerpaws.playerstages.PlayerStages;
import tiigerpaws.playerstages.stages.StageData;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class StageConfig implements SimpleSynchronousResourceReloadListener {
    private static final Gson GSON = new Gson();
    private static final Map<String, StageData> STAGES = new HashMap<>();

    public static void init() {
        PlayerStages.LOGGER.info("Registering stage config reload listener for SERVER_DATA");
        ResourceManagerHelper.get(ResourceType.SERVER_DATA)
                .registerReloadListener(new StageConfig());
    }

    @Override
    public Identifier getFabricId() {
        return PlayerStages.id("stage_config");
    }

    @Override
    public void reload(ResourceManager manager) {
        STAGES.clear();
        PlayerStages.LOGGER.info("Reloading stage configurations from data/playerstages/stages...");

        Map<Identifier, Resource> resources = manager.findResources("stages", path -> path.getPath().endsWith(".json"));
        if (resources.isEmpty()) {
            PlayerStages.LOGGER.warn("No stage JSON files found in data/playerstages/stages/. Ensure files are in src/main/resources/data/playerstages/stages/.");
        } else {
            PlayerStages.LOGGER.info("Found {} stage JSON resources.", resources.size());
        }

        for (Identifier id : resources.keySet()) {
            if (!id.getNamespace().equals("playerstages")) {
                PlayerStages.LOGGER.debug("Skipping resource {} (Namespace: {}) - not in playerstages namespace", id, id.getNamespace());
                continue;
            }
            try {
                Resource resource = manager.getResource(id).orElseThrow(() -> new IllegalStateException("Resource not found: " + id));
                try (InputStream stream = resource.getInputStream(); InputStreamReader reader = new InputStreamReader(stream)) {
                    JsonObject json = GSON.fromJson(reader, JsonObject.class);
                    if (json == null || !json.has("stage_id")) {
                        PlayerStages.LOGGER.error("Invalid stage JSON at {}: Missing stage_id or invalid JSON", id);
                        continue;
                    }
                    String stageId = json.get("stage_id").getAsString();
                    if (stageId.isEmpty()) {
                        PlayerStages.LOGGER.error("Invalid stage JSON at {}: stage_id is empty", id);
                        continue;
                    }
                    StageData stage = new StageData(stageId, json);
                    STAGES.put(stageId, stage);
                    PlayerStages.LOGGER.info("Loaded stage: {} (Name: {}, Level: {}, Restricted Items: {}, Restricted Blocks: {})",
                            stageId, stage.getName(), stage.getLevel(), stage.getRestrictedItems(), stage.getRestrictedBlocks());
                }
            } catch (Exception e) {
                PlayerStages.LOGGER.error("Failed to load stage {}: {}", id, e.getMessage(), e);
            }
        }
        PlayerStages.LOGGER.info("Loaded {} stages.", STAGES.size());
    }

    public static StageData getStage(String stageId) {
        return STAGES.get(stageId);
    }

    public static Map<String, StageData> getAllStages() {
        return STAGES;
    }

    public static boolean isRestricted(String playerStageId, Identifier id, String type) {
        StageData playerStage = STAGES.get(playerStageId);
        if (playerStage == null) {
            PlayerStages.LOGGER.warn("No stage found for ID: {}. Using default_stage.", playerStageId);
            return false;
        }

        int playerLevel = playerStage.getLevel();
        for (StageData stage : STAGES.values()) {
            if (stage.isLocked() && stage.getLevel() <= playerLevel && stage.isRestricted(id, type)) {
                PlayerStages.LOGGER.debug("Restriction applied for {} (Type: {}) in stage {} (Level: {}, Locked: {})",
                        id, type, stage.getStageId(), stage.getLevel(), stage.isLocked());
                return true;
            }
        }
        return false;
    }
}