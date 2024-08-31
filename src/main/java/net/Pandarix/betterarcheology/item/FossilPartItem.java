package net.Pandarix.betterarcheology.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;

import java.util.List;

public class FossilPartItem extends Item
{

    public FossilPartItem(Settings settings)
    {
        super(settings);
    }

    @Override
    public boolean isEnchantable(ItemStack stack)
    {
        return false;
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type)
    {
        tooltip.add(Text.translatable(this.getTranslationKey() + "_tooltip"));
        super.appendTooltip(stack, context, tooltip, type);
    }
}
