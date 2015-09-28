package com.penguinchao.ethermarket;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class Messages {
	private EtherMarket main;
	public Messages(EtherMarket etherMarket){
		main = etherMarket;
	}
	
	//Methods
	public void debugOut(String message){ //Outputs message to console if debug is enabled
		if (main.getConfig().getString("debugenabled") == "true"  ) {
			main.getLogger().info("[Debug] " + message);
		}
	}
	public void playerError(Player player, String message){
		player.sendMessage(ChatColor.RED + message);
	}
	public void playerSuccess(Player player, String message){
		player.sendMessage(ChatColor.GREEN + message);
	}
}
