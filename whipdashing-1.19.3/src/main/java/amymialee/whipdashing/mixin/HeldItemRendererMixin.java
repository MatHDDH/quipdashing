package amymialee.whipdashing.mixin;

import amymialee.whipdashing.entities.HookEntity;
import amymialee.whipdashing.items.WhipdashItem;
import amymialee.whipdashing.util.PlayerHookWrapper;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;

import net.minecraft.util.math.Vec3i;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HeldItemRenderer.class)
public abstract class HeldItemRendererMixin {
    private int hookTime = 0;

    @Shadow protected abstract void renderArmHoldingItem(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, float equipProgress, float swingProgress, Arm arm);

    @Inject(method = "getHandRenderType", at = @At("HEAD"), cancellable = true)
    private static void getHandRenderType(ClientPlayerEntity player, CallbackInfoReturnable<HeldItemRenderer.HandRenderType> cir) {
        boolean bl = player.getOffHandStack().getItem() instanceof WhipdashItem;
        if (bl) {
            cir.setReturnValue(HeldItemRenderer.HandRenderType.RENDER_BOTH_HANDS);
        }
    }

    @Inject(method = "renderFirstPersonItem", at = @At("HEAD"), cancellable = true)
    private void Whipdashing$RenderingArm(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (item.getItem() instanceof WhipdashItem) {
            ci.cancel();
        }
        if (!player.isUsingSpyglass() && player instanceof PlayerHookWrapper wrapper && wrapper.getHook() != null) {
            if (hookTime < 20) {
                hookTime++;
            }
        } else {
            if (hookTime > 0) {
                hookTime -= 2;
            }
        }
        if (hookTime > 0) {
            boolean bl = hand == Hand.OFF_HAND;
            Arm arm = bl ? player.getMainArm().getOpposite() : player.getMainArm();
            matrices.push();
            if (bl && !player.isInvisible()) {
                float swing = 0.06f / 20 * (Math.min(20, hookTime + tickDelta) - 0.03f);
                if (player instanceof PlayerHookWrapper wrapper && wrapper.getHook() != null) {
                    HookEntity.State state = wrapper.getHook().getState();
                    if (state == HookEntity.State.RETURNING_EMPTY || state == HookEntity.State.RETURNING_PULLING || state == HookEntity.State.RETURNING_REFLECTING || state == HookEntity.State.PULLING_OWNER) {
                        swing = 0.01f / 20 * (Math.min(20, hookTime + tickDelta) - 0.03f);
                        matrices.translate(0, -0.5f, 0);

                        matrices.multiply(new Quaternionf(0.34, 0.0,0.0,0.94));
                    }
                }
                this.renderArmHoldingItem(matrices, vertexConsumers, light, equipProgress, swing, arm);
            }
            matrices.pop();
        }
    }
}