package me.jamontoast.jothungergames.Effects;

import me.jamontoast.jothungergames.JoTHungerGames;
import me.jamontoast.jothungergames.Interfaces.SegmentEffectStrategy;
import me.jamontoast.jothungergames.Utilities.BlockUtils;
import me.jamontoast.jothungergames.Utilities.SegmentChecker;
import me.jamontoast.jothungergames.Utilities.SegmentUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashSet;

public class BeastEffect implements SegmentEffectStrategy {

    private LivingEntity warden;
    private BukkitRunnable pullBackWardenTask;
    private BukkitRunnable stopEffectTask;
    private final SegmentUtils segmentUtils = new SegmentUtils();
    private final BlockUtils blockUtils = new BlockUtils();

    @Override
    public void start(String segmentGroup, int segmentNumber, Long durationOverride) {

        SegmentChecker segmentChecker = new SegmentChecker(segmentGroup, segmentNumber);

        //initialise variables from config

        long effectDuration = 60;
        if (durationOverride != null) {
            effectDuration = durationOverride;
        } else if (!JoTHungerGames.getInstance().getConfig().getBoolean("effects master controls.enabled")) {
            effectDuration = JoTHungerGames.getInstance().getConfig().getLong("effects.beast.duration") * 20; //Convert seconds to ticks
        } else {
            effectDuration = JoTHungerGames.getInstance().getConfig().getLong("effects master controls.duration") * 20; //Convert seconds to ticks
        }

        Bukkit.getLogger().info("Starting Beast");
        SegmentUtils.setEffectActive(segmentGroup, segmentNumber, "BEAST", true);

        Location segmentCenter = SegmentUtils.getSegmentCenter(segmentGroup, segmentNumber);
        HashSet<Block> surfaceBlocks = BlockUtils.getSurfaceBlocks(segmentGroup, segmentNumber);
        if (surfaceBlocks.isEmpty()) {
            Bukkit.getLogger().info("No surface blocks found.");
            return;  // No surface blocks found.
        }

        // Find the closest surface block to the center
        Block closestBlock = BlockUtils.getClosestSurfaceBlockToCenter(surfaceBlocks, segmentCenter);

        // Now spawn the Warden at the closest surface block
        Location spawnLocation = closestBlock.getLocation();
        Bukkit.getLogger().info("Warden spawn location set to " + spawnLocation);
        spawnLocation.add(0.5, 2, 0.5); // Offsetting by 2 blocks above the surface and centering within the block
        if (warden != null) {
            warden.remove();
        }
        warden = (LivingEntity) closestBlock.getWorld().spawnEntity(spawnLocation, EntityType.WARDEN);

        // Prevent the Warden from de-spawning
        warden.setRemoveWhenFarAway(false);

        pullBackWardenTask = new BukkitRunnable() {

            @Override
            public void run() {

                Location wardenLocation = warden.getLocation();

                // Check if the mob is within the segment
                if (!segmentChecker.isInsideSegment(wardenLocation)) {
                    // A loop to pull the Warden back
                    int pullBackAttempts = 0;  // You can limit the number of attempts if needed
                    while (!segmentChecker.isInsideSegment(wardenLocation) && pullBackAttempts < 5) {

                        double dx = segmentCenter.getX() - wardenLocation.getX();
                        double dy = segmentCenter.getY() - wardenLocation.getY();
                        double dz = segmentCenter.getZ() - wardenLocation.getZ();
                        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);

                        // Normalize and apply a speed factor
                        dx /= length;
                        dy /= length;
                        dz /= length;
                        double speedFactor = pullBackAttempts;
                        dx *= speedFactor;
                        dy *= speedFactor;
                        dz *= speedFactor;

                        // Apply velocity
                        warden.setVelocity(new Vector(dx, dy, dz));

                        // Refresh the warden's location
                        wardenLocation = warden.getLocation();

                        // Uncomment the next line if you've set a maximum number of attempts
                        pullBackAttempts++;
                    }
                }
            }
        };
        pullBackWardenTask.runTaskTimer(JoTHungerGames.getInstance(), 0L,  20L);  // Repeats every second

        stopEffectTask = new BukkitRunnable() {

            @Override
            public void run() {
                stop(segmentGroup, segmentNumber);
            }
        };
        stopEffectTask.runTaskLater(JoTHungerGames.getInstance(), effectDuration);  // Starts after the effect duration has run out

    }
    @Override
    public void stop(String segmentGroup, int segmentNumber) {

        if(pullBackWardenTask != null) {
            Bukkit.getLogger().info("Stopping Beast");
            warden.remove();
            pullBackWardenTask.cancel();
            pullBackWardenTask = null; // Reset task
        }
        if(stopEffectTask != null) {
            stopEffectTask.cancel();
            stopEffectTask = null; // Reset task
        }
        SegmentUtils.setEffectActive(segmentGroup, segmentNumber, "BEAST", false);
    }
}