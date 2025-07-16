package tiigerpaws.playerstages.api;

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
    private final RestrictedData restrictions;
    private final List<UnlockCondition> unlockConditions;

    public StageData(String stageId, JsonObject json) {
        this.stageId = stageId;
        this.name = json.has("name") ? json.get("name").getAsString() : stageId;
        this.level = json.has("level") ? json.get("level").getAsInt() : 0;
        this.isFinal = json.has("final") ? json.get("final").getAsBoolean() : false;
        this.locked = true;
        this.restrictions = new RestrictedData(json.has("restricted") ? json.getAsJsonObject("restricted") : new JsonObject());
        this.unlockConditions = new ArrayList<>();
        if (json.has("unlock_conditions")) {
            JsonArray conditions = json.getAsJsonArray("unlock_conditions");
            for (JsonElement condition : conditions) {
                JsonObject condObj = condition.getAsJsonObject();
                String type = condObj.get("type").getAsString();
                UnlockCondition unlockCondition = switch (type) {
                    case "item_pickup" -> new ItemPickupCondition(new Identifier(condObj.get("item").getAsString()), condObj.has("count") ? condObj.get("count").getAsInt() : 1);
                    case "advancement" -> new AdvancementCondition(new Identifier(condObj.get("advancement").getAsString()), 1);
                    case "mob_kill" -> new MobKillCondition(new Identifier(condObj.get("entity").getAsString()), condObj.has("count") ? condObj.get("count").getAsInt() : 1);
                    case "dimension_visit" -> new DimensionVisitCondition(new Identifier(condObj.get("dimension").getAsString()), 1);
                    case "time_played" -> new TimePlayedCondition(condObj.get("ticks").getAsLong());
                    case "block_place" -> new BlockPlaceCondition(new Identifier(condObj.get("block").getAsString()), condObj.has("count") ? condObj.get("count").getAsInt() : 1);
                    case "block_break" -> new BlockBreakCondition(new Identifier(condObj.get("block").getAsString()), condObj.has("count") ? condObj.get("count").getAsInt() : 1);
                    case "item_craft" -> new ItemCraftCondition(new Identifier(condObj.get("item").getAsString()), condObj.has("count") ? condObj.get("count").getAsInt() : 1);
                    case "custom_event" -> new CustomEventCondition(condObj.get("event").getAsString());
                    default -> null;
                };
                if (unlockCondition != null) unlockConditions.add(unlockCondition);
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

    public RestrictedData getRestrictions() {
        return restrictions;
    }

    public List<UnlockCondition> getUnlockConditions() {
        return unlockConditions;
    }

    public boolean isRestricted(Identifier id, String type) {
        return restrictions.isRestricted(id, type);
    }
}