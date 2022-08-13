package amymialee.whipdashing.entities;

import amymialee.whipdashing.registries.WhipEntities;
import amymialee.whipdashing.registries.WhipItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.world.World;

public class LatchEntity extends Entity {
    public int latchAge;
    public int hookTime;

    public LatchEntity(EntityType<? extends LatchEntity> entityType, World world) {
        super(entityType, world);
        this.latchAge = this.random.nextInt(100000);
    }

    public LatchEntity(World world, double x, double y, double z) {
        this(WhipEntities.LATCH_ENTITY, world);
        this.setPosition(x, y, z);
    }

    public void tick() {
        this.latchAge++;
        if (this.hookTime > 0) {
            hookTime--;
        }
    }

    protected Entity.MoveEffect getMoveEffect() {
        return Entity.MoveEffect.NONE;
    }

    public boolean canHit() {
        return true;
    }

    public boolean damage(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) {
            return false;
        } else {
            if (!this.isRemoved() && !this.world.isClient) {
                if (source.getAttacker() instanceof PlayerEntity player && player.getMainHandStack().isOf(WhipItems.LATCH)) {
                    ItemEntity itemEntity = this.dropItem(WhipItems.LATCH);
                    if (itemEntity != null) {
                        itemEntity.setVelocity(itemEntity.getVelocity().add((this.random.nextFloat() - this.random.nextFloat()) * 0.1F, this.random.nextFloat() * 0.05F, (this.random.nextFloat() - this.random.nextFloat()) * 0.1F));
                        itemEntity.setPickupDelay(0);
                    }
                    this.discard();
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected float getEyeHeight(EntityPose pose, EntityDimensions dimensions) {
        return dimensions.height * 0.5F;
    }

    public ItemStack getPickBlockStack() {
        return new ItemStack(WhipItems.LATCH);
    }

    public Packet<?> createSpawnPacket() {
        return new EntitySpawnS2CPacket(this);
    }

    @Override
    protected void initDataTracker() {}

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {}

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {}
}