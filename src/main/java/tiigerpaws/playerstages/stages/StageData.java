package tiigerpaws.playerstages.stages;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class StageData {
    private final String stageId;
    private final String name;
    private final int level;
    private final boolean isFinal;
    private boolean locked;
    private final List<Identifier> restrictedItems;
    private final List<Identifier> restrictedBlocks;
    private final List<Identifier> restrictedDimensions;
    private final List<Identifier> restrictedRecipes;
    private final List<UnlockCondition> unlockConditions;

    public record UnlockCondition(String type, Identifier target, int count) {
    }

    public StageData(String stageId, JsonObject json) {
        this.stageId = stageId;
        this.name = json.has("name") ? json.get("name").getAsString() : stageId;
        this.level = json.has("level") ? json.get("level").getAsInt() : 0;
        this.isFinal = json.has("final") ? json.get("final").getAsBoolean() : false;
        this.locked = true; // Stages start locked by default
        this.restrictedItems = new ArrayList<>();
        this.restrictedBlocks = new ArrayList<>();
        this.restrictedDimensions = new ArrayList<>();
        this.restrictedRecipes = new ArrayList<>();
        this.unlockConditions = new ArrayList<>();

        if (json.has("restricted")) {
            JsonObject restricted = json.getAsJsonObject("restricted");
            if (restricted.has("items")) {
                JsonArray items = restricted.getAsJsonArray("items");
                for (JsonElement item : items) {
                    restrictedItems.add(new Identifier(item.getAsString()));
                }
            }
            if (restricted.has("blocks")) {
                JsonArray blocks = restricted.getAsJsonArray("blocks");
                for (JsonElement block : blocks) {
                    restrictedBlocks.add(new Identifier(block.getAsString()));
                }
            }
            if (restricted.has("dimensions")) {
                JsonArray dimensions = restricted.getAsJsonArray("dimensions");
                for (JsonElement dimension : dimensions) {
                    restrictedDimensions.add(new Identifier(dimension.getAsString()));
                }
            }
            if (restricted.has("recipes")) {
                JsonArray recipes = restricted.getAsJsonArray("recipes");
                for (JsonElement recipe : recipes) {
                    restrictedRecipes.add(new Identifier(recipe.getAsString()));
                }
            }
        }

        if (json.has("unlock_conditions")) {
            JsonArray conditions = json.getAsJsonArray("unlock_conditions");
            for (JsonElement condition : conditions) {
                JsonObject condObj = condition.getAsJsonObject();
                String type = condObj.get("type").getAsString();
                Identifier target = new Identifier(condObj.get(type.equals("item_pickup") ? "item" : "advancement").getAsString());
                int count = condObj.has("count") ? condObj.get("count").getAsInt() : 1;
                unlockConditions.add(new UnlockCondition(type, target, count));
            }
        }
    }

    public String getStageId() {
        return stageId;
    }

    public String getName() {
        return name;
    }

    public int getLevel() {
        return level;
    }

    public boolean isFinal() {
        return isFinal;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public List<Identifier> getRestrictedItems() {
        return restrictedItems;
    }

    public List<Identifier> getRestrictedBlocks() {
        return restrictedBlocks;
    }

    public List<Identifier> getRestrictedDimensions() {
        return restrictedDimensions;
    }

    public List<Identifier> getRestrictedRecipes() {
        return restrictedRecipes;
    }

    public List<UnlockCondition> getUnlockConditions() {
        return unlockConditions;
    }

    public boolean isRestricted(Identifier id, String type) {
        return switch (type) {
            case "block" -> restrictedBlocks.contains(id);
            case "dimension" -> restrictedDimensions.contains(id);
            case "recipe" -> restrictedRecipes.contains(id);
            default -> restrictedItems.contains(id);
        };
    }
}