# EtherMarket
This is a chestless shop plugin for Bukkit/Spigot/Cauldron. The shops are stored in a SQL database, and it's inventory, item descriptions, etc are all stored and retrieved from there. Players have the second line of the sign to write anything that they want, and the first time a shop is activated, the shop information is shown to the player instead of conducting a transaction. REQUIRES A UUID-COMPATIBLE VERSION OF VAULT (1.7.10 or later)


# TODO:
1) Implement alternative storage methods (SQLite and YAML)
2) Add a second config option that allows transactions to be logged, but not show up in console
3) Add ATM mechanics
4) Add WorldEdit Listener to prevent modification of shop blocks
5) Add support for multiworld economies
6) Create web portal that looks up shops and transactions
