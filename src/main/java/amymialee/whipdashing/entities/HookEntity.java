package amymialee.whipdashing.entities;

import amymialee.whipdashing.Whipdashing;
import amymialee.whipdashing.items.WhipdashItem;
import amymialee.whipdashing.registries.WhipEntities;
import amymialee.whipdashing.util.DashingProjectileWrapper;
import amymialee.whipdashing.util.PersistentGroundWrapper;
import amymialee.whipdashing.util.PlayerHookWrapper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class HookEntity extends ProjectileEntity {
    private static final TrackedData<Integer> HOOK_ENTITY_ID = DataTracker.registerData(HookEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private Entity hookedEntity;
    private State state;

    public HookEntity(EntityType<? extends HookEntity> type, World world) {
        super(type, world);
        this.setState(State.FLYING);
        this.ignoreCameraFrustum = true;
    }

    public HookEntity(PlayerEntity thrower, World world) {
        this(WhipEntities.HOOK_ENTITY, world);
        this.setOwner(thrower);
        this.refreshPositionAndAngles(thrower.getX(), thrower.getEyeY(), thrower.getZ(), thrower.getYaw(), thrower.getPitch());
        float f = -MathHelper.sin(getYaw() * 0.017453292F) * MathHelper.cos(getPitch() * 0.017453292F);
        float g = -MathHelper.sin(getPitch() * 0.017453292F);
        float h = MathHelper.cos(getYaw() * 0.017453292F) * MathHelper.cos(getPitch() * 0.017453292F);
        this.setVelocity(new Vec3d(f, g, h).normalize().multiply(3f));
    }

    protected void initDataTracker() {
        this.getDataTracker().startTracking(HOOK_ENTITY_ID, 0);
    }

    public void onTrackedDataSet(TrackedData<?> data) {
        if (HOOK_ENTITY_ID.equals(data)) {
            int i = this.getDataTracker().get(HOOK_ENTITY_ID);
            this.setHookedEntity(i > 0 ? this.world.getEntityById(i - 1) : null);
        }
        super.onTrackedDataSet(data);
    }

    public boolean shouldRender(double distance) {
        return distance < 4096.0;
    }

    public void updateTrackedPositionAndAngles(double x, double y, double z, float yaw, float pitch, int interpolationSteps, boolean interpolate) {}

    public void tick() {
        super.tick();
        PlayerEntity player = this.getPlayerOwner();
        if (player == null) {
            this.remove(false);
        } else if (this.keepIfValid(player) || this.world.isClient) {
            this.updateRotation();
            switch (this.getState()) {
                case FLYING -> {
                    this.checkForCollision();
                    this.move(MovementType.SELF, this.getVelocity());
                    if (this.onGround || this.horizontalCollision) {
                        this.setState(State.RETURNING_EMPTY);
                    }
                    if (this.getHookedEntity() != null) {
                        if (this.isHeavy(this.getHookedEntity(), player)) {
                            this.setState(State.HOOKED_HEAVY_ENTITY);
                        } else if (this.getHookedEntity() instanceof DashingProjectileWrapper && !(this.getHookedEntity() instanceof PersistentGroundWrapper ground && ground.isInGround())) {
                            this.setState(State.PUSHING_PROJECTILE);
                        } else {
                            this.setState(State.HOOKED_LIGHT_ENTITY);
                        }
                    }
                }
                case PUSHING_PROJECTILE -> {
                    this.move(MovementType.SELF, this.getVelocity());
                    if (this.getHookedEntity() != null && !this.getHookedEntity().isRemoved() && this.getHookedEntity().world.getRegistryKey() == this.world.getRegistryKey()) {
                        this.getHookedEntity().setPosition(this.getPos());
                        this.getHookedEntity().setVelocity(this.getVelocity());
                    }
                    if (this.onGround || this.horizontalCollision) {
                        this.setState(State.RETURNING_EMPTY);
                        this.setHookedEntity(null);
                    }
                }
                case HOOKED_HEAVY_ENTITY, HOOKED_LIGHT_ENTITY -> {
                    if (this.getHookedEntity() != null && !this.getHookedEntity().isRemoved() && this.getHookedEntity().world.getRegistryKey() == this.world.getRegistryKey()) {
                        this.setPosition(this.getHookedEntity().getX(), this.getHookedEntity().getBodyY(0.5), this.getHookedEntity().getZ());
                    } else {
                        this.updateHookedEntityId(null);
                        this.setState(State.RETURNING_EMPTY);
                    }
                }
                case RETURNING_EMPTY -> {
                    this.noClip = true;
                    double x = player.getX() - this.getX();
                    double y = player.getEyeY() - this.getY();
                    double z = player.getZ() - this.getZ();
                    Vec3d vec3d = new Vec3d(x, y, z).normalize().multiply(Math.min(4, this.distanceTo(player)));
                    this.setVelocity(vec3d);
                    this.move(MovementType.SELF, this.getVelocity());
                    if (this.distanceTo(player) < 0.1) {
                        this.remove(false);
                    }
                    if (this.world.isClient() && this.distanceTo(player) < 0.1) {
                        this.discard();
                    }
                }
                case RETURNING_PULLING -> {
                    this.noClip = true;
                    double x = player.getX() - this.getX();
                    double y = player.getY() - this.getY();
                    double z = player.getZ() - this.getZ();
                    Vec3d vec3d = new Vec3d(x, y, z).normalize().multiply(Math.min(2, this.distanceTo(player)));
                    this.setVelocity(vec3d);
                    this.move(MovementType.SELF, this.getVelocity());
                    if (this.getHookedEntity() != null && !this.getHookedEntity().isRemoved() && this.getHookedEntity().world.getRegistryKey() == this.world.getRegistryKey()) {
                        this.getHookedEntity().setPosition(this.getPos());
                        this.getHookedEntity().setVelocity(this.getVelocity());
                    }
                    if (this.getHookedEntity() != null && this.distanceTo(player) < 3) {
                        this.getHookedEntity().fallDistance = 0;
                        if (this.getHookedEntity() instanceof LivingEntity) {
                            this.getHookedEntity().setVelocity(0, 0.4, 0);
                        } else {
                            this.getHookedEntity().setVelocity(new Vec3d(x, y, z).normalize().multiply(2));
                        }
                        this.remove(true);
                    }
                    if (this.world.isClient() && this.distanceTo(player) < 0.1) {
                        this.discard();
                    }
                    if (this.getHookedEntity() instanceof DashingProjectileWrapper wrapper && wrapper.betrayOwner(player)) {
                        this.setState(State.RETURNING_REFLECTING);
                    }
                }
                case RETURNING_REFLECTING -> {
                    if (this.getHookedEntity() instanceof DashingProjectileWrapper wrapper && wrapper.getHomingTarget() != null) {
                        Entity target = wrapper.getHomingTarget();
                        double x = target.getX() - this.getX();
                        double y = target.getY() - this.getY();
                        double z = target.getZ() - this.getZ();
                        Vec3d vec3d = new Vec3d(x, y, z).normalize().multiply(Math.min(2, this.distanceTo(target)));
                        this.setVelocity(vec3d);
                        this.move(MovementType.SELF, this.getVelocity());
                        if (this.getHookedEntity() != null && !this.getHookedEntity().isRemoved() && this.getHookedEntity().world.getRegistryKey() == this.world.getRegistryKey()) {
                            this.getHookedEntity().setPosition(this.getPos());
                            this.getHookedEntity().setVelocity(this.getVelocity());
                        }
                        if (this.distanceTo(target) < player.distanceTo(target)) {
                            this.getHookedEntity().fallDistance = 0;
                            this.getHookedEntity().setVelocity(new Vec3d(x, y, z).normalize().multiply(2));
                            this.setState(State.RETURNING_EMPTY);
                            this.setHookedEntity(null);
                        }
                    } else {
                        this.setState(State.RETURNING_PULLING);
                    }
                }
                case PULLING_OWNER -> {
                    double x = this.getX() - player.getX();
                    double y = this.getY() - player.getY();
                    double z = this.getZ() - player.getZ();
                    Vec3d vec3d = new Vec3d(x, y, z).normalize().multiply(Math.min(0.75f, player.distanceTo(this)));
                    player.setVelocity(vec3d);
                    player.move(MovementType.SELF, player.getVelocity());
                    if (this.getHookedEntity() != null && !this.getHookedEntity().isRemoved() && this.getHookedEntity().world.getRegistryKey() == this.world.getRegistryKey()) {
                        this.setPosition(this.getHookedEntity().getX(), this.getHookedEntity().getBodyY(0.5), this.getHookedEntity().getZ());
                        this.setVelocity(this.getHookedEntity().getVelocity());
                    }
                    if (player.distanceTo(this) < 2) {
                        player.fallDistance = 0;
                        this.remove(true);
                        if (this.world.isClient()) {
                            if (this.getHookedEntity() instanceof LatchEntity latch) {
                                if (latch.isSlingshot()) {
                                    player.setVelocity(new Vec3d(x, y, z).normalize().multiply(2.25f));
                                } else {
                                    player.setVelocity(0, 0.2, 0);
                                }
                            } else {
                                player.setVelocity(0, 0.2, 0);
                            }
                            this.discard();
                        }
                    }
                }
            }
            this.refreshPosition();
            if (this.getHookedEntity() instanceof LatchEntity latch) {
                latch.hookTime = 4;
            }
        }
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        PlayerEntity user = this.getPlayerOwner();
        if (user != null) {
            if (state == State.RETURNING_EMPTY || state == State.RETURNING_PULLING || state == State.RETURNING_REFLECTING || state == State.PULLING_OWNER) {
                if (state != State.RETURNING_EMPTY && !user.isOnGround()) {
                    this.world.sendEntityStatus(this, (byte) 123);
                } else {
                    this.world.sendEntityStatus(this, (byte) 121);
                }
                world.playSound(null, user.getX(), user.getY(), user.getZ(), Whipdashing.HOOK_RETRIEVE, SoundCategory.PLAYERS, 4.0F, 1.8F);
            } else if (state == State.HOOKED_LIGHT_ENTITY || state == State.HOOKED_HEAVY_ENTITY) {
                world.playSound(null, user.getX(), user.getY(), user.getZ(), Whipdashing.HOOK_RETRIEVE, SoundCategory.PLAYERS, 4.0F, 0.4F);
            }
        }
        this.state = state;
    }

    private void remove(boolean sound) {
        if (!world.isClient()) {
            if (sound) {
                world.playSound(null, this.getX(), this.getY(), this.getZ(), Whipdashing.HOOK_RETURN, SoundCategory.PLAYERS, 0.6f, 4f);
            }
            this.discard();
        }
    }

    public void handleStatus(byte status) {
        if (status == 121 && this.world.isClient) {
            this.setState(State.RETURNING_EMPTY);
        }
        if (status == 122 && this.world.isClient) {
            this.setState(State.PULLING_OWNER);
        }
        Entity entity = this.getPlayerOwner();
        if (status == 123 && this.world.isClient && entity != null && entity == MinecraftClient.getInstance().player) {
            this.setState(State.RETURNING_EMPTY);
            entity.setVelocity(entity.getVelocity().x, Math.max(entity.getVelocity().y, 0.3), entity.getVelocity().z);
            entity.fallDistance = 0;
        }
        super.handleStatus(status);
    }

    public float distanceTo(Entity entity) {
        float f = (float)(this.getX() - entity.getX());
        float g = (float)(this.getY() - entity.getEyeY());
        float h = (float)(this.getZ() - entity.getZ());
        return MathHelper.sqrt(f * f + g * g + h * h);
    }

    private boolean keepIfValid(PlayerEntity player) {
        boolean bl = player.getOffHandStack().getItem() instanceof WhipdashItem;
        if (!player.isRemoved() && player.isAlive() && bl && player instanceof PlayerHookWrapper wrapper && wrapper.getHook() == this) {
            return true;
        }
        this.remove(false);
        return false;
    }

    private void checkForCollision() {
        Vec3d velocity = this.getVelocity();
        Vec3d pos = this.getPos();
        Vec3d target = pos.add(velocity.multiply(2));
        EntityHitResult entityHitResult = ProjectileUtil.getEntityCollision(world, this, pos, target, this.getBoundingBox().stretch(velocity).expand(2.0), this::canHit);
        if (entityHitResult != null) {
            this.onCollision(entityHitResult);
        } else {
            HitResult hitResult = ProjectileUtil.getCollision(this, this::canHit);
            this.onCollision(hitResult);
        }
    }

    protected boolean canHit(Entity entity) {
        if (!entity.isSpectator() && entity.isAlive()) {
            Entity entity2 = this.getOwner();
            return entity2 == null || !entity2.isConnectedThroughVehicle(entity);
        }
        return false;
    }

    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);
        if (!this.world.isClient && this.getHookedEntity() == null) {
            Entity entity = entityHitResult.getEntity();
            if ((entity instanceof LatchEntity latch && !latch.isActive()) || entity.getType().isIn(Whipdashing.IGNORED_ENTITIES) || entity.getScoreboardTags().contains("wd-ignored")) {
                return;
            }
            this.updateHookedEntityId(entityHitResult.getEntity());
        }
    }

    private void updateHookedEntityId(@Nullable Entity entity) {
        this.setHookedEntity(entity);
        this.getDataTracker().set(HOOK_ENTITY_ID, entity == null ? 0 : entity.getId() + 1);
    }

    public void writeCustomDataToNbt(NbtCompound nbt) {}

    public void readCustomDataFromNbt(NbtCompound nbt) {}

    public void use() {
        PlayerEntity playerEntity = this.getPlayerOwner();
        if (!this.world.isClient && playerEntity != null && this.keepIfValid(playerEntity)) {
            if (this.getState() == State.FLYING) {
                this.setState(State.RETURNING_EMPTY);
            } else if (this.getState() == State.HOOKED_HEAVY_ENTITY) {
                this.setState(State.PULLING_OWNER);
                this.world.sendEntityStatus(this, (byte) 122);
            } else if (this.getState() == State.HOOKED_LIGHT_ENTITY || this.getState() == State.PUSHING_PROJECTILE) {
                this.setState(State.RETURNING_PULLING);
            } else if (this.getState() == State.RETURNING_PULLING || state == State.RETURNING_REFLECTING || this.getState() == State.PULLING_OWNER) {
                this.setState(State.RETURNING_EMPTY);
                this.world.sendEntityStatus(this, (byte) 123);
            }
        }
    }

    public boolean isHeavy(Entity entity, LivingEntity owner) {
        ItemStack dash = owner.getOffHandStack();
        if (dash.getItem() instanceof WhipdashItem && dash.getOrCreateNbt().getBoolean("wd:light")) return true;
        return entity.getType().isIn(Whipdashing.HEAVY_ENTITIES) || entity.getScoreboardTags().contains("wd-heavy") || entity.getType().getTrackTickInterval() == Integer.MAX_VALUE || ((this.getHookedEntity() instanceof LivingEntity living && living.getMaxHealth() > owner.getMaxHealth() * 1.5f));
    }

    protected MoveEffect getMoveEffect() {
        return MoveEffect.NONE;
    }

    public void remove(RemovalReason reason) {
        this.onRemoved();
        super.remove(reason);
    }

    public void onRemoved() {
        this.setPlayerHook(null);
    }

    public void setOwner(@Nullable Entity entity) {
        super.setOwner(entity);
        this.setPlayerHook(this);
    }

    private void setPlayerHook(@Nullable HookEntity hook) {
        PlayerEntity playerEntity = this.getPlayerOwner();
        if (playerEntity instanceof PlayerHookWrapper wrapper) {
            wrapper.setHook(hook);
        }
    }

    @Nullable
    public PlayerEntity getPlayerOwner() {
        Entity entity = this.getOwner();
        return entity instanceof PlayerEntity player ? player : null;
    }

    @Nullable
    public Entity getHookedEntity() {
        return this.hookedEntity;
    }

    public void setHookedEntity(@Nullable Entity hookedEntity) {
        this.hookedEntity = hookedEntity;
    }

    public boolean canUsePortals() {
        return false;
    }

    public Packet<?> createSpawnPacket() {
        Entity entity = this.getOwner();
        return new EntitySpawnS2CPacket(this, entity == null ? this.getId() : entity.getId());
    }

    public void onSpawnPacket(EntitySpawnS2CPacket packet) {
        super.onSpawnPacket(packet);
        if (this.getPlayerOwner() == null) {
            this.kill();
        }
    }

    public enum State {
        FLYING,
        PUSHING_PROJECTILE,
        HOOKED_LIGHT_ENTITY,
        HOOKED_HEAVY_ENTITY,
        RETURNING_EMPTY,
        RETURNING_PULLING,
        RETURNING_REFLECTING,
        PULLING_OWNER
    }
}