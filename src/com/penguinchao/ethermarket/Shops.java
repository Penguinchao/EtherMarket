package com.penguinchao.ethermarket;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.UUID;

import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

public class Shops {

	private EtherMarket main;
	
	public Shops(EtherMarket etherMarket){
		main = etherMarket;
	}
	
	public Boolean isMakingShop(Player player){ //tests to see if the player is currently creating another shop
		String name = player.getName();
		if(main.PlayerMakingShop.containsKey(player.getName())){
			String hashValue = main.PlayerMakingShop.get(name);
			if(hashValue != "false"){
				return true;
			}
		} 
		return false;
	}
	public void setMakingShop(Player owner, Player establisher, Integer x, Integer y, Integer z, World world){ //Adds player to the hashMap plugin.PlayerMakingShop to mark them as making a shop
		establisher.sendMessage(ChatColor.GREEN + this.main.getConfig().getString("ask-for-item"));
		UUID ownerID = owner.getUniqueId();
		UUID establisherID = establisher.getUniqueId();
		String blockLocation = x + "," + y + "," + z + "," + world.getName()+","+ownerID.toString()+","+establisherID.toString();
		main.messages.debugOut("Marking " + establisher.getName()+" as creating a shop at "+blockLocation);
		main.PlayerMakingShop.put(establisher.getName(), blockLocation);
	}
	public Boolean isCorrectPricing(String priceline){ //tests to see if the buy line of a sign has the correct syntax
		//Correct Format: B 5 : 3 S
		//B for buy, number for buy price, colon, number for selling price, S for sell
		//TODO prevent negative numbers
		String[] brokenPrice = priceline.split(" ");
		if(brokenPrice == null){
			main.messages.debugOut("Price line was empty!");
			return false;
		}else if(brokenPrice.length != 5) {
			main.messages.debugOut("Price line did not have the correct amount of elements (5)");
			return false;
		}else{
			main.messages.debugOut("brokenPrice = " + brokenPrice[0]+brokenPrice[1]+brokenPrice[2]+brokenPrice[3]+brokenPrice[4]);
			if(brokenPrice[0].equals("B") ){
				if(NumberUtils.isNumber(brokenPrice[1])){
					if(brokenPrice[2].equals(":") ){
						if(NumberUtils.isNumber(brokenPrice[3])){
							if(brokenPrice[4].equals("S") ){
								return true;
							} else {
								main.messages.debugOut(brokenPrice[4]+ " should read 'S'");
							}
						} else {
							main.messages.debugOut(brokenPrice[3] + " is not a number");
						}
					} else {
						main.messages.debugOut(brokenPrice[2]+" is not a colon");
					}
				} else {
					main.messages.debugOut(brokenPrice[1] + " is not a number");
				}
			} else {
				main.messages.debugOut(brokenPrice[0]+" should read 'B'");
			}
			return false;
		}
	}
	public void establishShop(String owner, String establisher, String establisherName, Integer x, Integer y, Integer z, World world, ItemStack item, Float buy, Float sell){ //Adds the shop to the database and unmarks the player as making a shop
		String displayName = item.getItemMeta().getDisplayName();
		String material = item.getType().toString();
		String enchantments = main.enchantment.enchantmentsToString(item);
		String data = item.getData().toString();
		//Insert in to database
		String query = "INSERT INTO shops (shopowner, shopestablisher, x, y, z, world, itemname, itemmaterial, itemenchantments, buy, sell, data) VALUES ('"+
				owner+"', '"+
				establisher+"', '"+
				x+"', '"+
				y+"', '"+
				z+"', '"+
				world + "', '"+
				displayName+"', '"+
				material+"', '"+
				enchantments+"', '"+
				buy+"', '"+
				sell+"', '"+
				data+"' "+
				");";
		main.messages.debugOut("Performing Query:");
		main.messages.debugOut(query);
		try {
			PreparedStatement sql = main.connection.prepareStatement(query);
			sql.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		main.PlayerMakingShop.remove(establisherName);
		main.messages.debugOut(establisherName+" is finished making a shop");
		
	}
	public float getShopBuyPrice(Integer shopID){ //Returns the cost to buy a single item from the specified shop
		//Returns the price that it costs the customer to buy one item
		String query = "SELECT `buy` FROM `shops` WHERE `shop_id`="+shopID+";";
		main.messages.debugOut("Executing query: "+ query);
		String buyPrice = "0";
		try {
			PreparedStatement sql = main.connection.prepareStatement(query);
			ResultSet result = sql.executeQuery();
			main.messages.debugOut("Query Completed");
			result.next();
			buyPrice= result.getString("buy");
		}catch (SQLException e) {
			main.messages.debugOut("SQL Problem -- could not find shop's buy price");
			//e.printStackTrace();
		}
		main.messages.debugOut("getShopBuyPrice returned a value of "+buyPrice+" for the shopID "+shopID);
		float buyPriceFloat = Float.parseFloat(buyPrice);
		return buyPriceFloat;
	}
	public float getShopSellPrice(Integer shopID){ //Returns the asking price to sell a single item to the specified shop
		//Returns the price that the shop owner will pay the consumer for one item
		String query = "SELECT `sell` FROM `shops` WHERE `shop_id`="+shopID+";";
		main.messages.debugOut("Executing query: "+ query);
		String sellPrice = "0";
		try {
			PreparedStatement sql = main.connection.prepareStatement(query);
			ResultSet result = sql.executeQuery();
			main.messages.debugOut("Query Completed");
			result.next();
			sellPrice= result.getString("sell");
		}catch (SQLException e) {
			main.messages.debugOut("SQL Problem -- could not find shop's sell price");
			//e.printStackTrace();
		}
		main.messages.debugOut("getShopSellPrice returned a value of "+sellPrice+" for the shopID "+shopID);
		float sellPriceFloat = Float.parseFloat(sellPrice);
		return sellPriceFloat;
	}
	public ItemStack getShopItem(Integer shopID, Boolean fullStack, String seller){ //Returns the itemStack that is attempting to be purchased
		//Converts database strings for this item to an ItemStack
		main.messages.debugOut("getShopItem: Checking shop's existence");
		if(shopID == 0){
			main.messages.debugOut("Shop ID is ZERO, which is a placeholder for nonexistent shops -- returning a stack of 0 air");
			ItemStack returnMeError = new ItemStack(Material.AIR, 0);
			return returnMeError;
		}
		main.messages.debugOut("Getting shop item");
		Material itemMaterial = null;
		String material = null;
		String enchantments = null;
		String name = "";
		String data = "";
		String query = "SELECT * FROM `shops` WHERE `shop_id`="+shopID+";";
		main.messages.debugOut("Executing query: "+ query);
		try {
			PreparedStatement sql = main.connection.prepareStatement(query);
			ResultSet result = sql.executeQuery();
			main.messages.debugOut("Query Completed");
			result.next();
			material= result.getString("itemmaterial");
			enchantments= result.getString("itemenchantments");
			name = result.getString("itemname");
			data = result.getString("data");
		}catch (SQLException e) {
			main.messages.debugOut("Could not find shop item -- have you tried to manually change the database?");
			e.printStackTrace();
		}
		main.messages.debugOut("Beginning to assign variables");
		main.messages.debugOut("Data");
		String dataAsInt = main.inventory.getData(data);
		main.messages.debugOut("Material");
		itemMaterial= Material.getMaterial(material);
		//get the amount after checking full stack
		int quantity = 0;
		if(fullStack){
			quantity= itemMaterial.getMaxStackSize();
		}else {
			quantity = 1;
		}
		main.messages.debugOut("Creating New item stack");
		ItemStack shopItem = new ItemStack(itemMaterial, quantity, (short) Short.parseShort(dataAsInt) ) ;
		main.messages.debugOut("Creating item meta");
		ItemMeta meta = null;
		EnchantmentStorageMeta bookMeta = null;
		Boolean isBook = null;
		//Check if is a book
		if(material.contains("ENCHANTED_BOOK")){
			//Do for book items
			main.messages.debugOut("Item is an enchanted book -- making note of it");
			bookMeta = (EnchantmentStorageMeta) shopItem.getItemMeta();
			isBook = true;
		}else {
			//Do for non-book items
			main.messages.debugOut("Item is not an enchanted book");
			meta = shopItem.getItemMeta();
			isBook = false;
		}
		main.messages.debugOut("Item meta: Display Name");
		if(name.equalsIgnoreCase(null) || name.equalsIgnoreCase("") || name.equalsIgnoreCase("null") ){
			//Do Nothing'
			main.messages.debugOut("No set item name -- Skipping");
		}else{
			main.messages.debugOut("Setting item meta name to: "+name);
			if(isBook){
				bookMeta.setDisplayName(name);
			}else{
				meta.setDisplayName(name);
			}
		}
		if(shopItem.getMaxStackSize()==1){ 
			//Test to prevent generic blocks from having lore
			//Tests stack size instead of item material, because only valuable items don't stack
			if(main.getConfig().getString("item-receipt")=="true"){
				main.messages.debugOut("Setting receipt");
				if(isBook){
					main.messages.debugOut("Assigning book lore");
					bookMeta.setLore(Arrays.asList(main.getConfig().getString("item-receipt-text"), seller));
				}else{
					main.messages.debugOut("Assigning non-book lore");
					meta.setLore(Arrays.asList(main.getConfig().getString("item-receipt-text"), seller));
				}
			}
		}
		if(!isBook){
			main.messages.debugOut("Assigning non-book meta to the itemstack");
			shopItem.setItemMeta(meta);
		}
		main.messages.debugOut("Enchanting...");
		String[] brokenEnchantments = enchantments.split(";");
		if(!enchantments.equalsIgnoreCase("")){
			for(int i=0; i<brokenEnchantments.length; i++){
				main.messages.debugOut("Splitting: "+brokenEnchantments[i]);
				String[] brokenValues = brokenEnchantments[i].split(",");
				main.messages.debugOut("Split Version: "+brokenValues[0]+" and "+brokenValues[1]);
				String enchName = brokenValues[0];
				main.messages.debugOut("Final enchantment name string: "+enchName);
				main.messages.debugOut("Assigning enchantment variable");
				main.messages.debugOut(enchName);
				Enchantment currentEnchantment = Enchantment.getByName(enchName);
				main.messages.debugOut("Assigning enchantment level integer");
				int currentLevel = Integer.parseInt(brokenValues[1]);
				if(!isBook){
					main.messages.debugOut("Adding (un)safe enchantment to non-book: "+currentEnchantment.getName()+" at level "+currentLevel);
					shopItem.addUnsafeEnchantment(currentEnchantment, currentLevel);
				}else{
					main.messages.debugOut("Adding stored enchantment: "+currentEnchantment.getName()+" at level "+ currentLevel);
					if(bookMeta.addStoredEnchant(currentEnchantment, currentLevel, true)){
						main.messages.debugOut("Item meta was changed as a result of addStoredEnchant");
					}else{
						main.messages.debugOut("Item meta not changed! -- this should not happen");
					}
				}
				main.messages.debugOut("Enchantment added!");				
			}
			if(isBook){
				main.messages.debugOut("Assigning book meta to the itemstack");
				shopItem.setItemMeta(bookMeta);
			}
		}else{
			main.messages.debugOut("No enchantments -- Skipping");
		}
		main.messages.debugOut("Returning the itemstack");
		return shopItem;
	}
	public String getShopOwner(Integer shopID){ //Returns the owner's UUID of the specified shop from the database
		main.messages.debugOut("getShopOwner: Checking shop's existence");
		if(shopID == 0){
			main.messages.debugOut("Shop ID is ZERO, which is a placeholder for shops that don't exist -- returning empty string");
			String returnMeError = "";
			return returnMeError;
		}
		String query = "SELECT shopowner FROM `shops` WHERE `shop_id`="+shopID+";";
		main.messages.debugOut("Trying query: "+query);
		PreparedStatement sql;
		try {
			sql = main.connection.prepareStatement(query);
			ResultSet result = sql.executeQuery();
			result.next();
			return result.getString("shopowner");
		} catch (SQLException e) {
			//e.printStackTrace();
			main.messages.debugOut("Shop owner cannot be found -- is this a real shop?");
		}
		main.messages.debugOut("Shop not found");
		return "null";
	}
	public Integer getShopID(Integer x, Integer y, Integer z, World world){ //Return the shop ID through database queries
		Integer shopID;
		String query = "SELECT shop_id FROM `shops` WHERE `x`="+x+
				" AND `y`="+y+
				" AND `z`="+z+
				" AND `world`="+world.getName()+
				";";
		main.messages.debugOut("Trying query:"+query);
		PreparedStatement sql;
		try {
			sql = main.connection.prepareStatement(query);
			ResultSet result = sql.executeQuery();
			if (!result.next() ) {
			    main.messages.debugOut("no data");
			    return 0;
			}
			main.messages.debugOut("Shop ID is "+result.getInt("shop_id"));
			shopID = result.getInt("shop_id");	
		} catch (SQLException e) {
			main.messages.debugOut("Shop could not be found; has it been completed?");
			return 0;
		}
		main.messages.debugOut("Sign ID is "+shopID);
		return shopID;
	}
	public Integer getStock(Integer shopID){ //Returns the stock of the shop through database queries
		main.messages.debugOut("getStock: Checking shop's existence");
		if(shopID == 0){
			main.messages.debugOut("Shop ID is ZERO, which is a placeholder for nonexistent shops -- returning ZERO");
			Integer returnMeError = 0;
			return returnMeError;
		}
		String query = "SELECT `stock` FROM `shops` WHERE `shop_id`="+shopID+";";
		main.messages.debugOut(query);
		try {
			PreparedStatement sql = main.connection.prepareStatement(query);
			ResultSet result = sql.executeQuery();
			result.next();
			
			Integer stock= result.getInt("stock");
			return stock;
		} catch (SQLException e) {
			//e.printStackTrace();
			main.messages.debugOut("Stock could not be retrieved -- Is the shop real?");
		}
		main.messages.debugOut("getStock Failed -- returning value of 0");
		return 0;
	}
	public void setStock(Integer shopID, Integer stock){ //Changes the stock in the database for the specified shop
		main.messages.debugOut("setStock: Checking shop's existence");
		if(shopID == 0){
			main.messages.debugOut("Shop ID is ZERO, which is a placeholder for nonexistent shops -- doing nothing");
			return;
		}
		String query = "UPDATE `shops` SET stock=? WHERE shop_id=?;";
		main.messages.debugOut(query);
		try {
			PreparedStatement sql = main.connection.prepareStatement(query);
			sql.setInt(1, stock);
			sql.setInt(2, shopID);
			sql.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public void showShopInfo(Integer shopID, Player player){ //Display's the shop's information to the specified player
		main.messages.debugOut("showShopInfo: Checking shop's existence");
		if(shopID == 0){
			main.messages.debugOut("Shop ID is ZERO, which is a placeholder for nonexistent shops -- returning empty string");
			return;
		}
		String query = "SELECT * FROM `shops` WHERE `shop_id`="+shopID+";";
		main.messages.debugOut(query);
		try {
			PreparedStatement sql = main.connection.prepareStatement(query);
			ResultSet result = sql.executeQuery();
			main.messages.debugOut("Query Done");
			result.next();
			main.messages.debugOut("Getting material");
			String material= result.getString("itemmaterial");
			main.messages.debugOut("getting enchantments");
			String enchantments= result.getString("itemenchantments");
			main.messages.debugOut("getting stock");
			Integer stock= result.getInt("stock");
			main.messages.debugOut("getting buy price");
			Integer buy= result.getInt("buy");
			main.messages.debugOut("getting sell price");
			Integer sell= result.getInt("sell");
			main.messages.debugOut("getting data");
			String data= main.inventory.getData(result.getString("data"));
			int dataAsInt = Integer.parseInt(data);
			main.messages.debugOut("Giving all info:");
			main.messages.debugOut("Stock:"+stock+" Buy:"+buy+" Sell:"+sell+" Material:"+material+" Data:"+data+" Enchantments:"+enchantments);
			player.sendMessage(ChatColor.YELLOW+"Shop Information:");
			player.sendMessage(ChatColor.GREEN+"Item: "+ChatColor.BLUE+material);
			if(!data.equals("") && dataAsInt!=0 ){
				main.messages.debugOut("data not null");
				player.sendMessage(ChatColor.GREEN+"Data: "+ChatColor.BLUE+data);
			}
			if(!enchantments.equals("")){
				sayEnchantments(enchantments, player);
			}
			player.sendMessage(ChatColor.GREEN+"Buying Price: "+ChatColor.BLUE+buy);
			player.sendMessage(ChatColor.GREEN+"Selling Price: "+ChatColor.BLUE+sell);
			player.sendMessage(ChatColor.GREEN+"Current Stock: "+ChatColor.BLUE+stock);
			player.sendMessage(ChatColor.YELLOW+"Hit to buy or activate to sell");
			
		} catch (SQLException e) {
			//e.printStackTrace();
			main.messages.debugOut("SQL Error -- Is this shop real?");
		}
	}
	public void sayEnchantments(String enchantments, Player player){ //Display's the specified enchantments in a list format to the specified player; does nothing if list is empty
		String[] baseSplit = enchantments.split(";");
		if(Integer.valueOf(baseSplit.length)>0){
			if(baseSplit[0]!=""){
				player.sendMessage(ChatColor.GREEN+"Enchantments:");
				for(int i=0; i<baseSplit.length;i++){
					String[] bigSplit= baseSplit[i].split(",");
					player.sendMessage(ChatColor.GREEN+"-"+ChatColor.BLUE+main.enchantment.cleanEnchantmentName(bigSplit[0])+" "+bigSplit[1]);
				}
			}
		}
	}
	public Boolean getDestroyingShop(String playerName, Integer shopID){ //Returns true if player is trying to destroy the specified shop
		if(main.PlayerDestroyingShop.containsKey(playerName)){
			main.messages.debugOut("Player is destroying a shop. Checking which one");
			if(main.PlayerDestroyingShop.get(playerName).equals(shopID) ){
				main.messages.debugOut("Player is breaking a shop that was previously attempted to be broken");
				return true;
			}else {
				return false;
			}
		}else{
			return false;
		}
	}
	public void setDestroyingShop(String playerName, Integer shopID){ //Sets a player as destroying the specified shop
		main.PlayerDestroyingShop.put(playerName, shopID);
	}
	public void unsetDestroyingShop(String playerName){//Removes player from list of players destroying a shop
		main.PlayerDestroyingShop.remove(playerName);
	}
	public void deleteShop(int shopID){//Deletes shops from the database with the specified shop ID
		if(shopID > 0){
			String query = "DELETE FROM shops WHERE shop_id="+shopID+";";
			main.messages.debugOut("Performing Deletion Query:");
			main.messages.debugOut(query);
			try {
				PreparedStatement sql = main.connection.prepareStatement(query);
				sql.executeUpdate();
			} catch (SQLException e) {
				//e.printStackTrace();
				main.messages.debugOut("Shop could not be deleted from the database -- does it exist?");
			}
		}else{
			main.messages.debugOut("Shop value is not in database -- skipping");
		}
	}
	public Boolean shopBeingEstablished(Integer x, Integer y, Integer z, String world){ //Looks through the list of players making shops and determines if the block specified is a shop in-progress
		main.messages.debugOut("Looking for shop at xyz ("+x+","+y+","+z+"world");
		for(String value : main.PlayerMakingShop.values() ){
			main.messages.debugOut(value);
			String[] thisEntry = value.split(",");
			if(thisEntry[0].equals(x)){
				if(thisEntry[1].equals(y)){
					if(thisEntry[2].equals(z)){
						if(thisEntry[3].equals(world)){
							main.messages.debugOut("All items are the same -- this shop is being established");
							return true;
						}else{
							main.messages.debugOut(thisEntry[3]+" does not equal "+world);
							return false;
						}
					}else{
						main.messages.debugOut(thisEntry[2]+" does not equal "+z);
						return false;
					}
				}else{
					main.messages.debugOut(thisEntry[1]+" does not equal "+y);
					return false;
				}
			}else{
				main.messages.debugOut(thisEntry[0]+" does not equal "+x);
				return false;
			}
		}
		return false;
	}
	public Boolean isValidMaterial(Material material){//Compares material to a list of unsupported item types; returns true if material is not within that list
		Material[] invalidMaterials = {Material.WRITTEN_BOOK, Material.AIR};
		for(int i = 0; i<invalidMaterials.length; i++){
			if(invalidMaterials[i].equals(material)){
				return false;
			}
		}
		return true;
	}
}
