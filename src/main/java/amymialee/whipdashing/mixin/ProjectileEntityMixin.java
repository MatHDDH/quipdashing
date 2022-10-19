package amymialee.whipdashing.mixin;

import amymialee.whipdashing.util.DashingProjectileWrapper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ProjectileEntity.class)
public abstract class ProjectileEntityMixin extends Entity implements DashingProjectileWrapper {
    @Shadow public abstract @Nullable Entity getOwner();
    @Shadow public abstract void setOwner(@Nullable Entity entity);
    @Shadow public abstract void setVelocity(double x, double y, double z, float speed, float divergence);

    public ProjectileEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Unique
    public Entity target;

    @Override
    public Entity getHomingTarget() {
        return target;
    }

    @Override
    public void setHomingTarget(Entity target) {
        this.target = target;
    }

    @Override
    public boolean betrayOwner(Entity accomplice) {
        if (this.getOwner() != null && accomplice != this.getOwner()) {
            this.setHomingTarget(this.getOwner());
            this.setOwner(accomplice);
            return true;
        }
        return false;
    }

    @Inject(method = "onEntityHit", at = @At("HEAD"))
    private void Whipdashing$PiercingProjectiles(EntityHitResult entityHitResult, CallbackInfo ci) {
        entityHitResult.getEntity().timeUntilRegen = 0;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void Whipdashing$Homing(CallbackInfo ci) {
        if (target != null && target.isAlive()) {
            Vec3d targetPos = target.getPos();
            double x = (targetPos.x - getX());
            double y = (target.getBodyY(0.5f) - getY());
            double z = (targetPos.z - getZ());
            Vec3d velocity = new Vec3d(x, y, z);
            velocity.normalize();
            this.setVelocity(velocity.x, velocity.y, velocity.z, 2, 0);
        } else {
            target = null;
        }
    }

    @Inject(method = "onCollision", at = @At("HEAD"), cancellable = true)
    protected void Whipdashing$HomingEnd(HitResult hitResult, CallbackInfo ci) {
        if (hitResult instanceof EntityHitResult entityHitResult) {
            if (this.getHomingTarget() != null && entityHitResult.getEntity() == this.getOwner()) {
                ci.cancel();
            } else {
                this.setHomingTarget(null);
            }
        }
    }
}