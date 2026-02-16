package me.jamontoast.jothungergames.Tasks;

import me.jamontoast.jothungergames.Utilities.ArenaData;
import me.jamontoast.jothungergames.Utilities.ArenaManager;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

public class TeleportCountdownTask extends BukkitRunnable {

    private final Set<UUID> gamePlayers;
    private final Set<Location> spawnLocations;
    private final String arenaName;
    private int countdown;
    private final ArenaManager arenaManager = ArenaManager.getInstance();
    ArenaData arenaData;

    public TeleportCountdownTask(Set<UUID> gamePlayers, Set<Location> spawnLocations, String arenaName, int countdown) {
        this.gamePlayers = gamePlayers;
        this.spawnLocations = spawnLocations;
        this.arenaName = arenaName;
        this.countdown = countdown;
        arenaData = arenaManager.getArenaData(arenaName);
    }

    @Override
    public void run() {
        arenaData.setPlayerSpawnMap(new HashMap<>());

        if (countdown > 0) {
            // Broadcast countdown message
            for (UUID uuid : gamePlayers) {
                Player player = Bukkit.getPlayer(uuid);
                if (player == null || !player.isOnline()) continue;

                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.WHITE.toString() + ChatColor.BOLD + "Teleporting in " + ChatColor.YELLOW + countdown + ChatColor.WHITE + "..."));
            }
            countdown--;
        } else {
            for (UUID uuid : gamePlayers) {
                Player player = Bukkit.getPlayer(uuid);
                if (player == null || !player.isOnline()) continue;

                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(" "));
            }
            // Countdown finished, teleport players
            Iterator<Location> spawnLocIterator = spawnLocations.iterator();
            for (UUID uuid : gamePlayers) {
                Player player = Bukkit.getPlayer(uuid);
                if (player == null || !player.isOnline()) continue;

                if (spawnLocIterator.hasNext()) {
                    Location spawnLocation = spawnLocIterator.next();
                    arenaData.getPlayerSpawnMap().put(uuid, spawnLocation);
                    player.teleport(spawnLocation);
                }
                player.setHealth(20);
                player.setSaturation(10);
                player.setExp(0);
                player.setLevel(0);
                for (org.bukkit.potion.PotionEffect effect : player.getActivePotionEffects()) {
                    player.removePotionEffect(effect.getType());
                }
            }

            arenaManager.spawnLock(arenaName);
            cancel();
        }
    }
}
