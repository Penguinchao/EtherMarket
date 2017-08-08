package tech.relativelyobjective.ethermarket;

import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

public class Enchantment {
	private EtherMarket main;
	public Enchantment(EtherMarket etherMarket) {
		main = etherMarket;
	}
	public String enchantmentsToString(ItemStack item){ //Returns enchantments in the form of a string: EnchantmentOne,levelOne;EnchantmentTwo,levelTwo;
		Map<org.bukkit.enchantments.Enchantment, Integer> a = null;
		if(item.getData().toString().contains("ENCHANTED_BOOK")){
			main.messages.debugOut("Pulling enchantment information from enchanted book");
			//credit for code http://www.massapi.com/class/org/bukkit/inventory/meta/EnchantmentStorageMeta.html
			EnchantmentStorageMeta esm = (EnchantmentStorageMeta) item.getItemMeta();
            a = esm.getStoredEnchants();
		}else{
			main.messages.debugOut("Pulling enchantment information from enchanted non-book item");
			a= item.getEnchantments();
		}
		String returnMe = "";
		for(Entry<org.bukkit.enchantments.Enchantment, Integer> entry : a.entrySet()) {
		    returnMe = returnMe+entry.getKey().getName()+","+entry.getValue().toString()+";";
		    main.messages.debugOut("Enchantments: "+returnMe);
		}
		return returnMe;
	}
	public Boolean compareStringEnchantments(ItemStack playerItem, String shopItem){ //Returns true if the enchantments given are the same; Returns false if there is any difference
		String shopString = shopItem;
		String playerString = enchantmentsToString(playerItem);
		main.messages.debugOut("Comparing Enchantments:");
		String[] shopBundled = shopString.split(";");
		String[] playerBundled = playerString.split(";");
		int shopLength=shopBundled.length;
		int playerLength=playerBundled.length;
		main.messages.debugOut("shopString"+"["+shopLength+"]: "+shopString);
		main.messages.debugOut("playerString"+"["+playerLength+"]: "+playerString);
		if(Integer.valueOf(shopLength)==0 && Integer.valueOf(playerLength)==0){
			return true;
		}else{
			if(Integer.valueOf(playerLength)==Integer.valueOf(shopLength)){
				for(int i=0; i<shopLength; i++){
					if(findMatchingString(shopBundled[i], playerBundled)==false){
						return false;
					}
				}
				return true;
			}
			return false;
		}
	}
	public Boolean findMatchingString(String findMe, String[] list){ //Goes through the entire list of strings to see if any entry is the same as the given string (findMe)
		int length = list.length;
		for(int i=0; i<length; i++){
			if(findMe.equalsIgnoreCase(list[i])){
				main.messages.debugOut(findMe+" is the same as "+list[i]);
				return true;
			}
			main.messages.debugOut(findMe+" is not the same as "+list[i]);
		}
		main.messages.debugOut("The enchantment '"+findMe+"' could not be found in this list");
		return false;
	}
	public String cleanEnchantmentName(String name){ //Formats the enchantment name to be more user-friendly for display -- names are changed in the config
		String returnMe = main.getConfig().getString(name);
		if(returnMe == null){
			return name;
		}else{
			return returnMe;
		}
	}
}
