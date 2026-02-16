package me.jamontoast.jothungergames.Listeners;



import me.jamontoast.jothungergames.JoTHungerGames;

import me.jamontoast.jothungergames.Utilities.ArenaData;

import me.jamontoast.jothungergames.Utilities.ArenaManager;

import me.jamontoast.jothungergames.Utilities.ScoreboardBuilder;

import me.jamontoast.jothungergames.Utilities.SpectatorMenu;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import org.bukkit.event.EventHandler;

import org.bukkit.event.Listener;

import org.bukkit.event.player.PlayerJoinEvent;

import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.DisplaySlot;

import org.bukkit.scoreboard.Objective;

import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;


public class PlayerRejoinHandler implements Listener {

    private final String arenaName;
    private final ArenaManager arenaManager;
    private final ArenaData arenaData;

    public PlayerRejoinHandler(String arenaName) {
        this.arenaName = arenaName;
        this.arenaManager = ArenaManager.getInstance();
        this.arenaData = this.arenaManager.getArenaData(arenaName);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {

        Player player = event.getPlayer();

        if (!arenaData.getGamePlayers().contains(player.getUniqueId())) {
            return;
        }
        if (!arenaData.isActive()) {
            return;
        }

        restoreArenaScoreboard(player);

        if (arenaData.getTotalDeadPlayers().contains(player.getUniqueId())) {
            ensureSpectatorEffects(player);
        }

        player.setGameMode(GameMode.ADVENTURE);
    }

    private void restoreArenaScoreboard(Player player) {

        JoTHungerGames plugin = JoTHungerGames.getInstance();
        Objective killsObjective = plugin.getKillsObjective();


        if (arenaData.isEventBroadcastSidebar()) {
            player.setScoreboard(ScoreboardBuilder.getOrCreate(player)
                    .setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "Hunger Games")

                    .setLine(1, "Kills: " + ChatColor.RED + killsObjective.getScore(player.getName()).getScore())
                    .setLine(2, " ")
                    .setLine(3, "Players left: " + ChatColor.GREEN + arenaData.getAlivePlayers().size())
                    .setLine(4, "  ")
                    .setLine(5, "    ")
                    .setLine(6, "Next event:")
                    .setLine(7, "     ")
                    .setLine(8, "Game time: " + ChatColor.YELLOW + "00:00:00")
                    .setLine(9, "      ")
                    .build()
            );
        } else {
            player.setScoreboard(ScoreboardBuilder.getOrCreate(player)
                    .setDisplayName(ChatColor.BOLD.toString() + ChatColor.YELLOW + "Hunger Games")

                    .setLine(1, "Kills: " + ChatColor.RED + killsObjective.getScore(player.getName()).getScore())
                    .setLine(2, " ")
                    .setLine(3, "Players left: " + ChatColor.GREEN + arenaData.getAlivePlayers().size())
                    .setLine(4, "   ")
                    .setLine(5, "Game time: " + ChatColor.YELLOW + "00:00:00")
                    .setLine(6, "    ")
                    .build()
            );
        }

    }

    private void ensureSpectatorEffects(Player player) {
        player.setAllowFlight(true);
        player.setFlying(true);
        player.setCollidable(false);
        player.setCanPickupItems(false);
        player.setInvulnerable(true);

        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1));

        for (Player pl : Bukkit.getOnlinePlayers()) {
            pl.hidePlayer(JoTHungerGames.getInstance(), player);
        }

        if (arenaData.getHighlightPlayersTask() != null) {
            if (arenaData.getGameTime() >= arenaData.getHighlightStartTime()) {
                if (!player.getActivePotionEffects().contains(PotionEffectType.GLOWING)) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, Integer.MAX_VALUE, 1));
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED + "You are now visible to your opponents!"));
                }
            }
        }
    }

}