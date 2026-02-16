package me.jamontoast.jothungergames.Utilities;

import me.jamontoast.jothungergames.JoTHungerGames;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SegmentUtils {

    private static final JoTHungerGames plugin = JoTHungerGames.getInstance();

    public static void setEffectActive(String segmentGroup, int segmentNumber, String effectName, boolean isActive) {
        String basePath = "segment groups." + segmentGroup + ".segments." + segmentNumber + ".activeEffects";
        if (isActive) {
            plugin.getSegmentConfig().set(basePath + "." + effectName, true);
            Bukkit.getLogger().info("Effect " + effectName + " set to ACTIVE.");
        }
        else {
            plugin.getSegmentConfig().set(basePath + "." + effectName, null);
            Bukkit.getLogger().info("Effect " + effectName + " set to INACTIVE.");
        }
        plugin.saveSegmentConfig();
    }

    public static World getWorld(String segmentGroup) {
        FileConfiguration config = plugin.getSegmentConfig();
        String basePath = "segment groups." + segmentGroup;
        return Bukkit.getWorld(config.getString(basePath + ".world"));
    }

    public static Map<Block, BlockState> getSegmentBackup(String segmentGroup, int segmentNumber) {
        List<JoTHungerGames.Segment> segmentsList = JoTHungerGames.getInstance().getSegmentsFromConfig(segmentGroup);
        if(segmentsList.size() < segmentNumber) {
            // Log an error or handle this scenario
            return new HashMap<>(); // Empty list or however you wish to handle this
        }

        Map<Block, BlockState> segmentBackup = new HashMap<>();

        // Find the segment by number
        JoTHungerGames.Segment desiredSegment = segmentsList.get(segmentNumber - 1);

        for (Location blockLocation : desiredSegment.getBlocks()) {
            World world = blockLocation.getWorld();
            int x = blockLocation.getBlockX();
            int y = blockLocation.getBlockY();
            int z = blockLocation.getBlockZ();

            assert world != null;
            Block block = world.getBlockAt(x, y, z);
            BlockState blockState = block.getState();
            segmentBackup.put(block, blockState);

        }
        return segmentBackup;
    }

    public static void restoreSegment(String segmentGroup, int segmentNumber, Map<Block, BlockState> segmentBackup) {

        World world = getWorld(segmentGroup); // Assuming the world is the same for all blocks in the segment

        // Iterate through each entry in the segment backup
        for (Map.Entry<Block, BlockState> entry : segmentBackup.entrySet()) {
            BlockState backedUpState = entry.getValue(); // The state of the block from the backup

            // Get the current block at the backed-up block's location in the world
            Block currentBlock = world.getBlockAt(backedUpState.getLocation());

            // Check and set type if different
            if (!currentBlock.getType().equals(backedUpState.getType()) ||
                    !currentBlock.getBlockData().matches(backedUpState.getBlockData())) {
                backedUpState.update(true, false);
            }
        }
    }

    public static Location getSegmentCenter(String segmentGroup, int segmentNumber) {
        FileConfiguration config = plugin.getSegmentConfig();

        String basePath = "segment groups." + segmentGroup;

        String[] centerCoords = config.getString(basePath + ".center").split(",");
        World world = Bukkit.getWorld(config.getString(basePath + ".world"));
        Location center = new Location(world, Integer.parseInt(centerCoords[0]), Integer.parseInt(centerCoords[1]), Integer.parseInt(centerCoords[2]));

        int outerRadius = config.getInt(basePath + ".outerRadius");
        int innerRadius = config.getInt(basePath + ".innerRadius");

        double startAngle = Math.toRadians(config.getDouble(basePath + ".segments." + segmentNumber + ".startAngle"));
        double endAngle = Math.toRadians(config.getDouble(basePath + ".segments." + segmentNumber + ".endAngle"));

        // Average angle in radians
        double avgAngle = (startAngle + endAngle) / 2.0;

        // Midpoint radius
        double midpointRadius = (outerRadius + innerRadius) / 2.0;


        // Calculate the coordinates for the center point of the segment
        double x = center.getX() + (midpointRadius * Math.cos(avgAngle));
        double y = center.getY();  // Assuming the segment is flat
        double z = center.getZ() + (midpointRadius * Math.sin(avgAngle));

        Bukkit.getLogger().info("Segment Center is " + x + " " + y + " " + z);
        // Create a new Location object for the center point
        return new Location(world, x, y, z);
    }

    public static int getHeight(String segmentGroup) {
        FileConfiguration config = plugin.getSegmentConfig();

        String basePath = "segment groups." + segmentGroup;
        return config.getInt(basePath + ".height");
    }
    public static String[] getGroupCenter(String segmentGroup) {
        FileConfiguration config = plugin.getSegmentConfig();

        String basePath = "segment groups." + segmentGroup;
        String centerStr = config.getString(basePath + ".center");
        return centerStr.split(",");
    }
    public static int getOuterRadius(String segmentGroup) {
        FileConfiguration config = plugin.getSegmentConfig();

        String basePath = "segment groups." + segmentGroup;
        return config.getInt(basePath + ".outerRadius");
    }
    public static int getInnerRadius(String segmentGroup) {
        FileConfiguration config = plugin.getSegmentConfig();

        String basePath = "segment groups." + segmentGroup;
        return config.getInt(basePath + ".innerRadius");
    }
    public static double getStartAngle(String segmentGroup, int segmentNumber) {
        FileConfiguration config = plugin.getSegmentConfig();

        String basePath = "segment groups." + segmentGroup;
        double startAngle = config.getDouble(basePath + ".segments." + segmentNumber + ".startAngle");

        return startAngle;
    }
    public static double getEndAngle(String segmentGroup, int segmentNumber) {
        FileConfiguration config = plugin.getSegmentConfig();

        String basePath = "segment groups." + segmentGroup;
        double endAngle = config.getDouble(basePath + ".segments." + segmentNumber + ".endAngle");

        return endAngle;
    }

}
