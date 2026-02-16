package me.jamontoast.jothungergames.Listeners;

import me.jamontoast.jothungergames.JoTHungerGames;
import me.jamontoast.jothungergames.Utilities.*;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.UUID;

public class DeathListener implements Listener {
    private final Location deathSpawn;
    private Player winner;
    private final String arenaName;
    private final ArenaManager arenaManager;
    private final ArenaData arenaData;

    public DeathListener(String arenaName) {
        this.arenaName = arenaName;
        this.arenaManager = ArenaManager.getInstance();
        this.arenaData = this.arenaManager.getArenaData(arenaName);
        this.winner = null;
        this.deathSpawn = this.arenaData.getArenaDeathSpawn();
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        JoTHungerGames plugin = JoTHungerGames.getInstance();
        Player player = event.getPlayer();

        Bukkit.getLogger().info("[DeathListener] PlayerRespawnEvent fired for " + player.getName());
        Bukkit.getLogger().info("[DeathListener] Current respawn location: " + event.getRespawnLocation());
        Bukkit.getLogger().info("[DeathListener] Is player in gamePlayers? " + arenaData.getGamePlayers().contains(player.getUniqueId()));
        Bukkit.getLogger().info("[DeathListener] GamePlayers list: " + arenaData.getGamePlayers());

        if (arenaData.getGamePlayers().contains(player.getUniqueId())) {
            if (deathSpawn != null) {
                event.setRespawnLocation(deathSpawn);
                Bukkit.getLogger().info("Setting respawn location for " + player.getName() + " to " + deathSpawn);
            } else {
                Bukkit.getLogger().warning("Death spawn location is null, using world spawn.");
                event.setRespawnLocation(player.getWorld().getSpawnLocation());
            }
            new BukkitRunnable() {
                @Override
                public void run() {
                    SpectatorMenu.giveSpectatorCompass(player);
                    player.setGameMode(GameMode.ADVENTURE);
                    player.setAllowFlight(true);
                    player.setFlying(true);
                    player.setCollidable(false);
                    player.setCanPickupItems(false);
                    player.setInvulnerable(true);

                    player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1));

                    for (Player pl: Bukkit.getOnlinePlayers()){
                        pl.hidePlayer(player);
                    }

                    Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
                    Team spectatorTeam = scoreboard.getTeam("Spectators");
                    if (spectatorTeam == null) {
                        spectatorTeam = scoreboard.registerNewTeam("Spectators");
                        spectatorTeam.setPrefix(ChatColor.GRAY.toString() + ChatColor.BOLD + "SPECTATOR " + ChatColor.GRAY);
                        spectatorTeam.setCanSeeFriendlyInvisibles(true);
                    }
                    spectatorTeam.addEntry(player.getName());

                    Bukkit.getLogger().info("Set " + player.getName() + " to spectator");
                    /*if (deathSpawn != null) {
                        player.teleport(deathSpawn);
                    } else {
                        Bukkit.getLogger().warning("Death spawn location is null, cannot teleport player.");
                        // Optionally teleport to a fallback location
                        player.teleport(player.getWorld().getSpawnLocation());
                    }
                     */

                }
            }.runTaskLater(plugin, 2L); // 2 tick delay to ensure respawn completes
        }else {
            Bukkit.getLogger().warning(player.getName() + " not in game players list during respawn");
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        JoTHungerGames plugin = JoTHungerGames.getInstance();
        Player victim = event.getEntity();
        Player killer = victim.getKiller();
        Bukkit.getLogger().info("[DeathListener] PlayerDeathEvent fired for " + victim.getName());
        Bukkit.getLogger().info("[DeathListener] Is victim in gamePlayers? " + arenaData.getGamePlayers().contains(victim.getUniqueId()));
        Bukkit.getLogger().info(victim + " has died.");

        if (arenaData.getGamePlayers().contains(victim.getUniqueId())) {
            Bukkit.getLogger().info("[DeathListener] Processing death for " + victim.getName());
            arenaManager.addDeadPlayer(victim, arenaName);
            arenaData.recordPlayerDeath(victim);


            int kills = plugin.getKillsObjective().getScore(victim.getName()).getScore();
            int survivalTime = arenaData.getPlayerSurvivalTime(victim);
            SpectatorCurrency.calculateCurrency(victim, kills, survivalTime);

            Bukkit.getLogger().info("[DeathListener] Currency calculated: " + SpectatorCurrency.getCurrency(victim));

            for (UUID uuid : arenaData.getGamePlayers()) {
                Player player = Bukkit.getPlayer(uuid);
                if (player == null || !player.isOnline()) continue;
                ScoreboardBuilder.getOrCreate(player).setLine(3, "Players left: " + ChatColor.GREEN + (arenaData.getAlivePlayers().size()));
            }

            if (killer != null && arenaData.getGamePlayers().contains(killer.getUniqueId())) {
                Score score = plugin.getKillsObjective().getScore(killer.getName());
                int newScore = score.getScore() + 1;
                score.setScore(newScore);

                ScoreboardBuilder.getOrCreate(killer)
                        .setLine(1, "Kills: " + ChatColor.RED + newScore);
            }

            new BukkitRunnable() {
                @Override
                public void run() {
                    for (UUID uuid : arenaData.getGamePlayers()) {
                        Player player = Bukkit.getPlayer(uuid);
                        if (player == null || !player.isOnline()) continue;
                        player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST_FAR, 45.0f, 0.3f);
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.GRAY.toString() + ChatColor.BOLD + "A player has died"));
                    }
                }
            }.runTaskLater(plugin, 20L); // 10 ticks delay (approximately 1 second)

            Bukkit.getLogger().info("[DeathListener] Death processing complete for " + victim.getName());

            /*new BukkitRunnable() {
                @Override
                public void run() {
                    victim.spigot().respawn();

                }
            }.runTaskLater(plugin, 1L); // 1 ticks delay (approximately 1/20 second)*/

            /*if (arenaData.getAlivePlayers().size() == 1 || (arenaData.getTotalDeadPlayers().size() + 1) == arenaData.getGamePlayers().size()) {
                for (UUID uuid : arenaData.getGamePlayers()) {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player == null || !player.isOnline()) continue;

                    if (!arenaData.getTotalDeadPlayers().contains(player)) {
                        winner = player;
                    }
                    arenaManager.handleGameWin(winner, arenaName);
                }
            }
            if (arenaData.getAlivePlayers().isEmpty() || arenaData.getTotalDeadPlayers().size() == arenaData.getGamePlayers().size()) {
                arenaManager.handleGameWin(null, arenaName);
            }*/
        }
    }
}