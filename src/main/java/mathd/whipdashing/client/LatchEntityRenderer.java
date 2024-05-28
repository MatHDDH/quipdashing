package mathd.whipdashing.client;

import mathd.whipdashing.Whipdashing;
import mathd.whipdashing.WhipdashingClient;
import mathd.whipdashing.entities.LatchEntity;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

public class LatchEntityRenderer extends EntityRenderer<LatchEntity> {
    private static final Identifier TEXTURE_INACTIVE = Whipdashing.id("textures/entity/latch_inactive.png");
    private static final Identifier TEXTURE = Whipdashing.id("textures/entity/latch.png");
    private static final Identifier TEXTURE_SLINGSHOT = Whipdashing.id("textures/entity/latch_slingshot.png");
    private static final RenderLayer LAYER_INACTIVE = RenderLayer.getEntityCutoutNoCull(TEXTURE_INACTIVE);
    private static final RenderLayer LAYER = RenderLayer.getEntityCutoutNoCull(TEXTURE);
    private static final RenderLayer LAYER_SLINGSHOT = RenderLayer.getEntityCutoutNoCull(TEXTURE_SLINGSHOT);

    private static final String GLASS = "glass";
    private final ModelPart core;
    private final ModelPart frame;

    public LatchEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
        this.shadowRadius = 0.5F;
        ModelPart modelPart = context.getPart(WhipdashingClient.LATCH);
        this.frame = modelPart.getChild(GLASS);
        this.core = modelPart.getChild(EntityModelPartNames.CUBE);
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        modelPartData.addChild(GLASS, ModelPartBuilder.create().uv(0, 0).cuboid(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F), ModelTransform.NONE);
        modelPartData.addChild(EntityModelPartNames.CUBE, ModelPartBuilder.create().uv(0, 16).cuboid(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F), ModelTransform.NONE);
        return TexturedModelData.of(modelData, 32, 32);
    }

    public void render(LatchEntity latchEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        matrixStack.push();
        boolean active = latchEntity.isActive();
        float j = ((float)latchEntity.latchAge + g) / 16.0F;
        if (latchEntity.hookTime > 0) {
            j *= 4;
        }
        VertexConsumer vertexConsumer;
        if (active) {
            if (latchEntity.isSlingshot()) {
                vertexConsumer = vertexConsumerProvider.getBuffer(LAYER_SLINGSHOT);
            } else {
                vertexConsumer = vertexConsumerProvider.getBuffer(LAYER);
            }
        } else {
            vertexConsumer = vertexConsumerProvider.getBuffer(LAYER_INACTIVE);
        }
        matrixStack.push();
        matrixStack.scale(2F, 2F, 2F);
        matrixStack.translate(0.0, -0.5, 0.0);
        int k = OverlayTexture.DEFAULT_UV;
        if (active) {
            matrixStack.multiply(RotationAxis.POSITIVE_Y.rotation(j));
            matrixStack.translate(0.0, 1.0f, 0.0);

        } else {
            matrixStack.translate(0.0, 1.0f, 0.0);
        }
        this.frame.render(matrixStack, vertexConsumer, i, k);
        if (active) {
            matrixStack.multiply(RotationAxis.POSITIVE_X.rotation(j));

        }
        matrixStack.scale(0.875F, 0.875F, 0.875F);
        this.frame.render(matrixStack, vertexConsumer, i, k);
        if (active) {
            matrixStack.multiply(RotationAxis.POSITIVE_Z.rotation(j));
            ;
        }
        matrixStack.scale(0.875F, 0.875F, 0.875F);
        if (active) {
            if (latchEntity.hookTime > 0) {
                matrixStack.scale(0.65F, 0.65F, 0.65F);
            } else if (latchEntity.shrinkTime > 0) {
                boolean changing = latchEntity.shrinkTime != latchEntity.lastShrink;
                boolean growing = latchEntity.shrinkTime > latchEntity.lastShrink;
                float modifier = 1 - (0.45f / 3) * (latchEntity.shrinkTime + (changing ? (growing ? g : -g) : 0));
                matrixStack.scale(modifier, modifier, modifier);
            }
        }
        this.core.render(matrixStack, vertexConsumer, i, k);
        matrixStack.pop();
        matrixStack.pop();
        super.render(latchEntity, f, g, matrixStack, vertexConsumerProvider, i);
    }

    public Identifier getTexture(LatchEntity latch) {
        if (latch.isActive()) {
            if (latch.isSlingshot()) {
                return TEXTURE_SLINGSHOT;
            } else {
                return TEXTURE;
            }
        } else {
            return TEXTURE_INACTIVE;
        }
    }
}