package me.jamontoast.jothungergames.Tasks;

import me.jamontoast.jothungergames.Utilities.ArenaData;
import me.jamontoast.jothungergames.Utilities.ArenaManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DayEndCheckTask extends BukkitRunnable {

    private String arenaName;
    private World world;
    ArenaManager arenaManager;
    ArenaData arenaData;

    public DayEndCheckTask(String arenaName) {
        arenaManager = ArenaManager.getInstance();
        arenaData = arenaManager.getArenaData(arenaName);
        this.arenaName = arenaName;
        world = arenaData.getArenaWorld();

    }

    @Override
    public void run() {
        long time = world.getTime();
        if (time >= 13000 && time <= 13100) {
            int currentDay = arenaData.getCurrentDay();
            if (arenaData.getDeadPlayersByDay().getOrDefault(currentDay, new ArrayList<>()) != null) {
                arenaManager.
                displayDeadPlayers(arenaName, currentDay);
            }
            advanceDay();
        }
    }

    public void advanceDay() {
        int nextDay = arenaData.getCurrentDay() + 1;
        arenaData.setCurrentDay(nextDay);
        arenaData.getDeadPlayersByDay().put(nextDay, new ArrayList<>());
    }
}
