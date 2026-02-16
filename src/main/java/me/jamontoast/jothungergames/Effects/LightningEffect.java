package me.jamontoast.jothungergames.Effects;

import me.jamontoast.jothungergames.JoTHungerGames;
import me.jamontoast.jothungergames.Interfaces.SegmentEffectStrategy;
import me.jamontoast.jothungergames.Utilities.BlockUtils;
import me.jamontoast.jothungergames.Utilities.SegmentUtils;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;


public class LightningEffect implements SegmentEffectStrategy {

    private BukkitRunnable lightningTask; //define runnable
    private BukkitRunnable lightningStrike; //define runnable
    private BukkitRunnable stopLightningEffectTask; //define runnable
    private final SegmentUtils segmentUtils = new SegmentUtils();
    private final Random random = new Random();


    @Override
    public void start(String segmentGroup, int segmentNumber, Long durationOverride) {

        //initialise variables from config
        long effectDuration = 60;
        if (durationOverride != null) {
            effectDuration = durationOverride;
        } else if (!JoTHungerGames.getInstance().getConfig().getBoolean("effects master controls.enabled")) {
            effectDuration = JoTHungerGames.getInstance().getConfig().getLong("effects.lightning.duration") * 20; //Convert seconds to ticks
        } else {
            effectDuration = JoTHungerGames.getInstance().getConfig().getLong("effects master controls.duration") * 20; //Convert seconds to ticks
        }
        long intervalTicks = JoTHungerGames.getInstance().getConfig().getLong("effects.lightning.interval") * 20;  // Convert seconds to ticks
        double strikePercentage = JoTHungerGames.getInstance().getConfig().getDouble("effects.lightning.strikePercentage");
        Bukkit.getLogger().info("Starting Lightning");

        segmentUtils.setEffectActive(segmentGroup, segmentNumber, "LIGHTNING", true);

        HashSet<Block> surfaceBlocks = BlockUtils.getSurfaceBlocks(segmentGroup, segmentNumber);
        if (surfaceBlocks.isEmpty()) {
            Bukkit.getLogger().info("No surface blocks found.");
            return;  // No surface blocks found.
        }

        int numberOfStrikes = Math.max(1, (int) (surfaceBlocks.size() * strikePercentage)); // Number of blocks to strike each interval, at least 1

        lightningTask = new BukkitRunnable() {
            @Override
            public void run() {

                long nextInterval = random.nextLong(intervalTicks) + 10; // Generate a new random interval

                Bukkit.getScheduler().runTaskLater(JoTHungerGames.getInstance(), () -> {

                    for (int strike = 0; strike < numberOfStrikes; strike++) {
                        // Randomly select a block
                        int randomIndex = random.nextInt(surfaceBlocks.size());
                        Iterator<Block> iterator = surfaceBlocks.iterator();
                        Block randomBlock = null;

                        for (int index = 0; index <= randomIndex; index++) {
                        randomBlock = iterator.next();
                        }
                        // Strike lightning
                        randomBlock.getWorld().strikeLightning(randomBlock.getLocation());
                    }
                }, nextInterval);
            }
        };
        lightningTask.runTaskTimer(JoTHungerGames.getInstance(), 0L, intervalTicks);

        stopLightningEffectTask = new BukkitRunnable() {

            @Override
            public void run() {
                stop(segmentGroup, segmentNumber);
            }
        };
        stopLightningEffectTask.runTaskLater(JoTHungerGames.getInstance(), effectDuration);  // Starts after the effect duration has run out
    }

    @Override
    public void stop(String segmentGroup, int segmentNumber) {
        if(lightningTask != null) {
            Bukkit.getLogger().info("Stopping Lightning");
            lightningTask.cancel();
            lightningTask = null; // Reset task
        }
        if(stopLightningEffectTask != null) {
            stopLightningEffectTask.cancel();
            stopLightningEffectTask = null; // Reset task
        }
        segmentUtils.setEffectActive(segmentGroup, segmentNumber, "LIGHTNING", false);
    }
}