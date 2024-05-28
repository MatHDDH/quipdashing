package mathd.whipdashing.util;

import net.minecraft.entity.Entity;

public interface DashingProjectileWrapper {
    Entity getHomingTarget();
    void setHomingTarget(Entity target);
    boolean betrayOwner(Entity accomplice);
}