package net.Pandarix.betterarcheology.enchantment;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.Pandarix.betterarcheology.BetterArcheologyConfig;
import net.minecraft.enchantment.EnchantmentEffectContext;
import net.minecraft.enchantment.EnchantmentLevelBasedValue;
import net.minecraft.enchantment.effect.EnchantmentEntityEffect;
import net.minecraft.entity.Entity;
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

    /**
     * Only displays audio for the Enchantment.
     * The actual effects were moved to {@link net.Pandarix.betterarcheology.mixin.PenetratingStrikeMixin}.
     *
     * @param world   ServerWorld the affected entities are in
     * @param level   int the level of the Enchantment
     * @param context EnchantmentEffectContext of the Enchantment containing additional info
     * @param target  target Entity that was hit
     * @param pos     Vec3d of the target
     */
    @Override
    public void apply(ServerWorld world, int level, EnchantmentEffectContext context, Entity target, Vec3d pos)
    {
        if (!BetterArcheologyConfig.artifactsEnabled.get() || !BetterArcheologyConfig.penetratingStrikeEnabled.get())
        {
            return;
        }

        world.playSound(null, pos.x, pos.y, pos.z, SoundEvents.ITEM_ARMOR_EQUIP_CHAIN, SoundCategory.BLOCKS, 1f, .7f, world.getRandom().nextLong());
    }

    @Override
    public MapCodec<? extends EnchantmentEntityEffect> getCodec()
    {
        return CODEC;
    }
}

