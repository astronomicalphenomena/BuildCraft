/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.lib.gui.slots;

import buildcraft.builders.ItemBlueprint;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class SlotInput extends SlotBase {

	public SlotInput(IInventory iinventory, int slotIndex, int posX, int posY) {
		super(iinventory, slotIndex, posX, posY);
	}
	@Override
	public boolean isItemValid(ItemStack itemstack)
	{
		if(itemstack.getItem() instanceof ItemBlueprint)
		{
			if(itemstack.getTagCompound().hasKey("used"))
			{
				return false;
			}
			else
			{
				itemstack.getTagCompound().setBoolean("used",true);
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean canTakeStack(EntityPlayer par1EntityPlayer) {
		return false;
	}
}
