package net.Pandarix.betterarcheology.screen;

import net.Pandarix.betterarcheology.BetterArcheology;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.collection.DefaultedList;

public class FossilInventoryScreenHandler extends ScreenHandler
{
    private final Inventory inventory;
    private final PlayerInventory playerInventory;

    public FossilInventoryScreenHandler(int syncId, PlayerInventory inventory)
    {
        this(syncId, inventory, new SimpleInventory(1));
    }

    public FossilInventoryScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory)
    {
        super(ModScreenHandlers.FOSSIL_SCREEN_HANDLER, syncId);
        checkSize(inventory, 1);
        inventory.onOpen(playerInventory.player);
        this.inventory = inventory;
        this.playerInventory = playerInventory;

        this.addSlot(new Slot(inventory, 0, 80, 22)
        {
            @Override
            public void markDirty()
            {
                super.markDirty();
                FossilInventoryScreenHandler.this.onContentChanged(this.inventory);
            }
        });

        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);
    }

    @Override
    public void onClosed(PlayerEntity player)
    {
        super.onClosed(player);
        this.inventory.onClose(player);
    }

    @Override
    public void onContentChanged(Inventory inventory)
    {
        try
        {
            if (this.playerInventory.player instanceof ServerPlayerEntity serverPlayerEntity)
            {
                DefaultedList<ItemStack> contents = DefaultedList.ofSize(this.inventory.size());
                for (int slot = 0; slot < this.inventory.size(); slot++)
                {
                    contents.add(this.inventory.getStack(slot));
                }
                serverPlayerEntity.networkHandler.sendPacket(new InventoryS2CPacket(this.syncId, this.nextRevision(), contents, this.getCursorStack()));
                this.inventory.markDirty();
            }
        } catch (Exception e)
        {
            BetterArcheology.LOGGER.warn("Could not send Inventory update of Fossil Screen!", e);
        }
        super.onContentChanged(inventory);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int invSlot)
    {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(invSlot);
        if (slot != null && slot.hasStack())
        {
            ItemStack originalStack = slot.getStack();
            newStack = originalStack.copy();
            if (invSlot < this.inventory.size())
            {
                if (!this.insertItem(originalStack, this.inventory.size(), this.slots.size(), true))
                {
                    return ItemStack.EMPTY;
                }
            } else if (!this.insertItem(originalStack, 0, this.inventory.size(), false))
            {
                return ItemStack.EMPTY;
            }

            if (originalStack.isEmpty())
            {
                slot.setStack(ItemStack.EMPTY);
            } else
            {
                slot.markDirty();
            }
        }

        return newStack;
    }

    @Override
    public boolean canUse(PlayerEntity player)
    {
        return this.inventory.canPlayerUse(player);
    }

    //Helper Method to add Players Inventoryslots to Screen
    private void addPlayerInventory(PlayerInventory playerInventory)
    {
        /*Adding Slots of Main Inventory
                COL:    COL:    COL:    ...
        ROW:    slot    slot    slot
        ROW:    slot    slot    slot
        ...
        */

        //MainSize is the number of Slots besides the Armor and Offhand
        //HotbarSize is the number of Slots in the Hotbar, which incidentally is the number of slots per Column
        int inventorySize = PlayerInventory.MAIN_SIZE - PlayerInventory.getHotbarSize();    //Because Main includes the Hotbar Slots, we have to subtract them to get the raw Inventory size
        int inventoryRows = inventorySize / PlayerInventory.getHotbarSize();    //All Slots : Slots per Column = Number of Rows to draw
        int inventoryColumns = PlayerInventory.getHotbarSize();

        //For every Row in the Inventory
        for (int i = 0; i < inventoryRows; ++i)
        {
            //Add a slot for every Column in the Row
            for (int l = 0; l < inventoryColumns; ++l)
            {
                //Numbers are Minecrafts pre-defined offsets due to the textures
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 86 + i * 18));
            }
        }
    }

    //Helper Method to add Players HotBarSlots to Screen
    private void addPlayerHotbar(PlayerInventory playerInventory)
    {
        //Adds a new Slot to the Screen for every Slot in the Players Hotbar
        for (int i = 0; i < PlayerInventory.getHotbarSize(); ++i)
        {
            //Numbers are Minecrafts pre-defined offsets due to the textures
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 144));
        }
    }
}
