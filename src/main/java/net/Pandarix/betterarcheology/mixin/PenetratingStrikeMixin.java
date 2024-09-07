package net.Pandarix.betterarcheology.mixin;

import net.Pandarix.betterarcheology.BetterArcheology;
import net.Pandarix.betterarcheology.BetterArcheologyConfig;
import net.Pandarix.betterarcheology.enchantment.ModEnchantments;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentHelper.class)
public class PenetratingStrikeMixin
{
    @Inject(method = "getProtectionAmount", at = @At("RETURN"), cancellable = true)
    private static void injectMethod(ServerWorld world, LivingEntity user, DamageSource damageSource, CallbackInfoReturnable<Float> cir)
    {
        if (BetterArcheologyConfig.artifactsEnabled.get() && BetterArcheologyConfig.penetratingStrikeEnabled.get())
        {
            try
            {
                RegistryEntry.Reference<Enchantment> ba$penetratingStrike = user.getWorld().getRegistryManager().getWrapperOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(ModEnchantments.PENETRATING_STRIKE_KEY);
                if (damageSource.getWeaponStack() != null && EnchantmentHelper.getLevel(ba$penetratingStrike, damageSource.getWeaponStack()) >= 1)
                {
                    BetterArcheology.LOGGER.info("Protection before: " + cir.getReturnValue());

                    float dmgWithReducedProt = cir.getReturnValue() * (float) (1 - BetterArcheologyConfig.penetratingStrikeIgnorance.get());
                    BetterArcheology.LOGGER.info("Protection after: " + dmgWithReducedProt);
                    cir.setReturnValue(Math.max(0, dmgWithReducedProt));
                }
            } catch (Exception e)
            {
                BetterArcheology.LOGGER.error("Could not test for Penetrating Strike because EnchantmentEntry could not be found", e);
            }
        }
        cir.setReturnValue(cir.getReturnValue());
    }
}
