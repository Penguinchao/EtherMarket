package com.penguinchao.ethermarket;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
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
					main.messages.configSuccess(player, "shop-established");
				}else{
					main.messages.debugOut("Invalid Items");
					main.messages.configError(player, "invalid-item");
				}
			} else {
				main.messages.debugOut("Player did not hit the shop that he was making");
				main.messages.configError(player, "finish-other-shop");
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
			main.messages.configError(player, "insufficient-funds");
			return;
		}else{
			main.messages.debugOut("Seller can pay");
		}
		//Check player inventory
		if(main.inventory.getInventoryItemCount(player, shopItem) < amount){
			main.messages.debugOut("Not enough items in player's inventory");
			main.messages.configError(player, "not-enough-items");
			return;
		}else{
			main.messages.debugOut("Player has enough items in inventory");
		}
		//Do transaction
		if( shopUUID.equals(userUUID) ){
			main.messages.debugOut("Shop owner UUID is the same: "+shopUUID);
		}else{
			//TODO Sale notification to shop owner if online
			org.bukkit.OfflinePlayer sellerPlayer = Bukkit.getOfflinePlayer(UUID.fromString(shopUUID));
			org.bukkit.OfflinePlayer buyerPlayer = Bukkit.getOfflinePlayer(UUID.fromString(userUUID));
			main.messages.debugOut("Shop owner and player UUIDs are different");
			main.messages.debugOut("Changing player's vault balance");
			main.eco.depositPlayer(buyerPlayer, totalValue);
			main.messages.debugOut("Changing buyer's vault balance");
			main.eco.withdrawPlayer(sellerPlayer, totalValue);
			main.database.logTransaction(UUID.fromString(shopUUID), UUID.fromString(userUUID), amount, totalValue, shopID, shopItem.toString(), false);
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
		ItemStack shopItem = main.shops.getShopItem(shopID, sneaking, seller);
		float totalValue = shopItem.getAmount()*main.shops.getShopBuyPrice(shopID);
		main.messages.debugOut("Getting stock");
		Integer stock = main.shops.getStock(shopID);
		Integer amount = shopItem.getAmount();
		//Check stock
		if(stock < amount ){
			main.messages.debugOut("Insufficient stock");
			main.messages.configError(player, "insufficient-stock");
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
			main.messages.configError(player, "insufficient-funds");
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
			main.database.logTransaction(UUID.fromString(shopUUID), UUID.fromString(userUUID), amount, totalValue, shopID, shopItem.toString(), true);
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
	public Boolean isAttachedToShop(Block block){
		//main.messages.debugOut("Checking if the block is attached to a sign ");
		Sign[] attachedSigns = getAttachedSigns(block);
		//main.messages.debugOut("Looked up list of signs");
		if(attachedSigns == null){
			//main.messages.debugOut("List empty");
			return false;
		}else if(attachedSigns.length == 0){
			//main.messages.debugOut("List empty");
			return false;
		}
		//main.messages.debugOut("List is not empty");
		for(int i = 0; i < attachedSigns.length; i++){
			//main.messages.debugOut("Checking if sign is a shop #"+i);
			//main.messages.debugOut(attachedSigns[i].toString());
			if( (((Sign) attachedSigns[i]).getLine(0) ).equals(main.getConfig().getString("sign-header"))){//TODO
				//main.messages.debugOut("Sign is a shop sign");
				return true;
			}else{
				//main.messages.debugOut("Sign is not a shop sign");
			}
		}
		//main.messages.debugOut("No signs are shops");
		return false;
	}
	public Sign[] getAttachedSigns(Block block){
		//Get nearby blocks
		//main.messages.debugOut("Getting adjacent blocks...");
		Block[] adjacentBlocks = new Block[5];
		Location origin = block.getLocation();
		Integer originX = origin.getBlockX();
		Integer originY = origin.getBlockY();
		Integer originZ = origin.getBlockZ();
		World originWorld = origin.getWorld();
		Location above = new Location(originWorld, originX, originY + 1, originZ);
		adjacentBlocks[0] = above.getBlock();
		Location adjacent1 = new Location(originWorld, originX + 1, originY, originZ);
		adjacentBlocks[1] = adjacent1.getBlock();
		Location adjacent2 = new Location(originWorld, originX - 1, originY, originZ);
		adjacentBlocks[2] = adjacent2.getBlock();
		Location adjacent3 = new Location(originWorld, originX, originY, originZ + 1);
		adjacentBlocks[3] = adjacent3.getBlock();
		Location adjacent4 = new Location(originWorld, originX, originY, originZ - 1);
		adjacentBlocks[4] = adjacent4.getBlock();
		//Convert sign blocks into sign objects
		//main.messages.debugOut("Creating tempSign array");
		Integer signCount = 0;
		//org.bukkit.material.Sign[] tempSign = new org.bukkit.material.Sign[5];
		Sign[] tempSignBlock = new Sign[5];
		//main.messages.debugOut("Converting sign blocks");
		for(int i = 0; i < 5; i++){
			if(adjacentBlocks[i].getType() == Material.WALL_SIGN || adjacentBlocks[i].getType() == Material.SIGN_POST){
				//main.messages.debugOut("Block is a sign");
				//Block is a sign
				org.bukkit.material.Sign thisSign = (org.bukkit.material.Sign) adjacentBlocks[i].getState().getData();
				//main.messages.debugOut("Testing if sign is attached to this block");
			    if(adjacentBlocks[i].getRelative(thisSign.getAttachedFace()).equals(block)){
			    	//main.messages.debugOut("This sign is attached");
			    	//Sign is attached to this block
			    	//main.messages.debugOut("Saving material.sign");
			    	//tempSign[signCount] = thisSign;
			    	//main.messages.debugOut("Saving block.sign");
			    	tempSignBlock[signCount] = (Sign) adjacentBlocks[i].getState();
			    	//main.messages.debugOut("Moving on to next iteration");
			    	signCount++;
			    }else{
			    	//main.messages.debugOut("This sign is not attached");
			    }
			}else{
				//main.messages.debugOut("Block is not a sign");;
			}
		}
		if(signCount == 0){
			//main.messages.debugOut("Sign count is zero. Returning null");
			return null;
		}
		//main.messages.debugOut("Sign count is "+signCount);
		Sign[] returnMe = new Sign[signCount];
		for(int i = 0; i < signCount; i++){
			returnMe[i] = tempSignBlock[i];
		}
		//main.messages.debugOut("Returning attached signs");
		return returnMe;
	}
	public Boolean blocksAttachedToShop(List<Block> movedBlocks){
		if(movedBlocks.isEmpty()){
			return false;
		}
		main.messages.debugOut("Iterating through array");
		for(Block block : movedBlocks){
			if(isAttachedToShop(block)){
				main.messages.debugOut("Attached to shop");
				return true;
			}else{
				main.messages.debugOut("Not attached to shop");
			}
		}
		return false;
	}
}
