package tiigerpaws.playerstages.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import tiigerpaws.playerstages.api.PlayerStageManager;
import tiigerpaws.playerstages.api.StageData;
import tiigerpaws.playerstages.config.ConfigLoader;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class StageCommands {
    public static void init() {
        CommandRegistrationCallback.EVENT.register(StageCommands::register);
    }

    private static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("stage")
                .requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("set")
                        .then(CommandManager.argument("player", EntityArgumentType.players())
                                .then(CommandManager.argument("stage_id", StringArgumentType.word())
                                        .suggests((context, builder) -> CommandSource.suggestMatching(ConfigLoader.getAllStages().keySet(), builder))
                                        .executes(context -> {
                                            ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                                            String stageId = StringArgumentType.getString(context, "stage_id");
                                            if (ConfigLoader.getStage(stageId) == null) {
                                                context.getSource().sendError(Text.literal("Stage not found."));
                                                return 0;
                                            }
                                            PlayerStageManager.setStage(player, stageId);
                                            context.getSource().sendFeedback(() -> Text.literal("Set " + player.getName().getString() + "'s stage to " + stageId), true);
                                            return 1;
                                        }))))
                .then(CommandManager.literal("reset")
                        .then(CommandManager.argument("player", EntityArgumentType.players())
                                .executes(context -> {
                                    ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                                    PlayerStageManager.resetStage(player);
                                    context.getSource().sendFeedback(() -> Text.literal("Reset " + player.getName().getString() + "'s stage to level 1"), true);
                                    return 1;
                                })))
                .then(CommandManager.literal("get")
                        .then(CommandManager.argument("player", EntityArgumentType.players())
                                .executes(context -> {
                                    ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                                    String stageId = PlayerStageManager.getStage(player);
                                    StageData stage = ConfigLoader.getStage(stageId);
                                    String stageInfo = stage != null
                                            ? "Level " + stage.getLevel() + " (" + stage.getName() + ", ID: " + stageId + ")"
                                            : "Level unknown (ID: " + stageId + ")";
                                    context.getSource().sendFeedback(() -> Text.literal(player.getName().getString() + "'s current stage: " + stageInfo), false);
                                    return 1;
                                })))
                .then(CommandManager.literal("list")
                        .executes(context -> {
                            StringBuilder stages = new StringBuilder("Available stages:\n");
                            for (StageData stage : ConfigLoader.getAllStages().values()) {
                                stages.append(stage.getStageId()).append(" (").append(stage.getName()).append(", Level ").append(stage.getLevel()).append(")\n");
                            }
                            context.getSource().sendFeedback(() -> Text.literal(stages.toString()), false);
                            return 1;
                        })));
    }
}