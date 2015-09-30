package com.penguinchao.ethermarket;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

import org.bukkit.Bukkit;

public class Database {
	private EtherMarket main;
	
	public Database(EtherMarket etherMarket) {
		main = etherMarket;
		databaseConnect();
		checkTables();
	}
	public void logTransaction(UUID shopOwner, UUID customer, Integer amount, Integer shopID, String itemString, Boolean buying){
		String logString;
		logString = customer+" ";
		if(buying){
			logString = logString+"bought ";
		}else{
			logString = logString+"sold ";
		}
		logString = logString+amount+" items ("+itemString+") through a shop belonging to "+shopOwner;
		if(main.getConfig().getString("log-transactions").equals("true")){
			main.getLogger().info("[Transaction] "+logString);
		}else{
			main.messages.debugOut("Logging transaction: "+logString);
		}
	}
	public void databaseConnect(){
		String mysqlHostName= main.getConfig().getString("mysqlHostName");
		String mysqlPort	= main.getConfig().getString("mysqlPort");
		String mysqlUsername= main.getConfig().getString("mysqlUsername");
		String mysqlPassword= main.getConfig().getString("mysqlPassword");
		String mysqlDatabase= main.getConfig().getString("mysqlDatabase");
		String dburl = "jdbc:mysql://" + mysqlHostName + ":" + mysqlPort + "/" + mysqlDatabase;
		main.messages.debugOut("Attempting to connect to the database "+mysqlDatabase+" at "+mysqlHostName);
		try{
			main.connection = DriverManager.getConnection(dburl, mysqlUsername, mysqlPassword);
		}catch(Exception exception){
			main.getLogger().info("[ERROR] Could not connect to the database -- disabling EtherMarket");
			main.databaseConnected = false;
			Bukkit.getPluginManager().disablePlugin(main);
		}
	}
	public void checkTables(){
		//Shop Table
		String shopString1 = "CREATE TABLE IF NOT EXISTS `shops` ( `shop_id` INT(11) NOT NULL AUTO_INCREMENT , "
				+ "`stock` INT(11) NOT NULL DEFAULT '0' , `buy` FLOAT NOT NULL , `sell` FLOAT NOT NULL , `shopowner` VARCHAR(36) NOT NULL , "
				+ "`shopestablisher` VARCHAR(36) NOT NULL , `x` INT NOT NULL , `y` INT NOT NULL , `z` INT NOT NULL , "
				+ "`world` VARCHAR(50) NOT NULL , `itemname` VARCHAR(50) NOT NULL , "
				+ "`itemmaterial` VARCHAR(50) NOT NULL , `itemenchantments` VARCHAR(200) NOT NULL ,`data` VARCHAR(50) NOT NULL , UNIQUE (`shop_id`) ) ENGINE = InnoDB; ";
		String shopString2 = "ALTER TABLE `shops` DROP PRIMARY KEY;";
		String shopString3 = "ALTER TABLE `shops` ADD PRIMARY KEY (`shop_id`); ";
		main.messages.debugOut("Ensuring database exists...");
		main.messages.debugOut(shopString1);
		try {
			PreparedStatement sql = main.connection.prepareStatement(shopString1);
			sql.executeUpdate();
		} catch (SQLException e) {
			main.getLogger().info("[ERROR] Could not check database tables");
			e.printStackTrace();
		}
		main.messages.debugOut("Dropping primary key if it exists...");
		main.messages.debugOut(shopString2);
		try {
			PreparedStatement sql = main.connection.prepareStatement(shopString2);
			sql.executeUpdate();
		} catch (SQLException e) {
			main.messages.debugOut("No primary key assigned yet...");
		}
		main.messages.debugOut("Creating primary key...");
		main.messages.debugOut(shopString3);
		try {
			PreparedStatement sql = main.connection.prepareStatement(shopString3);
			sql.executeUpdate();
		} catch (SQLException e) {
			main.messages.debugOut("Cannot add primary key:");
			e.printStackTrace();
		}
		//Transaction logging
		if(main.getConfig().getString("log-transactions").equals("true")){
			main.messages.debugOut("Transaction logging is enabled. Creating tables...");
			String transactionString1 = "CREATE TABLE IF NOT EXISTS `transactions` ( `transaction_id` INT(11) NOT NULL AUTO_INCREMENT , "+
					"`shop_id` INT(11) NOT NULL , `customer` VARCHAR(36) NOT NULL , `shop_owner` VARCHAR(36) NOT NULL , `item` VARCHAR(150) NOT NULL , "+
					"`quantity` INT(11) NOT NULL , `total_cost` FLOAT NOT NULL , `purchase` BOOLEAN NOT NULL , UNIQUE (`transaction_id`) )";
			String transactionString2 = "ALTER TABLE `transactions` DROP PRIMARY KEY;";
			String transactionString3 = "ALTER TABLE `transactions` ADD PRIMARY KEY (`transaction_id`); ";
			try {
				PreparedStatement sql = main.connection.prepareStatement(transactionString1);
				sql.executeUpdate();
			} catch (SQLException e) {
				main.getLogger().info("[ERROR] Could not check database table");
				e.printStackTrace();
			}
			main.messages.debugOut("Dropping primary key if it exists...");
			main.messages.debugOut(transactionString2);
			try {
				PreparedStatement sql = main.connection.prepareStatement(transactionString2);
				sql.executeUpdate();
			} catch (SQLException e) {
				main.messages.debugOut("No primary key assigned yet...");
			}
			main.messages.debugOut("Creating primary key...");
			main.messages.debugOut(transactionString3);
			try {
				PreparedStatement sql = main.connection.prepareStatement(transactionString3);
				sql.executeUpdate();
			} catch (SQLException e) {
				main.messages.debugOut("Cannot add primary key:");
				e.printStackTrace();
			}
		}
	}
}