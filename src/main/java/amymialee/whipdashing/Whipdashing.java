package amymialee.whipdashing;

import amymialee.whipdashing.items.WhipdashItem;
import amymialee.whipdashing.registries.WhipEntities;
import amymialee.whipdashing.registries.WhipItems;
import amymialee.whipdashing.util.PlayerHookWrapper;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.GameMode;

public class Whipdashing implements ModInitializer {
    public static final String MOD_ID = "whipdashing";
    public static final TagKey<EntityType<?>> HEAVY_ENTITIES = TagKey.of(Registry.ENTITY_TYPE_KEY, id("heavy_entities"));
    public static final Identifier whipdashUsage = id("whipdash");
    public static final Identifier whipdashRetract = id("whipdash_retract");
    public static final Identifier whipdashJump = id("whipdash_jump");
    public static final SoundEvent HOOK_THROW = Registry.register(Registry.SOUND_EVENT, "whipdashing.entity.hook.throw", new SoundEvent(id("whipdashing.entity.hook.throw")));
    public static final SoundEvent HOOK_RETRIEVE = Registry.register(Registry.SOUND_EVENT, "whipdashing.entity.hook.retrieve", new SoundEvent(id("whipdashing.entity.hook.retrieve")));
    public static final SoundEvent WHIPDASH_SWAP = Registry.register(Registry.SOUND_EVENT, "whipdashing.item.whipdash.swap", new SoundEvent(id("whipdashing.item.whipdash.swap")));
    public static final SoundEvent HOOK_RETURN = Registry.register(Registry.SOUND_EVENT, "whipdashing.entity.hook.return", new SoundEvent(id("whipdashing.entity.hook.return")));

    @Override
    public void onInitialize() {
        WhipItems.init();
        WhipEntities.init();
        ServerPlayNetworking.registerGlobalReceiver(whipdashUsage, ((server, player, handler, buf, responseSender) -> server.execute(() -> {
            ItemStack itemStack = player.getStackInHand(Hand.OFF_HAND);
            if (itemStack.getItem() instanceof WhipdashItem item) {
                if (player.interactionManager.getGameMode() == GameMode.SPECTATOR) {
                    return;
                } else if (player.getItemCooldownManager().isCoolingDown(item)) {
                    return;
                }
                item.useWhip(player.world, player, Hand.OFF_HAND);
                player.swingHand(Hand.OFF_HAND, true);
            }
        })));
        ServerPlayNetworking.registerGlobalReceiver(whipdashRetract, ((server, player, handler, buf, responseSender) -> server.execute(() -> {
            ItemStack itemStack = player.getStackInHand(Hand.OFF_HAND);
            if (itemStack.getItem() instanceof WhipdashItem item) {
                if (player.interactionManager.getGameMode() == GameMode.SPECTATOR) {
                    return;
                } else if (player.getItemCooldownManager().isCoolingDown(item)) {
                    return;
                }
                item.retractWhip(player.world, player, Hand.OFF_HAND);
                player.swingHand(Hand.OFF_HAND, true);
            }
        })));
        ServerPlayNetworking.registerGlobalReceiver(whipdashJump, ((server, player, handler, buf, responseSender) -> server.execute(() -> {
            if (player instanceof PlayerHookWrapper wrapper) {
                wrapper.getHook().discard();
                wrapper.setHook(null);
                player.world.playSound(null, player.getX(), player.getY(), player.getZ(), Whipdashing.HOOK_RETURN, SoundCategory.PLAYERS, 0.6f, 4f);
                player.fallDistance = 0;
            }
        })));
    }

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }
}