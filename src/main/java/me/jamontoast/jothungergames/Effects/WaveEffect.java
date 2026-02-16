package me.jamontoast.jothungergames.Effects;

import me.jamontoast.jothungergames.JoTHungerGames;
import me.jamontoast.jothungergames.Interfaces.SegmentEffectStrategy;
import me.jamontoast.jothungergames.Utilities.BlockUtils;
import me.jamontoast.jothungergames.Utilities.SegmentChecker;
import me.jamontoast.jothungergames.Utilities.SegmentUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class WaveEffect implements SegmentEffectStrategy {

    private BukkitRunnable waveTask; //define runnable
    private BukkitRunnable pushTask; //define runnable
    private final SegmentUtils segmentUtils = new SegmentUtils();
    private Map<Block, BlockState> segmentBackup;

    @Override
    public void start(String segmentGroup, int segmentNumber, Long durationOverride) {

        // Backup the segment
        segmentBackup = SegmentUtils.getSegmentBackup(segmentGroup, segmentNumber);

        // Wave characteristics
        final int waveThickness = JoTHungerGames.getInstance().getConfig().getInt("effects.wave.waveThickness"); // wave width
        final int wavePeakHeight = JoTHungerGames.getInstance().getConfig().getInt("effects.wave.wavePeakHeight"); // wave max height
        final int waveSpread = JoTHungerGames.getInstance().getConfig().getInt("effects.wave.waveSpread"); // how many blocks the wave moves per interval
        final long waveSpeed = JoTHungerGames.getInstance().getConfig().getInt("effects.wave.waveSpeed"); // how many ticks between each spread
        final double waveStrength = JoTHungerGames.getInstance().getConfig().getInt("effects.wave.waveStrength"); // how strongly the wave pushes players

        // Get segment details using SegmentUtils
        String[] centerCoords = SegmentUtils.getGroupCenter(segmentGroup);
        World world = SegmentUtils.getWorld(segmentGroup);
        Location center = new Location(world, Integer.parseInt(centerCoords[0]), Integer.parseInt(centerCoords[1]), Integer.parseInt(centerCoords[2]));
        int outerRadius = SegmentUtils.getOuterRadius(segmentGroup);
        int innerRadius = SegmentUtils.getInnerRadius(segmentGroup);
        int segmentHeight = SegmentUtils.getHeight(segmentGroup);

        boolean stopAtInner = JoTHungerGames.getInstance().getConfig().getBoolean("effects.wave.stopAtInnerRadius", true);

        // Calculate segment angles
        double startAngleClockwise = SegmentUtils.getStartAngle(segmentGroup, segmentNumber);
        double endAngleClockwise = SegmentUtils.getEndAngle(segmentGroup, segmentNumber);

        double startAngle = Math.toRadians(endAngleClockwise);
        double endAngle = Math.toRadians(startAngleClockwise);

        Bukkit.getLogger().info("start angle: " + startAngle);
        Bukkit.getLogger().info("end angle: " + endAngle);

        //get Air blocks, reset water blocks for collection
        final HashSet<Block> airBlocks = new HashSet<>(BlockUtils.getMaterialBlocks(segmentGroup, segmentNumber, Material.AIR));
        HashSet<Block> waterBlocks = new HashSet<>();

        SegmentChecker segmentChecker = new SegmentChecker(segmentGroup, segmentNumber);

        world.setGameRule(GameRule.WATER_SOURCE_CONVERSION, false);

        prepareSegmentEdgesForWave(center, startAngle, endAngle, outerRadius, innerRadius, world, segmentHeight, wavePeakHeight, airBlocks);

        Bukkit.getLogger().info("Starting Wave");
        segmentUtils.setEffectActive(segmentGroup, segmentNumber, "WAVE", true);

        waveTask = new BukkitRunnable() {
            double waveFront = outerRadius;
            double waveBack = outerRadius + waveThickness;

            final double stopRadius = stopAtInner ? innerRadius : 0;

            @Override
            public void run() {

                // Check if the wave has reached the inner segment
                if (waveBack <= stopRadius) { //uses stopRadius, either innerRadius or 0 for center
                    for (Block airBlock : airBlocks) {

                        if (airBlock.getType() == Material.WATER || airBlock.getType() == Material.STRUCTURE_VOID) {
                                airBlock.setType(Material.AIR);
                        }
                    }
                    this.cancel();
                    stop(segmentGroup, segmentNumber);
                    return;
                }

                // Handle wave clearing
                handleWaveClearing(center, startAngle, endAngle, waveFront, waveBack, wavePeakHeight, outerRadius, waterBlocks);
                // Handle wave advance
                if (waveFront >= stopRadius) {
                    handleWaveAdvance(center, startAngle, endAngle, waveFront, waveBack, wavePeakHeight, airBlocks, waterBlocks);
                }

                // Update the wave's front and back
                waveFront -= waveSpread;
                waveBack -= waveSpread;
            }
        };
        waveTask.runTaskTimer(JoTHungerGames.getInstance(), 0L, waveSpeed); // Adjust the period as needed for the wave speed

        pushTask = new BukkitRunnable() {

            //pushes players along with the wave if they are caught in it

            @Override
            public void run() {

                Set<Player> currentPlayersInSegment = new HashSet<>();
                for (Player player : Bukkit.getOnlinePlayers()) {
                    Location playerLocation = player.getLocation();
                    if (segmentChecker.isInsideSegment(playerLocation)) {
                        if (playerLocation.getBlock().getType() == Material.WATER) {

                            double dx = center.getX() - playerLocation.getX();
                            double dy = center.getY() - playerLocation.getY();
                            double dz = center.getZ() - playerLocation.getZ();
                            double length = Math.sqrt(dx * dx + dy * dy + dz * dz);

                            // Normalize and apply a speed factor
                            dx /= length;
                            dy /= length;
                            dz /= length;
                            dx *= waveStrength;
                            dy *= waveStrength;
                            dz *= waveStrength;

                            // Apply velocity
                            player.setVelocity(new Vector(dx, dy, dz));

                        }
                    }
                }
            }
        };
        pushTask.runTaskTimer(JoTHungerGames.getInstance(), 0L, 5L);
    }

    private void handleWaveAdvance (Location center, double startAngle, double endAngle, double waveFront, double waveBack, int wavePeakHeight, HashSet<Block> airBlocks, HashSet<Block> waterBlocks) {
        World world = center.getWorld();

        // Calculate the wave's midpoint and the range of the wave
        double waveMidPoint = (waveFront + waveBack) / 2;
        double waveRange = (waveBack - waveFront) / 2;

        for (double radius = waveBack; radius >= waveFront; radius--) {
            for (double angle = startAngle; angle >= endAngle; angle -= Math.PI / 180) {
                double x = center.getX() + radius * Math.cos(angle);
                double z = center.getZ() + radius * Math.sin(angle);

                // Calculate the normalized distance of current radius from the wave's midpoint
                double normalizedDistance = (radius - waveMidPoint) / waveRange; // This will be 0 at the center and -1 or 1 at the waveFront or waveBack
                // Calculate the height factor as a function of the distance from the wave's midpoint
                double heightFactor = Math.cos(Math.PI * normalizedDistance); // Cosine curve: 1 at the center of the wave, 0 at the edges
                heightFactor = (heightFactor + 1) / 2; // Normalize between 0 and 1
                int waveHeight = (int) (wavePeakHeight * heightFactor); // Apply the height factor

                for (int y = center.getBlockY(); y < center.getBlockY() + waveHeight; y++) {
                    Location loc = new Location(world, x, y, z);
                    assert world != null;
                    Block block = world.getBlockAt(loc);
                    Material blockType = block.getType();

                    if (airBlocks.contains(block) && blockType != Material.VINE) {
                        if (radius >= waveFront && radius <= waveBack) {
                            if (blockType == Material.AIR) {
                                block.setType(Material.WATER);
                                waterBlocks.add(block);
                            }
                        }
                    }
                }
            }
        }
    }

    private void handleWaveClearing(Location center, double startAngle, double endAngle, double waveFront, double waveBack, int wavePeakHeight, double outerRadius, HashSet<Block> waterBlocks) {
        World world = center.getWorld();

        // Calculate the wave's midpoint and the range of the wave
        double waveMidPoint = (waveFront + waveBack) / 2;
        double waveRange = (waveBack - waveFront) / 2;

        // Loop over the area just behind the wave back to clear any remaining water
        for (double radius = outerRadius + 1; radius >= waveMidPoint + 1; radius--) {
            for (double angle = startAngle; angle >= endAngle; angle -= Math.PI / 180) {
                double x = center.getX() + radius * Math.cos(angle);
                double z = center.getZ() + radius * Math.sin(angle);

                // Calculate the normalized distance of current radius from the wave's midpoint
                double normalizedDistance = (radius - waveMidPoint) / waveRange; // This will be 0 at the center and -1 or 1 at the waveFront or waveBack
                // Calculate the height factor as a function of the distance from the wave's midpoint
                double heightFactor = 0.5 * (1 + Math.cos(Math.PI * normalizedDistance)); // Cosine curve: 1 at the center of the wave, 0 at the edges
                int removalHeight = (int) (wavePeakHeight * heightFactor); // Apply the height factor

                for (int y = center.getBlockY() + wavePeakHeight; y > center.getBlockY() + removalHeight; y--) {
                    Location loc = new Location(world, x, y, z);
                    assert world != null;
                    Block block = world.getBlockAt(loc);

                    if (waterBlocks.contains(block)) {
                        if (block.getType() == Material.WATER) {
                            block.setType(Material.AIR); // Remove water blocks
                        }
                    }
                }
            }
        }
    }

    private void prepareSegmentEdgesForWave(Location center, double startAngle, double endAngle, int outerRadius, int innerRadius, World world, int segmentHeight, int wavePeakHeight, HashSet<Block> airBlocks) {
        // Calculate edge blocks based on angles and replace air with structure voids
        // This is a conceptual representation. You'll need to implement the actual logic based on how your segments are defined
        for (int radius = outerRadius; radius >= innerRadius; radius--) {

            double xStart = center.getX() + radius * Math.cos(startAngle);
            double zStart = center.getZ() + radius * Math.sin(startAngle);
            for (int yStart = center.getBlockY() + wavePeakHeight; yStart > center.getBlockY(); yStart--) {
                Location loc = new Location(world, xStart, yStart, zStart);
                assert world != null;
                Block block = world.getBlockAt(loc);

                if (block.getType() == Material.AIR) {
                    block.setType(Material.STRUCTURE_VOID); // Replace air with structure void so that water cannot flow outward
                }
            }

            double xEnd = center.getX() + radius * Math.cos(endAngle);
            double zEnd = center.getZ() + radius * Math.sin(endAngle);
            for (int yEnd = center.getBlockY() + wavePeakHeight; yEnd > center.getBlockY(); yEnd--) {
                Location loc = new Location(world, xEnd, yEnd, zEnd);
                assert world != null;
                Block block = world.getBlockAt(loc);

                if (block.getType() == Material.AIR) {
                    block.setType(Material.STRUCTURE_VOID); // Replace air with structure void so that water cannot flow outward
                }
            }
        }
    }


    @Override
    public void stop(String segmentGroup, int segmentNumber) {
        // Stop effect logic
        World world = SegmentUtils.getWorld(segmentGroup);
        if (pushTask != null) {
            pushTask.cancel(); //cancel running task
            pushTask = null;
        }
        if (waveTask != null) {
            Bukkit.getLogger().info("Stopping Wave");
            waveTask.cancel(); //cancel running task
            waveTask = null;
            world.setGameRule(GameRule.WATER_SOURCE_CONVERSION, true);
        }
        SegmentUtils.setEffectActive(segmentGroup, segmentNumber, "WAVE", false);
        //restore segment
        SegmentUtils.restoreSegment(segmentGroup, segmentNumber, segmentBackup);
    }
}