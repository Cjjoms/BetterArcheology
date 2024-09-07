package net.Pandarix.betterarcheology.compat.jei.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.Pandarix.betterarcheology.BetterArcheology;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class IdentifyingRecipe implements Recipe<CraftingRecipeInput>
{
    private final Ingredient input;
    private final ItemStack result;
    private static int POSSIBLE_RESULT_COUNT = 0;

    public IdentifyingRecipe(Ingredient inputItems, ItemStack result)
    {
        this.input = inputItems;
        this.result = result;
    }

    @Override
    public boolean matches(CraftingRecipeInput input, World world)
    {
        if (world.isClient())
        {
            return false;
        }

        return this.input.test(input.getStackInSlot(0));
    }

    @Override
    public boolean isIgnoredInRecipeBook()
    {
        return true;
    }

    @Override
    @NotNull
    public DefaultedList<Ingredient> getIngredients()
    {
        return DefaultedList.copyOf(Ingredient.EMPTY, input);
    }

    @Override
    public ItemStack craft(CraftingRecipeInput input, RegistryWrapper.WrapperLookup lookup)
    {
        return this.getResult(lookup);
    }

    @Override
    public boolean fits(int width, int height)
    {
        return true;
    }

    public ItemStack getResult(int amountOfEnchantsPossible)
    {
        //Adding the Enchantment Tags
        ItemStack modifiedResultBook = result.copy();
        //apply custom naming to the book
        modifiedResultBook.set(DataComponentTypes.ITEM_NAME, Text.translatable("item.betterarcheology.identified_artifact"));
        modifiedResultBook.set(DataComponentTypes.LORE, new LoreComponent(
                List.of(Text.literal(String.format("Chance: 1/%d", amountOfEnchantsPossible)).withColor(Formatting.AQUA.getColorValue()))
        ));
        return modifiedResultBook;
    }

    @Override
    public ItemStack getResult(RegistryWrapper.WrapperLookup registriesLookup)
    {
        if (POSSIBLE_RESULT_COUNT == 0)
        {
            POSSIBLE_RESULT_COUNT = registriesLookup.getWrapperOrThrow(RegistryKeys.ENCHANTMENT).streamEntries().filter(reference -> reference.registryKey().getValue().getNamespace().equals(BetterArcheology.MOD_ID)).toList().size();
        }
        return getResult(POSSIBLE_RESULT_COUNT);
    }

    @Override
    @NotNull
    public RecipeSerializer<?> getSerializer()
    {
        return Serializer.INSTANCE;
    }

    @Override
    @NotNull
    public RecipeType<?> getType()
    {
        return Type.INSTANCE;
    }

    public static class Type implements RecipeType<IdentifyingRecipe>
    {
        public static final Type INSTANCE = new Type();
    }

    public static class Serializer implements RecipeSerializer<IdentifyingRecipe>
    {
        private static final MapCodec<IdentifyingRecipe> CODEC = RecordCodecBuilder.mapCodec(
                (builder) -> builder.group(
                        Ingredient.DISALLOW_EMPTY_CODEC.fieldOf("input").forGetter((IdentifyingRecipe recipe) -> recipe.input),
                        ItemStack.CODEC.fieldOf("result").forGetter((IdentifyingRecipe recipe) -> recipe.result)
                ).apply(builder, IdentifyingRecipe::new));

        public static final PacketCodec<RegistryByteBuf, IdentifyingRecipe> PACKET_CODEC = PacketCodec.ofStatic(
                IdentifyingRecipe.Serializer::write, IdentifyingRecipe.Serializer::read
        );

        public static final Serializer INSTANCE = new Serializer();

        @Override
        public MapCodec<IdentifyingRecipe> codec()
        {
            return CODEC;
        }

        @Override
        public PacketCodec<RegistryByteBuf, IdentifyingRecipe> packetCodec()
        {
            return PACKET_CODEC;
        }

        public static void write(RegistryByteBuf packetByteBuf, IdentifyingRecipe recipe)
        {
            Ingredient.PACKET_CODEC.encode(packetByteBuf, recipe.input);
            ItemStack.PACKET_CODEC.encode(packetByteBuf, recipe.result);
        }

        public static IdentifyingRecipe read(RegistryByteBuf packetByteBuf)
        {
            Ingredient input = Ingredient.PACKET_CODEC.decode(packetByteBuf);
            ItemStack result = ItemStack.PACKET_CODEC.decode(packetByteBuf);
            return new IdentifyingRecipe(input, result);
        }
    }
}