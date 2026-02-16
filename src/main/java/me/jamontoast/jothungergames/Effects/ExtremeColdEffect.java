package me.jamontoast.jothungergames.Effects;

import me.jamontoast.jothungergames.JoTHungerGames;
import me.jamontoast.jothungergames.Interfaces.SegmentEffectStrategy;
import me.jamontoast.jothungergames.Utilities.BlockUtils;
import me.jamontoast.jothungergames.Utilities.SegmentChecker;
import me.jamontoast.jothungergames.Utilities.SegmentUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Snow;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;


public class ExtremeColdEffect implements SegmentEffectStrategy {

    private BukkitRunnable freezeTask; //define runnable
    private BukkitRunnable freezeDamageTask; //define runnable
    private BukkitRunnable snowSpreadTask; //define runnable
    private BukkitRunnable snowClearTask; //define runnable
    private BukkitRunnable stopColdEffectTask; //define runnable
    private final SegmentUtils segmentUtils = new SegmentUtils();
    private final Random random = new Random();
    private Set<Block> airBlocksAboveSurface = new HashSet<>();
    private HashSet<Block> snowBlocks = new HashSet<>();

    @Override
    public void start(String segmentGroup, int segmentNumber, Long durationOverride) {

        // Start effect logic
        SegmentChecker segmentChecker = new SegmentChecker(segmentGroup, segmentNumber);

        //initialise variables from config
        long effectDuration = 60;
        if (durationOverride != null) {
            effectDuration = durationOverride;
        } else if (!JoTHungerGames.getInstance().getConfig().getBoolean("effects master controls.enabled")) {
            effectDuration = JoTHungerGames.getInstance().getConfig().getLong("effects.extreme_cold.duration") * 20; //Convert seconds to ticks
        } else {
            effectDuration = JoTHungerGames.getInstance().getConfig().getLong("effects master controls.duration") * 20; //Convert seconds to ticks
        }

        // Get all valid air blocks above the surface
        airBlocksAboveSurface = BlockUtils.getAirBlocksAboveSurface(segmentGroup, segmentNumber, 1);
        if (airBlocksAboveSurface.isEmpty()) {
            // There are no air blocks above the surface in this segment, so return.
            Bukkit.getLogger().warning("No suitable air blocks for snow in segment: " + segmentNumber);
            return;
        }

        Bukkit.getLogger().info("Starting Extreme Cold");
        segmentUtils.setEffectActive(segmentGroup, segmentNumber, "EXTREME_COLD", true);

        freezeTask = new BukkitRunnable() {

            @Override
            public void run() {

                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (segmentChecker.isInsideSegment(player.getLocation())) {

                        // Apply freezing effect
                        int currentTicksFrozen = player.getFreezeTicks();
                        int newFreezeTicks = Math.min(currentTicksFrozen + 5, player.getMaxFreezeTicks()); // Ensure not exceeding max
                        player.setFreezeTicks(newFreezeTicks);

                        // Provide subtitle feedback to player
                        if (currentTicksFrozen == 0) { // If not previously frozen
                            player.sendTitle("", "You feel an extreme cold envelop you...", 10, 40, 20); // Customize as needed
                        }
                    }
                }
            }
        };
        freezeTask.runTaskTimer(JoTHungerGames.getInstance(), 0L,  1L);  // Repeats every tick

        freezeDamageTask = new BukkitRunnable() {

            @Override
            public void run() {

                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getFreezeTicks() > 0) {
                        player.damage(1);
                    }
                }
            }
        };
        freezeDamageTask.runTaskTimer(JoTHungerGames.getInstance(), 0L,  100L);  //Repeats every 5 seconds

        snowSpreadTask = new BukkitRunnable() {
            List<Block> potentialSnowBlocks = new ArrayList<>(airBlocksAboveSurface);
            int blocksToChange = potentialSnowBlocks.size() / 10;  // Set the number of blocks to change each cycle

            @Override
            public void run() {

                if (potentialSnowBlocks.isEmpty()) {
                    this.cancel();
                }

                Collections.shuffle(potentialSnowBlocks);  // Randomly shuffle the list

                for (int i = 0; i < blocksToChange && !potentialSnowBlocks.isEmpty(); i++) {
                    Block block = potentialSnowBlocks.remove(0); // Take and remove the first block from the shuffled list

                    if (!snowBlocks.contains(block) && block.getType() == Material.AIR && (random.nextInt(4) < 3)) {
                        block.setType(Material.SNOW);  // Set the block to a snow layer
                        BlockData data = block.getBlockData();

                        if (data instanceof Snow) {
                            // Setting the level of snow (how thick or thin the layer is)
                            Snow snowLayer = (Snow) data;
                            snowLayer.setLayers(random.nextInt(2) + 1); // Randomize the snow layer's thickness
                            block.setBlockData(snowLayer);
                        }

                        snowBlocks.add(block); // Add to the set of blocks that have been changed to snow
                    }
                }
            }
        };
        snowSpreadTask.runTaskTimer(JoTHungerGames.getInstance(), 0L,  20L);  // Repeats every second

        stopColdEffectTask = new BukkitRunnable() {

            @Override
            public void run() {
                clearSnow(segmentGroup, segmentNumber, snowBlocks);
            }
        };
        stopColdEffectTask.runTaskLater(JoTHungerGames.getInstance(), effectDuration);  // Starts after the effect duration has run out
    }

    private void clearSnow (String segmentGroup, int segmentNumber, HashSet<Block> snowBlocks) {
        snowClearTask = new BukkitRunnable() {
            List<Block> snowBlocksList = new ArrayList<>(snowBlocks);
            int blocksToChange = snowBlocksList.size() / 10;  // Set the number of blocks you want to change each cycle

            @Override
            public void run() {

                if (snowBlocksList.isEmpty()) {
                    this.cancel();
                    stop(segmentGroup, segmentNumber);
                }

                Collections.shuffle(snowBlocksList);  // Randomly shuffle the list

                for (int i = 0; i < blocksToChange && !snowBlocksList.isEmpty(); i++) {
                    Block block = snowBlocksList.remove(0); // Take and remove the first block from the shuffled list

                    if (snowBlocks.contains(block) && block.getType() == Material.SNOW) {
                        block.setType(Material.AIR);  // Set the block to air
                    }
                }
            }
        };
        snowClearTask.runTaskTimer(JoTHungerGames.getInstance(), 0L,  10L);  // Repeats every half second
    }

    @Override
    public void stop(String segmentGroup, int segmentNumber) {
        // Stop effect logic

        if(freezeTask != null) {
            Bukkit.getLogger().info("Stopping Extreme Cold");
            freezeTask.cancel();
            freezeTask = null; // Reset task
        }
        if(freezeDamageTask != null) {
            freezeDamageTask.cancel();
            freezeDamageTask = null; // Reset task
        }
        if(snowSpreadTask != null) {
            snowSpreadTask.cancel();
            snowSpreadTask = null; // Reset task
        }
        if(snowClearTask != null) {
            snowClearTask.cancel();
            snowClearTask = null; // Reset task
        }
        if(stopColdEffectTask != null) {
            stopColdEffectTask.cancel();
            stopColdEffectTask = null; // Reset task
        }
        for (Block block : airBlocksAboveSurface) {
            if (block.getType() == Material.SNOW) {
                block.setType(Material.AIR);
            }
        }

        segmentUtils.setEffectActive(segmentGroup, segmentNumber, "EXTREME_COLD", false);
    }
}