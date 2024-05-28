package mathd.whipdashing.registries;

import mathd.whipdashing.Whipdashing;
import mathd.whipdashing.items.LatchItem;
import mathd.whipdashing.items.WhipdashItem;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.util.Rarity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

@SuppressWarnings("unused")
public class WhipItems {
    public static final Item WHIPDASH = registerItem("whipdash", new WhipdashItem(new FabricItemSettings().maxCount(1).rarity(Rarity.EPIC)), ItemGroups.getDefaultTab());
    public static final Item TRIPLATCH = registerItem("triplatch", new LatchItem(false, new FabricItemSettings().rarity(Rarity.UNCOMMON)), ItemGroups.getDefaultTab());
    public static final Item SLIPLATCH = registerItem("sliplatch", new LatchItem(true, new FabricItemSettings().rarity(Rarity.UNCOMMON)), ItemGroups.getDefaultTab());

    public static void init() {}

    private static Item registerItem(String name, Item item, ItemGroup tab) {
        Item obj = Registry.register(Registries.ITEM, Whipdashing.id(name), item);
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> entries.add(obj));
        return obj;
    }
}