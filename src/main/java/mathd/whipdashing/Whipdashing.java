package mathd.whipdashing;

import mathd.whipdashing.items.WhipdashItem;
import mathd.whipdashing.registries.WhipEntities;
import mathd.whipdashing.registries.WhipItems;
import mathd.whipdashing.util.PlayerHookWrapper;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;

import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registry;
import net.minecraft.registry.Registries;
import net.minecraft.world.GameMode;

public class Whipdashing implements ModInitializer {
    public static final String MOD_ID = "whipdashing";
    public static final TagKey<EntityType<?>> HEAVY_ENTITIES = TagKey.of(Registries.ENTITY_TYPE.getKey(), id("heavy_entities"));
    public static final TagKey<EntityType<?>> IGNORED_ENTITIES = TagKey.of(Registries.ENTITY_TYPE.getKey(), id("ignored_entities"));
    public static final Identifier whipdashUsage = id("whipdash");
    public static final Identifier whipdashRetract = id("whipdash_retract");
    public static final Identifier whipdashJump = id("whipdash_jump");

    private static SoundEvent soundRegister(String name){
        Identifier id = new Identifier(Whipdashing.MOD_ID, name);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }


    public static final SoundEvent HOOK_THROW = soundRegister("entity.hook.throw");
    public static final SoundEvent HOOK_RETRIEVE = soundRegister("entity.hook.retrieve");
    public static final SoundEvent WHIPDASH_SWAP = soundRegister("item.whipdash.swap");
    public static final SoundEvent HOOK_RETURN = soundRegister("entity.hook.return");






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