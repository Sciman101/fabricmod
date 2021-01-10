package info.sciman.skilltable.screen;

import info.sciman.skilltable.SkillTableMod;
import info.sciman.skilltable.skills.Skill;
import info.sciman.skilltable.skills.Skills;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;

import java.util.List;

public class SkillScreenHandler extends ScreenHandler {
    private final Inventory inventory;

    private final PlayerEntity player;
    private final ScreenHandlerContext context;

    public List<Skill> playerQualifiedSkills;

    public SkillScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, ScreenHandlerContext.EMPTY);
    }

    public SkillScreenHandler(int id, PlayerInventory playerInventory, ScreenHandlerContext screenHandlerContext) {
        super(SkillTableMod.SKILL_TABLE_SCREEN_HANDLER,id);

        this.inventory = new SimpleInventory(2) {};
        // Add inventory slots
        this.addSlot(new Slot(this.inventory, 0, 15, 25) { // Extra slot
            public boolean canInsert(ItemStack stack) {
                return true;
            }
            public int getMaxStackAmount() {
                return 1;
            }
        });
        this.addSlot(new Slot(this.inventory, 1, 15, 46) { // Lapis slot
            public boolean canInsert(ItemStack stack) {
                return stack.getItem() == Items.LAPIS_LAZULI;
            }
        });

        // Add player inventory slots
        int k;
        for(k = 0; k < 3; ++k) {
            for(int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + k * 9 + 9, 8 + j * 18, 84 + k * 18));
            }
        }
        for(k = 0; k < 9; ++k) {
            this.addSlot(new Slot(playerInventory, k, 8 + k * 18, 142));
        }

        this.player = playerInventory.player;
        this.context = screenHandlerContext;

        // Find qualified skills
        findQualifiedSkills();
    }

    private void findQualifiedSkills() {
        playerQualifiedSkills = Skills.getQualifiedSkills(this.player);
    }

    public void onContentChanged(Inventory inventory) {
        if (inventory == this.inventory) {
        }
    }

    // Drop our inventory when screen is closed
    public void close(PlayerEntity player) {
        super.close(player);
        this.context.run((world, blockPos) -> {
            this.dropInventory(player, player.world, this.inventory);
        });
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return canUse(this.context, player, SkillTableMod.BLOCK_SKILL_TABLE);
    }

    public ItemStack transferSlot(PlayerEntity player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = (Slot)this.slots.get(index);
        if (slot != null && slot.hasStack()) {
            ItemStack itemStack2 = slot.getStack();
            itemStack = itemStack2.copy();
            if (index == 0) {
                if (!this.insertItem(itemStack2, 2, 38, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (index == 1) {
                if (!this.insertItem(itemStack2, 2, 38, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (itemStack2.getItem() == Items.LAPIS_LAZULI) {
                if (!this.insertItem(itemStack2, 1, 2, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (((Slot)this.slots.get(0)).hasStack() || !((Slot)this.slots.get(0)).canInsert(itemStack2)) {
                    return ItemStack.EMPTY;
                }
                ItemStack itemStack3 = itemStack2.copy();
                itemStack3.setCount(1);
                itemStack2.decrement(1);
                ((Slot)this.slots.get(0)).setStack(itemStack3);
            }

            if (itemStack2.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }

            if (itemStack2.getCount() == itemStack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTakeItem(player, itemStack2);
        }

        return itemStack;
    }
}
