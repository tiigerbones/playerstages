package tiigerpaws.playerstages;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tiigerpaws.playerstages.commands.StageCommands;
import tiigerpaws.playerstages.config.RestrictionConfig;
import tiigerpaws.playerstages.config.StageConfig;
import tiigerpaws.playerstages.events.BlockBreakRestriction;
import tiigerpaws.playerstages.events.ItemUseRestriction;
import tiigerpaws.playerstages.events.RestrictionHandler;
import tiigerpaws.playerstages.stages.StageData;
import tiigerpaws.playerstages.stages.UnlockConditionHandler;

public class PlayerStages implements ModInitializer {
	public static final String MOD_ID = "playerstages";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static Identifier id(String path) {
		return new Identifier(MOD_ID, path);
	}

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Player Stages mod");
		StageConfig.init(); // Load stage definitions
		RestrictionConfig.init(); // Load and generate restriction config
		BlockBreakRestriction.register();
		ItemUseRestriction.register();
		StageCommands.init(); // Register commands
		UnlockConditionHandler.init(); // Register unlock condition handlers


		ItemTooltipCallback.EVENT.register((stack, context, lines) -> {
			Identifier itemId = Registries.ITEM.getId(stack.getItem());
			for (StageData stage : StageConfig.getAllStages().values()) {
				if (stage.isLocked() && stage.isRestricted(itemId, "item")) {
					PlayerStages.LOGGER.debug("Adding restriction tooltip for item {} in stage {}", itemId, stage.getStageId());
					lines.add(Text.literal("You are not experienced enough to use this item!")
							.styled(style -> style.withColor(0xFF5555).withItalic(true)));
					break;
				}
			}
		});
	}
}