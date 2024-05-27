package amymialee.whipdashing.mixin;

import amymialee.whipdashing.Whipdashing;
import amymialee.whipdashing.entities.HookEntity;
import amymialee.whipdashing.util.ClientUtil;
import amymialee.whipdashing.util.PlayerHookWrapper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements PlayerHookWrapper {
    @Unique
    HookEntity hook;

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public void setHook(HookEntity hook) {
        this.hook = hook;
    }

    @Override
    public HookEntity getHook() {
        return this.hook;
    }

    @Inject(method = "tickMovement", at = @At("HEAD"))
    public void Whipdashing$PullOwner(CallbackInfo ci) {
        if (this.hook != null && this.hook.getState() == HookEntity.State.PULLING_OWNER) {
            if (this.jumping) {
                this.jump();
                float f = this.getYaw() * 0.017453292F;
                this.setVelocity(this.getVelocity().add(-MathHelper.sin(f), 0.0, MathHelper.cos(f)));
                if (world.isClient()) {
                    ClientUtil.whipJump();
                }
                this.hook.discard();
                this.setHook(null);
                this.world.playSound(null, this.getX(), this.getY(), this.getZ(), Whipdashing.HOOK_RETURN, SoundCategory.PLAYERS, 0.6f, 4f);
                this.fallDistance = 0;
            }
        }
    }
}