package me.jamontoast.jothungergames.Utilities;



import me.jamontoast.jothungergames.JoTHungerGames;

import me.jamontoast.jothungergames.Tasks.GameTimerTask;
import org.bukkit.Bukkit;

import org.bukkit.ChatColor;

import org.bukkit.entity.Player;

import org.bukkit.scoreboard.Objective;

import org.bukkit.scoreboard.Score;

import org.bukkit.scoreboard.Scoreboard;



import java.util.*;

import java.util.stream.Collectors;


public class EndGameScoreboard {


    public static void displayEndGameScoreboard(String arenaName, Player winner) {

        ArenaManager arenaManager = ArenaManager.getInstance();
        ArenaData arenaData = arenaManager.getArenaData(arenaName);

        if (arenaData == null) {
            return;
        }

        Set<UUID> allPlayers = new HashSet<>(arenaData.getGamePlayers());
        JoTHungerGames plugin = JoTHungerGames.getInstance();
        Objective killsObjective = plugin.getKillsObjective();
        if (killsObjective == null) {
            Bukkit.getLogger().warning("[EndGameScoreboard] Kills objective not found!");
            return;
        }

        List<KillerStats> topKillers = getTopKillers(allPlayers, killsObjective, 3);

        int seconds = arenaData.getGameTime();
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;
        String formattedTime = String.format("%02d:%02d:%02d", hours, minutes, secs);

        for (UUID uuid : allPlayers) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline()) continue;
            displayEndGameScoreboardToPlayer(player, winner, topKillers, arenaData, killsObjective, formattedTime);
        }

        announceResults(allPlayers, winner, topKillers, arenaName, killsObjective);
    }

    private static List<KillerStats> getTopKillers(Set<UUID> uuids, Objective killsObjective, int topN) {
        Map<String, Integer> killMap = new HashMap<>();

        for (UUID uuid : uuids) {
            Player player = Bukkit.getPlayer(uuid);

            if (player != null) {
                Score score = killsObjective.getScore(player.getName());
                int kills = score.getScore();

                if (kills > 0) {
                    killMap.put(player.getName(), kills);
                }
            }
        }

        // Sort by kills (descending) and return top N
        return killMap.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(topN)
                .map(entry -> new KillerStats(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }


    private static void displayEndGameScoreboardToPlayer(Player player, Player winner,
                                                         List<KillerStats> topKillers, ArenaData arenaData, Objective killsObjective, String formattedTime) {
        String winnerName = "None";
        if (winner != null) {
            winnerName = winner.getName();
        }

        ScoreboardBuilder builder = ScoreboardBuilder.getOrCreate(player)
                .setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "Hunger Games")
                .setLine(1, "Kills: " + ChatColor.RED + killsObjective.getScore(player.getName()).getScore())  // FIX: Added .getScore()
                .setLine(2, " ");

        int currentLine = 3;

        // Display top killers (only if they exist)
        if (topKillers.size() >= 3) {
            builder.setLine(currentLine++, topKillers.get(2).playerName + ChatColor.DARK_GRAY + " - " + ChatColor.RED + topKillers.get(2).kills);
            builder.setLine(currentLine++, "  ");
        }

        if (topKillers.size() >= 2) {
            builder.setLine(currentLine++, topKillers.get(1).playerName + ChatColor.DARK_GRAY + " - " + ChatColor.RED + topKillers.get(1).kills);
            builder.setLine(currentLine++, "   ");
        }

        if (topKillers.size() >= 1) {
            builder.setLine(currentLine++, topKillers.get(0).playerName + ChatColor.DARK_GRAY + " - " + ChatColor.RED + topKillers.get(0).kills);
            builder.setLine(currentLine++, "    ");
        }

        if (!topKillers.isEmpty()) {
            builder.setLine(currentLine++, ChatColor.RED + "Top killers:");
            builder.setLine(currentLine++, "     ");
        }

        builder.setLine(currentLine++, ChatColor.GOLD + "" + ChatColor.BOLD + "Winner: " + ChatColor.WHITE + (winnerName))
                .setLine(currentLine++, "      ")
                .setLine(currentLine++, "Game time: " + ChatColor.YELLOW + formattedTime)
                .setLine(currentLine++, "      ");
        builder.removeLine(7);

        player.setScoreboard(builder.build());
    }


    private static void announceResults(Set<UUID> uuids, Player winner,

                                        List<KillerStats> topKillers, String arenaName, Objective killsObjective) {

        String divider = ChatColor.GOLD + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬";

        for (UUID uuid : uuids) {
            Player player = Bukkit.getPlayer(uuid);

            if (player != null && player.isOnline()) {
                player.sendMessage("");
                player.sendMessage(divider);
                player.sendMessage("");
                player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "HUNGER GAMES - GAME OVER");
                player.sendMessage("");
                if (winner != null) {
                    player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "CONGRATULATIONS " + ChatColor.RESET +
                            winner.getName() + ChatColor.GOLD + " for winning with " + ChatColor.RED + killsObjective.getScore(winner.getName()) + ChatColor.GOLD + "kills!");
                }else {
                    player.sendMessage(ChatColor.RED + "Unfortunately, this Hunger Games had no winner.");
                }

                player.sendMessage("");

                if (!topKillers.isEmpty()) {
                    player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "TOP KILLERS:");
                    player.sendMessage("");

                    for (int i = 0; i < topKillers.size(); i++) {

                        KillerStats stats = topKillers.get(i);

                        if (stats != null) {
                            player.sendMessage("  " + ChatColor.DARK_GRAY + " - " +
                                    ChatColor.WHITE + stats.getPlayerName() +
                                    " " +
                                    ChatColor.RED + stats.getKills());
                        }
                    }
                }

                player.sendMessage("");
                player.sendMessage(divider);
                player.sendMessage("");
            }
        }
    }

    private static class KillerStats {

        private final String playerName;
        private final int kills;

        public KillerStats(String playerName, int kills) {
            this.playerName = playerName;
            this.kills = kills;
        }

        public String getPlayerName() {
            return playerName;
        }

        public int getKills() {
            return kills;
        }
    }
}