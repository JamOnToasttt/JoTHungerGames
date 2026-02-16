package me.jamontoast.jothungergames.Effects;

import me.jamontoast.jothungergames.JoTHungerGames;
import me.jamontoast.jothungergames.Interfaces.SegmentEffectStrategy;
import me.jamontoast.jothungergames.Utilities.BlockUtils;
import me.jamontoast.jothungergames.Utilities.SegmentChecker;
import me.jamontoast.jothungergames.Utilities.SegmentUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class FirestormEffect implements SegmentEffectStrategy {

    private BukkitRunnable spreadFireTask; //define runnable
    private BukkitRunnable clearFireTask; //define runnable
    private BukkitRunnable stopFirestormEffectTask; //define runnable
    private int spreadFactor = 1; // To control the number of blocks fire will spread to each iteration
    private int spreadFactorInt = 1; // To control the number of blocks fire will spread to each iteration
    private final SegmentUtils segmentUtils = new SegmentUtils();
    private final Random random = new Random();

    private final Set<Block> validFireBlocks = new HashSet<>();
    private final Queue<Location> fireQueue = new LinkedList<>();
    private final Queue<Location> fireClearQueue = new LinkedList<>();
    private final Set<Block> visitedBlocks = new HashSet<>();
    private HashSet<Block> fireBlocks = new HashSet<>();
    private Location fireOrigin;
    private Map<Block, BlockState> segmentBackup;

    @Override
    public void start(String segmentGroup, int segmentNumber, Long durationOverride) {

        //clear old values just in case
        visitedBlocks.clear();//clears affected blocks
        fireBlocks.clear();//clears affected blocks
        fireQueue.clear();
        fireClearQueue.clear();

        // Backup the segment
        segmentBackup = SegmentUtils.getSegmentBackup(segmentGroup, segmentNumber);

        //initialise variables from config
        long effectDuration = 60;
        if (durationOverride != null) {
            effectDuration = durationOverride;
        } else if (!JoTHungerGames.getInstance().getConfig().getBoolean("effects master controls.enabled")) {
            effectDuration = JoTHungerGames.getInstance().getConfig().getLong("effects.firestorm.duration") * 20; //Convert seconds to ticks
        } else {
            effectDuration = JoTHungerGames.getInstance().getConfig().getLong("effects master controls.duration") * 20; //Convert seconds to ticks
        }
        long intervalTicks = JoTHungerGames.getInstance().getConfig().getLong("effects.firestorm.interval") * 20;  // Convert seconds to ticks
        boolean autoClear = JoTHungerGames.getInstance().getConfig().getBoolean("effects.firestorm.autoClear");

        // Get segment details using SegmentUtils
        World world = SegmentUtils.getWorld(segmentGroup);
        Location segmentCenter = SegmentUtils.getSegmentCenter(segmentGroup, segmentNumber);

        //get segment Blocks
        List<JoTHungerGames.Segment> segmentsList = JoTHungerGames.getInstance().getSegmentsFromConfig(segmentGroup);
        HashSet<Block> segmentBlocks = new HashSet<>();
        // Find the segment by number
        JoTHungerGames.Segment desiredSegment = segmentsList.get(segmentNumber - 1);

        for (Location blockLocation : desiredSegment.getBlocks()) {
            int x = blockLocation.getBlockX();
            int y = blockLocation.getBlockY();
            int z = blockLocation.getBlockZ();

            assert world != null;
            Block block = world.getBlockAt(x, y, z);
            segmentBlocks.add(block);
        }

        //Get all air blocks next to air-exposed blocks in segment
        HashSet<Block> airNextToSolid = BlockUtils.getAirNextToSolid(segmentGroup, segmentNumber);

        validFireBlocks.addAll(airNextToSolid);

        if (airNextToSolid.isEmpty()) {
            // There are no air blocks above the surface in this segment, so return.
            Bukkit.getLogger().warning("No suitable flammable blocks for Firestorm in segment: " + segmentNumber);
            return;
        }

        // Choose center block as the fire origin
        fireOrigin = BlockUtils.getClosestSurfaceBlockToCenter(airNextToSolid, segmentCenter).getLocation();
        fireQueue.add(fireOrigin);
        visitedBlocks.add(fireOrigin.getBlock());

        SegmentChecker segmentChecker = new SegmentChecker(segmentGroup, segmentNumber);

        Bukkit.getLogger().info("Starting Firestorm");
        SegmentUtils.setEffectActive(segmentGroup, segmentNumber, "FIRESTORM", true);

        // Scheduled task to expand fire
        spreadFireTask = new BukkitRunnable() {

            @Override
            public void run() {

                for (int i = 0; i < spreadFactor; i++) { // Spread the fire 'spreadFactor' number of times in each iteration
                    if (fireQueue.isEmpty()) {
                        Bukkit.getLogger().info("Fire spread queue empty");
                        visitedBlocks.clear();
                        if (autoClear) {
                            clearFire(segmentGroup, segmentNumber, fireBlocks);
                        }
                        this.cancel();
                        break; // Break if the queue is empty
                    }

                    Location current = fireQueue.poll();
                    Block currentBlock = current.getBlock();

                    if (random.nextInt(3) < 2) {  // 2/3 chance because it's successful for 0 and 1
                        // Place fire at current location
                        if (validFireBlocks.contains(currentBlock) && currentBlock.getType() == Material.AIR) {
                            fireBlocks.add(currentBlock);
                            currentBlock.setType(Material.FIRE);
                        }
                    }

                    // Get neighbors
                    List<Location> neighbors = Arrays.asList(
                            current.clone().add(1, 0, 0),
                            current.clone().subtract(1, 0, 0),
                            current.clone().add(0, 1, 0),
                            current.clone().subtract(0, 1, 0),
                            current.clone().add(0, 0, 1),
                            current.clone().subtract(0, 0, 1)
                    );

                    // Randomize the order
                    Collections.shuffle(neighbors);

                    for (Location neighbor : neighbors) {
                        // Generate a random boolean that's true roughly half the time
                        boolean shouldAdd = random.nextBoolean();
                        Block neighborBlock = neighbor.getBlock();

                        if (shouldAdd && validFireBlocks.contains(neighborBlock) && !visitedBlocks.contains(neighborBlock)) {
                            visitedBlocks.add(neighborBlock);
                            fireQueue.add(neighbor);
                        }
                    }
                }
                // Update spreadFactor for the next iteration
                spreadFactorInt += 1;
                spreadFactor = ((spreadFactorInt * 6)) * 2;

            }
        };
        spreadFireTask.runTaskTimer(JoTHungerGames.getInstance(), 0L, intervalTicks); // Adjust the period as needed for the wave speed

        stopFirestormEffectTask = new BukkitRunnable() {
            @Override
            public void run() {
                clearFire(segmentGroup, segmentNumber, fireBlocks);
            }
        };
        stopFirestormEffectTask.runTaskLater(JoTHungerGames.getInstance(), effectDuration);  // Starts after the effect duration has run out

    }

    private void clearFire(String segmentGroup, int segmentNumber, HashSet<Block> fireBlocks) {
        clearFireTask = new BukkitRunnable() {
            List<Block> fireBlocksList = new ArrayList<>(fireBlocks);
            int blocksToChange = fireBlocksList.size() / 10;  // Set the number of blocks you want to change each cycle

            @Override
            public void run() {

                if (fireBlocksList.isEmpty()) {
                    this.cancel();
                    stop(segmentGroup, segmentNumber);
                }

                Collections.shuffle(fireBlocksList);  // Randomly shuffle the list

                for (int i = 0; i < blocksToChange && !fireBlocksList.isEmpty(); i++) {
                    Block block = fireBlocksList.remove(0); // Take and remove the first block from the shuffled list

                    if (fireBlocks.contains(block) && block.getType() == Material.FIRE) {
                        block.setType(Material.AIR);  // Set the block to air
                    }
                }
            }
        };
        clearFireTask.runTaskTimer(JoTHungerGames.getInstance(), 0L,  10L);  // Repeats every half second
    }

    @Override
    public void stop(String segmentGroup, int segmentNumber) {
        if (stopFirestormEffectTask != null) {
            stopFirestormEffectTask.cancel();
            stopFirestormEffectTask = null;
        }
        if(spreadFireTask != null) {
            Bukkit.getLogger().info("Stopping Firestorm");
            spreadFireTask.cancel();
            spreadFireTask = null;
        }
        if(clearFireTask != null) {
            clearFireTask.cancel();
            clearFireTask = null;
        }
        // Reset spreadFactor
        spreadFactor = 1;
        visitedBlocks.clear();//clears affected blocks
        fireQueue.clear();
        fireClearQueue.clear();
        fireBlocks.clear();

        SegmentUtils.setEffectActive(segmentGroup, segmentNumber, "FIRESTORM", false);
        //restore segment
        SegmentUtils.restoreSegment(segmentGroup, segmentNumber, segmentBackup);
    }

}