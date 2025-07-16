package tiigerpaws.playerstages.tooltip;

import net.minecraft.registry.Registries;
import tiigerpaws.playerstages.PlayerStages;
import tiigerpaws.playerstages.api.StageData;
import tiigerpaws.playerstages.config.ConfigLoader;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class TooltipHandler {
    public static void init() {
        ItemTooltipCallback.EVENT.register((stack, context, lines) -> {
            Identifier itemId = Registries.ITEM.getId(stack.getItem());
            for (StageData stage : ConfigLoader.getAllStages().values()) {
                if (stage.isLocked() && stage.isRestricted(itemId, "item")) {
                    PlayerStages.LOGGER.debug("Adding restriction tooltip for item {} in stage {}", itemId, stage.getStageId());
                    lines.add(Text.literal("Restricted until stage: " + stage.getName())
                            .styled(style -> style.withColor(0xFF5555).withItalic(true)));
                    break;
                }
            }
        });
    }
}