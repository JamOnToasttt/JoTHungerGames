package me.jamontoast.jothungergames.Effects;

import me.jamontoast.jothungergames.JoTHungerGames;
import me.jamontoast.jothungergames.Interfaces.SegmentEffectStrategy;
import me.jamontoast.jothungergames.Utilities.BlockUtils;
import me.jamontoast.jothungergames.Utilities.SegmentChecker;
import me.jamontoast.jothungergames.Utilities.SegmentUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.CaveSpider;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class SpidersEffect implements SegmentEffectStrategy {

    private BukkitRunnable spidersTask;
    private BukkitRunnable spidersRemovalTask;
    private  BukkitRunnable stopSpidersEffectTask;
    private final SegmentUtils segmentUtils = new SegmentUtils();
    private final Random random = new Random();
    private Set<Player> playersInSegment = new HashSet<>();
    private final Map<UUID, EntityType> spawnedSpider = new HashMap<>();


    @Override
    public void start(String segmentGroup, int segmentNumber, Long durationOverride) {
        // Start effect logic

        //initialise variables from config
        long effectDuration = 60;
        if (durationOverride != null) {
            effectDuration = durationOverride;
        } else if (!JoTHungerGames.getInstance().getConfig().getBoolean("effects master controls.enabled")) {
            effectDuration = JoTHungerGames.getInstance().getConfig().getLong("effects.spiders.duration") * 20; //Convert seconds to ticks
        } else {
            effectDuration = JoTHungerGames.getInstance().getConfig().getLong("effects master controls.duration") * 20; //Convert seconds to ticks
        }
        long intervalTicks = JoTHungerGames.getInstance().getConfig().getLong("effects.spiders.interval") * 20;  // Convert seconds to ticks
        int maxSpiders = JoTHungerGames.getInstance().getConfig().getInt("effects.spiders.maxSpiders", 1); //Get number of insects per wave

        SegmentChecker segmentChecker = new SegmentChecker(segmentGroup, segmentNumber);

        Bukkit.getLogger().info("Starting Spiders");
        SegmentUtils.setEffectActive(segmentGroup, segmentNumber, "SPIDERS", true);

        HashSet<Block> surfaceBlocks = BlockUtils.getSurfaceBlocks(segmentGroup, segmentNumber);
        if (surfaceBlocks.isEmpty()) {
            Bukkit.getLogger().info("No surface blocks found.");
            return;  // No surface blocks found.
        }

        //Task for checking players in a segment and spawning silverfish near them
        spidersTask = new BukkitRunnable() {
            @Override
            public void run() {

                Set<Player> currentPlayersInSegment = new HashSet<>();

                // Check each player and spawn insects if they are in the segment
                for (Player player : Bukkit.getOnlinePlayers()) {
                    HashSet<Block> spawnBlocks = null;
                    if (segmentChecker.isInsideSegment(player.getLocation())) {

                        currentPlayersInSegment.add(player); // Add player to the set

                        //radius is number of blocks out from a player (1 is a 3x3x3 cube, 2 is a 5x5x5 cube, etc.)
                        spawnBlocks = BlockUtils.getClosestSurfaceBlocksToPlayer(player, surfaceBlocks, 5);

                        // Spawn spiders near player
                        int spawnCount = random.nextInt(maxSpiders + 1);
                        if (spawnCount > 0) {
                            spawnSpidersNearPlayer(spawnBlocks, spawnCount);
                        }

                    }
                }
                // Detect players who have left the segment
                for (Player previousPlayer : playersInSegment) {
                    if (!currentPlayersInSegment.contains(previousPlayer)) {
                        Bukkit.getLogger().info(previousPlayer + " has left segment " + segmentNumber + " ");
                        // This player has left the segment.
                    }
                }

                playersInSegment = currentPlayersInSegment; // Update our record of players in segment.
            }

        };
        spidersTask.runTaskTimer(JoTHungerGames.getInstance(), 0L,  intervalTicks);  // Repeats every interval, selected in config

        // Task for removing insects if they leave the segment
        spidersRemovalTask = new BukkitRunnable() {
            @Override
            public void run() {

                Iterator<Map.Entry<UUID, EntityType>> iterator = spawnedSpider.entrySet().iterator();

                while (iterator.hasNext()) {
                    Map.Entry<UUID, EntityType> entry = iterator.next();
                    Entity entity = Bukkit.getEntity(entry.getKey());

                    if (entity == null || !entity.isValid()) {
                        iterator.remove(); // Remove from tracking if no longer valid
                        continue;
                    }

                    if (!segmentChecker.isInsideSegment(entity.getLocation())) {
                        entity.remove(); // This kills the entity
                        iterator.remove(); // Remove from tracking
                    }
                }
            }
        };
        spidersRemovalTask.runTaskTimer(JoTHungerGames.getInstance(), 0L,  20L);  // Repeats every second

        stopSpidersEffectTask = new BukkitRunnable() {

            @Override
            public void run() {
                stop(segmentGroup, segmentNumber);
            }
        };
        stopSpidersEffectTask.runTaskLater(JoTHungerGames.getInstance(), effectDuration);  // Starts after the effect duration has run out
    }

    @Override
    public void stop(String segmentGroup, int segmentNumber) {
        // Stop effect logic
        if(spidersTask != null) {
            Bukkit.getLogger().info("Stopping Spiders");
            spidersTask.cancel();
            spidersTask = null;
        }
        if(spidersRemovalTask != null) {
            spidersRemovalTask.cancel();
            spidersRemovalTask = null;
        }
        if(stopSpidersEffectTask != null) {
            stopSpidersEffectTask.cancel();
            stopSpidersEffectTask = null;
        }
        segmentUtils.setEffectActive(segmentGroup, segmentNumber, "SPIDERS", false);

        // Kill all spawned silverfish
        for (UUID entityId : spawnedSpider.keySet()) {
            Entity entity = Bukkit.getEntity(entityId);
            if (entity != null && entity.isValid()) {
                entity.remove();  // This will kill the silverfish
            }
        }
        // Clear the record of spawned entities
        spawnedSpider.clear();
    }

    private void spawnSpidersNearPlayer(HashSet<Block> spawnBlocks, int numSpiders) {

        int size = spawnBlocks.size();

        for (int spider = 0; spider < numSpiders; spider++) {
            if (size == 0) {
                //No blocks available to spawn silverfish
                return;
            }

            // Determine a random index in the set
            int randomIndex = random.nextInt(size);

            // Obtain an iterator for the set
            Iterator<Block> iterator = spawnBlocks.iterator();

            // Iterate through the set to the random index
            Block randomBlock = null;
            for (int j = 0; j <= randomIndex; j++) {
                randomBlock = iterator.next();
            }
            // Assuming the block is suitable for spawning, get the location to spawn the spiders
            Location spawnLocation = randomBlock.getLocation().add(0.5, 3, 0.5); // Center the spawn location on the block

            // Get the world of the block and spawn the spider
            World world = randomBlock.getWorld();
            CaveSpider caveSpider = (CaveSpider) world.spawnEntity(spawnLocation, EntityType.CAVE_SPIDER);


            // Apply the speed effect to make the spider twice as fast
            // Note: The duration (in ticks), and amplifier (1 for double speed) might need to be adjusted
            caveSpider.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2));
            // Set spider to be aware
            caveSpider.setAware(true);

            // Optionally, find the nearest player and set it as the spider's target
            Player nearestPlayer = findNearestPlayer(spawnLocation, world.getPlayers());
            if (nearestPlayer != null) {
                caveSpider.setTarget(nearestPlayer);
            }

            // Keep track of spawned spider
            spawnedSpider.put(caveSpider.getUniqueId(), EntityType.CAVE_SPIDER);
        }
    }

    // Helper method to find the nearest player
    private Player findNearestPlayer(Location location, Collection<Player> players) {
        Player nearestPlayer = null;
        double nearestDistanceSquared = Double.MAX_VALUE;

        for (Player player : players) {
            double distanceSquared = player.getLocation().distanceSquared(location);
            if (distanceSquared < nearestDistanceSquared) {
                nearestDistanceSquared = distanceSquared;
                nearestPlayer = player;
            }
        }
        return nearestPlayer;
    }
}