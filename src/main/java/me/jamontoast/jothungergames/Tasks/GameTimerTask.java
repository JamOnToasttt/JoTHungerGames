package me.jamontoast.jothungergames.Tasks;

import me.jamontoast.jothungergames.Utilities.ScoreboardBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Set;
import java.util.UUID;

public class GameTimerTask extends BukkitRunnable {

    private final Set<UUID> gamePlayers;
    private final boolean eventBroadcastSidebar;
    private int secondsElapsed;

    public GameTimerTask(Set<UUID> gamePlayers, boolean eventBroadcastSidebar) {
        this.gamePlayers = gamePlayers;
        this.eventBroadcastSidebar = eventBroadcastSidebar;
        secondsElapsed = 0;
    }

    @Override
    public void run() {
        secondsElapsed++;
        String formattedTime = formatTime(secondsElapsed);

        for (UUID uuid : gamePlayers) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline()) continue;

            if (eventBroadcastSidebar) {
                ScoreboardBuilder.getOrCreate(player).setLine(8, "Game time: " + ChatColor.YELLOW + formattedTime);
            }else {
                ScoreboardBuilder.getOrCreate(player).setLine(5, "Game time: " + ChatColor.YELLOW + formattedTime);
            }
        }
    }

    private String formatTime(int seconds) {
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }

    public int getArenaTime() {
        return secondsElapsed;
    }
}
