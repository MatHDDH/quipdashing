package amymialee.whipdashing.items;

import amymialee.whipdashing.entities.LatchEntity;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

import java.util.List;

public class LatchItem extends Item {
    public LatchItem(FabricItemSettings settings) {
        super(settings);
    }

    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos blockPos = context.getBlockPos();
        BlockPos blockPos2 = blockPos.add(context.getSide().getVector());
        if (!world.isAir(blockPos2)) {
            return ActionResult.FAIL;
        } else {
            double d = blockPos2.getX();
            double e = blockPos2.getY();
            double f = blockPos2.getZ();
            List<Entity> list = world.getOtherEntities(null, new Box(d, e, f, d + 1.0, e + 2.0, f + 1.0));
            if (!list.isEmpty()) {
                return ActionResult.FAIL;
            } else {
                if (world instanceof ServerWorld) {
                    LatchEntity latchEntity = new LatchEntity(world, d + 0.5, e - 0.5, f + 0.5);
                    world.spawnEntity(latchEntity);
                    world.emitGameEvent(context.getPlayer(), GameEvent.ENTITY_PLACE, blockPos2);
                }
                context.getStack().decrement(1);
                return ActionResult.success(world.isClient);
            }
        }
    }
}