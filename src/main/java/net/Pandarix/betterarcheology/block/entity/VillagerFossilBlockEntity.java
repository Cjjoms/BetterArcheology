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
        int luminance = Block.getBlockFromItem(this.getInventoryContents().getItem()).getDefaultState().getLuminance();
        nbt.putInt("luminance", luminance);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup)
    {
        Inventories.readNbt(nbt, inventory, registryLookup);
        int luminance = nbt.getInt("luminance");
        if (this.world != null)
        {
            this.world.setBlockState(this.getPos(), world.getBlockState(this.getPos()).with(VillagerFossilBlock.INVENTORY_LUMINANCE, luminance));
        }
        super.readNbt(nbt, registryLookup);
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

    //update luminance of block based on the luminance of the item given when it would be in its placed state
    @Override
    public void onClose(PlayerEntity player)
    {
        ImplementedInventory.super.onClose(player);
        int luminance = Block.getBlockFromItem(this.getInventoryContents().getItem()).getDefaultState().getLuminance();
        player.getWorld().setBlockState(this.getPos(), world.getBlockState(this.getPos()).with(VillagerFossilBlock.INVENTORY_LUMINANCE, luminance));
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

    public void setInventory(DefaultedList<ItemStack> inventory)
    {
        for (int i = 0; i < inventory.size(); i++)
        {
            this.inventory.set(i, inventory.get(i));
        }
    }

/*    @Override
    public void markDirty()
    {
        assert world != null;
        world.updateListeners(pos, getCachedState(), getCachedState(), 3);
        if (!world.isClient())
        {
            PacketByteBuf data = PacketByteBufs.create();
            data.writeInt(inventory.size());
            for (ItemStack itemStack : inventory)
            {
                data.writeItemStack(itemStack);
            }
            data.writeBlockPos(getPos());

            for (ServerPlayerEntity player : PlayerLookup.tracking((ServerWorld) world, getPos()))
            {
                ServerPlayNetworking.send(player, ModMessages.ITEM_SYNC, data);
            }
        }

        super.markDirty();
    }*/

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket()
    {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup)
    {
        return createNbt(registryLookup);
    }
}
