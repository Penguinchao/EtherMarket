package tech.relativelyobjective.ethermarket;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Inventory {
	private EtherMarket main;
	public Inventory(EtherMarket etherMarket){
		main = etherMarket;
	}
	public void softAddItem(ItemStack item, Player player){ //Adds an item to the player's inventory; if it cannot fit, it is dropped to the floor to prevent it from being lost
		//Credit: https://bukkit.org/threads/check-if-there-is-enough-space-in-inventory.134923/
		//This command is used even when the items should fit, because it accounts for potential human error where an item may
		//not fit in the inventory
		HashMap<Integer,ItemStack> excess = player.getInventory().addItem(item);
		for( Map.Entry<Integer, ItemStack> me : excess.entrySet() ){
			player.getWorld().dropItem(player.getLocation(), me.getValue() );
		}
	}
	public Boolean canAddItem(ItemStack item, Player player){ //Checks to see if there is room to add an item to a player's inventory
		//Credit: https://bukkit.org/threads/check-if-there-is-enough-space-in-inventory.134923/
		int freeSpace = 0;
		for(ItemStack i : player.getInventory() ){
			if(i == null){
				freeSpace += item.getType().getMaxStackSize();
			} else if (i.getType() == item.getType() ){
				freeSpace += (i.getType().getMaxStackSize() - i.getAmount());
			}
		}
		main.messages.debugOut("Item has: "+item.getAmount()+" and freeSpace is: "+freeSpace);
		if(item.getAmount() > freeSpace){
			main.messages.debugOut("There is not enough free space in the inventory");
			return false;
		}else{
			main.messages.debugOut("There is enough free space in the inventory");
			return true;
		}
	}
	public int getInventoryItemCount(Player player, ItemStack shopItem){ //Returns how many of a specific item are in the player's inventory
		main.messages.debugOut("Getting player Inventory and comparing with the shop item");
		ItemStack[] inventory = player.getInventory().getContents();
		int returnMe = 0;
		int invSlot = 1;
		for(ItemStack item:inventory){
			main.messages.debugOut("Inventory slot: "+invSlot);
			invSlot++;
			if(areEqualItems(item, shopItem)){
				main.messages.debugOut("Items match -- getting stack size");
				returnMe += item.getAmount();
			}else {
				main.messages.debugOut("Not the same item");
			}
		}
		return returnMe;
	}
	public void removePlayerItem(Player player, ItemStack item, int amountToRemove){ //Removes the selected amount of the item from the players inventory
		int amountLeft = amountToRemove;
		main.messages.debugOut("Searching for "+amountLeft+" items");
		ItemStack[] inventory = player.getInventory().getContents();
		int invSlot = 1;
		for(ItemStack currentItem:inventory){
			if(amountLeft > 0){
				main.messages.debugOut("Inventory slot: "+invSlot);
				main.messages.debugOut("Amount remaining:"+amountLeft);
				invSlot++;
				if(areEqualItems(currentItem, item)){
					main.messages.debugOut("Items match -- getting stack size");
					main.messages.debugOut("Stack size:"+currentItem.getAmount());
					int stackSize = currentItem.getAmount();
					if(stackSize > amountLeft){
						main.messages.debugOut("There are more items in this stack than needed");
						currentItem.setAmount(stackSize-amountLeft);
						amountLeft = 0;
					}else {
						main.messages.debugOut("This stack does not have enough to deposit the item -- deducting amount");
						player.getInventory().removeItem(currentItem);
						main.messages.debugOut("removingItemAmount: "+currentItem.getAmount() );
						amountLeft -= currentItem.getAmount();
					}
					
				}else {
					main.messages.debugOut("Not the same item");
				}
			}else {
				main.messages.debugOut("Amount left is 0; breaking loop");
				break;
			}
		}
	}
	public Boolean areEqualItems(ItemStack playerItem, ItemStack shopItem){ //Checks to see if the two items are equivalent
		main.messages.debugOut("Comparing two item stacks");
		if(playerItem == null || shopItem == null){
			main.messages.debugOut("At least one item is null -- returning false");
			return false;
		}else {
			main.messages.debugOut("Items are not null. Continuing...");
		}
		main.messages.debugOut("Getting material: playerItem");
		Material playerItemMaterial = playerItem.getType();
		Material shopItemMaterial = shopItem.getType();
		if(playerItemMaterial.equals(shopItemMaterial) ){
			main.messages.debugOut("Item types are the same");
			if(playerItem.getItemMeta().getDisplayName() == shopItem.getItemMeta().getDisplayName() ){
				main.messages.debugOut("Display names are the same");
				String shopEnchants = main.enchantment.enchantmentsToString(shopItem);
				if(main.enchantment.compareStringEnchantments(playerItem, shopEnchants)){
					main.messages.debugOut("Enchantments for the two items are the same");
					if(playerItem.getData().toString().equals( shopItem.getData().toString() ) ){
						main.messages.debugOut("data matches -- item matches");
						return true;
					}else{
						return false;
					}
				}else {
					main.messages.debugOut("Enchantments are not the same -- Item doesn't match");
					return false;
				}
			}else {
				main.messages.debugOut("Display names are different");
			}
		}else {
			main.messages.debugOut("Item types are different");
			return false;
		}
		return false;
	}
	public String getData(String rawData){//Convert data like "RED WOOL(14)" to 14
		if(rawData.equals("")){
			main.messages.debugOut("No data. Leaving blank");
			return "0";
		}else{
			main.messages.debugOut("Raw data:"+ rawData);
			String[] afterFirst = rawData.split("\\(");
			main.messages.debugOut("Splitting raw Data part one - done");
			String[] beforeSecond = afterFirst[1].split("\\)");
			main.messages.debugOut("Splitting raw Data part two - done");
			String returnMe = beforeSecond[0];
			return returnMe;
		}
	}
	
}
