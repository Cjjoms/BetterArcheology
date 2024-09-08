package net.Pandarix.betterarcheology.block.entity;

import net.Pandarix.betterarcheology.block.custom.VillagerFossilBlock;
import net.Pandarix.betterarcheology.screen.FossilInventoryScreenHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class VillagerFossilBlockEntity extends BlockEntity implements NamedScreenHandlerFactory, ImplementedInventory
{
    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(1, ItemStack.EMPTY);

    public VillagerFossilBlockEntity(BlockPos pos, BlockState state)
    {
        super(ModBlockEntities.VILLAGER_FOSSIL, pos, state);
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup)
    {
        super.writeNbt(nbt, registryLookup);
        Inventories.writeNbt(nbt, inventory, registryLookup);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup)
    {
        super.readNbt(nbt, registryLookup);
        inventory.clear();
        Inventories.readNbt(nbt, inventory, registryLookup);
        markDirty();
    }

    @Override
    public DefaultedList<ItemStack> getItems()
    {
        return this.inventory;
    }

    @Override
    public Text getDisplayName()
    {
        return Text.translatable(getCachedState().getBlock().getTranslationKey());
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player)
    {
        return new FossilInventoryScreenHandler(syncId, inv, this);
    }

    public ItemStack getInventoryContents()
    {
        return this.getStack(0);
    }

    @Override
    public void markDirty()
    {
        if (this.world != null)
        {
            world.updateListeners(pos, getCachedState(), getCachedState(), 3);
            if (!world.isClient())
            {
                int luminance = Block.getBlockFromItem(this.getInventoryContents().getItem()).getDefaultState().getLuminance();
                world.setBlockState(this.getPos(), getCachedState().with(VillagerFossilBlock.INVENTORY_LUMINANCE, luminance));
            }
        }
        super.markDirty();
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket()
    {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup)
    {
        NbtCompound nbt = super.toInitialChunkDataNbt(registryLookup);
        writeNbt(nbt, registryLookup);
        return nbt;
    }
}
