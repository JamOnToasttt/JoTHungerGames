package me.jamontoast.jothungergames.Tasks;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class SpawnLockMessageTask extends BukkitRunnable {

    private final Set<UUID> gamePlayers;
    private int restrictionSeconds;

    public SpawnLockMessageTask(Set<UUID> gamePlayers, int restrictionTime) {

        this.gamePlayers = gamePlayers;
        restrictionSeconds = restrictionTime; // Time to keep players at spawn in seconds
    }

    @Override
    public void run() {
        if (restrictionSeconds > 0) {
            for (UUID uuid : gamePlayers) {
                Player player = Bukkit.getPlayer(uuid);
                if (player == null || !player.isOnline()) continue;

                player.sendTitle(
                        ChatColor.GOLD.toString() + ChatColor.BOLD + "Game Starts In:",
                        ChatColor.RED.toString() + ChatColor.BOLD + restrictionSeconds,
                        0, 21, 10
                );
                player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, 45.0f, 0.6f);
            }
            restrictionSeconds--;
        } else {
            for (UUID uuid : gamePlayers) {
                Player player = Bukkit.getPlayer(uuid);
                if (player == null || !player.isOnline()) continue;

                player.sendTitle(ChatColor.GOLD.toString() + ChatColor.BOLD + "START!",
                        " ",
                        5, 30, 10);

                player.playSound(player.getLocation(), Sound.ENTITY_WITHER_DEATH, 10.0f, 1.0f);

            }
            // End the restriction
            cancel();
        }
    }
}
