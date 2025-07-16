package tiigerpaws.playerstages.commands;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import tiigerpaws.playerstages.config.ConfigLoader;
import net.minecraft.command.CommandSource;

import java.util.concurrent.CompletableFuture;

public class StageArgumentType implements ArgumentType<String> {
    public static StageArgumentType stage() {
        return new StageArgumentType();
    }

    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
        String stageId = reader.readUnquotedString();
        if (ConfigLoader.getStage(stageId) == null) {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.literalIncorrect().createWithContext(reader, stageId);
        }
        return stageId;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(ConfigLoader.getAllStages().keySet(), builder);
    }
}