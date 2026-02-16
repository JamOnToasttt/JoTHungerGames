package me.jamontoast.jothungergames.Utilities;



import me.jamontoast.jothungergames.JoTHungerGames;

import org.bukkit.Bukkit;

import org.bukkit.ChatColor;

import org.bukkit.Material;

import org.bukkit.Sound;

import org.bukkit.entity.Player;

import org.bukkit.event.EventHandler;

import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import org.bukkit.event.block.Action;

import org.bukkit.event.inventory.InventoryClickEvent;

import org.bukkit.event.inventory.InventoryCloseEvent;


import org.bukkit.event.player.PlayerInteractEvent;


import org.bukkit.inventory.Inventory;

import org.bukkit.inventory.ItemStack;

import org.bukkit.inventory.meta.ItemMeta;

import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;


import java.util.*;



public class SpectatorMenu implements Listener {



    private static final String COMPASS_NAME = ChatColor.GREEN + "Player Menu";

    // Track which inventory type the player is viewing

    private static final Map<UUID, String> currentMenu = new HashMap<>(); // "player_select", "action", "shop", "category"

    private static final Map<UUID, Player> selectedTarget = new HashMap<>();

    private static final Map<UUID, String> currentCategory = new HashMap<>();

    private static final Map<UUID, Integer> currentPage = new HashMap<>(); // For pagination



    private static final int PLAYERS_PER_PAGE = 9; // 2 rows × 9 slots, leaving bottom row for navigation



    public SpectatorMenu() {

        Bukkit.getPluginManager().registerEvents(this, JoTHungerGames.getInstance());

        Bukkit.getLogger().info("[SpectatorMenu] Event listeners registered!");

    }


    public static void giveSpectatorCompass(Player player) {

        ItemStack compass = new ItemStack(Material.COMPASS);

        ItemMeta meta = compass.getItemMeta();

        if (meta != null) {

            meta.setDisplayName(COMPASS_NAME);

            List<String> lore = new ArrayList<>();

            lore.add(ChatColor.GRAY + "Right-click to open menu");

            meta.setLore(lore);

            compass.setItemMeta(meta);

        }



        player.getInventory().clear();

        player.getInventory().setItem(0, compass);

        updateCurrencyDisplay(player);

    }



    public static void removeSpectator(Player player) {

        currentMenu.remove(player.getUniqueId());

        selectedTarget.remove(player.getUniqueId());

        currentCategory.remove(player.getUniqueId());

        currentPage.remove(player.getUniqueId());

    }



    private static void updateCurrencyDisplay(Player player) {

        int total = SpectatorCurrency.getCurrency(player);

        int gold = total / 100;

        int silver = (total % 100) / 10;

        int copper = total % 10;



        // Gold (slot 7)

        ItemStack goldStack = new ItemStack(Material.GOLD_INGOT, Math.max(1, gold));

        ItemMeta goldMeta = goldStack.getItemMeta();

        if (goldMeta != null) {

            if (gold <= 0) {
                goldMeta.setDisplayName(ChatColor.RED + "Gold: " + gold);
            }else {
                goldMeta.setDisplayName(ChatColor.GOLD + "Gold: " + gold);
            }

            goldStack.setItemMeta(goldMeta);

        }

        player.getInventory().setItem(6, goldStack);



        // Iron (slot 8)

        ItemStack silverStack = new ItemStack(Material.IRON_INGOT, Math.max(1, silver));

        ItemMeta silverMeta = silverStack.getItemMeta();

        if (silverMeta != null) {

            if (silver <= 0) {
                silverMeta.setDisplayName(ChatColor.RED + "Silver: " + silver);
            }else {
                silverMeta.setDisplayName(ChatColor.WHITE + "Silver: " + silver);
            }

            silverStack.setItemMeta(silverMeta);

        }

        player.getInventory().setItem(7, silverStack);



        // Copper (slot 9)

        ItemStack copperStack = new ItemStack(Material.COPPER_INGOT, Math.max(1, copper));

        ItemMeta copperMeta = copperStack.getItemMeta();

        if (copperMeta != null) {

            if (copper <= 0) {
                copperMeta.setDisplayName(ChatColor.RED + "Copper: " + copper);
            }else {
                copperMeta.setDisplayName(net.md_5.bungee.api.ChatColor.of("#B87333") + "Copper: " + copper);
            }

            copperStack.setItemMeta(copperMeta);

        }

        player.getInventory().setItem(8, copperStack);

    }



    public static void openPlayerSelectMenu(Player spectator, String arenaName) {

        openPlayerSelectMenu(spectator, arenaName, 1);

    }



    public static void openPlayerSelectMenu(Player spectator, String arenaName, int page) {

        ArenaManager arenaManager = ArenaManager.getInstance();

        ArenaData arenaData = arenaManager.getArenaData(arenaName);



        if (arenaData == null) {

            spectator.sendMessage(ChatColor.RED + "Arena not found!");

            return;

        }



        Set<UUID> alivePlayersSet = arenaData.getAlivePlayers();



        if (alivePlayersSet.isEmpty()) {

            spectator.sendMessage(ChatColor.RED + "No alive players!");

            return;

        }



        List<UUID> alivePlayers = new ArrayList<>(alivePlayersSet);

        int totalPlayers = alivePlayers.size();

        int totalPages = (int) Math.ceil((double) totalPlayers / PLAYERS_PER_PAGE);



        // Validate page number

        if (page < 1) page = 1;

        if (page > totalPages) page = totalPages;



        // Create chest GUI (2 rows = 18 slots)

        Inventory gui = Bukkit.createInventory(null, 18, ChatColor.DARK_GRAY + "Select a Player (" + page + "/" + totalPages + ")");



        // Calculate player range for this page

        int startIndex = (page - 1) * PLAYERS_PER_PAGE;

        int endIndex = Math.min(startIndex + PLAYERS_PER_PAGE, totalPlayers);



        // Add player heads (slots 0-8 in top row)

        int slot = 0;

        for (int i = startIndex; i < endIndex; i++) {


            Player alivePlayer = Bukkit.getPlayer(alivePlayers.get(i));
            if (alivePlayer == null || !alivePlayer.isOnline()) continue;

            ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);

            SkullMeta skullMeta = (SkullMeta) playerHead.getItemMeta();



            if (skullMeta != null) {

                skullMeta.setOwningPlayer(alivePlayer);

                skullMeta.setDisplayName(ChatColor.GREEN + alivePlayer.getName());



                List<String> lore = new ArrayList<>();

                lore.add(ChatColor.GRAY + "Click to select");

                lore.add(ChatColor.YELLOW + "Health: " + Math.round(alivePlayer.getHealth()) + "/" + Math.round(alivePlayer.getMaxHealth()));

                skullMeta.setLore(lore);



                playerHead.setItemMeta(skullMeta);

            }



            gui.setItem(slot++, playerHead);

        }



        // Previous page button (slot 9, bottom left) - only if not on first page

        if (page > 1) {

            ItemStack prevButton = new ItemStack(Material.ARROW);

            ItemMeta prevMeta = prevButton.getItemMeta();

            if (prevMeta != null) {

                prevMeta.setDisplayName(ChatColor.YELLOW + "← Previous");

                prevButton.setItemMeta(prevMeta);

            }

            gui.setItem(9, prevButton);

        }



        // Next page button (slot 17, bottom right) - only if more pages exist

        if (page < totalPages) {

            ItemStack nextButton = new ItemStack(Material.ARROW);

            ItemMeta nextMeta = nextButton.getItemMeta();

            if (nextMeta != null) {

                nextMeta.setDisplayName(ChatColor.YELLOW + "Next →");

                nextButton.setItemMeta(nextMeta);

            }

            gui.setItem(17, nextButton);

        }



        currentMenu.put(spectator.getUniqueId(), "player_select");

        currentPage.put(spectator.getUniqueId(), page);

        spectator.openInventory(gui);

    }


    private static void openActionMenu(Player spectator, Player target) {

        selectedTarget.put(spectator.getUniqueId(), target);



        Inventory gui = Bukkit.createInventory(null, 27, ChatColor.DARK_GRAY + "Actions: " + ChatColor.GOLD + target.getName());



        // Teleport option (slot 11)

        ItemStack teleportItem = new ItemStack(Material.ENDER_PEARL);

        ItemMeta teleportMeta = teleportItem.getItemMeta();

        if (teleportMeta != null) {

            teleportMeta.setDisplayName(ChatColor.AQUA + "Teleport to " + target.getName());

            List<String> lore = new ArrayList<>();

            lore.add(ChatColor.GRAY + "Click to spectate this player");

            teleportMeta.setLore(lore);

            teleportItem.setItemMeta(teleportMeta);

        }

        gui.setItem(11, teleportItem);



        // Sponsor option (slot 15)

        ItemStack sponsorItem = new ItemStack(Material.GOLD_BLOCK);

        ItemMeta sponsorMeta = sponsorItem.getItemMeta();

        if (sponsorMeta != null) {

            sponsorMeta.setDisplayName(ChatColor.GOLD + "Sponsor " + target.getName());

            List<String> lore = new ArrayList<>();

            lore.add(ChatColor.GRAY + "Send items to this player");

            lore.add(ChatColor.GOLD + "Your balance: " + SpectatorCurrency.getColoredFormattedCurrency(spectator));

            sponsorMeta.setLore(lore);

            sponsorItem.setItemMeta(sponsorMeta);

        }

        gui.setItem(15, sponsorItem);



        // Back button (slot 22)

        addBackButton(gui, 22);



        currentMenu.put(spectator.getUniqueId(), "action");

        spectator.openInventory(gui);

    }


    private static void openSponsorShop(Player spectator) {

        Inventory gui = Bukkit.createInventory(null, 27, ChatColor.DARK_GRAY + "Sponsor Shop");



        // Food (slot 10)

        ItemStack foodItem = new ItemStack(Material.ENCHANTED_GOLDEN_APPLE);

        ItemMeta foodMeta = foodItem.getItemMeta();

        if (foodMeta != null) {

            foodMeta.setDisplayName(ChatColor.YELLOW + "Food");

            List<String> lore = new ArrayList<>();

            lore.add(ChatColor.GRAY + "Browse food items");

            foodMeta.setLore(lore);

            foodItem.setItemMeta(foodMeta);

        }

        gui.setItem(10, foodItem);



        // Potions (slot 12)

        ItemStack potionItem = new ItemStack(Material.POTION);
        PotionMeta potionMeta = (PotionMeta) potionItem.getItemMeta();

        if (potionMeta != null) {

            potionMeta.addCustomEffect(new PotionEffect(PotionEffectType.REGENERATION, 45, 1), true);

            potionMeta.setDisplayName(ChatColor.YELLOW + "Potions");

            List<String> lore = new ArrayList<>();

            lore.add(ChatColor.GRAY + "Browse potions");

            potionMeta.setLore(lore);

            potionItem.setItemMeta(potionMeta);

        }

        gui.setItem(12, potionItem);



        // Weapons (slot 14)

        ItemStack weaponItem = new ItemStack(Material.DIAMOND_AXE);

        ItemMeta weaponMeta = weaponItem.getItemMeta();

        if (weaponMeta != null) {

            weaponMeta.setDisplayName(ChatColor.YELLOW + "Weapons");

            List<String> lore = new ArrayList<>();

            lore.add(ChatColor.GRAY + "Browse weapons");

            weaponMeta.setLore(lore);

            weaponItem.setItemMeta(weaponMeta);

        }

        gui.setItem(14, weaponItem);



        // Utility (slot 16)

        ItemStack armorItem = new ItemStack(Material.DIAMOND_CHESTPLATE);

        ItemMeta armorMeta = armorItem.getItemMeta();

        if (armorMeta != null) {

            armorMeta.setDisplayName(ChatColor.YELLOW + "Armor");

            List<String> lore = new ArrayList<>();

            lore.add(ChatColor.GRAY + "Browse armor");

            armorMeta.setLore(lore);

            armorItem.setItemMeta(armorMeta);

        }

        gui.setItem(16, armorItem);



        // Back button (slot 22)

        addBackButton(gui, 22);



        currentMenu.put(spectator.getUniqueId(), "shop");

        spectator.openInventory(gui);

    }


    private static void openCategoryShop(Player spectator, String category) {

        currentCategory.put(spectator.getUniqueId(), category);



        List<SponsorShop.ShopItem> items = SponsorShop.getItemsByCategory(category);



        int rows = (int) Math.ceil((items.size() + 1) / 9.0); // +1 for back button

        rows = Math.min(Math.max(rows, 3), 6); // Min 3 rows, max 6 rows



        Inventory gui = Bukkit.createInventory(null, rows * 9, ChatColor.DARK_GRAY + category);



        // Add items to inventory

        for (int i = 0; i < items.size() && i < 45; i++) {

            gui.setItem(i, items.get(i).getDisplayItem());

        }



        // Back button (bottom right)

        addBackButton(gui, (rows * 9) - 5);



        currentMenu.put(spectator.getUniqueId(), "category");

        spectator.openInventory(gui);

    }



    private static void addBackButton(Inventory gui, int slot) {

        ItemStack backItem = new ItemStack(Material.ARROW);

        ItemMeta backMeta = backItem.getItemMeta();

        if (backMeta != null) {

            backMeta.setDisplayName(ChatColor.RED + "Go Back");

            backItem.setItemMeta(backMeta);

        }

        gui.setItem(slot, backItem);

    }



    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)

    public void onCompassUse(PlayerInteractEvent event) {

        Player player = event.getPlayer();

        ItemStack item = event.getItem();


        // Handle compass menu opening

        if (item != null && item.getType() == Material.COMPASS) {

                if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {


                    ItemMeta meta = item.getItemMeta();

                    if (meta != null && COMPASS_NAME.equals(meta.getDisplayName())) {

                        event.setCancelled(true);


                        Bukkit.getLogger().info("[SpectatorMenu] Compass clicked by " + player.getName());


                        ArenaManager arenaManager = ArenaManager.getInstance();

                        String arenaName = arenaManager.getArenaForPlayer(player);


                        if (arenaName != null) {

                            Bukkit.getLogger().info("[SpectatorMenu] Opening menu for arena: " + arenaName);

                            openPlayerSelectMenu(player, arenaName);

                        } else {

                            Bukkit.getLogger().warning("[SpectatorMenu] Player " + player.getName() + " not in any arena!");

                            player.sendMessage(ChatColor.RED + "You are not in an active game!");

                        }

                        return;

                    }

                }
        }
    }



    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {

        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }



        Player player = (Player) event.getWhoClicked();
        String menuType = currentMenu.get(player.getUniqueId());



        // If player is in a custom menu, cancel ALL clicks (in both top and bottom inventory)

        if (menuType != null) {

            event.setCancelled(true);



            // Check if they clicked the view's top inventory (our custom GUI)

            if (event.getClickedInventory() == null ||
                    !event.getClickedInventory().equals(event.getView().getTopInventory())) {
                return; // Clicked in player's own inventory or outside, just cancel and return
            }
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) {
                return;
            }
            ItemMeta meta = clicked.getItemMeta();
            if (meta == null) return;
            String displayName = meta.getDisplayName();
            Bukkit.getLogger().info("[SpectatorMenu] Menu: " + menuType + ", Clicked: " + displayName + ", Material: " + clicked.getType());



            // Handle pagination arrows in player select menu
            if (menuType.equals("player_select")) {

                if (clicked.getType() == Material.ARROW) {

                    if (displayName.contains("Previous")) {

                        // Go to previous page

                        int page = currentPage.getOrDefault(player.getUniqueId(), 1);

                        ArenaManager arenaManager = ArenaManager.getInstance();

                        String arenaName = arenaManager.getArenaForPlayer(player);

                        if (arenaName != null) {

                            openPlayerSelectMenu(player, arenaName, page - 1);

                        }

                        return;

                    } else if (displayName.contains("Next")) {

                        // Go to next page

                        int page = currentPage.getOrDefault(player.getUniqueId(), 1);

                        ArenaManager arenaManager = ArenaManager.getInstance();

                        String arenaName = arenaManager.getArenaForPlayer(player);

                        if (arenaName != null) {

                            openPlayerSelectMenu(player, arenaName, page + 1);

                        }

                        return;

                    }

                }

            }



            // Handle back button

            if (clicked.getType() == Material.ARROW && displayName.equals(ChatColor.RED + "Go Back")) {

                handleBackButton(player, menuType);

                return;

            }



            switch (menuType) {

                case "player_select":

                    if (clicked.getType() == Material.PLAYER_HEAD) {
                        SkullMeta skullMeta = (SkullMeta) meta;

                        if (skullMeta.getOwningPlayer() != null) {
                            Player target = skullMeta.getOwningPlayer().getPlayer();

                            if (target != null && target.isOnline()) {
                                openActionMenu(player, target);

                            } else {
                                player.sendMessage(ChatColor.RED + "That player is no longer online!");
                                player.closeInventory();
                            }
                        }
                    }
                    break;

                case "action":

                    if (clicked.getType() == Material.ENDER_PEARL) {
                        Player target = selectedTarget.get(player.getUniqueId());

                        if (target != null && target.isOnline()) {
                            player.teleport(target);
                            player.sendMessage(ChatColor.GREEN + "Teleported to " + target.getName());
                            player.closeInventory();

                        } else {
                            player.sendMessage(ChatColor.RED + "Target is no longer online!");
                            player.closeInventory();
                        }

                    } else if (clicked.getType() == Material.GOLD_BLOCK) {
                        // Open sponsor shop
                        openSponsorShop(player);
                    }
                    break;



                case "shop":

                    if (displayName.equals(ChatColor.YELLOW + "Food")) {
                        openCategoryShop(player, "Food");

                    } else if (displayName.equals(ChatColor.YELLOW + "Potions")) {
                        openCategoryShop(player, "Potions");

                    } else if (displayName.equals(ChatColor.YELLOW + "Weapons")) {
                        openCategoryShop(player, "Weapons");

                    } else if (displayName.equals(ChatColor.YELLOW + "Armor")) {
                        openCategoryShop(player, "Armor");
                    }
                    break;

                case "category":

                    if (meta.hasLore()) {
                        List<String> lore = meta.getLore();

                        if (!lore.isEmpty() && ChatColor.stripColor(lore.get(0)).startsWith("Cost: ")) {
                            handlePurchase(player, clicked, meta);
                        }
                    }
                    break;
            }
        }
    }



    private void handleBackButton(Player player, String currentMenuType) {

        ArenaManager arenaManager = ArenaManager.getInstance();

        String arenaName = arenaManager.getArenaForPlayer(player);



        switch (currentMenuType) {

            case "action":

                // Go back to player select

                if (arenaName != null) {

                    int page = currentPage.getOrDefault(player.getUniqueId(), 1);

                    openPlayerSelectMenu(player, arenaName, page);

                }

                break;

            case "shop":

                // Go back to action menu

                Player target = selectedTarget.get(player.getUniqueId());

                if (target != null) {

                    openActionMenu(player, target);

                }

                break;

            case "category":

                // Go back to shop

                openSponsorShop(player);

                break;

            default:

                player.closeInventory();

                break;

        }

    }



    private void handlePurchase(Player player, ItemStack clicked, ItemMeta meta) {

        Player target = selectedTarget.get(player.getUniqueId());



        if (target == null || !target.isOnline()) {

            player.sendMessage(ChatColor.RED + "Target player is no longer online!");

            player.closeInventory();

            return;

        }


        List<String> lore = meta.getLore();

        String costLine = ChatColor.stripColor(lore.get(0)).replace("Cost: ", "").trim();



        // Parse cost from format: "1 Gold, 2 Silver, 3 Copper" or "1g 2i 3c"

        int cost = 0;



        // Split by comma first to handle "1 Gold, 2 Silver, 3 Copper" format

        String[] currencyParts = costLine.split(",");

        for (String currencyPart : currencyParts) {

            currencyPart = currencyPart.trim();

            String[] parts = currencyPart.split(" ");



            if (parts.length >= 2) {

                try {

                    int amount = Integer.parseInt(parts[0]);

                    String type = parts[1].toLowerCase();



                    if (type.equals("gold") || type.equals("g")) {

                        cost += amount * 100;

                    } else if (type.equals("silver") || type.equals("s")) {

                        cost += amount * 10;

                    } else if (type.equals("copper") || type.equals("c")) {

                        cost += amount;

                    }

                } catch (NumberFormatException e) {

                    // Skip if parsing fails

                }

            } else if (parts.length == 1) {

                // Handle old short format "1g", "2i", "3c"
                String part = parts[0];

                if (part.endsWith("g")) {
                    cost += Integer.parseInt(part.substring(0, part.length() - 1)) * 100;
                } else if (part.endsWith("i")) {
                    cost += Integer.parseInt(part.substring(0, part.length() - 1)) * 10;
                } else if (part.endsWith("c")) {
                    cost += Integer.parseInt(part.substring(0, part.length() - 1));
                }
            }
        }

        if (!SpectatorCurrency.canAfford(player, cost)) {
            player.sendMessage(ChatColor.RED + "You cannot afford this!");
            player.sendMessage(ChatColor.RED + "Cost: " + SpectatorCurrency.formatCurrencyDisplay(cost));
            player.sendMessage(ChatColor.GOLD + "Your balance: " + SpectatorCurrency.getColoredFormattedCurrency(player));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);

            return;
        }

        SpectatorCurrency.removeCurrency(player, cost);

        ItemStack giveItem = clicked.clone();
        ItemMeta giveMeta = giveItem.getItemMeta();

        if (giveMeta != null) {
            giveMeta.setLore(null);
            giveItem.setItemMeta(giveMeta);
        }

        HashMap<Integer, ItemStack> leftover = target.getInventory().addItem(giveItem);

        if (!leftover.isEmpty()) {

            for (ItemStack item : leftover.values()) {
                target.getWorld().dropItemNaturally(target.getLocation(), item);
            }
            target.sendMessage(ChatColor.YELLOW + "Your inventory is full! Some items were dropped nearby.");
        }

        String itemName = meta.getDisplayName();

        target.sendTitle(
                ChatColor.GOLD + "SPONSORED!",
                ChatColor.YELLOW + player.getName() + ChatColor.GREEN + " sent you " + ChatColor.stripColor(itemName),
                10, 70, 20
        );

        target.playSound(target.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
        player.sendMessage(ChatColor.GREEN + "Successfully sponsored " + ChatColor.stripColor(itemName) + " to " + target.getName() + "!");
        player.sendMessage(ChatColor.GOLD + "Remaining balance: " + SpectatorCurrency.getColoredFormattedCurrency(player));
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);

        updateCurrencyDisplay(player);
        String category = currentCategory.get(player.getUniqueId());

        if (category != null) {
            openCategoryShop(player, category);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {

        if (event.getPlayer() instanceof Player) {
            Player player = (Player) event.getPlayer();
            // Clean up menu tracking when they close
            // Only clean up menu tracking if they're actually closing the menu
            // Check after a short delay to see if a new menu was opened

            new org.bukkit.scheduler.BukkitRunnable() {

                @Override
                public void run() {
                    // If player doesn't have an open inventory, they truly closed the menu
                    if (player.getOpenInventory().getTopInventory().getSize() == player.getInventory().getSize()) {
                        currentMenu.remove(player.getUniqueId());
                        Bukkit.getLogger().info("[SpectatorMenu] Cleaned up menu tracking for " + player.getName());
                    }
                }
            }.runTaskLater(JoTHungerGames.getInstance(), 1L);
        }
    }
}