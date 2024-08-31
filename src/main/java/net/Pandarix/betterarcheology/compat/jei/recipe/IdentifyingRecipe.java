package net.Pandarix.betterarcheology.compat.jei.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.Pandarix.betterarcheology.BetterArcheology;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class IdentifyingRecipe implements Recipe<CraftingRecipeInput>
{
    private final Ingredient input;
    private final ItemStack result;
    private static final int POSSIBLE_RESULT_COUNT = Registries.ENCHANTMENT.streamEntries().filter(reference -> reference.registryKey().getValue().getNamespace().equals(BetterArcheology.MOD_ID)).toList().size();

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
        return this.getResult();
    }

    @Override
    public boolean fits(int width, int height)
    {
        return true;
    }

    @Override
    public ItemStack getResult(RegistryWrapper.WrapperLookup registriesLookup)
    {
        return this.getResult();
    }

    /**
     * Extra method instead of {@link #getResult} for use without unnecessary parameter
     *
     * @return ItemStack to be crafted when done
     */
    public ItemStack getResult()
    {
        //Adding the Enchantment Tags
        ItemStack modifiedResultBook = result.copy();

        //Adding the Custom Name Tags
        NbtCompound nameModification = new NbtCompound();
        nameModification.putString("Name", "{\"translate\":\"item.betterarcheology.identified_artifact\"}");

        //Adding Chance as Lore Tag
        NbtList lore = new NbtList();
        lore.add(NbtString.of(String.format("{\"text\":\"Chance: 1/%d\",\"color\":\"aqua\"}", POSSIBLE_RESULT_COUNT)));
        nameModification.put("Lore", lore);

        //output the book with the modifications
        modifiedResultBook.setSubNbt("display", nameModification);
        return modifiedResultBook;
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