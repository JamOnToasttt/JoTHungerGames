package me.jamontoast.jothungergames.Utilities;

import me.jamontoast.jothungergames.JoTHungerGames;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTable;

import java.util.*;

public class ChestUtilities {

    public Set<Location> getChestLocations(String arenaName, String chestType) {
        FileConfiguration config = JoTHungerGames.getInstance().getArenaConfig();
        if (chestType.contains("_refill")) {
            chestType = chestType.replaceAll("_refill", "");
        }
        String basePath = "arenas." + arenaName + ".chests." + chestType.toLowerCase() + ".locations";
        String worldName = config.getString("arenas." + arenaName + ".world");
        if (worldName == null) {
            Bukkit.getLogger().warning("World name is null in config for arena: " + arenaName);
            return null;
        }
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            Bukkit.getLogger().warning("World not found for arena: " + arenaName);
            return null;
        }


        ConfigurationSection chestLocationSection = config.getConfigurationSection(basePath);
        if (chestLocationSection == null) {
            Bukkit.getLogger().warning("No chest locations found in config for path: " + basePath);
            return null;
        }

        Set<Location> chestLocations = new HashSet<>();
        for (String key : chestLocationSection.getKeys(false)) {
            String[] coords = Objects.requireNonNull(config.getString(basePath + "." + key)).split(",");
            if (coords.length != 3) {
                Bukkit.getLogger().warning("Invalid chest coordinates at key: " + key + " in arena: " + arenaName);
                continue;
            }
            try {
                int x = Integer.parseInt(coords[0]);
                int y = Integer.parseInt(coords[1]);
                int z = Integer.parseInt(coords[2]);
                Location location = new Location(world, x, y, z);
                chestLocations.add(location);
            } catch (NumberFormatException e) {
                Bukkit.getLogger().warning("Invalid number format for coordinates at key: " + key + " in arena: " + arenaName);
            }
        }
        return chestLocations;
    }

    public List<Long> getRefillTimes(String arenaName, String type) {
        FileConfiguration config = JoTHungerGames.getInstance().getArenaConfig();
        List<Long> refillTimes = new ArrayList<>();

        String basePath = "arenas." + arenaName + ".chests." + type + ".refills";
        ConfigurationSection section = config.getConfigurationSection(basePath);
        if (section != null) {
            for (String key : section.getKeys(false)) {
                refillTimes.add(section.getLong(key) * 20); // Convert seconds to ticks
            }
        }
        return refillTimes;
    }

    public void refillChestsByType(String arenaName, String chestType) {
        ArenaManager arenaManager = ArenaManager.getInstance();
        ArenaData arenaData = arenaManager.getArenaData(arenaName);

        Set<Location> chestLocations = getChestLocations(arenaName, chestType);

        LootTable lootTable = null;
        boolean resetChests = false;

        switch (chestType.toLowerCase()) {
            case "center" -> {
                lootTable = JoTHungerGames.getInstance().getCenterLootTable();
                resetChests = true;
            }
            case "center_refill" -> {
                lootTable = JoTHungerGames.getInstance().getCenterRefillLootTable();
            }
            case "normal" -> {
                lootTable = JoTHungerGames.getInstance().getNormalLootTable();
                resetChests = true;
            }
            case "normal_refill" -> {
                lootTable = JoTHungerGames.getInstance().getNormalRefillLootTable();
            }
            case "prime" -> {
                lootTable = JoTHungerGames.getInstance().getPrimeLootTable();
                resetChests = true;
            }
            case "prime_refill" -> {
                lootTable = JoTHungerGames.getInstance().getPrimeRefillLootTable();
            }
        }

        if (lootTable == null) {
            Bukkit.getLogger().warning("Loot table is null for chest type: " + chestType);
            return;
        }

        for (Location chestLocation : chestLocations) {
            Block block = chestLocation.getBlock();
            if (!block.getType().equals(Material.CHEST)){
                block.setType(Material.CHEST);
            }
            if (block.getState() instanceof Chest chest) {
                if (resetChests) {
                    chest.getBlockInventory().clear();
                }
                refillChest(chest, lootTable);
            }
        }
        if (arenaData != null) {
            Set<UUID> gamePlayers = arenaData.getGamePlayers();
            boolean eventBroadcastActionbar = arenaData.isEventBroadcastActionbar();

            // *** ADD THIS DEBUG ***
            Bukkit.getLogger().info("=== REFILL DEBUG ===");
            Bukkit.getLogger().info("Arena Data: " + (arenaData != null ? "exists" : "null"));
            Bukkit.getLogger().info("Chest Type: " + chestType);
            Bukkit.getLogger().info("eventBroadcastActionbar: " + eventBroadcastActionbar);
            Bukkit.getLogger().info("gamePlayers size: " + (gamePlayers != null ? gamePlayers.size() : "null"));

            switch (chestType.toLowerCase()) {
                case "center" -> {
                    Bukkit.getLogger().info("Reset center chests");
                }
                case "center_refill" -> {
                    if (eventBroadcastActionbar) {
                        Bukkit.getLogger().info("Sending center refill messages...");
                        sendChestRefillMessages("Cornucopia chests have been refilled!", gamePlayers);
                    }else {
                        Bukkit.getLogger().info("Action bar is disabled!");
                    }
                    Bukkit.getLogger().info("Refilled center chests");
                }
                case "normal" -> {
                    Bukkit.getLogger().info("Reset normal chests");
                }
                case "normal_refill" -> {
                    if (eventBroadcastActionbar) {
                        Bukkit.getLogger().info("Sending normal refill messages...");
                        sendChestRefillMessages("Wild chests have been refilled!", gamePlayers);
                    }else {
                        Bukkit.getLogger().info("Action bar is disabled!");
                    }
                    Bukkit.getLogger().info("Refilled normal chests");
                }
                case "prime" -> {
                    Bukkit.getLogger().info("Reset prime chests");
                }
                case "prime_refill" -> {
                    if (eventBroadcastActionbar) {
                        Bukkit.getLogger().info("Sending prime refill messages...");
                        sendChestRefillMessages("Prime chests have been refilled!", gamePlayers);
                    }else {
                        Bukkit.getLogger().info("Action bar is disabled!");
                    }
                    Bukkit.getLogger().info("Refilled prime chests");
                }
            }
        }
    }

    private void sendChestRefillMessages(String message, Set<UUID> gamePlayers) {
        Bukkit.getLogger().info("=== SENDING ACTION BAR ===");
        Bukkit.getLogger().info("Message: " + message);
        Bukkit.getLogger().info("Players: " + gamePlayers.size());
        for (UUID uuid : gamePlayers) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline()) continue;

            Bukkit.getLogger().info("Sending to: " + player.getName());
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.YELLOW + message));
            player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 10.0f, 1.0f);
        }
    }
    private void refillChest(Chest chest, LootTable lootTable) {
        if (lootTable != null) {
            Inventory chestInventory = chest.getInventory();
            LootContext context = new LootContext.Builder(chest.getLocation()).build();

            //lootTable.fillInventory(chestInventory, new Random(), context);

            // Get loot items from the loot table
            Collection<ItemStack> loot = lootTable.populateLoot(new Random(), context);

            // Get all empty slot indices
            List<Integer> emptySlots = new ArrayList<>();
            for (int i = 0; i < chestInventory.getSize(); i++) {
                ItemStack item = chestInventory.getItem(i);
                if (item == null || item.getType() == Material.AIR) {
                    emptySlots.add(i);
                }
            }

            // Shuffle empty slots for random placement
            Collections.shuffle(emptySlots);

            // Add items to random empty slots
            int slotIndex = 0;
            for (ItemStack item : loot) {
                if (item != null && item.getType() != Material.AIR) {
                    if (slotIndex < emptySlots.size()) {
                        chestInventory.setItem(emptySlots.get(slotIndex), item);
                        slotIndex++;
                    } else {
                        // No more empty slots
                        break;
                    }
                }
            }

        } else {
            Bukkit.getLogger().warning("Loot table is null.");
        }
    }

}
