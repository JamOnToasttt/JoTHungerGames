package me.jamontoast.jothungergames.Tasks;

import me.jamontoast.jothungergames.JoTHungerGames;
import me.jamontoast.jothungergames.Utilities.ArenaData;
import me.jamontoast.jothungergames.Utilities.ArenaManager;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Set;
import java.util.UUID;

public class HighlightPlayersTask extends BukkitRunnable {

    private final String arenaName;

    public HighlightPlayersTask(String arenaName){
        this.arenaName = arenaName;
    }

    @Override
    public void run() {
        ArenaManager arenaManager = ArenaManager.getInstance();
        ArenaData arenaData = arenaManager.getArenaData(arenaName);
        Set<UUID> totalDeadPlayers = arenaData.getTotalDeadPlayers();
        Set<UUID> gamePlayers = arenaData.getGamePlayers();

        Bukkit.getLogger().info("Highlighting remaining players.");
        for (UUID uuid : gamePlayers) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline()) continue;

            if (!totalDeadPlayers.contains(player)) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, Integer.MAX_VALUE, 1));
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED + "You are now visible to your opponents!"));
            }
        }
    }
}

