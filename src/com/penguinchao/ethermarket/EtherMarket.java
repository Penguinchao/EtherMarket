package com.penguinchao.ethermarket;

import java.sql.Connection;
import java.util.HashMap;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.plugin.java.JavaPlugin;


public class EtherMarket extends JavaPlugin {
	//Global Variables
	public Boolean databaseConnected = true;
	public HashMap<String, Integer> ActivePlayerShop = new HashMap<String, Integer>();
	public HashMap<String, String> PlayerMakingShop = new HashMap<String, String>();
	public HashMap<String, Integer> PlayerDestroyingShop = new HashMap<String, Integer>();
	public Connection connection;
	
	//Define Class Objects
	public Messages messages;
	public Database database;
	public Enchantment enchantment;
	public Inventory inventory;
	public Shops shops;
	public Vault vault;
	public Economy eco;
	public EventFunctions eventFunctions;
	public EventListeners eventListeners;
	//Methods
	@Override
	public void onEnable(){
		saveDefaultConfig();
		//Initialize Objects
		messages = new Messages(this);
		database = new Database(this);
		inventory = new Inventory(this);
		enchantment = new Enchantment(this);
		shops = new Shops(this);
		vault = new Vault(this);
		eventFunctions = new EventFunctions(this);
		eventListeners = new EventListeners(this);
		vault.setupEconomy();
		//TODO Check vault dependencies and database, and if not setup, disable plugin with warning
	}
}
