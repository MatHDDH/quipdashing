package amymialee.whipdashing.items;

import amymialee.whipdashing.Whipdashing;
import amymialee.whipdashing.WhipdashingClient;
import amymialee.whipdashing.entities.HookEntity;
import amymialee.whipdashing.util.PlayerHookWrapper;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.stat.Stats;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class WhipdashItem extends Item {
    public WhipdashItem(FabricItemSettings settings) {
        super(settings);
    }

    public void useWhip(World world, PlayerEntity user, Hand hand) {
        if (hand == Hand.OFF_HAND && user instanceof PlayerHookWrapper wrapper) {
            if (wrapper.getHook() != null) {
                wrapper.getHook().discard();
            }
            if (!world.isClient) {
                world.playSound(null, user.getX(), user.getY(), user.getZ(), Whipdashing.HOOK_THROW, SoundCategory.PLAYERS, 0.5F, 0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F));
                world.spawnEntity(new HookEntity(user, world));
            }
            user.incrementStat(Stats.USED.getOrCreateStat(this));
        }
    }

    public void retractWhip(World world, PlayerEntity user, Hand hand) {
        if (hand == Hand.OFF_HAND && user instanceof PlayerHookWrapper wrapper) {
            if (wrapper.getHook() != null) {
                if (!world.isClient) {
                    wrapper.getHook().use();
                }
            }
        }
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("whipdashing.whipdash.description.1", Text.translatable("whipdashing.whipdash.description.hold").formatted(Formatting.GOLD), WhipdashingClient.whipdashKeybinding.getBoundKeyLocalizedText() == null ? Text.empty() : WhipdashingClient.whipdashKeybinding.getBoundKeyLocalizedText().getWithStyle(Style.EMPTY.withFormatting(Formatting.GOLD)).get(0), Text.translatable("whipdashing.whipdash.description.release").formatted(Formatting.GOLD)));
        tooltip.add(Text.empty());
        tooltip.add(Text.translatable("whipdashing.whipdash.description.2"));
        tooltip.add(Text.translatable("whipdashing.whipdash.description.3"));
        tooltip.add(Text.empty());
        tooltip.add(Text.translatable("whipdashing.whipdash.description.4", MinecraftClient.getInstance().options.swapHandsKey.getBoundKeyLocalizedText() == null ? Text.empty() : MinecraftClient.getInstance().options.swapHandsKey.getBoundKeyLocalizedText().getWithStyle(Style.EMPTY.withFormatting(Formatting.GOLD)).get(0)));
        super.appendTooltip(stack, world, tooltip, context);
    }
}