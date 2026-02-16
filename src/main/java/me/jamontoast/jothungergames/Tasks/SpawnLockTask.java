package me.jamontoast.jothungergames.Tasks;

import me.jamontoast.jothungergames.Utilities.ArenaManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class SpawnLockTask extends BukkitRunnable {

    private final Set<UUID> gamePlayers;
    private Map<Player, Set<Location>> playerBarrierMap;
    private final Map<UUID, Location> playerSpawnMap;
    String arenaName;
    private int restrictionTicks;
    private ArenaManager arenaManager = ArenaManager.getInstance();

    public SpawnLockTask(Set<UUID> gamePlayers, Map<UUID, Location> playerSpawnMap, String arenaName, int restrictionTime) {

        this.gamePlayers = gamePlayers;
        this.playerSpawnMap = playerSpawnMap;
        this.arenaName = arenaName;
        this.playerBarrierMap = new HashMap<>();
        restrictionTicks = restrictionTime*20; // Time to keep players at spawn in ticks (10 seconds)
    }

    @Override
    public void run() {
        if (restrictionTicks > 0) {
            for (UUID uuid : gamePlayers) {
                Player player = Bukkit.getPlayer(uuid);
                if (player == null || !player.isOnline()) continue;

                if (!playerBarrierMap.containsKey(player)) {
                    Set<Location> barrierLocations = createBarrierAroundPlayer(player);
                    playerBarrierMap.put(player, barrierLocations);
                }
            }
            restrictionTicks--;
        } else {
            for (UUID uuid : gamePlayers) {
                Player player = Bukkit.getPlayer(uuid);
                if (player == null || !player.isOnline()) continue;

                if (playerBarrierMap.containsKey(player)) {
                    removeBarrier(playerBarrierMap.get(player));
                }
            }
            arenaManager.gracePeriodStart(arenaName);
            arenaManager.startArenaTasks(arenaName);
            cancel();
        }
    }

    private Set<Location> createBarrierAroundPlayer(Player player) {
        Set<Location> barrierLocations = new HashSet<>();
        Location playerLocation = playerSpawnMap.get(player.getUniqueId());
        World world = playerLocation.getWorld();

        int height = 3;

        // Define the positions relative to the player's location
        int[][] offsets = {
                {-1, 0, 0}, // -x
                {1, 0, 0},  // +x
                {0, 0, -1}, // -z
                {0, 0, 1}   // +z
        };

        for (int[] offset : offsets) {
            for (int y = 0; y < height; y++) {
                Location loc = playerLocation.clone().add(offset[0], y, offset[2]);
                assert world != null;
                if (world.getBlockAt(loc).getType() == Material.AIR) {
                    world.getBlockAt(loc).setType(Material.BARRIER);
                    barrierLocations.add(loc);
                }
            }
        }
        return barrierLocations;
    }
    private void removeBarrier(Set<Location> barrierLocations) {
        for (Location loc : barrierLocations) {
            if (loc.getBlock().getType() == Material.BARRIER) {
                loc.getBlock().setType(Material.AIR);
            }
        }
    }
}
