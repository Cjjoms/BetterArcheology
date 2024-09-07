package net.Pandarix.betterarcheology.util;

import dev.emi.trinkets.api.SlotGroup;
import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketsApi;
import net.Pandarix.betterarcheology.BetterArcheology;
import net.Pandarix.betterarcheology.enchantment.ModEnchantments;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ElytraItem;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class ArtifactEnchantmentHelper
{
    public static boolean hasSoaringWinds(PlayerEntity player)
    {
        if (player == null)
        {
            return false;
        }

        try
        {
            RegistryEntry.Reference<Enchantment> soaringWinds = player.getWorld().getRegistryManager().getWrapperOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(ModEnchantments.SOARING_WINDS_KEY);

            //  if the Item is an Elytra and has Soaring winds, return true
            if (player.getEquippedStack(EquipmentSlot.CHEST).getItem() instanceof ElytraItem
                    && EnchantmentHelper.getLevel(soaringWinds, player.getEquippedStack(EquipmentSlot.CHEST)) >= 1)
            {
                return true;
            }

            // If trinkets is installed, check for back-slot
            if (FabricLoader.getInstance().isModLoaded("trinkets") && FabricLoader.getInstance().isModLoaded("elytraslot"))
            {
                //failsafe
                Map<String, SlotGroup> trinketSlots = TrinketsApi.getPlayerSlots(player);
                if (trinketSlots != null && !trinketSlots.isEmpty() && trinketSlots.containsKey("chest"))
                {
                    // if there is a cape-slot
                    if (trinketSlots.get("chest").getSlots().containsKey("cape"))
                    {
                        // if the player has trinkets Data
                        Optional<TrinketComponent> trinketData = TrinketsApi.getTrinketComponent(player);
                        if (trinketData.isPresent())
                        {
                            // check for a trinkets slot named "cape" with an ElytraItem with Soaring winds on it
                            return trinketData.get().getAllEquipped().stream().anyMatch((pair) ->
                                    Objects.equals(pair.getLeft().inventory().getSlotType().getName(), "cape")
                                            && pair.getRight().getItem() instanceof ElytraItem
                                            && EnchantmentHelper.getLevel(soaringWinds, pair.getRight()) >= 1
                            );
                        }
                    }
                }
            }
        } catch (Exception e)
        {
            BetterArcheology.LOGGER.error("Could not find Soaring Winds in the Enchantment Registries!", e);
        }
        // if nothing succeeded, false
        return false;
    }
}