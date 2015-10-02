package com.penguinchao.ethermarket;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class EventListeners implements Listener {
	private EtherMarket main;
	public EventListeners(EtherMarket etherMarket) {
		main = etherMarket;
		main.getServer().getPluginManager().registerEvents(this, main);
	}
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onSignActivate(PlayerInteractEvent event){ //Used to interact with an existing shop
		main.messages.debugOut("PlayerInteractEvent");
		Boolean rightClick;
		Boolean sneaking;
		Block activatedBlock;
		//Check action type
		if(event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR){
			main.messages.debugOut("A back-click happened");
			rightClick = true;
			main.messages.debugOut("Getting Block...");
			if(event.getAction() == Action.RIGHT_CLICK_BLOCK){
				main.messages.debugOut("RIGHT_CLICK_BLOCK");
				activatedBlock = event.getClickedBlock();
			}else if(event.getAction() == Action.RIGHT_CLICK_AIR){
				activatedBlock = event.getPlayer().getTargetBlock(null, 6);
				main.messages.debugOut("RIGHT_CLICK_AIR");
			}else{
				main.messages.debugOut("Event not found -- This should not happen.");
				return;
			}
		}else if(event.getAction() == Action.LEFT_CLICK_BLOCK){
			main.messages.debugOut("LEFT_CLICK_BLOCK");
			rightClick = false;
			main.messages.debugOut("Getting Block");
			activatedBlock = event.getClickedBlock();
		}else{
			main.messages.debugOut("Event is not relevant. Returning...");
			return;
		}
		//Catch null blocks from RIGHT_CLICK_AIR
		if(activatedBlock == null){
			main.messages.debugOut("Block is null. Returning... (Player too far from block?)");
			return;
		}
		//Check Sneaking
		sneaking = event.getPlayer().isSneaking();
		main.messages.debugOut("Player is Sneaking: "+sneaking);
		//Check Block Type
		if(activatedBlock.getType() == Material.SIGN_POST || activatedBlock.getType() == Material.WALL_SIGN){
			main.messages.debugOut("Player hit a Sign");
		}else{
			main.messages.debugOut("Player did not hit a sign");
			return;
		}
		//Check Sign Header
		main.messages.debugOut("Casting block as a sign");
		BlockState blockState = activatedBlock.getState();
		org.bukkit.block.Sign sign = (org.bukkit.block.Sign) blockState;
		main.messages.debugOut("Checking Sign header");
		if(sign.getLine(0).equals(main.getConfig().getString("sign-header")) ){
			main.messages.debugOut("Sign is a shop sign");
			if(event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR){
				main.messages.debugOut("Catching right-clicks to prevent block dupes");
				event.setCancelled(true);
			}
			main.messages.debugOut("Activating Shop...");
			main.eventFunctions.activateShop(sign, event.getPlayer(), sneaking, rightClick);
		}else{
			main.messages.debugOut("Sign is not a shop");
		}
	}
	@EventHandler
	public void onSignCreate(SignChangeEvent event){ //Used to begin creating a shop sign
		main.messages.debugOut("SignChangeEvent");
		if(event.isCancelled()){
			main.messages.debugOut("Event cancelled by another plugin. Returning");
			return;
		}
		//Check if sign is a shop sign
		if (event.getLine(0).equals(main.getConfig().getString("sign-header")) ){
			main.messages.debugOut("A Shop Sign was created");
		}else{
			main.messages.debugOut("The Sign is not a shop");
			return;
		}
		//Check if player is making a shop
		if(main.shops.isMakingShop(event.getPlayer())==false){
			main.messages.debugOut("Player is not making a shop");
		}else{
			main.messages.configError(event.getPlayer(), "already-making-shop");
			event.getBlock().breakNaturally();
			event.setCancelled(true);
			return;
		}
		//Check player's permissions
		if (event.getPlayer().hasPermission("ethermarket.player.makeshop")){
			main.messages.debugOut("Player has permission to make a shop");
		}else{
			main.messages.configError(event.getPlayer(), "no-permission-creation");
			main.messages.debugOut("Player did not have permission to make a shop");
			event.getBlock().breakNaturally();
			event.setCancelled(true);
			return;
		}
		//Check price syntax
		if(main.shops.isCorrectPricing(event.getLine(2)) ){
			main.messages.debugOut("Shop's Syntax is correct");
			main.messages.configSuccess(event.getPlayer(), "begin-making-shop");
		}else{
			main.messages.configError(event.getPlayer(), "incorrect-syntax");
			event.getBlock().breakNaturally();
			event.setCancelled(true);
			return;
		}
		//Finish Event Listeners
		event.setLine(3, event.getPlayer().getDisplayName());
		main.shops.setMakingShop(event.getPlayer(), event.getPlayer(), event.getBlock().getX(), event.getBlock().getY(), event.getBlock().getZ(), event.getBlock().getWorld());
	}
	@EventHandler
	public void onSignDestroy(BlockBreakEvent event){ //Used to prevent unauthorized, direct destruction of the shop block
		main.messages.debugOut("BlockBreakEvent");
		//Check Block Type
		if(event.getBlock() == null){
			return;
		}
		if(event.getBlock().getType() == Material.SIGN_POST || event.getBlock().getType() == Material.WALL_SIGN){
			main.messages.debugOut("Player is attempting to destroy a Sign");
		}else{
			main.messages.debugOut("Player did not destroy a sign");
			return;
		}
		//Check Sign Header
		main.messages.debugOut("Casting block as a sign");
		BlockState blockState = event.getBlock().getState();
		org.bukkit.block.Sign sign = (org.bukkit.block.Sign) blockState;
		main.messages.debugOut("Checking Sign header");
		if(sign.getLine(0).equals(main.getConfig().getString("sign-header")) ){
			main.messages.debugOut("Sign is a shop sign");
		}else{
			main.messages.debugOut("Sign is not a shop");
			return;
		}
		//Check if shop is currently being established
		main.messages.debugOut("Checking if a shop is currently being established here");
		if(main.shops.shopBeingEstablished(event.getBlock().getX(), event.getBlock().getY(), event.getBlock().getZ(), event.getBlock().getWorld().toString() ) ){
			main.messages.configError(event.getPlayer(), "shop-being-made");
			main.messages.debugOut("A shop is currently being made here");
			event.setCancelled(true);
			return;
		}else{
			main.messages.debugOut("A shop is not being made here");
		}
		//Check shop's ID
		main.messages.debugOut("Checking shop ID");
		Integer shopID = main.shops.getShopID(event.getBlock().getX(), event.getBlock().getY(), event.getBlock().getZ(), event.getBlock().getWorld() );
		if(shopID.equals(0) ){
			main.messages.debugOut("Shop id is 0, which means that it does not exist -- This should not happen, so the shop will be destroyed"); 
			main.messages.debugOut("An unestablished shop was destroyed.");
			return;
		}else{
			main.messages.debugOut("Shop id is "+shopID);
		}
		//Get shop's owner
		main.messages.debugOut("Getting shop's owner");
		UUID shopOwner = UUID.fromString(main.shops.getShopOwner(shopID));
		main.messages.debugOut("Shop owner's UUID is "+shopOwner.toString() );
		//Check shop ownership
		if(event.getPlayer().getUniqueId().equals(shopOwner)){
			main.messages.debugOut("Player owns the shop");
		} else if(event.getPlayer().hasPermission("ethermarket.admin.destroyothershop")){
			main.messages.debugOut("Player does not own shop, but does have ethermarket.admin.destroyothershop");
		} else{
			main.messages.configError(event.getPlayer(), "shop-is-owned");
			main.messages.debugOut("Player does not own shop");
			event.setCancelled(true);
			return;
		}
		//Check sneaking
		main.messages.debugOut("Checking to see if the player is sneaking");
		if(event.getPlayer().isSneaking()){
			main.messages.debugOut("Player is sneaking");
		}else{
			main.messages.debugOut("Player is not sneaking");
			main.messages.configError(event.getPlayer(), "not-sneaking");
			event.setCancelled(true);
			return;
		}
		//Checking destruction status
		main.messages.debugOut("Shop destruction is allowed");
		if(main.eventFunctions.destroyingShop(shopID, event.getPlayer())){
			main.messages.debugOut("Player is destroying the shop");
			main.messages.debugOut("Pulling items before destruction");
			main.shops.emptyShopStock(event.getPlayer(), shopID);
			main.messages.debugOut("Removing shop from Database");
			main.shops.deleteShop(shopID);
			main.shops.unsetDestroyingShop(event.getPlayer().getDisplayName());
			event.setCancelled(false);
			main.messages.debugOut("Event Complete");
			main.messages.configSuccess(event.getPlayer(), "destroy-success");
		}else{
			main.messages.debugOut("Player is not destroying the shop -- Cancelling Event");
			main.messages.configSuccess(event.getPlayer(), "destroy-again");
			event.setCancelled(true);
			return;
		}
	}
	@EventHandler
	public void onPlayerLogout(PlayerQuitEvent event){
		main.messages.debugOut("PlayerQuitEvent");
		main.PlayerMakingShop.remove(event.getPlayer().getDisplayName());
		main.ActivePlayerShop.remove(event.getPlayer().getDisplayName());
		main.PlayerDestroyingShop.remove(event.getPlayer().getDisplayName());
	}
	@EventHandler
	public void onBlockDestroy(BlockBreakEvent event){
		main.messages.debugOut("BlockBreakEvent");
		if(main.eventFunctions.isAttachedToShop( event.getBlock() )){
			if(event.getPlayer() != null){
				main.messages.configError(event.getPlayer(), "attached-shop");
			}
			event.setCancelled(true);
		}else{
			main.messages.debugOut("Block is not attached to a shop");
		}
	}
	
	//This Event is was commented out, because the physics event will check this 
	@EventHandler
	public void onPistonPush(BlockPistonExtendEvent event){
		main.messages.debugOut("BlockPistonExtendEvent");
		//Get a hashmap of the blocks moved, then iterate through it
		if(main.eventFunctions.blocksAttachedToShop(event.getBlocks())){
			main.messages.debugOut("Cancelling piston push, because shops are attached.");
			event.setCancelled(true);
		}
	}
	@EventHandler
	public void onPistonRetract(BlockPistonRetractEvent event){
		main.messages.debugOut("BlockPistonRetractEvent");
		//Get a hashmap of the blocks moved, then iterate through it
		if(main.eventFunctions.isAttachedToShop(event.getRetractLocation().getBlock())){
			event.setCancelled(true);
		}
	}
	@EventHandler
	public void onPhysicsCheck(BlockPhysicsEvent event){
		//Commented out the debug for the physics events, because it made the console useless
		//main.messages.debugOut("BlockPhysicsEvent");
		//if(event.getBlock().getType() == Material.SAND || event.getBlock().getType() == Material.GRAVEL){
		//	main.messages.debugOut("This is a block that can fall");
			if(main.eventFunctions.isAttachedToShop(event.getBlock())){
				main.messages.debugOut("Event would affect a shop block. Cancelling...");
				event.setCancelled(true);
			}else{
				return;
			}
		//}else{
		//	return;
		//}
	}
	@EventHandler
	public void onBurn(BlockBurnEvent event){
		if( main.eventFunctions.isAttachedToShop(event.getBlock()) ){
			event.setCancelled(true);
		}else if(event.getBlock().getType() == Material.SIGN_POST || event.getBlock().getType() == Material.WALL_SIGN){
			if(((Sign) event.getBlock().getState()).getLine(0).equals(main.getConfig().getString("sign-header"))){
				main.messages.debugOut("Preventing burning of a shop");
				event.setCancelled(true);
			}else{
				return;
			}
		}else {
			return;
		}
	}
	@EventHandler
	public void onExplode(EntityExplodeEvent event){
		main.messages.debugOut("EntityExplodeEvent");
		for(Block block : event.blockList()){
			if(main.eventFunctions.isAttachedToShop(block)){
				main.messages.debugOut("Event is related to a shop. Removing Block");
				//event.blockList().clear();
				event.setCancelled(true);
			}else if(block.getType() == Material.SIGN_POST || block.getType() == Material.WALL_SIGN){
				main.messages.debugOut("Block is a sign");
				if( ((Sign) block.getState() ).getLine(0).equals(main.getConfig().getString("sign-header"))){
					main.messages.debugOut("Event hurts a shop. Removing block");
					//event.blockList().clear();
					event.setCancelled(true);
				}else{
					main.messages.debugOut("Event sign is not a shop");
				}
			}
		}
	}
}