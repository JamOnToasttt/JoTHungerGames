package me.jamontoast.jothungergames.Tasks;

import me.jamontoast.jothungergames.Utilities.ArenaData;
import me.jamontoast.jothungergames.Utilities.ArenaManager;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Set;
import java.util.UUID;

public class BorderShrinkTask extends BukkitRunnable {
    private final String arenaName;
    private final WorldBorder border;
    private final double borderRadius;
    private final long borderDuration;
    private final boolean eventBroadcastActionbar;

    public BorderShrinkTask(String arenaName, WorldBorder border, double borderRadius, long borderDuration, boolean eventBroadcastActionbar) {
        this.arenaName = arenaName;
        this.border = border;
        this.borderRadius = borderRadius;
        this.borderDuration = borderDuration;
        this.eventBroadcastActionbar = eventBroadcastActionbar;
    }

    @Override
    public void run() {
        ArenaManager arenaManager = ArenaManager.getInstance();
        ArenaData arenaData = arenaManager.getArenaData(arenaName);
        Set<UUID> gamePlayers = arenaData.getGamePlayers();

        Bukkit.getLogger().info("Shrinking border.");
        if (eventBroadcastActionbar) {
            for (UUID uuid : gamePlayers) {
                Player player = Bukkit.getPlayer(uuid);
                if (player == null || !player.isOnline()) continue;

                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED + "The border has begun to shrink!"));
            }
        }
        border.setSize(borderRadius, borderDuration);
    }
}