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
	public double getVaultBalance(UUID playerUUID){ //Returns the player's vault balance in the form of a double; I have no idea why they use memory-hogging doubles, but it's vault's method of storage, not mine
		main.messages.debugOut("getting player from UUID");
		org.bukkit.OfflinePlayer player = Bukkit.getOfflinePlayer(playerUUID );
		
		main.messages.debugOut("getting vault balance for player with UUID: "+playerUUID.toString() );
		main.messages.debugOut("player name is: "+player.getName() );
		double returnMe = main.eco.getBalance(player);
		
		main.messages.debugOut("balance is: "+returnMe);
		return returnMe;
	}
	/*
	public void changeVaultBalance(UUID playerUUID, float amount){ //Changes the vault balance of the specified player by the given amount; Negative numbers ARE allowed
		main.messages.debugOut("Changing vault balance...");
		double amountDouble = Double.valueOf(amount);
		double absoluteValueDouble = Math.abs(amountDouble);
		org.bukkit.OfflinePlayer player = Bukkit.getOfflinePlayer(playerUUID );
		if(amountDouble == 0){
			//Do nothing
			main.messages.debugOut("Value is zero -- skipping");
		}else if(amountDouble > 0){
			//Number is positive -- Deposit
			main.messages.debugOut("Depositing to seller: "+absoluteValueDouble);
			main.eco.depositPlayer(player, absoluteValueDouble);
		}else if(amountDouble < 0) {
			//Number is negative -- Withdraw
			main.messages.debugOut("Withdrawing from buyer: "+absoluteValueDouble);
			main.eco.withdrawPlayer(player, absoluteValueDouble);
		}else {
			main.messages.debugOut("Amount is not above, below, or at zero -- is it null?");
			return;
		}
		main.messages.debugOut("Done!");
	}
	*/
}
