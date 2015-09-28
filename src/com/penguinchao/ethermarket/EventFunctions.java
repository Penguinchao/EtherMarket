package com.penguinchao.ethermarket;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class EventFunctions {
	private EtherMarket main;
	EventFunctions(EtherMarket etherMarket){
		main = etherMarket;
	}
	public void activateShop(Sign sign, Player player, Boolean sneaking, Boolean rightClick){ //This happens when a player hits a sign with a shop header
		main.messages.debugOut("Activating shop");
		Integer shopID = main.shops.getShopID(sign.getX(), sign.getY(), sign.getZ(), sign.getWorld());
		main.messages.debugOut("Obtained shop ID is: "+shopID);
		String[] buyLine = sign.getLine(2).split(" ");
		float buy = Float.parseFloat(buyLine[1]);
		float sell = Float.parseFloat(buyLine[3]);
		main.messages.debugOut("Checking to see if player is making a shop");
		if( main.shops.isMakingShop(player)  ){
			main.messages.debugOut("Player is making a shop");
			String[] playerHashInfo = main.PlayerMakingShop.get(player.getName() ).split(",");
			Integer playerX = Integer.valueOf(playerHashInfo[0]);
			Integer playerY = Integer.valueOf(playerHashInfo[1]);
			Integer playerZ = Integer.valueOf(playerHashInfo[2]);
			String playerWorld = playerHashInfo[3];
			main.messages.debugOut(playerX+ " " + sign.getX());
			main.messages.debugOut(playerY+ " " + sign.getY());
			main.messages.debugOut(playerZ+ " " + sign.getZ());
			main.messages.debugOut(playerWorld+" "+sign.getWorld().toString());
			if(sign.getX()==playerX && sign.getY()==playerY && sign.getZ()==playerZ && sign.getWorld().getName().equals(playerWorld) ){
				main.messages.debugOut(player.getName()+" hit the shop that (s)he was creating");
				org.bukkit.inventory.ItemStack equipped = player.getItemInHand();
				if(main.shops.isValidMaterial(equipped.getType())){
					main.shops.establishShop(player.getUniqueId().toString(), player.getUniqueId().toString(), player.getDisplayName(), sign.getX(), sign.getY(), sign.getZ(), sign.getWorld(), equipped, buy, sell);
				}
			} else {
				main.messages.debugOut("Player did not hit the shop that he was making");
			}
		} else if( !shopID.equals(0) ){
			main.messages.debugOut("Player is not making a shop; transacting with this one");
			transactWithShop(shopID, player, sneaking, rightClick, sign.getLine(3));
		} else {
			main.messages.debugOut("Player is not making a shop and shop ID equals 0; do nothing");
		}
	}
	public void transactWithShop(Integer shopID, Player player, Boolean sneaking, Boolean rightClick, String seller){//This happens if a shop is hit/activated and the shop is not currently being made
		Integer currentShop;
		//Checking current Shop
		if(!main.ActivePlayerShop.containsKey(player.getName()) ){
			main.messages.debugOut("Player does not have an active shop. Making active one this shop");
			main.shops.showShopInfo(shopID, player);
			main.ActivePlayerShop.put(player.getName(), shopID);
			return;
		}else{
			main.messages.debugOut("Player has an active shop");
			currentShop = main.ActivePlayerShop.get(player.getName());
		}
		if(currentShop.equals(shopID)){
			main.messages.debugOut("Player has activated his current shop");
		}else{
			main.messages.debugOut("Player has not activated his current shop. Making it this one");
			main.shops.showShopInfo(shopID, player);
			main.ActivePlayerShop.put(player.getName(), shopID);
			return;
		}
		//Checking if Buying or Selling
		if(rightClick){
			sellToShop(shopID, player, sneaking);
		}else{
			buyFromShop(shopID, player, sneaking, seller);
		}
	}
	@SuppressWarnings("deprecation")
	private void sellToShop(Integer shopID, Player player, Boolean sneaking) {
		main.messages.debugOut("Getting shop owner UUID");
		String shopUUID = main.shops.getShopOwner(shopID);
		main.messages.debugOut(shopUUID);
		main.messages.debugOut("Getting user UUID");
		String userUUID = player.getUniqueId().toString();
		main.messages.debugOut(userUUID);
		
		main.messages.debugOut("Player does not own shop");
		ItemStack shopItem = main.shops.getShopItem(shopID, sneaking, "");
		Integer amount = shopItem.getAmount();
		float totalValue = amount*main.shops.getShopSellPrice(shopID);
		//Check shop's balance
		if( shopUUID.equals(userUUID) ){
			main.messages.debugOut("User owns shop");
		} else if(main.vault.getVaultBalance(UUID.fromString(main.shops.getShopOwner(shopID))) < totalValue ){
			main.messages.debugOut("Seller insufficient funds");
			//TODO notification
			return;
		}else{
			main.messages.debugOut("Seller can pay");
		}
		//Check player inventory
		if(main.inventory.getInventoryItemCount(player, shopItem) < amount){
			main.messages.debugOut("Not enough items in player's inventory");
			return;
			//TODO notification
		}else{
			main.messages.debugOut("Player has enough items in inventory");
		}
		//Do transaction
		if( shopUUID.equals(userUUID) ){
			main.messages.debugOut("Shop owner UUID is the same: "+shopUUID);
			//TODO Deposit notification
		}else{
			//TODO Sale notification
			org.bukkit.OfflinePlayer sellerPlayer = Bukkit.getOfflinePlayer(UUID.fromString(shopUUID));
			org.bukkit.OfflinePlayer buyerPlayer = Bukkit.getOfflinePlayer(UUID.fromString(userUUID));
			main.messages.debugOut("Shop owner and player UUIDs are different");
			main.messages.debugOut("Changing player's vault balance");
			main.eco.depositPlayer(buyerPlayer, totalValue);
			main.messages.debugOut("Changing buyer's vault balance");
			main.eco.withdrawPlayer(sellerPlayer, totalValue);
		}
		main.messages.debugOut("Removing player's items");
		main.inventory.removePlayerItem(player, shopItem, shopItem.getAmount());
		main.messages.debugOut("Changing shops stock");
		main.shops.setStock(shopID, main.shops.getStock(shopID) + amount);
		player.updateInventory();
		main.messages.debugOut("Transaction Complete");
	}
	private void buyFromShop(Integer shopID, Player player, Boolean sneaking, String seller){
		main.messages.debugOut("Getting shop owner UUID");
		String shopUUID = main.shops.getShopOwner(shopID);
		main.messages.debugOut(shopUUID);
		main.messages.debugOut("Getting user UUID");
		String userUUID = player.getUniqueId().toString();
		main.messages.debugOut(userUUID);
		main.messages.debugOut("Player does not own shop");
		ItemStack shopItem = main.shops.getShopItem(shopID, sneaking, seller);
		float totalValue = shopItem.getAmount()*main.shops.getShopSellPrice(shopID);
		main.messages.debugOut("Getting stock");
		Integer stock = main.shops.getStock(shopID);
		Integer amount = shopItem.getAmount();
		//Check stock
		if(stock < amount ){
			main.messages.debugOut("Insufficient stock");
			//TODO message
			return;
		}else{
			main.messages.debugOut("Sufficient stock");
		}
		//Check balance
		main.messages.debugOut("Checking player balances");
		if( shopUUID.equals(userUUID) ){
			main.messages.debugOut("User owns shop");
		} else if(main.vault.getVaultBalance(player.getUniqueId()) < totalValue ){
			main.messages.debugOut("Buyer insufficient funds");
			//TODO notification
			return;
		}else{
			main.messages.debugOut("Buyer can pay");
		}
		//Check inventory space
		if(main.inventory.canAddItem(shopItem, player)){
			main.messages.debugOut("Can add item");
		}else{
			main.messages.debugOut("Cannot fit items");
			//TODO Cannot fit items message
			return;
		}
		//Do transaction
		if( shopUUID.equals(userUUID) ){
			main.messages.debugOut("Shop owner UUID is the same: "+shopUUID);
			//TODO Deposit notification
		}else{
			//TODO Sale notification
			// ;potyuiop[
			org.bukkit.OfflinePlayer sellerPlayer = Bukkit.getOfflinePlayer(UUID.fromString(shopUUID));
			org.bukkit.OfflinePlayer buyerPlayer = Bukkit.getOfflinePlayer(UUID.fromString(userUUID));
			double finalValue = Double.valueOf(totalValue);
			main.messages.debugOut("Shop owner and player UUIDs are different");
			main.messages.debugOut("Changing shop's vault balance");
			main.eco.depositPlayer(sellerPlayer, finalValue);
			main.messages.debugOut("Changing player's vault balance");
			main.eco.withdrawPlayer(buyerPlayer, finalValue);
		}
		main.inventory.softAddItem(shopItem, player);
		main.shops.setStock(shopID, main.shops.getStock(shopID)-shopItem.getAmount() );
		main.messages.debugOut("Transaction Complete");
	}
	public Boolean destroyingShop(Integer shopID, Player player){//This checks to see if the player tried to destroy an owned shop already
		if(main.shops.getDestroyingShop(player.getDisplayName(), shopID)){
			main.messages.debugOut("Player is destroying this shop. Allowing Destruction...");
			return true;
		}else{
			main.messages.debugOut("Player is not yet destroying this shop. Changing...");
			main.shops.setDestroyingShop(player.getDisplayName(), shopID);
			return false;
		}
	}
}
