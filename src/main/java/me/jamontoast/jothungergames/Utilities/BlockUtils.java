package me.jamontoast.jothungergames.Utilities;

import me.jamontoast.jothungergames.JoTHungerGames;
import me.jamontoast.jothungergames.JoTHungerGames.Segment;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.List;
import java.util.HashSet;
public class BlockUtils {


    public static HashSet<Block> getMaterialBlocks(String segmentGroup, int segmentNumber, Material material) {

        JoTHungerGames plugin = JoTHungerGames.getInstance();

        List<Segment> segmentsList = plugin.getSegmentsFromConfig(segmentGroup);

        if(segmentsList.size() < segmentNumber) {
            // Log an error or handle this scenario
            return new HashSet<>(); // Empty list or however you wish to handle this
        }

        // Find the segment by number
        Segment desiredSegment = segmentsList.get(segmentNumber - 1);

        HashSet<Block> materialBlocks = new HashSet<>();

        for (Location blockLocation : desiredSegment.getBlocks()) {
            World world = blockLocation.getWorld();
            assert world != null;
            Block block = world.getBlockAt(blockLocation);

            if (material.equals(block.getType())) {
                materialBlocks.add(block);
            }
        }
        return materialBlocks;
    }

    public static HashSet<Block> getFlammableBlocks(String segmentGroup, int segmentNumber) {
        List<Segment> segmentsList = JoTHungerGames.getInstance().getSegmentsFromConfig(segmentGroup);

        if(segmentsList.size() < segmentNumber) {
            // Log an error or handle this scenario
            return new HashSet<>(); // Empty list or however you wish to handle this
        }

        // Find the segment by number
        Segment desiredSegment = segmentsList.get(segmentNumber - 1);

        List<Material> flammableMaterials = JoTHungerGames.getInstance().getFlammableMaterials();
        HashSet<Block> flammableBlocks = new HashSet<>();

        for (Location blockLocation : desiredSegment.getBlocks()) {
            World world = blockLocation.getWorld();
            assert world != null;
            Block block = world.getBlockAt(blockLocation);
            if (!flammableMaterials.contains(block.getType())) {
                flammableBlocks.add(block);
            }
        }
        return flammableBlocks;
    }

    public static HashSet<Block> getAirExposedBlocks(String segmentGroup, int segmentNumber) {
        HashSet<Block> exposedBlocks = new HashSet<>();
        List<Segment> segmentsList = JoTHungerGames.getInstance().getSegmentsFromConfig(segmentGroup);

        if(segmentsList.size() < segmentNumber) {
            // Log an error or handle this scenario
            return new HashSet<>(); // Empty list or however you wish to handle this
        }

        // Find the segment by number
        Segment desiredSegment = segmentsList.get(segmentNumber - 1);

        World world = SegmentUtils.getWorld(segmentGroup); // Assuming all blocks are in the same world

        // Iterate through each block in the segment
        for (Location blockLocation : desiredSegment.getBlocks()) {
            Block block = world.getBlockAt(blockLocation);
            boolean isExposed = false;

            // Check all six adjacent blocks (above, below, north, south, east, west)
            BlockFace[] faces = new BlockFace[]{
                    BlockFace.UP, BlockFace.DOWN,
                    BlockFace.NORTH, BlockFace.SOUTH,
                    BlockFace.EAST, BlockFace.WEST
            };

            for (BlockFace face : faces) {
                Block relative = block.getRelative(face);
                if (relative.getType() == Material.AIR) {
                    isExposed = true;
                    break;
                }
            }

            // If any adjacent block is air, add the current block to the exposed blocks set
            if (isExposed) {
                exposedBlocks.add(block);
            }
        }
        return exposedBlocks;
    }

    public static HashSet<Block> getAirNextToSolid(String segmentGroup, int segmentNumber) {
        HashSet<Block> adjacentAirBlocks = new HashSet<>();
        List<Segment> segmentsList = JoTHungerGames.getInstance().getSegmentsFromConfig(segmentGroup);

        if(segmentsList.size() < segmentNumber) {
            // Log an error or handle this scenario
            return new HashSet<>(); // Empty list or however you wish to handle this
        }

        // Find the segment by number
        Segment desiredSegment = segmentsList.get(segmentNumber - 1);

        World world = SegmentUtils.getWorld(segmentGroup); // Assuming all blocks are in the same world

        // Iterate through each block in the segment
        for (Location blockLocation : desiredSegment.getBlocks()) {
            Block block = world.getBlockAt(blockLocation);
            boolean isAdjacentAir = false;

            if (block.getType() == Material.AIR) {
                // Check all six adjacent blocks (above, below, north, south, east, west)
                BlockFace[] faces = new BlockFace[]{
                        BlockFace.UP, BlockFace.DOWN,
                        BlockFace.NORTH, BlockFace.SOUTH,
                        BlockFace.EAST, BlockFace.WEST
                };
                for (BlockFace face : faces) {
                    Block relative = block.getRelative(face);
                    if (relative.getType() != Material.AIR && relative.getType().isSolid()) {
                        isAdjacentAir = true;
                        break;
                    }
                }

                // If any adjacent block is air, add the current block to the exposed blocks set
                if (isAdjacentAir) {
                    adjacentAirBlocks.add(block);
                }
            }
        }
        return adjacentAirBlocks;
    }

    private static final HashMap<String, HashSet<Block>> surfaceBlocksCache = new HashMap<>();

    public static HashSet<Block> getSurfaceBlocks(String segmentGroup, int segmentNumber) {

        String cacheKey = segmentGroup + "_" + segmentNumber;
        if (surfaceBlocksCache.containsKey(cacheKey)) {
            return surfaceBlocksCache.get(cacheKey);
        }

        JoTHungerGames plugin = JoTHungerGames.getInstance();

        List<Segment> segmentsList = plugin.getSegmentsFromConfig(segmentGroup);
        int segmentGroupHeight = plugin.getSegmentConfig().getInt("segment groups." + segmentGroup + ".height");
        String segmentGroupCenter = plugin.getSegmentConfig().getString("segment groups." + segmentGroup + ".center");
        String[] centerCoords = segmentGroupCenter.split(",");
        int centerY = Integer.parseInt(centerCoords[1]);

        if(segmentsList.size() < segmentNumber) {
            return new HashSet<>();
        }

        Segment desiredSegment = segmentsList.get(segmentNumber - 1);
        List<Material> ignoredMaterials = plugin.getIgnoredMaterials();
        HashSet<Block> surfaceBlocks = new HashSet<>();
        World world = null;

        for (Location blockLocation : desiredSegment.getBlocks()) {
            if (world == null) world = blockLocation.getWorld();
            int x = blockLocation.getBlockX();
            int y = blockLocation.getBlockY();
            int z = blockLocation.getBlockZ();

            if (y >= centerY && y <= (centerY + segmentGroupHeight)) {
                Block block = world.getBlockAt(x, y, z);
                Block aboveBlock = block.getRelative(0, 1, 0);
                if (!ignoredMaterials.contains(block.getType()) &&
                        (aboveBlock.getType() == Material.AIR || ignoredMaterials.contains(aboveBlock.getType()))) {
                    surfaceBlocks.add(block);
                }
            }
        }
        surfaceBlocksCache.put(cacheKey, surfaceBlocks);
        return surfaceBlocks;
    }


    public static void clearCache(String segmentGroup) {
        Configuration segmentConfig = JoTHungerGames.getInstance().getSegmentConfig();
        ConfigurationSection numSegmentsLocationSection = segmentConfig.getConfigurationSection("segment groups." + segmentGroup + ".segments");
        int numSegments = numSegmentsLocationSection.getKeys(false).size();

        for (int i = 1; i <= numSegments; i++) {
            surfaceBlocksCache.remove(segmentGroup + "_" + i);
        }
    }

    public static HashSet<Block> getAirBlocksAboveSurface(String segmentGroup, int segmentNumber, int maxHeight) {

        List<Segment> segmentsList = JoTHungerGames.getInstance().getSegmentsFromConfig(segmentGroup);

        if (segmentsList.size() < segmentNumber) {
            return new HashSet<>();
        }

        HashSet<Block> surfaceBlocks = getSurfaceBlocks(segmentGroup, segmentNumber);
        HashSet<Block> airBlocksAboveSurface = new HashSet<>();

        for (Block surfaceBlock : surfaceBlocks) {
            World world = surfaceBlock.getWorld();
            int x = surfaceBlock.getX();
            int y = surfaceBlock.getY();
            int z = surfaceBlock.getZ();

            for (int i = 1; i <= maxHeight; i++) {
                Block blockAbove = world.getBlockAt(x, y + i, z);
                if (blockAbove.getType() == Material.AIR) {
                    airBlocksAboveSurface.add(blockAbove);
                }
            }
        }

        return airBlocksAboveSurface;
    }

        public static Block getClosestSurfaceBlockToCenter(HashSet<Block> surfaceBlocks, Location center) {
            if (surfaceBlocks == null || surfaceBlocks.isEmpty()) {
                return null;
            }

            Block closestBlock = null;
            double closestDistance = Double.MAX_VALUE;

            for (Block block : surfaceBlocks) {
                double distance = center.distance(block.getLocation());

                if (distance < closestDistance) {
                    closestDistance = distance;
                    closestBlock = block;
                }
            }
            return closestBlock;
        }

        public static HashSet<Block> getClosestSurfaceBlocksToPlayer(Player player, HashSet<Block> surfaceBlocks, int radius) {
            HashSet<Block> closestSurfaceBlocks = new HashSet<>();
            Location playerLocation = player.getLocation();
            //a radius of 1 will net a 3x3x3 cube

            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        // Get the block at the current offset from the player
                        Block blockAtOffset = playerLocation.getWorld().getBlockAt(
                                playerLocation.getBlockX() + x,
                                playerLocation.getBlockY() + y,
                                playerLocation.getBlockZ() + z);

                        // Check if this block is a surface block
                        if (surfaceBlocks.contains(blockAtOffset)) {
                            closestSurfaceBlocks.add(blockAtOffset);
                        }
                    }
                }
            }

            return closestSurfaceBlocks;
        }
}
