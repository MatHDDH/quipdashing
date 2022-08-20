package amymialee.whipdashing.entities;

import amymialee.whipdashing.items.LatchItem;
import amymialee.whipdashing.registries.WhipEntities;
import amymialee.whipdashing.registries.WhipItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class LatchEntity extends Entity {
    public static final TrackedData<Integer> LATCH_STATUS = DataTracker.registerData(LatchEntity.class, TrackedDataHandlerRegistry.INTEGER);
    public int latchAge;
    public int hookTime;
    public int shrinkTime;
    public int lastShrink = 0;

    public LatchEntity(EntityType<? extends LatchEntity> entityType, World world) {
        super(entityType, world);
        this.latchAge = this.random.nextInt(100000);
    }

    public LatchEntity(World world, double x, double y, double z) {
        this(WhipEntities.LATCH_ENTITY, world);
        this.setPosition(x, y, z);
    }

    public void tick() {
        if (LatchStatus.values()[dataTracker.get(LATCH_STATUS)].active) {
            this.latchAge++;
        } else {
            if (latchAge % 20 != 0) {
                this.latchAge++;
            }
        }
        if (this.hookTime > 0) {
            hookTime--;
        }
        PlayerEntity playerEntity = world.getClosestPlayer(this.getX(), this.getY(), this.getZ(), 3.0, false);
        this.lastShrink = this.shrinkTime;
        if (playerEntity != null) {
            if (this.shrinkTime < 3) {
                this.shrinkTime++;
            }
        } else if (this.shrinkTime > 0) {
            this.shrinkTime--;
        }
    }

    @Override
    public ActionResult interact(PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (stack.getItem() instanceof LatchItem) {
            this.toggleActive();
        }
        return super.interact(player, hand);
    }

    protected Entity.MoveEffect getMoveEffect() {
        return Entity.MoveEffect.NONE;
    }

    public boolean damage(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) {
            return false;
        } else {
            if (!this.isRemoved() && !this.world.isClient) {
                if (source.getAttacker() instanceof PlayerEntity player && player.getMainHandStack().getItem() instanceof LatchItem) {
                    ItemEntity itemEntity;
                    if (isSlingshot()) {
                        itemEntity = this.dropItem(WhipItems.SLIPLATCH);
                    } else {
                        itemEntity = this.dropItem(WhipItems.TRIPLATCH);
                    }
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
        if (isSlingshot()) {
            return new ItemStack(WhipItems.SLIPLATCH);
        }
        return new ItemStack(WhipItems.TRIPLATCH);
    }

    public Packet<?> createSpawnPacket() {
        return new EntitySpawnS2CPacket(this);
    }

    @Override
    protected void initDataTracker() {
        this.dataTracker.startTracking(LATCH_STATUS, 1);
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        this.dataTracker.set(LATCH_STATUS, nbt.getInt("latch_status"));
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.putInt("latch_status", this.dataTracker.get(LATCH_STATUS));
    }

    public boolean isActive() {
        return LatchStatus.values()[this.dataTracker.get(LATCH_STATUS)].active;
    }

    public void toggleActive() {
        int status = this.dataTracker.get(LATCH_STATUS);
        if (isActive()) {
            this.dataTracker.set(LATCH_STATUS, status - 1);
        } else {
            this.dataTracker.set(LATCH_STATUS, status + 1);
        }
    }

    public boolean isSlingshot() {
        return this.dataTracker.get(LATCH_STATUS) > 1;
    }

    public void toggleSlingshot() {
        int status = this.dataTracker.get(LATCH_STATUS);
        if (isSlingshot()) {
            this.dataTracker.set(LATCH_STATUS, status - 2);
        } else {
            this.dataTracker.set(LATCH_STATUS, status + 2);
        }
    }

    enum LatchStatus {
        INACTIVE_STANDARD(false),
        STANDARD(true),
        INACTIVE_SLINGSHOT(false),
        SLINGSHOT(true);

        final boolean active;

        LatchStatus(boolean active) {
            this.active = active;
        }
    }
}