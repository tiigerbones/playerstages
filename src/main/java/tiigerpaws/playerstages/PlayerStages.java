package tiigerpaws.playerstages;

import net.minecraft.util.Identifier;
import tiigerpaws.playerstages.commands.StageCommands;
import tiigerpaws.playerstages.config.ConfigLoader;
import tiigerpaws.playerstages.events.BlockBreakRestriction;
import tiigerpaws.playerstages.events.ItemUseRestriction;
import tiigerpaws.playerstages.events.UnlockConditionHandler;
import tiigerpaws.playerstages.network.StageNetworkHandler;
import tiigerpaws.playerstages.tooltip.TooltipHandler;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlayerStages implements ModInitializer {
	public static final String MOD_ID = "playerstages";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static Identifier id(String path) {
		return new Identifier(MOD_ID, path);
	}

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Player Stages mod");
		ConfigLoader.init(); // Load all configs
		StageCommands.init(); // Register commands
		StageNetworkHandler.init(); // Initialize networking
		BlockBreakRestriction.register(); // Register block break restrictions
		ItemUseRestriction.register(); // Register item use restrictions
		UnlockConditionHandler.init(); // Register unlock condition handlers
		TooltipHandler.init(); // Register tooltip handler
	}
}