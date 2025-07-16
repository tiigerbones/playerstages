package tiigerpaws.playerstages.api;

import com.google.gson.JsonObject;
import net.minecraft.util.Identifier;
import java.util.ArrayList;
import java.util.List;

public class RestrictedData {
    private final List<Identifier> items;
    private final List<Identifier> blocks;
    private final List<Identifier> dimensions;
    private final List<Identifier> recipes;

    public RestrictedData(JsonObject restrictedJson) {
        items = new ArrayList<>();
        blocks = new ArrayList<>();
        dimensions = new ArrayList<>();
        recipes = new ArrayList<>();
        if (restrictedJson != null) {
            if (restrictedJson.has("items")) {
                restrictedJson.getAsJsonArray("items").forEach(item -> items.add(new Identifier(item.getAsString())));
            }
            if (restrictedJson.has("blocks")) {
                restrictedJson.getAsJsonArray("blocks").forEach(block -> blocks.add(new Identifier(block.getAsString())));
            }
            if (restrictedJson.has("dimensions")) {
                restrictedJson.getAsJsonArray("dimensions").forEach(dim -> dimensions.add(new Identifier(dim.getAsString())));
            }
            if (restrictedJson.has("recipes")) {
                restrictedJson.getAsJsonArray("recipes").forEach(recipe -> recipes.add(new Identifier(recipe.getAsString())));
            }
        }
    }

    public boolean isRestricted(Identifier id, String type) {
        return switch (type) {
            case "block" -> blocks.contains(id);
            case "dimension" -> dimensions.contains(id);
            case "recipe" -> recipes.contains(id);
            default -> items.contains(id);
        };
    }

    public List<Identifier> getItems() { return items; }
    public List<Identifier> getBlocks() { return blocks; }
    public List<Identifier> getDimensions() { return dimensions; }
    public List<Identifier> getRecipes() { return recipes; }
}