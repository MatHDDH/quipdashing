package amymialee.whipdashing.client;

import amymialee.whipdashing.Whipdashing;
import amymialee.whipdashing.entities.HookEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

public class HookEntityRenderer extends EntityRenderer<HookEntity> {
    private static final Identifier HOOK = Whipdashing.id("textures/entity/hook.png");
    private static final Identifier HOOK_LIGHT = Whipdashing.id("textures/entity/hook_light.png");
    private static final RenderLayer LAYER = RenderLayer.getEntityCutout(HOOK);
    private static final RenderLayer LAYER_LIGHT = RenderLayer.getEntityCutout(HOOK_LIGHT);

    public HookEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    public void render(HookEntity hookEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        var playerEntity = hookEntity.getPlayerOwner();
        if (playerEntity != null) {
            matrixStack.push();
            matrixStack.push();
            matrixStack.scale(0.5F, 0.5F, 0.5F);
            matrixStack.multiply(this.dispatcher.getRotation());
            matrixStack.multiply(RotationAxis.POSITIVE_Y.rotation(180.0F));
            var entry = matrixStack.peek();
            var matrix4f = entry.getPositionMatrix();
            var matrix3f = entry.getNormalMatrix();
            VertexConsumer vertexConsumer;
            var whip = playerEntity.getOffHandStack();
            if (whip.getNbt() != null && whip.getNbt().getBoolean("wd:light")) {
                vertexConsumer = vertexConsumerProvider.getBuffer(LAYER_LIGHT);
            } else {
                vertexConsumer = vertexConsumerProvider.getBuffer(LAYER);
            }
            vertex(vertexConsumer, matrix4f, matrix3f, i, 0.0F, 0, 0, 1);
            vertex(vertexConsumer, matrix4f, matrix3f, i, 1.0F, 0, 1, 1);
            vertex(vertexConsumer, matrix4f, matrix3f, i, 1.0F, 1, 1, 0);
            vertex(vertexConsumer, matrix4f, matrix3f, i, 0.0F, 1, 0, 0);
            matrixStack.pop();
            var j = playerEntity.getMainArm() == Arm.RIGHT ? -1 : 1;
            double o;
            double p;
            double q;
            float r;
            if (this.dispatcher.gameOptions != null && this.dispatcher.gameOptions.getPerspective().isFirstPerson() && playerEntity == MinecraftClient.getInstance().player) {
                var s = 960.0 / (double) this.dispatcher.gameOptions.getFov().getValue();
                var returning = hookEntity.getState() == HookEntity.State.RETURNING_EMPTY || hookEntity.getState() == HookEntity.State.RETURNING_PULLING || hookEntity.getState() == HookEntity.State.RETURNING_REFLECTING || hookEntity.getState() == HookEntity.State.PULLING_OWNER;
                var vec3d = this.dispatcher.camera.getProjection().getPosition((float)j * (returning ? 0.3f : 0.15f), -0.1f).add(0, 0.02, 0);
                vec3d = vec3d.multiply(s);
                o = MathHelper.lerp(g, playerEntity.prevX, playerEntity.getX()) + vec3d.x;
                p = MathHelper.lerp(g, playerEntity.prevY, playerEntity.getY()) + vec3d.y;
                q = MathHelper.lerp(g, playerEntity.prevZ, playerEntity.getZ()) + vec3d.z;
                r = 1.65f;
            } else {
                var l = MathHelper.lerp(g, playerEntity.prevBodyYaw, playerEntity.bodyYaw) * 0.017453292F;
                double d = MathHelper.sin(l);
                double e = MathHelper.cos(l);
                var m = (double) j * 0.35;
                o = MathHelper.lerp(g, playerEntity.prevX, playerEntity.getX()) - e * m;
                p = playerEntity.prevY + (double)playerEntity.getStandingEyeHeight() + (playerEntity.getY() - playerEntity.prevY) * (double)g - 0.65;
                q = MathHelper.lerp(g, playerEntity.prevZ, playerEntity.getZ()) - d * m;
                r = playerEntity.isInSneakingPose() ? -0.1875F : 0.0F;
            }
            var s = MathHelper.lerp(g, hookEntity.prevX, hookEntity.getX());
            var t = MathHelper.lerp(g, hookEntity.prevY, hookEntity.getY()) + 0.25;
            var u = MathHelper.lerp(g, hookEntity.prevZ, hookEntity.getZ());
            var v = (float)(o - s);
            var w = (float)(p - t) + r;
            var x = (float)(q - u);
            var vertexConsumer2 = vertexConsumerProvider.getBuffer(RenderLayer.getLineStrip());
            var entry2 = matrixStack.peek();
            for(var z = 0; z <= 16; ++z) {
                renderFishingLine(v, w, x, vertexConsumer2, entry2, percentage(z, 16), percentage(z + 1, 16));
            }
            matrixStack.pop();
            super.render(hookEntity, f, g, matrixStack, vertexConsumerProvider, i);
        }
    }

    private static float percentage(int value, int max) {
        return (float)value / (float)max;
    }

    private static void vertex(VertexConsumer buffer, Matrix4f matrix, Matrix3f normalMatrix, int light, float x, int y, int u, int v) {
        buffer.vertex(matrix, x - 0.5F, (float)y - 0.5F, 0.0F).color(255, 255, 255, 255).texture((float)u, (float)v).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(normalMatrix, 0.0F, 1.0F, 0.0F).next();
    }

    private static void renderFishingLine(float x, float y, float z, VertexConsumer buffer, MatrixStack.Entry matrices, float segmentStart, float segmentEnd) {
        var f = x * segmentStart;
        var g = y * segmentStart;
        var h = z * segmentStart;
        var i = x * segmentEnd - f;
        var j = y * segmentEnd - g;
        var k = z * segmentEnd - h;
        var l = MathHelper.sqrt(i * i + j * j + k * k);
        i /= l;
        j /= l;
        k /= l;
        buffer.vertex(matrices.getPositionMatrix(), f, g, h).color(0, 0, 0, 255).normal(matrices.getNormalMatrix(), i, j, k).next();
    }

    public Identifier getTexture(HookEntity hookEntity) {
        var player = hookEntity.getPlayerOwner();
        if (player != null) {
            var whip = player.getOffHandStack();
            if (whip.getNbt() != null && whip.getNbt().getBoolean("wd:light")) {
                return HOOK_LIGHT;
            }
        }
        return HOOK;
    }
}