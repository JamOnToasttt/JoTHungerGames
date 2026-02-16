package me.jamontoast.jothungergames.Effects;

import me.jamontoast.jothungergames.JoTHungerGames;
import me.jamontoast.jothungergames.Interfaces.SegmentEffectStrategy;
import me.jamontoast.jothungergames.Utilities.BlockUtils;
import me.jamontoast.jothungergames.Utilities.SegmentChecker;
import me.jamontoast.jothungergames.Utilities.SegmentUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class ParalysingFogEffect implements SegmentEffectStrategy {

    private BukkitRunnable fogSpreadTask; //Task for spreading fog
    private BukkitRunnable particleTask; //Task for reapplying particles
    private BukkitRunnable effectApplyTask; // Task for applying effects to players
    private BukkitRunnable stopFogEffectTask;
    private int spreadFactor = 1; // To control the number of blocks fog will spread to each iteration
    private int spreadFactorInt = 1; // To control the number of blocks fire will spread to each iteration
    private final SegmentUtils segmentUtils = new SegmentUtils();
    private final Random random = new Random();
    private final Set<String> validAirBlocksAboveSurface = new HashSet<>();
    private Location fogOrigin;
    private final Queue<Location> fogQueue = new LinkedList<>();
    private final Set<String> visitedBlocks = new HashSet<>();

    @Override
    public void start(String segmentGroup, int segmentNumber, Long durationOverride) {
        //clear old values just in case
        visitedBlocks.clear();//clears affected blocks
        fogQueue.clear();

        Location segmentCenter = SegmentUtils.getSegmentCenter(segmentGroup, segmentNumber);

        //initialise variables from config
        long effectDuration = 60;
        if (durationOverride != null) {
            effectDuration = durationOverride;
        } else if (!JoTHungerGames.getInstance().getConfig().getBoolean("effects master controls.enabled")) {
            effectDuration = JoTHungerGames.getInstance().getConfig().getLong("effects.paralysing_fog.duration") * 20; //Convert seconds to ticks
        } else {
            effectDuration = JoTHungerGames.getInstance().getConfig().getLong("effects master controls.duration") * 20; //Convert seconds to ticks
        }
        long intervalTicks = JoTHungerGames.getInstance().getConfig().getLong("effects.paralysing_fog.interval") * 20;  // Convert seconds to ticks
        int fogHeight = JoTHungerGames.getInstance().getConfig().getInt("effects.paralysing_fog.height");

        SegmentChecker segmentChecker = new SegmentChecker(segmentGroup, segmentNumber);

        // Get all valid air blocks above the surface
        HashSet<Block> airBlocksAboveSurface = BlockUtils.getAirBlocksAboveSurface(segmentGroup, segmentNumber, fogHeight);
        for (Block block : airBlocksAboveSurface) {
            validAirBlocksAboveSurface.add(serializeLocation(block.getLocation()));
        }
        if (airBlocksAboveSurface.isEmpty()) {
            // There are no air blocks above the surface in this segment, so return.
            Bukkit.getLogger().warning("No suitable air blocks for Paralysing Fog in segment: " + segmentNumber);
            return;
        }

        // Choose center block as the fog origin
        fogOrigin = BlockUtils.getClosestSurfaceBlockToCenter(airBlocksAboveSurface, segmentCenter).getLocation();

        fogQueue.add(fogOrigin);
        visitedBlocks.add(serializeLocation(fogOrigin));


        Bukkit.getLogger().info("Starting Paralysing Fog");
        SegmentUtils.setEffectActive(segmentGroup, segmentNumber, "PARALYSING_FOG", true);

        // Scheduled task to expand fog and check for players
        fogSpreadTask = new BukkitRunnable() {
            @Override
            public void run() {

                for (int i = 0; i < spreadFactor; i++) { // Spread the fog 'spreadFactor' number of times in each iteration
                    if (fogQueue.isEmpty()) {
                        break; // Break if the queue is empty
                    }

                    Location current = fogQueue.poll();

                    double xOffset = random.nextGaussian() * 0.2; // 0.2 as standard deviation, adjust as needed
                    double yOffset = random.nextGaussian() * 0.2;
                    double zOffset = random.nextGaussian() * 0.2;

                    // Render fog at current location
                    Objects.requireNonNull(current.getWorld()).spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, current, 20, xOffset, yOffset, zOffset, 0);

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

                        if (shouldAdd && validAirBlocksAboveSurface.contains(serializeLocation(neighbor)) && !visitedBlocks.contains(serializeLocation(neighbor))) {
                            fogQueue.add(neighbor);
                            visitedBlocks.add(serializeLocation(neighbor));
                        }
                    }
                }

                // Update spreadFactor for the next iteration
                spreadFactorInt += 1;
                spreadFactor = ((spreadFactorInt * 6)) * 2;
            }

        };
        fogSpreadTask.runTaskTimer(JoTHungerGames.getInstance(), 0L,  intervalTicks);  // Repeats every interval

        if (effectApplyTask != null && !effectApplyTask.isCancelled()) {
            effectApplyTask.cancel();
        }
        effectApplyTask = new BukkitRunnable() {
            @Override
            public void run() {

                // Check for players in fog
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (visitedBlocks.contains(serializeLocation(player.getLocation()))) {
                        if (player.getLocation().getBlock().getType() == Material.WATER) {
                            player.removePotionEffect(PotionEffectType.SLOWNESS);
                            player.removePotionEffect(PotionEffectType.POISON);
                        } else {
                            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 101, 1)); // Example values
                            player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 41, 1)); // Example values
                        }
                    } else if ((player.getPotionEffect(PotionEffectType.SLOWNESS) != null) || (player.getPotionEffect(PotionEffectType.POISON) != null)) {
                        if ((player.getLocation().getBlock().getType() == Material.WATER)) {
                            player.removePotionEffect(PotionEffectType.SLOWNESS);
                            player.removePotionEffect(PotionEffectType.POISON);
                            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 41, 3));
                        }
                    }
                }
            }
        };
        effectApplyTask.runTaskTimer(JoTHungerGames.getInstance(), 20L,  20L);  // Repeats every second

        if (particleTask != null && !particleTask.isCancelled()) {
            particleTask.cancel();
        }
        particleTask = new BukkitRunnable() {
            @Override
            public void run() {

                for (String serializedLoc : visitedBlocks) {
                    Location loc = deserializeLocation(serializedLoc, segmentGroup);

                    // Check if there are any players within a certain radius (e.g., 10 blocks)
                    Collection<Entity> nearbyEntities = Objects.requireNonNull(loc.getWorld()).getNearbyEntities(loc, 10, 10, 10);

                    // Check if any of the nearby entities are players
                    boolean playerNearby = nearbyEntities.stream().anyMatch(entity -> entity instanceof Player);

                    // If a player is nearby, then spawn particles
                    if (playerNearby) {
                        double xOffset = random.nextGaussian() * 0.2; // 0.2 as standard deviation, adjust as needed
                        double yOffset = random.nextGaussian() * 0.2;
                        double zOffset = random.nextGaussian() * 0.2;
                        Objects.requireNonNull(loc.getWorld()).spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, loc, 20, xOffset, yOffset, zOffset, 0);
                    }
                }
            }
        };
        particleTask.runTaskTimer(JoTHungerGames.getInstance(), 20L,  40L);  // Repeats every 2 seconds

        if (stopFogEffectTask != null && !stopFogEffectTask.isCancelled()) {
            stopFogEffectTask.cancel();
        }
        stopFogEffectTask = new BukkitRunnable() {

            @Override
            public void run() {
                stop(segmentGroup, segmentNumber);
            }
        };
        stopFogEffectTask.runTaskLater(JoTHungerGames.getInstance(), effectDuration);  // Starts after the effect duration has run out

    }

    private String serializeLocation(Location loc) {
        return loc.getBlockX() + "_" + loc.getBlockY() + "_" + loc.getBlockZ();
    }

    // A new method to deserialize location from your serialized string
    private Location deserializeLocation(String serialized, String segmentGroup) {
        String[] parts = serialized.split("_");
        World currentWorld = SegmentUtils.getWorld(segmentGroup); // Use SegmentChecker's getWorld method
        return new Location(
                currentWorld,
                Integer.parseInt(parts[0]),
                Integer.parseInt(parts[1]),
                Integer.parseInt(parts[2])
        );
    }

    @Override
    public void stop(String segmentGroup, int segmentNumber) {
        if(fogSpreadTask != null) {
            Bukkit.getLogger().info("Stopping Paralysing Fog");
            fogSpreadTask.cancel();
            fogSpreadTask = null;
        }
        if(effectApplyTask != null) {
            effectApplyTask.cancel();
            effectApplyTask = null;
        }
        if(particleTask != null) {
            particleTask.cancel();
            particleTask = null;
        }
        if(stopFogEffectTask != null) {
            stopFogEffectTask.cancel();
            stopFogEffectTask = null;
        }
        // Reset spreadFactor
        spreadFactor = 1;
        visitedBlocks.clear();//clears affected blocks
        fogQueue.clear();

        SegmentUtils.setEffectActive(segmentGroup, segmentNumber, "PARALYSING_FOG", false);
    }

}