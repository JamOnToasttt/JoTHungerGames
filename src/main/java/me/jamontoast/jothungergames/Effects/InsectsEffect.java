package me.jamontoast.jothungergames.Effects;

import me.jamontoast.jothungergames.JoTHungerGames;
import me.jamontoast.jothungergames.Interfaces.SegmentEffectStrategy;
import me.jamontoast.jothungergames.Utilities.BlockUtils;
import me.jamontoast.jothungergames.Utilities.SegmentChecker;
import me.jamontoast.jothungergames.Utilities.SegmentUtils;
import org.bukkit.entity.Silverfish;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class InsectsEffect implements SegmentEffectStrategy {

    private BukkitRunnable insectsTask;
    private BukkitRunnable insectsRemovalTask;
    private  BukkitRunnable stopInsectsEffectTask;
    private final SegmentUtils segmentUtils = new SegmentUtils();
    private final Random random = new Random();
    private Set<Player> playersInSegment = new HashSet<>();
    private final Map<UUID, EntityType> spawnedSilverfish = new HashMap<>();


    @Override
    public void start(String segmentGroup, int segmentNumber, Long durationOverride) {
        // Start effect logic

        //initialise variables from config
        long effectDuration = 60;
        if (durationOverride != null) {
            effectDuration = durationOverride;
        } else if (!JoTHungerGames.getInstance().getConfig().getBoolean("effects master controls.enabled")) {
            effectDuration = JoTHungerGames.getInstance().getConfig().getLong("effects.insects.duration") * 20; //Convert seconds to ticks
        } else {
            effectDuration = JoTHungerGames.getInstance().getConfig().getLong("effects master controls.duration") * 20; //Convert seconds to ticks
        }
        long intervalTicks = JoTHungerGames.getInstance().getConfig().getLong("effects.insects.interval") * 20;  // Convert seconds to ticks
        int maxInsects = JoTHungerGames.getInstance().getConfig().getInt("effects.insects.maxInsects", 1); //Get number of insects per wave

        SegmentChecker segmentChecker = new SegmentChecker(segmentGroup, segmentNumber);

        Bukkit.getLogger().info("Starting Insects");
        SegmentUtils.setEffectActive(segmentGroup, segmentNumber, "INSECTS", true);

        HashSet<Block> surfaceBlocks = BlockUtils.getSurfaceBlocks(segmentGroup, segmentNumber);
        if (surfaceBlocks.isEmpty()) {
            Bukkit.getLogger().info("No surface blocks found.");
            return;  // No surface blocks found.
        }

        //Task for checking players in a segment and spawning silverfish near them
        insectsTask = new BukkitRunnable() {
            @Override
            public void run() {

                Set<Player> currentPlayersInSegment = new HashSet<>();

                // Check each player and spawn insects if they are in the segment
                for (Player player : Bukkit.getOnlinePlayers()) {
                    HashSet<Block> spawnBlocks = null;
                    if (segmentChecker.isInsideSegment(player.getLocation())) {

                        currentPlayersInSegment.add(player); // Add player to the set

                        //radius is number of blocks out from a player (1 is a 3x3x3 cube, 2 is a 5x5x5 cube, etc.)
                        spawnBlocks = BlockUtils.getClosestSurfaceBlocksToPlayer(player, surfaceBlocks, 2);

                        // Spawn insects near player
                        int spawnCount = random.nextInt(maxInsects + 1);
                        if (spawnCount > 0) {
                            spawnInsectsNearPlayer(spawnBlocks, spawnCount);
                        }

                    }
                }
                // Detect players who have left the segment
                for (
                        Player previousPlayer : playersInSegment) {
                    if (!currentPlayersInSegment.contains(previousPlayer)) {
                        Bukkit.getLogger().info(previousPlayer + " has left segment " + segmentNumber + " ");
                        // This player has left the segment.
                    }
                }

                playersInSegment = currentPlayersInSegment; // Update our record of players in segment.
            }

        };
        insectsTask.runTaskTimer(JoTHungerGames.getInstance(), 0L,  intervalTicks);  // Repeats every interval, selected in config

        // Task for removing insects if they leave the segment
        insectsRemovalTask = new BukkitRunnable() {

            @Override
            public void run() {
                Iterator<Map.Entry<UUID, EntityType>> iterator = spawnedSilverfish.entrySet().iterator();

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
        insectsRemovalTask.runTaskTimer(JoTHungerGames.getInstance(), 0L,  20L);  // Repeats every second

        stopInsectsEffectTask = new BukkitRunnable() {

            @Override
            public void run() {
                stop(segmentGroup, segmentNumber);
            }
        };
        stopInsectsEffectTask.runTaskLater(JoTHungerGames.getInstance(), effectDuration);  // Starts after the effect duration has run out
    }

    private void spawnInsectsNearPlayer(HashSet<Block> spawnBlocks, int numInsects) {

        int size = spawnBlocks.size();

        for (int insect = 0; insect < numInsects; insect++) {
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
            // Assuming the block is suitable for spawning, get the location to spawn the silverfish
            Location spawnLocation = randomBlock.getLocation().add(0.5, 1, 0.5); // Center the spawn location on the block, +1 y to make sure the entity is above the surface

            // Get the world of the block and spawn the silverfish
            World world = randomBlock.getWorld();
            Silverfish silverfish = (Silverfish) world.spawnEntity(spawnLocation, EntityType.SILVERFISH);

            // Apply the speed effect to make the silverfish twice as fast
            // Note: The duration (in ticks), and amplifier (1 for double speed) might need to be adjusted
            silverfish.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));

            // Keep track of spawned silverfish
            spawnedSilverfish.put(silverfish.getUniqueId(), EntityType.SILVERFISH);
        }
    }

    @Override
    public void stop(String segmentGroup, int segmentNumber) {
        // Stop effect logic
        if(insectsTask != null) {
            Bukkit.getLogger().info("Stopping Insects");
            insectsTask.cancel();
            insectsTask = null;
        }
        if(insectsRemovalTask != null) {
            insectsRemovalTask.cancel();
            insectsRemovalTask = null;
        }
        if(stopInsectsEffectTask != null) {
            stopInsectsEffectTask.cancel();
            stopInsectsEffectTask = null;
        }
        segmentUtils.setEffectActive(segmentGroup, segmentNumber, "INSECTS", false);

        // Kill all spawned silverfish
        for (UUID entityId : spawnedSilverfish.keySet()) {
            Entity entity = Bukkit.getEntity(entityId);
            if (entity != null && entity.isValid()) {
                entity.remove();  // This will kill the silverfish
            }
        }
        // Clear the record of spawned entities
        spawnedSilverfish.clear();
    }
}