package me.jamontoast.jothungergames.Tasks;

import me.jamontoast.jothungergames.Utilities.ArenaData;
import me.jamontoast.jothungergames.Utilities.ArenaManager;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameRule;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Set;

public class LocatorBarTask extends BukkitRunnable {

    private final String arenaName;

    public LocatorBarTask(String arenaName){
        this.arenaName = arenaName;
    }

    @Override
    public void run() {
        ArenaManager arenaManager = ArenaManager.getInstance();
        ArenaData arenaData = arenaManager.getArenaData(arenaName);
        arenaData.getArenaWorld().setGameRule(GameRule.LOCATOR_BAR, true);
        Bukkit.getLogger().info("Enabling locator bar.");
    }
}

