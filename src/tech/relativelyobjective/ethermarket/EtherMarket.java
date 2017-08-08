package tech.relativelyobjective.ethermarket;

import java.sql.Connection;
import java.util.HashMap;

import net.milkbowl.vault.economy.Economy;

import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
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
	public Commands commands;

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
		commands = new Commands(this);
		if(vault.setupEconomy() == false ){
			getLogger().info("Vault is not enabled. Disabling EtherMarket...");
			Bukkit.getPluginManager().disablePlugin(this);
		}
	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) { 
		if (cmd.getName().equalsIgnoreCase("ethermarket") || cmd.getName().equalsIgnoreCase("em") ){
			if(args.length == 0){
				//Player gave no arguments -- Sending command list
				commands.showHelp(sender);
			}else if(args[0].equalsIgnoreCase("setstock")){
				if(args.length == 2){
					if(NumberUtils.isNumber(args[1])){
						Integer newStock = Integer.parseInt(args[1]);
						if( (newStock % 1) == 0 ){
							commands.setStock(sender, newStock);
						}else{
							sender.sendMessage(ChatColor.RED + getConfig().getString("syntax-setstock"));
						}
					}else{
						sender.sendMessage(ChatColor.RED + getConfig().getString("syntax-setstock"));
					}
				}else{
					sender.sendMessage(ChatColor.RED + getConfig().getString("syntax-setstock"));
				}
			}
		}
		return false;
	}
}
