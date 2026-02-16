package me.jamontoast.jothungergames.Utilities;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SpectatorCurrency {

    private static final Map<UUID, Integer> playerCurrency = new HashMap<>();

    // Base currency + (kills * KILL_VALUE) + (survival_seconds * SURVIVAL_VALUE)
    private static final int BASE_CURRENCY = 10;
    private static final int KILL_VALUE = 50;
    private static final double SURVIVAL_VALUE = 0.83; // 1 gold per 2 minutes


    public static void calculateCurrency(Player player, int kills, long survivalTimeSeconds) {
        int killBonus = kills * KILL_VALUE;
        int survivalBonus = (int) Math.round(survivalTimeSeconds * SURVIVAL_VALUE);
        int total = BASE_CURRENCY + killBonus + survivalBonus;

        Bukkit.getLogger().info("[SpectatorCurrency] Player " + player.getName() + " died with " + kills + " kills, worth 5 iron each, and survived for " + survivalTimeSeconds + " seconds, worth 0.83 copper each." );

        playerCurrency.put(player.getUniqueId(), total);
    }


    public static int getCurrency(Player player) {
        return playerCurrency.getOrDefault(player.getUniqueId(), 0);
    }
    public static String getFormattedCurrency(Player player) {
        int total = getCurrency(player);
        return formatCurrency(total);
    }
    public static String getColoredFormattedCurrency(Player player) {
        int total = getCurrency(player);
        int gold = total / 100;
        int silver = (total % 100) / 10;
        int copper = total % 10;

        StringBuilder result = new StringBuilder();
        if (gold > 0) {
            result.append(ChatColor.GOLD).append(gold).append(" Gold");
        }
        if (silver > 0) {
            if (gold > 0) {
                result.append(ChatColor.GOLD).append(", ");
            }
            result.append(ChatColor.WHITE).append(silver).append(" Silver");
        }
        if (copper > 0 || (gold == 0 && silver == 0)) {
            if (silver > 0) {
                result.append(ChatColor.WHITE).append(", ");
            }
            result.append(net.md_5.bungee.api.ChatColor.of("#B87333")).append(copper).append(" Copper");
        }

        return result.toString().trim();
    }

    public static String formatCurrencyDisplay(int total) {

        int gold = total / 100;

        int silver = (total % 100) / 10;

        int copper = total % 10;



        StringBuilder result = new StringBuilder();

        if (gold > 0) {

            result.append(ChatColor.GOLD).append(gold).append(" Gold");

        }

        if (silver > 0) {

            if (gold > 0) {

                result.append(ChatColor.RESET).append(", ");

            }

            result.append(ChatColor.WHITE).append(silver).append(" Silver");

        }

        if (copper > 0 || (gold == 0 && silver == 0)) {

            if (silver > 0 || gold > 0) {

                result.append(ChatColor.RESET).append(", ");

            }

            result.append(net.md_5.bungee.api.ChatColor.of("#B87333")).append(copper).append(" Copper");

        }



        return result.toString().trim();

    }
    public static String formatCurrency(int total) {
        int gold = total / 100;
        int silver = (total % 100) / 10;
        int copper = total % 10;

        StringBuilder result = new StringBuilder();
        if (gold > 0) {
            result.append(gold).append(" Gold");
        }
        if (silver > 0) {
            if (gold > 0) {
                result.append(", ");
            }
            result.append(silver).append(" Silver");
        }
        if (copper > 0 || (gold == 0 && silver == 0)) {
            if (silver > 0) {
                result.append(", ");
            }
            result.append(copper).append(" Copper");
        }

        return result.toString().trim();
    }

    public static void addCurrency(Player player, int amount) {
        int current = getCurrency(player);
        playerCurrency.put(player.getUniqueId(), current + amount);
    }

    public static boolean removeCurrency(Player player, int amount) {
        int current = getCurrency(player);
        if (current >= amount) {
            playerCurrency.put(player.getUniqueId(), current - amount);
            return true;
        }
        return false;
    }

    public static boolean canAfford(Player player, int amount) {
        return getCurrency(player) >= amount;
    }

    public static void clearCurrency(Player player) {
        playerCurrency.remove(player.getUniqueId());
    }

    public static void clearAll() {
        playerCurrency.clear();
    }
}
