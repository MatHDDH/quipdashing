package amymialee.whipdashing.items;

import amymialee.whipdashing.Whipdashing;
import amymialee.whipdashing.entities.HookEntity;
import amymialee.whipdashing.util.PlayerHookWrapper;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.sound.SoundCategory;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class WhipdashItem extends Item {
    public WhipdashItem(FabricItemSettings settings) {
        super(settings);
    }

    public void useWhip(World world, PlayerEntity user, Hand hand) {
        if (hand == Hand.OFF_HAND && user instanceof PlayerHookWrapper wrapper) {
            if (wrapper.getHook() == null) {
                if (!world.isClient) {
                    world.playSound(null, user.getX(), user.getY(), user.getZ(), Whipdashing.HOOK_THROW, SoundCategory.PLAYERS, 0.5F, 0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F));
                    world.spawnEntity(new HookEntity(user, world));
                }
                user.incrementStat(Stats.USED.getOrCreateStat(this));
            }
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
}