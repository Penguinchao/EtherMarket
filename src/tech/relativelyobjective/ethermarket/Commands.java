package tech.relativelyobjective.ethermarket;

import java.util.Set;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commands {
	EtherMarket main;
	public Commands(EtherMarket etherMarket){
		main = etherMarket;
	}
	
	public void showHelp(CommandSender sender){
		String[] helpList = getHelpList();
		sender.sendMessage(ChatColor.YELLOW + "EtherMarket Commands:");
		for(int i = 0; i < helpList.length; i++){
			sender.sendMessage(ChatColor.YELLOW + "-"+ ChatColor.GREEN +helpList[i]);
		}
	}
	private String[] getHelpList(){
		String[] returnMe = new String[2];
		returnMe[0] = " Sets the stock of the shop that is being looked at:";
		returnMe[1] = "- /ethermarket setstock <New Stock>";
		return returnMe;
	}
	public void setStock(CommandSender sender, Integer newStock) {
		//Checking if a player
		main.messages.debugOut("Setting shop stock");
		if (sender instanceof Player){
			main.messages.debugOut("Sender is a player");
		}else{
			main.getLogger().info(ChatColor.RED + "Stock can only be set from a player");
			return;
		}
		main.messages.debugOut("Sender is a player");
		//Checking permissions
		Player player = (Player) sender;
		if(player.hasPermission("ethermarket.admin.setstock")){
			main.messages.debugOut("Player has permission");
		}else{
			main.messages.configError(player, "no-permission-setstock");
			return;
		}
		//Get block that player is looking at
		@SuppressWarnings("deprecation")
		Block targetBlock = player.getTargetBlock((Set<Material>) null, 6);
		if(targetBlock == null){
			main.messages.debugOut("Block is null");
			main.messages.configError(player, "look-at-sign");
			return;
		}
		if(targetBlock.getType() == Material.SIGN_POST || targetBlock.getType() == Material.WALL_SIGN){
			main.messages.debugOut("Block is a sign");
		}else{
			main.messages.debugOut("Block is not a sign");
			main.messages.configError(player, "look-at-sign");
			return;
		}
		Sign sign = (Sign) targetBlock.getState();
		if(sign.getLine(0).equals(main.getConfig().getString("sign-header"))){
			main.messages.debugOut("Sign is a shop");
		}else{
			main.messages.debugOut("Sign is not a shop");
			main.messages.configError(player, "look-at-sign");
			return;
		}
		//Lookup shop
		Integer shopID = main.shops.getShopID(targetBlock.getX(), targetBlock.getY(), targetBlock.getZ(), targetBlock.getWorld());
		if(shopID == 0){
			main.messages.configError(player, "not-exist");
		}else{
			main.shops.setStock(shopID, newStock);
			main.messages.configSuccess(player, "stock-success");
		}
	}
}
