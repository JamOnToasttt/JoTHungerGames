package me.jamontoast.jothungergames.Effects;

import me.jamontoast.jothungergames.JoTHungerGames;
import me.jamontoast.jothungergames.Interfaces.SegmentEffectStrategy;
import me.jamontoast.jothungergames.Utilities.BlockUtils;
import me.jamontoast.jothungergames.Utilities.SegmentUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.block.data.type.PinkPetals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class PoisonFlowersEffect implements SegmentEffectStrategy {

    private BukkitRunnable flowersSpreadTask; //define runnable
    private BukkitRunnable flowersClearTask; //define runnable
    private BukkitRunnable poisonTask; //define runnable
    private BukkitRunnable particlesTask; //define runnable
    private BukkitRunnable stopPoisonFlowersEffectTask; //define runnable
    private final Random random = new Random();
    private HashSet<Block> airNextToSolid = new HashSet<>();
    private final HashSet<Block> flowerBlocks = new HashSet<>();
    private final HashSet<Location> particleSpawns = new HashSet<>();

    @Override
    public void start(String segmentGroup, int segmentNumber, Long durationOverride) {
        // Start effect logic

        //initialise variables from config
        long effectDuration = 60;
        if (durationOverride != null) {
            effectDuration = durationOverride;
        } else if (!JoTHungerGames.getInstance().getConfig().getBoolean("effects master controls.enabled")) {
            effectDuration = JoTHungerGames.getInstance().getConfig().getLong("effects.poison_flowers.duration") * 20; //Convert seconds to ticks
        } else {
            effectDuration = JoTHungerGames.getInstance().getConfig().getLong("effects master controls.duration") * 20; //Convert seconds to ticks
        }

        //Get all air blocks next to air-exposed blocks in segment
        airNextToSolid = BlockUtils.getAirNextToSolid(segmentGroup, segmentNumber);
        if (airNextToSolid.isEmpty()) {
            // There are no air blocks above the surface in this segment, so return.
            Bukkit.getLogger().warning("No suitable air blocks for flowers in segment: " + segmentNumber);
            return;
        }

        Bukkit.getLogger().info("Starting Poison Flowers");
        SegmentUtils.setEffectActive(segmentGroup, segmentNumber, "POISON_FLOWERS", true);

        List<Block> potentialFlowerBlocks = new ArrayList<>(airNextToSolid);
        int blocksToChange = potentialFlowerBlocks.size() / 5;  // Set the number of blocks you want to change each cycle

        flowersSpreadTask = new BukkitRunnable() {

                @Override
                public void run() {

                    if (potentialFlowerBlocks.isEmpty()) {
                        Bukkit.getLogger().info("Finished flower spread");
                        this.cancel();
                    }

                    Collections.shuffle(potentialFlowerBlocks);  // Randomly shuffle the list

                    for (int i = 0; i < blocksToChange && !potentialFlowerBlocks.isEmpty(); i++) {
                        Block block = potentialFlowerBlocks.remove(0); // Take and remove the first block from the shuffled list

                        if (!flowerBlocks.contains(block) && block.getType() == Material.AIR && (random.nextInt(52) < 1)) {

                            Material flowerType = determineFlowerType(block);
                            Location flowerLocation = block.getLocation();
                            World world = flowerLocation.getWorld();
                            BlockData data;

                            if (flowerType == Material.PITCHER_PLANT) {
                                Block topBlock = block.getRelative(BlockFace.UP);
                                topBlock.setType(flowerType);
                                if (topBlock.getBlockData() instanceof Bisected topBisected) {
                                    topBisected.setHalf(Bisected.Half.TOP);
                                    topBlock.setBlockData(topBisected); // Apply the block data changes to the block
                                }

                                block.setType(flowerType);  // Set the block to the determined flower
                                data = block.getBlockData();
                                if (data instanceof Bisected bottomBisected) {
                                    bottomBisected.setHalf(Bisected.Half.BOTTOM);
                                    block.setBlockData(bottomBisected); // Apply the block data changes to the block
                                }
                                flowerBlocks.add(topBlock);
                            }
                            else if (flowerType == Material.PINK_PETALS) {
                                block.setType(flowerType);  // Set the block to the determined flower
                                data = block.getBlockData();
                                if (data instanceof PinkPetals petalNumber) {
                                    // Safe cast because we checked with instanceof
                                    petalNumber.setFlowerAmount(random.nextInt(4) + 1); // Randomize the number of flowers for pinkPetals
                                    block.setBlockData(petalNumber);
                                }
                            }
                            else if (flowerType == Material.GLOW_LICHEN) {
                                block.setType(flowerType);  // Set the block to the determined flower
                                data = block.getBlockData();

                                if (data instanceof MultipleFacing multipleFacing) {

                                    // Reset all allowed faces to false
                                    for (BlockFace face : multipleFacing.getAllowedFaces()) {
                                        multipleFacing.setFace(face, false);
                                    }

                                    // Identify and set the appropriate faces to true
                                    for (BlockFace face : multipleFacing.getAllowedFaces()) {
                                        Block relativeBlock = block.getRelative(face);
                                        if (relativeBlock.getType().isSolid()) {
                                            multipleFacing.setFace(face, true);
                                        }
                                    }

                                    // Apply the block data changes to the block
                                    block.setBlockData(multipleFacing);
                                }
                            }
                            else {
                                block.setType(flowerType);  // Set the block to the determined flower
                            }

                            flowerBlocks.add(block); // Add to the set of blocks that have been changed to flowers

                            // Define the radius for particle effect around each flower
                            int radius = 2; // Adjust as needed
                            for (int dx = -radius; dx <= radius; dx++) {
                                for (int dy = -radius; dy <= radius; dy++) {
                                    for (int dz = -radius; dz <= radius; dz++) {
                                        // Calculate the location for the particle
                                        Location particleLocation = flowerLocation.clone().add(dx, dy, dz);

                                        // Check if the location is suitable for spawning particles
                                        assert world != null;
                                        if (world.getBlockAt(particleLocation).getType() == Material.AIR) {
                                            // Spawn the particle at the calculated location
                                            double xOffset = random.nextGaussian() * 0.2;
                                            double yOffset = random.nextGaussian() * 0.2;
                                            double zOffset = random.nextGaussian() * 0.2;
                                            particleSpawns.add(particleLocation);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
        };
        flowersSpreadTask.runTaskTimer(JoTHungerGames.getInstance(), 0L, 20L); //repeats every second

        poisonTask = new BukkitRunnable() {

            @Override
            public void run() {

                for (Block flowerBlock : flowerBlocks) {
                    Location flowerLocation = flowerBlock.getLocation();
                    World world = flowerLocation.getWorld();
                    if (world == null) continue;

                    // Assuming a radius of 3 blocks for poison effect. Adjust as needed.
                    double radius = 2.0;
                    Collection<Entity> nearbyEntities = world.getNearbyEntities(flowerLocation, radius, radius, radius);
                    for (Entity entity : nearbyEntities) {
                        if (entity instanceof Player player) {
                            // Apply poison effect for a certain duration and intensity. Adjust as needed.
                            player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 1));
                        }
                    }
                }
            }
        };
        poisonTask.runTaskTimer(JoTHungerGames.getInstance(), 20L, 20L); //repeats every second

        particlesTask = new BukkitRunnable() {

            @Override
            public void run() {

                Bukkit.getLogger().info("Spawning particles");
                for (Location currentLocation : particleSpawns) {
                    World world = currentLocation.getWorld();

                    // Check if there are any players within a certain radius (e.g., 10 blocks)
                    Collection<Entity> nearbyEntities = Objects.requireNonNull(world).getNearbyEntities(currentLocation, 12, 12, 12);

                    // If a player is nearby, then spawn particles
                    for (Entity entity : nearbyEntities) {
                        if (entity instanceof Player) {
                            if (random.nextBoolean()) {
                                double xOffset = random.nextGaussian() * 0.2; // 0.2 as standard deviation, adjust as needed
                                double yOffset = random.nextGaussian() * 0.2;
                                double zOffset = random.nextGaussian() * 0.2;
                                Objects.requireNonNull(world).spawnParticle(Particle.SPORE_BLOSSOM_AIR, currentLocation, 20, xOffset, yOffset, zOffset, 0);
                            }
                            break;
                        }
                    }
                }
            }
        };
        particlesTask.runTaskTimer(JoTHungerGames.getInstance(), 20L, 40L); //repeats every 2 seconds

        stopPoisonFlowersEffectTask = new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.getLogger().info("flowers stop initiated");
                clearFlowers(segmentGroup, segmentNumber, flowerBlocks);
            }
        };
        stopPoisonFlowersEffectTask.runTaskLater(JoTHungerGames.getInstance(), effectDuration);  // Starts after the effect duration has run out
    }

    private Material determineFlowerType(Block block) {

        Material flowerType;
        boolean isUnderside = false;
        boolean isGrounded = false;
        boolean isDoubleAir = false;
        // Check all six adjacent blocks (above, below, north, south, east, west)
        BlockFace[] faces = new BlockFace[]{
                        BlockFace.UP, BlockFace.DOWN,
                        BlockFace.NORTH, BlockFace.SOUTH,
                        BlockFace.EAST, BlockFace.WEST
        };
        for (BlockFace face : faces) {
            Block relative = block.getRelative(face);
            if (relative.getType() != Material.AIR && relative.getType().isSolid() && face.equals(BlockFace.UP)) {
                isUnderside = true;
                break;
            }else if (relative.getType() == Material.AIR && face.equals(BlockFace.UP)) {
                isDoubleAir = true;
            }
            else if (relative.getType() != Material.AIR && relative.getType().isSolid() && face.equals(BlockFace.DOWN)) {
                isGrounded = true;
            }
        }

        // If any adjacent block is air, add the current block to the exposed blocks set
        if (isUnderside) {
            flowerType = Material.SPORE_BLOSSOM;
        }
        else if (isGrounded){
            int randomInt = random.nextInt(2);
            if (randomInt == 0) {
                flowerType = Material.PINK_PETALS;
            } else {
                if (isDoubleAir) {
                    flowerType = Material.PITCHER_PLANT;
                }
                else {
                    flowerType = Material.PINK_PETALS;
                }
            }
        }
        else {
            flowerType = Material.GLOW_LICHEN;
        }

        return flowerType;
    }

    private void clearFlowers (String segmentGroup, int segmentNumber, HashSet<Block> flowerBlocks) {
        Bukkit.getLogger().info("Starting clear flowers");
        List<Block> flowerBlocksList = new ArrayList<>(flowerBlocks);
        int blocksToChange = flowerBlocksList.size() / 10;  // Set the number of blocks you want to change each cycle
        if(particlesTask != null) {
            particlesTask.cancel();
            particlesTask = null; // Reset task
        }

        flowersClearTask = new BukkitRunnable() {

            @Override
            public void run() {

                if (flowerBlocksList.isEmpty()) {
                    stop(segmentGroup, segmentNumber);
                    this.cancel();
                }

                Collections.shuffle(flowerBlocksList);  // Randomly shuffle the list

                for (int i = 0; i < blocksToChange && !flowerBlocksList.isEmpty(); i++) {
                    Block block = flowerBlocksList.remove(0); // Take and remove the first block from the shuffled list
                    Material blockType = block.getType();

                    if (flowerBlocks.contains(block) && (blockType == Material.SPORE_BLOSSOM || blockType == Material.PINK_PETALS || blockType == Material.PITCHER_PLANT || blockType == Material.GLOW_LICHEN)) {
                        block.setType(Material.AIR);  // Set the block to air
                    }
                }
            }
        };
        flowersClearTask.runTaskTimer(JoTHungerGames.getInstance(), 0L,  10L);  // Repeats every half second
    }

    @Override
    public void stop(String segmentGroup, int segmentNumber) {
        // Stop effect logic
        if(poisonTask != null) {
            Bukkit.getLogger().info("Stopping Poisonous Flowers");
            poisonTask.cancel();
            poisonTask = null; // Reset task
        }
        if(particlesTask != null) {
            particlesTask.cancel();
            particlesTask = null; // Reset task
        }
        if(flowersSpreadTask != null) {
            flowersSpreadTask.cancel();
            flowersSpreadTask = null; // Reset task
        }
        if(flowersClearTask != null) {
            flowersClearTask.cancel();
            flowersClearTask = null; // Reset task
        }
        if(stopPoisonFlowersEffectTask != null) {
            stopPoisonFlowersEffectTask.cancel();
            stopPoisonFlowersEffectTask = null; // Reset task
        }
        for (Block block : airNextToSolid) {
            Material blockType = block.getType();
            if (blockType == Material.SPORE_BLOSSOM || blockType == Material.PINK_PETALS || blockType == Material.PITCHER_PLANT || blockType == Material.GLOW_LICHEN) {
                block.setType(Material.AIR);
            }
        }

        SegmentUtils.setEffectActive(segmentGroup, segmentNumber, "POISON_FLOWERS", false);
    }
}