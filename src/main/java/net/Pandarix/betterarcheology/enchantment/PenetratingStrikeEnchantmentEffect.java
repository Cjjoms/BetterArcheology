package net.Pandarix.betterarcheology.enchantment;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.Pandarix.betterarcheology.BetterArcheology;
import net.Pandarix.betterarcheology.BetterArcheologyConfig;
import net.minecraft.enchantment.EnchantmentEffectContext;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelBasedValue;
import net.minecraft.enchantment.effect.EnchantmentEntityEffect;
import net.minecraft.entity.DamageUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;

public record PenetratingStrikeEnchantmentEffect(EnchantmentLevelBasedValue amount) implements EnchantmentEntityEffect
{
    public static final MapCodec<PenetratingStrikeEnchantmentEffect> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    EnchantmentLevelBasedValue.CODEC.fieldOf("amount").forGetter(PenetratingStrikeEnchantmentEffect::amount)
            ).apply(instance, PenetratingStrikeEnchantmentEffect::new));

    @Override
    public void apply(ServerWorld world, int level, EnchantmentEffectContext context, Entity user, Vec3d pos, boolean newlyApplied)
    {
        apply(world, level, context, user, pos);
    }

    @Override
    public void apply(ServerWorld world, int level, EnchantmentEffectContext context, Entity target, Vec3d pos)
    {
        if (!BetterArcheologyConfig.artifactsEnabled.get() || !BetterArcheologyConfig.penetratingStrikeEnabled.get())
        {
            return;
        }

        if (target instanceof LivingEntity livingEntity && livingEntity.getRecentDamageSource() != null)
        {
            float enchantmentProtectionFactor = EnchantmentHelper.getProtectionAmount(world, livingEntity, livingEntity.getRecentDamageSource());

            //damagevalue of the current weapon
            float damageInflicted = 0;

            //TODO: das hier debuggen, wenn sich das game starten lässt
            BetterArcheology.LOGGER.info("test: " + context.stack().getItem().getAttributeModifiers().modifiers());

            float totalProtectedDamage = DamageUtil.getInflictedDamage(damageInflicted, enchantmentProtectionFactor);

            if (context.owner() instanceof PlayerEntity player)
            {
                livingEntity.damage(player.getDamageSources().magic(), (float) (totalProtectedDamage * BetterArcheologyConfig.penetratingStrikeIgnorance.get()));
            }

            world.playSound(null, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), SoundEvents.ITEM_ARMOR_EQUIP_CHAIN, SoundCategory.BLOCKS, 1f, .7f, world.getRandom().nextLong());
        }
    }

    @Override
    public MapCodec<? extends EnchantmentEntityEffect> getCodec()
    {
        return CODEC;
    }
}

