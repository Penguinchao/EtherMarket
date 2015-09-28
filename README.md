# EtherMarket
This is a chestless shop plugin for Bukkit/Spigot/Cauldron. The shops are stored in a SQL database, and it's inventory, item descriptions, etc are all stored and retrieved from there. Players have the second line of the sign to write anything that they want, and the first time a shop is activated, the shop information is shown to the player instead of conducting a transaction. REQUIRES A UUID-COMPATIBLE VERSION OF VAULT (1.7.10 or later)


# TODO:
-Address bug where blocks are placed when the shop is clicked (dupe
cheat)
-On shop destruction, add inventory to the player, then drop the rest on
the ground (possibly make a config setting for always dropping on
ground)
-Add functionality to log transactions in the database
-Prevent the breaking of the block that a sign is attached to
-Add messages to the users (config)
-Add support for multiworld economies
-Possibly add ATM mechanics
