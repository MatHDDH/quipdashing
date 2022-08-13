package amymialee.whipdashing.mixin;

import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.hit.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ProjectileEntity.class)
public class ProjectileEntityMixin {
    @Inject(method = "onEntityHit", at = @At("HEAD"))
    private void main(EntityHitResult entityHitResult, CallbackInfo ci) {
        entityHitResult.getEntity().timeUntilRegen = 0;
    }
}