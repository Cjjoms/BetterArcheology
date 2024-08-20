package net.Pandarix.betterarcheology.item;

import net.Pandarix.betterarcheology.BetterArcheology;
import net.Pandarix.betterarcheology.block.custom.FossilBaseBlock;
import net.Pandarix.betterarcheology.block.custom.FossilBaseBodyBlock;
import net.Pandarix.betterarcheology.block.custom.FossilBaseHeadBlock;
import net.Pandarix.betterarcheology.block.custom.FossilBaseWithEntityBlock;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

public class ModItems
{

    //ITEM ENTRIES-------------------------------------------------------------------------//
    //BRUSHES
    public static final Item IRON_BRUSH = registerItem("iron_brush", (Item) (new BetterBrushItem((new Item.Settings()).maxDamage(128), 8)), ModItemGroup.BETTER_ARCHEOLOGY_ITEMGROUP);
    public static final Item DIAMOND_BRUSH = registerItem("diamond_brush", (Item) (new BetterBrushItem((new Item.Settings()).maxDamage(512), 6)), ModItemGroup.BETTER_ARCHEOLOGY_ITEMGROUP);
    public static final Item NETHERITE_BRUSH = registerItem("netherite_brush", (Item) (new BetterBrushItem((new Item.Settings()).maxDamage(1024), 4)), ModItemGroup.BETTER_ARCHEOLOGY_ITEMGROUP);

    public static final Item ARTIFACT_SHARDS = registerItem("artifact_shards", new Item(new FabricItemSettings().rarity(Rarity.UNCOMMON)), ModItemGroup.BETTER_ARCHEOLOGY_ITEMGROUP);

    public static final Item UNIDENTIFIED_ARTIFACT = registerItem("unidentified_artifact", new Item(new FabricItemSettings().rarity(Rarity.UNCOMMON)), ModItemGroup.BETTER_ARCHEOLOGY_ITEMGROUP);

    //LOOT ITEMS
    public static final Item BOMB_ITEM = registerItem("bomb", new BombItem(new FabricItemSettings().rarity(Rarity.COMMON).maxCount(16)), ModItemGroup.BETTER_ARCHEOLOGY_ITEMGROUP);
    public static final Item TORRENT_TOTEM = registerItem("torrent_totem", new TorrentTotemItem(new FabricItemSettings().rarity(Rarity.UNCOMMON).maxCount(1).maxDamage(32)), ModItemGroup.BETTER_ARCHEOLOGY_ITEMGROUP);

    public static final Item SOUL_TOTEM = registerItem("soul_totem", new SoulTotemItem(new FabricItemSettings().rarity(Rarity.UNCOMMON).maxDamage(32)), ModItemGroup.BETTER_ARCHEOLOGY_ITEMGROUP);

    //REGISTERING--------------------------------------------------------------------------//

    /**
     * Registers given Item into Registry with BetterArcheology identifier
     *
     * @param name Item registry name entry
     * @param item Instance of Item
     * @return Registry entry of given Item
     */
    public static Item registerItem(String name, Item item, RegistryKey<ItemGroup> group)
    {
        Item registeredItem = Registry.register(Registries.ITEM, new Identifier(BetterArcheology.MOD_ID, name), item);

        ItemGroupEvents.modifyEntriesEvent(group).register(entries -> entries.add(registeredItem));
        return registeredItem;
    }

    public static Item registerItemWithoutTab(String name, Item item)
    {
        return Registry.register(Registries.ITEM, new Identifier(BetterArcheology.MOD_ID, name), item);
    }

    //LOGGER-----------------------------------------------------------------------------//
    public static void registerModItems()
    {
        //status message
        BetterArcheology.LOGGER.info("Registering Items from " + BetterArcheology.MOD_ID);
    }

    public static boolean isFossil(Block block)
    {
        return block instanceof FossilBaseBodyBlock
                || block instanceof FossilBaseWithEntityBlock
                || block instanceof FossilBaseHeadBlock
                || block instanceof FossilBaseBlock;
    }
}
