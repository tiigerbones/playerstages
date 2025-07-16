package tiigerpaws.playerstages.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceType;
import org.jetbrains.annotations.NotNull;
import tiigerpaws.playerstages.PlayerStages;
import tiigerpaws.playerstages.api.StageData;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class ConfigLoader implements SimpleSynchronousResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<String, StageData> STAGES = new HashMap<>();
    private static boolean restrictionDrop = true;
    private static boolean restrictionDamage = false;
    private static boolean reduceEffectiveness = false;
    private static boolean blockPlacementRestricted = true;
    private static boolean hideFromLoot = false;
    private static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve("playerstages");

    public static void init() {
        try {
            Files.createDirectories(CONFIG_DIR);
            generateDefaultConfig("playerstagesconfig.json");
            generateDefaultConfig("itemsconfig.json");
            // Add other configs (dimensionconfig.json, etc.) as needed
        } catch (IOException e) {
            PlayerStages.LOGGER.error("Failed to create config directory: {}", e.getMessage());
        }
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new ConfigLoader());
    }

    private static void generateDefaultConfig(String fileName) {
        Path configPath = CONFIG_DIR.resolve(fileName);
        if (!Files.exists(configPath)) {
            JsonObject defaultConfig = getJsonObject(fileName);
            try {
                Files.writeString(configPath, GSON.toJson(defaultConfig));
                PlayerStages.LOGGER.info("Generated default config: {}", configPath);
            } catch (IOException e) {
                PlayerStages.LOGGER.error("Failed to generate default config {}: {}", fileName, e.getMessage());
            }
        }
    }

    private static @NotNull JsonObject getJsonObject(String fileName) {
        JsonObject defaultConfig = new JsonObject();
        if (fileName.equals("playerstagesconfig.json")) {
            defaultConfig.addProperty("restriction_drop", true);
            defaultConfig.addProperty("restriction_damage", false);
            defaultConfig.addProperty("reduce_effectiveness", false);
            defaultConfig.addProperty("block_placement_restricted", true);
            defaultConfig.addProperty("hide_from_loot", false);
        } else if (fileName.equals("itemsconfig.json")) {
            // Define item-specific config settings
        }
        return defaultConfig;
    }

    @Override
    public Identifier getFabricId() {
        return PlayerStages.id("config_loader");
    }

    @Override
    public void reload(ResourceManager manager) {
        STAGES.clear();
        PlayerStages.LOGGER.info("Reloading stage configurations...");
        Map<Identifier, Resource> resources = manager.findResources("stages", path -> path.getPath().endsWith(".json"));
        for (Identifier id : resources.keySet()) {
            if (!id.getNamespace().equals(PlayerStages.MOD_ID)) continue;
            try (InputStream stream = manager.getResource(id).get().getInputStream(); InputStreamReader reader = new InputStreamReader(stream)) {
                JsonObject json = GSON.fromJson(String.valueOf(reader), JsonObject.class);
                if (json == null || !json.has("stage_id")) continue;
                String stageId = json.get("stage_id").getAsString();
                if (stageId.isEmpty()) continue;
                STAGES.put(stageId, new StageData(stageId, json));
                PlayerStages.LOGGER.info("Loaded stage: {}", stageId);
            } catch (Exception e) {
                PlayerStages.LOGGER.error("Failed to load stage {}: {}", id, e.getMessage());
            }
        }
        // Load restriction configs from config/playerstages/
        try {
            Path configPath = CONFIG_DIR.resolve("playerstagesconfig.json");
            if (Files.exists(configPath)) {
                JsonObject json = GSON.fromJson(Files.newBufferedReader(configPath), JsonObject.class);
                restrictionDrop = !json.has("restriction_drop") || json.get("restriction_drop").getAsBoolean();
                restrictionDamage = json.has("restriction_damage") && json.get("restriction_damage").getAsBoolean();
                reduceEffectiveness = json.has("reduce_effectiveness") && json.get("reduce_effectiveness").getAsBoolean();
                blockPlacementRestricted = !json.has("block_placement_restricted") || json.get("block_placement_restricted").getAsBoolean();
                hideFromLoot = json.has("hide_from_loot") && json.get("hide_from_loot").getAsBoolean();
            }
        } catch (IOException e) {
            PlayerStages.LOGGER.error("Failed to load playerstagesconfig.json: {}", e.getMessage());
        }
    }

    public static StageData getStage(String stageId) { return STAGES.get(stageId); }
    public static Map<String, StageData> getAllStages() { return STAGES; }
    public static boolean isRestricted(String stageId, Identifier id, String type) {
        StageData stage = STAGES.get(stageId);
        if (stage == null) return false;
        int playerLevel = stage.getLevel();
        for (StageData otherStage : STAGES.values()) {
            if (otherStage.isLocked() && otherStage.getLevel() <= playerLevel && otherStage.isRestricted(id, type)) {
                return true;
            }
        }
        return false;
    }

    // Getters for restriction settings
    public static boolean isRestrictionDrop() { return restrictionDrop; }
    public static boolean isRestrictionDamage() { return restrictionDamage; }
    public static boolean isReduceEffectiveness() { return reduceEffectiveness; }
    public static boolean isBlockPlacementRestricted() { return blockPlacementRestricted; }
    public static boolean isHideFromLoot() { return hideFromLoot; }
}