package me.jamontoast.jothungergames.Tasks;

import me.jamontoast.jothungergames.Utilities.ScoreboardBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

public class EventTimerTask extends BukkitRunnable {

    private final Set<UUID> gamePlayers;
    private TreeMap<Long, String> eventsMap;
    private long ticksElapsed = 0;


    public EventTimerTask(Set<UUID> gamePlayers, TreeMap<Long, String> eventsMap) {
        this.gamePlayers = gamePlayers;
        this.eventsMap = eventsMap;
    }

    @Override
    public void run() {
        ticksElapsed += 20;

        Map.Entry<Long, String> nextEvent = eventsMap.higherEntry(ticksElapsed);
        if (nextEvent != null) {
            long timeUntilNextEvent = nextEvent.getKey() - ticksElapsed;
            int secondsUntilNextEvent = (int) (timeUntilNextEvent / 20); // Convert ticks to seconds
            String formattedTime = formatTimeMinutesSeconds(secondsUntilNextEvent);

            for (UUID uuid : gamePlayers) {
                Player player = Bukkit.getPlayer(uuid);
                if (player == null || !player.isOnline()) continue;

                if (nextEvent.getValue().contains("refill")) {
                    ScoreboardBuilder.getOrCreate(player).setLine(5, ChatColor.GREEN + nextEvent.getValue() + formattedTime);
                }else {
                    ScoreboardBuilder.getOrCreate(player).setLine(5, ChatColor.GOLD + nextEvent.getValue() + formattedTime);
                }
            }
        } else {
            for (UUID uuid : gamePlayers) {
                Player player = Bukkit.getPlayer(uuid);
                if (player == null || !player.isOnline()) continue;

                ScoreboardBuilder.getOrCreate(player).setLine(5, "       ");
            }
            this.cancel(); // Stop the task if there are no more events
        }
    }

    private String formatTimeMinutesSeconds(int seconds) {
        int minutes = seconds / 60;
        int secs = seconds % 60;
        return String.format("%02d:%02d", minutes, secs);
    }
}
