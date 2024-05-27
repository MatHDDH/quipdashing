package amymialee.whipdashing;

import amymialee.whipdashing.client.HookEntityRenderer;
import amymialee.whipdashing.client.LatchEntityRenderer;
import amymialee.whipdashing.registries.WhipEntities;
import amymialee.whipdashing.registries.WhipItems;
import amymialee.whipdashing.util.PlayerHookWrapper;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class WhipdashingClient implements ClientModInitializer {
    public static final EntityModelLayer LATCH = new EntityModelLayer(Whipdashing.id("latch"), "main");
    public static final KeyBinding whipdashKeybinding = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.%s.whipdash".formatted(Whipdashing.MOD_ID), InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_R, "category.%s".formatted(Whipdashing.MOD_ID)));
    private static boolean active = false;

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(WhipEntities.HOOK_ENTITY, HookEntityRenderer::new);
        EntityRendererRegistry.register(WhipEntities.LATCH_ENTITY, LatchEntityRenderer::new);
        EntityModelLayerRegistry.registerModelLayer(LATCH, LatchEntityRenderer::getTexturedModelData);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (whipdashKeybinding.wasPressed() && whipdashKeybinding.isPressed() && !active) {
                active = true;
                ClientPlayNetworking.send(Whipdashing.whipdashUsage, PacketByteBufs.empty());
            }
            if (!whipdashKeybinding.isPressed() && active) {
                active = false;
                ClientPlayNetworking.send(Whipdashing.whipdashRetract, PacketByteBufs.empty());
            }
        });
    }

    static {
        ModelPredicateProviderRegistry.register(WhipItems.WHIPDASH, new Identifier("cast"), (stack, world, entity, number) -> entity instanceof PlayerHookWrapper wrapper && entity.getOffHandStack() == stack && wrapper.getHook() != null ? 1.0f : 0.0f);
        ModelPredicateProviderRegistry.register(WhipItems.WHIPDASH, new Identifier("light"), (stack, world, entity, number) -> stack.getNbt() != null && stack.getNbt().getBoolean("wd:light") ? 1.0f : 0.0f);
    }
}