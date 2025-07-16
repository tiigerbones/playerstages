package tiigerpaws.playerstages.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class JsonHelper {
    public static List<Identifier> parseIdentifierArray(JsonArray array) {
        List<Identifier> identifiers = new ArrayList<>();
        for (JsonElement element : array) {
            identifiers.add(new Identifier(element.getAsString()));
        }
        return identifiers;
    }
}