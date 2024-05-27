package amymialee.whipdashing.registries;

import amymialee.whipdashing.Whipdashing;
import amymialee.whipdashing.entities.HookEntity;
import amymialee.whipdashing.entities.LatchEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

@SuppressWarnings("unused")
public class WhipEntities {
    public static final EntityType<HookEntity> HOOK_ENTITY = registerEntity("hook_entity", FabricEntityTypeBuilder.<HookEntity>create(SpawnGroup.MISC, HookEntity::new).disableSaving().disableSummon().dimensions(EntityDimensions.fixed(0.25f, 0.25f)).trackRangeChunks(4).build());
    public static final EntityType<LatchEntity> LATCH_ENTITY = registerEntity("latch_entity", FabricEntityTypeBuilder.<LatchEntity>create(SpawnGroup.MISC, LatchEntity::new).dimensions(EntityDimensions.changing(2.0F, 2.0F)).trackRangeChunks(16).trackedUpdateRate(Integer.MAX_VALUE).build());

    public static void init() {}

    private static <T extends Entity> EntityType<T> registerEntity(String name, EntityType<T> entity) {
        return Registry.register(Registries.ENTITY_TYPE, Whipdashing.id(name), entity);
    }
}