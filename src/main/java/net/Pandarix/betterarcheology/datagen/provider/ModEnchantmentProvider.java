package net.Pandarix.betterarcheology.datagen.provider;

import net.Pandarix.betterarcheology.enchantment.ModEnchantments;
import net.Pandarix.betterarcheology.enchantment.PenetratingStrikeEnchantmentEffect;
import net.Pandarix.betterarcheology.util.ModTags;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentLevelBasedValue;
import net.minecraft.enchantment.effect.EnchantmentEffectTarget;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.ItemTags;

import java.util.concurrent.CompletableFuture;

public class ModEnchantmentProvider extends FabricDynamicRegistryProvider
{
    public ModEnchantmentProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture)
    {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup registries, Entries entries)
    {
        RegistryWrapper<Item> itemLookup = registries.getWrapperOrThrow(RegistryKeys.ITEM);

        register(entries, ModEnchantments.PENETRATING_STRIKE_KEY, Enchantment.builder(
                        Enchantment.definition(
                                itemLookup.getOrThrow(ItemTags.WEAPON_ENCHANTABLE),
                                2, // probability of showing up in the enchantment table - will be ignored due to it being treasure
                                1, // max level
                                Enchantment.leveledCost(1, 10), // cost per level (base)
                                Enchantment.leveledCost(1, 15), // cost per level (max)
                                7, // anvil applying cost
                                AttributeModifierSlot.HAND
                        ))
                .addEffect(EnchantmentEffectComponentTypes.POST_ATTACK,
                        EnchantmentEffectTarget.ATTACKER,
                        EnchantmentEffectTarget.VICTIM,
                        new PenetratingStrikeEnchantmentEffect(EnchantmentLevelBasedValue.constant(1))));

        register(entries, ModEnchantments.TUNNELING_KEY, Enchantment.builder(
                Enchantment.definition(
                        itemLookup.getOrThrow(ModTags.Items.TUNNELING_ITEMS),
                        2, // probability of showing up in the enchantment table - will be ignored due to it being treasure
                        1, // max level
                        Enchantment.leveledCost(1, 10), // cost per level (base)
                        Enchantment.leveledCost(1, 15), // cost per level (max)
                        7, // anvil applying cost
                        AttributeModifierSlot.HAND
                )));

        register(entries, ModEnchantments.SOARING_WINDS_KEY, Enchantment.builder(
                Enchantment.definition(
                        itemLookup.getOrThrow(ModTags.Items.ELYTRAS),
                        2, // probability of showing up in the enchantment table - will be ignored due to it being treasure
                        1, // max level
                        Enchantment.leveledCost(1, 10), // cost per level (base)
                        Enchantment.leveledCost(1, 15), // cost per level (max)
                        7, // anvil applying cost
                        AttributeModifierSlot.ANY
                )));
    }

    private static void register(Entries entries, RegistryKey<Enchantment> key, Enchantment.Builder builder, ResourceCondition... resourceConditions)
    {
        entries.add(key, builder.build(key.getValue()), resourceConditions);
    }

    @Override
    public String getName()
    {
        return "Enchantment Generator";
    }
}
