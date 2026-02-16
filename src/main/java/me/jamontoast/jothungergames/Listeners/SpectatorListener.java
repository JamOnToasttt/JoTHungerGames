package me.jamontoast.jothungergames.Listeners;

import me.jamontoast.jothungergames.JoTHungerGames;
import me.jamontoast.jothungergames.Utilities.*;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.List;

public class SpectatorListener implements Listener {
    private final String arenaName;
    private final ArenaManager arenaManager;
    private final ArenaData arenaData;

    public SpectatorListener(String arenaName) {
        this.arenaName = arenaName;
        this.arenaManager = ArenaManager.getInstance();
        this.arenaData = this.arenaManager.getArenaData(arenaName);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (!arenaData.getTotalDeadPlayers().contains(player.getUniqueId())) {
            return;
        }
        Block clickedBlock = event.getClickedBlock();

        // Allow chest viewing but prevent physical opening
        if (clickedBlock != null && clickedBlock.getType() == Material.CHEST) {

            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {

                event.setCancelled(true);
                Chest chest = (Chest) clickedBlock.getState();
                Inventory chestInventory = Bukkit.createInventory(null, chest.getInventory().getSize(),

                        ChatColor.GRAY + "Viewing Chest");

                chestInventory.setContents(chest.getInventory().getContents());
                player.openInventory(chestInventory);
            }
            return;
        }
        // Prevent all other block interactions for spectators

        if (clickedBlock != null) {
            Material type = clickedBlock.getType();
            // Check for interactive blocks

            if (type.name().contains("DOOR") ||
                    type.name().contains("GATE") ||
                    type.name().contains("TRAPDOOR") ||
                    type.name().contains("BUTTON") ||
                    type.name().contains("LEVER") ||
                    type.name().contains("PRESSURE_PLATE") ||
                    type == Material.TRIPWIRE) {

                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {

        Player player = event.getPlayer();

        if (arenaData.getTotalDeadPlayers().contains(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {

        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();

        if (!arenaData.getTotalDeadPlayers().contains(player.getUniqueId())) {
            return;
        }

        String title = event.getView().getTitle();
        if (title.contains("Viewing Chest")) {
            event.setCancelled(true);
            return;
        }

        ItemStack clicked = event.getCurrentItem();

        if (clicked != null) {
            Material type = clicked.getType();

            if (type == Material.COMPASS || type == Material.GOLD_INGOT ||
                    type == Material.IRON_INGOT || type == Material.COPPER_INGOT) {

                ItemMeta meta = clicked.getItemMeta();

                if (meta != null && meta.hasDisplayName()) {

                    String name = meta.getDisplayName();

                    if (name.contains("Player Menu") ||
                            name.contains("Gold:") ||
                            name.contains("Silver:") ||
                            name.contains("Copper:")) {

                        event.setCancelled(true);

                    }
                }
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (!arenaData.getGamePlayers().contains(player.getUniqueId())) {
            return;
        }
        if (arenaData.getTotalDeadPlayers().contains(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (!arenaData.getGamePlayers().contains(player.getUniqueId())) {
            return;
        }
        if (arenaData.getTotalDeadPlayers().contains(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player damager = (Player) event.getDamager();

            if (!arenaData.getGamePlayers().contains(damager.getUniqueId())) {
                return;
            }
            if (arenaData.getTotalDeadPlayers().contains(damager.getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onSwapHands(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();

        if (!arenaData.getGamePlayers().contains(player.getUniqueId())) {
            return;
        }
        if (arenaData.getTotalDeadPlayers().contains(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }

}