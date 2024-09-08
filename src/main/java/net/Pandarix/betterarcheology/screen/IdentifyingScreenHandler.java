package net.Pandarix.betterarcheology.screen;

import net.Pandarix.betterarcheology.BetterArcheology;
import net.Pandarix.betterarcheology.block.entity.ArcheologyTableBlockEntity;
import net.Pandarix.betterarcheology.item.ModItems;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.BrushItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.collection.DefaultedList;

public class IdentifyingScreenHandler extends ScreenHandler
{
    private final Inventory inventory;
    private final PropertyDelegate propertyDelegate;
    private final PlayerInventory playerInventory;

    public IdentifyingScreenHandler(int syncId, PlayerInventory inventory)
    {
        //size of SimpleInventory has to be same as in Defaulted List of ArcheologyTableBlockEntity;
        //size of ArrayPropertyDelegate has to be the same as the PropertyDelegate of ArcheologyTableBlockEntity (number of ints being tracked)
        this(syncId, inventory, new SimpleInventory(ArcheologyTableBlockEntity.INV_SIZE), new ArrayPropertyDelegate(ArcheologyTableBlockEntity.PROPERTY_DELEGATES));
    }

    public IdentifyingScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory, PropertyDelegate delegate)
    {
        super(ModScreenHandlers.IDENTIFYING_SCREEN_HANDLER, syncId);    //creates a new Instance of Screenhandler
        checkSize(inventory, ArcheologyTableBlockEntity.INV_SIZE);

        this.inventory = inventory; //sets the Screens Inventory to the given Inventory
        this.playerInventory = playerInventory;

        //opens Inventory
        inventory.onOpen(playerInventory.player);

        //defines delegate field
        this.propertyDelegate = delegate;

        //SLOTS
        this.addSlot(new Slot(inventory, 0, 80, 20));
        this.addSlot(new Slot(inventory, 1, 26, 48));
        this.addSlot(new IdentifyingOutputSlot(inventory, 2, 134, 48));

        //Bottom screen components to render current players inventory & hotbar
        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);
        addProperties(delegate);
    }

    public boolean isCrafting()
    {
        return propertyDelegate.get(0) > 0;
    }

    public int getScaledProgress()
    {
        int progress = this.propertyDelegate.get(0);
        int maxProgress = this.propertyDelegate.get(1);                         // Maximum Progress, after reaching: progress done
        int progressArrowSize = 74;                                             // This is the width in pixels of your arrow

        return maxProgress != 0 && progress != 0 ? progress * progressArrowSize / maxProgress : 0;
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot)
    {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot2 = (Slot) this.slots.get(slot);
        if (slot2 != null && slot2.hasStack())
        {
            ItemStack itemStack2 = slot2.getStack();
            itemStack = itemStack2.copy();
            if (slot == 2)
            {
                if (!this.insertItem(itemStack2, 3, this.slots.size() - 1, true))
                {
                    return ItemStack.EMPTY;
                }

                slot2.onQuickTransfer(itemStack2, itemStack);
            } else if (slot != 1 && slot != 0)
            {
                if (itemStack2.getItem() instanceof BrushItem)
                {
                    if (!this.insertItem(itemStack2, 0, 1, false))
                    {
                        return ItemStack.EMPTY;
                    }
                } else if (itemStack2.isOf(ModItems.UNIDENTIFIED_ARTIFACT))
                {
                    if (!this.insertItem(itemStack2, 1, 2, false))
                    {
                        return ItemStack.EMPTY;
                    }
                }
            } else if (!this.insertItem(itemStack2, 3, this.slots.size() - 1, false))
            {
                return ItemStack.EMPTY;
            }

            if (itemStack2.isEmpty())
            {
                slot2.setStack(ItemStack.EMPTY);
            } else
            {
                slot2.markDirty();
            }

            if (itemStack2.getCount() == itemStack.getCount())
            {
                return ItemStack.EMPTY;
            }

            slot2.onTakeItem(player, itemStack2);
        }

        return itemStack;
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
            BetterArcheology.LOGGER.warn("Could not send Inventory update of Identifying Screen!", e);
        }
        super.onContentChanged(inventory);
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
