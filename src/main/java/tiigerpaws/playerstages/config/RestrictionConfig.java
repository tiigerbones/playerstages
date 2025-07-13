package tiigerpaws.playerstages.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import tiigerpaws.playerstages.PlayerStages;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;

public class RestrictionConfig implements SimpleSynchronousResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static boolean restrictionDrop = true;
    private static boolean restrictionDamage = false;
    private static boolean reduceEffectiveness = false;
    private static boolean blockPlacementRestricted = true;
    private static boolean hideFromLoot = false;
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("playerstages/restriction_config.json");

    public static void init() {
        // Ensure config directory exists
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            // Generate default config if it doesn't exist
            if (!Files.exists(CONFIG_PATH)) {
                generateDefaultConfig();
            }
        } catch (IOException e) {
            PlayerStages.LOGGER.error("Failed to create config directory or file: " + e.getMessage(), e);
        }
        ResourceManagerHelper.get(ResourceType.SERVER_DATA)
                .registerReloadListener(new RestrictionConfig());
    }

    private static void generateDefaultConfig() {
        String defaultConfig = """
                {
                  // If true, drops the item when a player tries to use a restricted item or block.
                  "restriction_drop": true,
                  // If true, deals half a heart of damage to the player when they try to use a restricted item or block.
                  "restriction_damage": false,
                  // If true, reduces the effectiveness of restricted items (e.g., tools mine slower, weapons deal 1 damage).
                  // Note: Full implementation may require additional mod logic.
                  "reduce_effectiveness": false,
                  // If true, prevents players from placing restricted blocks.
                  "block_placement_restricted": true,
                  // If true, hides restricted items from loot tables (e.g., chests, mob drops).
                  // Note: Requires additional loot table event handling to fully implement.
                  "hide_from_loot": false
                }
                """;
        try {
            Files.writeString(CONFIG_PATH, defaultConfig);
            PlayerStages.LOGGER.info("Generated default restriction_config.json at " + CONFIG_PATH);
        } catch (IOException e) {
            PlayerStages.LOGGER.error("Failed to generate default restriction_config.json: " + e.getMessage(), e);
        }
    }

    @Override
    public Identifier getFabricId() {
        return PlayerStages.id("restriction_config");
    }

    @Override
    public void reload(ResourceManager manager) {
        PlayerStages.LOGGER.info("Reloading restriction config...");
        try {
            Resource resource = manager.getResource(PlayerStages.id("restriction_config.json")).orElse(null);
            if (resource == null) {
                PlayerStages.LOGGER.warn("No restriction_config.json found in resources, checking config directory...");
                if (Files.exists(CONFIG_PATH)) {
                    try (BufferedReader reader = Files.newBufferedReader(CONFIG_PATH)) {
                        JsonObject json = GSON.fromJson(reader, JsonObject.class);
                        parseConfig(json);
                    }
                } else {
                    PlayerStages.LOGGER.warn("No restriction_config.json found, using defaults.");
                }
                return;
            }
            try (InputStream stream = resource.getInputStream(); InputStreamReader reader = new InputStreamReader(stream)) {
                JsonObject json = GSON.fromJson(reader, JsonObject.class);
                parseConfig(json);
            }
        } catch (Exception e) {
            PlayerStages.LOGGER.error("Failed to load restriction_config.json: " + e.getMessage(), e);
        }
    }

    private void parseConfig(JsonObject json) {
        if (json != null) {
            restrictionDrop = json.has("restriction_drop") ? json.get("restriction_drop").getAsBoolean() : true;
            restrictionDamage = json.has("restriction_damage") ? json.get("restriction_damage").getAsBoolean() : false;
            reduceEffectiveness = json.has("reduce_effectiveness") ? json.get("reduce_effectiveness").getAsBoolean() : false;
            blockPlacementRestricted = json.has("block_placement_restricted") ? json.get("block_placement_restricted").getAsBoolean() : true;
            hideFromLoot = json.has("hide_from_loot") ? json.get("hide_from_loot").getAsBoolean() : false;
            PlayerStages.LOGGER.info("Loaded restriction config: drop=" + restrictionDrop + ", damage=" + restrictionDamage + ", reduceEffectiveness=" + reduceEffectiveness + ", blockPlacementRestricted=" + blockPlacementRestricted + ", hideFromLoot=" + hideFromLoot);
        }
    }

    public static boolean isRestrictionDrop() {
        return restrictionDrop;
    }

    public static boolean isRestrictionDamage() {
        return restrictionDamage;
    }

    public static boolean isReduceEffectiveness() {
        return reduceEffectiveness;
    }

    public static boolean isBlockPlacementRestricted() {
        return blockPlacementRestricted;
    }

    public static boolean isHideFromLoot() {
        return hideFromLoot;
    }
}