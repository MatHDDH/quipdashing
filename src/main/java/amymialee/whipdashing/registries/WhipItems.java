package amymialee.whipdashing.registries;

import amymialee.whipdashing.Whipdashing;
import amymialee.whipdashing.items.LatchItem;
import amymialee.whipdashing.items.WhipdashItem;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Rarity;
import net.minecraft.util.registry.Registry;

@SuppressWarnings("unused")
public class WhipItems {
    public static final Item WHIPDASH = registerItem("whipdash", new WhipdashItem(new FabricItemSettings().maxCount(1).rarity(Rarity.EPIC).group(ItemGroup.TOOLS)));
    public static final Item LATCH = registerItem("latch", new LatchItem(new FabricItemSettings().rarity(Rarity.UNCOMMON).group(ItemGroup.TOOLS)));

    public static void init() {}

    private static Item registerItem(String name, Item item) {
        return Registry.register(Registry.ITEM, Whipdashing.id(name), item);
    }
}