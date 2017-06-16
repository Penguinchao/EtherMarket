package com.penguinchao.ethermarket;

import java.util.UUID;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

public class Vault {
	private EtherMarket main;
	
	Vault(EtherMarket etherMarket){
		main = etherMarket;
	}
	//Vault Functions
	public boolean setupEconomy() {
        if (main.getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = main.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        main.eco = rsp.getProvider();
        return main.eco != null;
    }
	public double getVaultBalance(UUID playerUUID){ //Returns the player's vault balance in the form of a double
		main.messages.debugOut("getting player from UUID");
		org.bukkit.OfflinePlayer player = Bukkit.getOfflinePlayer(playerUUID );
		
		main.messages.debugOut("getting vault balance for player with UUID: "+playerUUID.toString() );
		main.messages.debugOut("player name is: "+player.getName() );
		double returnMe = main.eco.getBalance(player);
		
		main.messages.debugOut("balance is: "+returnMe);
		return returnMe;
	}
}
