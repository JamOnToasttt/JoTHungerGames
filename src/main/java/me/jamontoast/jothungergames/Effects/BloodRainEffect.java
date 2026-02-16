package me.jamontoast.jothungergames.Effects;

import me.jamontoast.jothungergames.JoTHungerGames;
import me.jamontoast.jothungergames.Interfaces.SegmentEffectStrategy;
import me.jamontoast.jothungergames.Utilities.SegmentChecker;
import me.jamontoast.jothungergames.Utilities.SegmentUtils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;


import java.util.*;

public class BloodRainEffect implements SegmentEffectStrategy {



    private BukkitRunnable bloodRainTask;
    private BukkitRunnable stopBloodRainEffectTask;
    private final String BloodRain = "jothungergames.bloodrain_1";
    private final SegmentUtils segmentUtils = new SegmentUtils();
    private final Random random = new Random();
    private Set<Player> playersInSegment = new HashSet<>();
    private Map<Player, Long> playersHearingSound = new HashMap<>();

    @Override
    public void start(String segmentGroup, int segmentNumber, Long durationOverride) {

        SegmentChecker segmentChecker = new SegmentChecker(segmentGroup, segmentNumber);

        //initialise variables from config
        long effectDuration = 60;
        if (durationOverride != null) {
            effectDuration = durationOverride;
        } else if (!JoTHungerGames.getInstance().getConfig().getBoolean("effects master controls.enabled")) {
            effectDuration = JoTHungerGames.getInstance().getConfig().getLong("effects.blood_rain.duration") * 20; //Convert seconds to ticks
        } else {
            effectDuration = JoTHungerGames.getInstance().getConfig().getLong("effects master controls.duration") * 20; //Convert seconds to ticks
        }

        // Define the potion effects you want to apply
        PotionEffect blindnessEffect = new PotionEffect(PotionEffectType.BLINDNESS, 41, 1); // Duration just beyond 1 second to ensure continuous effect, and amplifier of 1

        Bukkit.getLogger().info("Starting Blood Rain");
        segmentUtils.setEffectActive(segmentGroup, segmentNumber, "BLOOD_RAIN", true);
        playersHearingSound.clear();

        bloodRainTask = new BukkitRunnable() {

            @Override
            public void run () {

                Set<Player> currentPlayersInSegment = new HashSet<>();

                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (segmentChecker.isInsideSegment(player.getLocation())) {

                        currentPlayersInSegment.add(player); // Add player to the set

                        // Apply blindness effect
                        player.addPotionEffect(blindnessEffect);

                        // Spawn raindrop particles above player's head
                        for (int i = 5; i <= 24; i++) {
                            spawnParticlesAbovePlayer(player, 20, i);
                        }

                        // Play rain sound effect for the player
                        long currentTime = System.currentTimeMillis();
                        long fiveMinutesInMillis = 5 * 60 * 1000; // 5 minutes in milliseconds

                        if (!playersHearingSound.containsKey(player) || (currentTime - playersHearingSound.get(player) > fiveMinutesInMillis)) {
                            Bukkit.getLogger().info("Playing blood rain sound for " + player);
                            player.playSound(player.getLocation(), BloodRain, SoundCategory.WEATHER, 10.0F, 1.0F);

                            playersHearingSound.put(player, currentTime);
                        }
                    }
                }
                // Detect players who have left the segment
                for (Player previousPlayer : playersInSegment) {
                    if (!currentPlayersInSegment.contains(previousPlayer)) {
                        // This player has left the segment.
                        // Play a short or silent sound to effectively stop the current sound.
                        previousPlayer.stopSound(BloodRain, SoundCategory.WEATHER);
                        playersHearingSound.remove(previousPlayer);
                    }
                }

                playersInSegment = currentPlayersInSegment; // Update our record of players in segment.

            }
        };
        bloodRainTask.runTaskTimer(JoTHungerGames.getInstance(), 0L,  20L);  // Repeats every second

        stopBloodRainEffectTask = new BukkitRunnable() {

            @Override
            public void run() {
                stop(segmentGroup, segmentNumber);
            }
        };
        stopBloodRainEffectTask.runTaskLater(JoTHungerGames.getInstance(), effectDuration);  // Starts after the effect duration has run out
    }

    private void spawnParticlesAbovePlayer(Player player, int particlesAmount, double height) {
        double width = 10.0; // Width of the area where particles will spawn.
        double length = 10.0; // Length of the area where particles will spawn.

        for (int i = 0; i < particlesAmount; i++) {
            double x = player.getLocation().getX() - width / 2.0 + random.nextDouble() * width;
            double z = player.getLocation().getZ() - length / 2.0 + random.nextDouble() * length;
            double y = player.getLocation().getY() + height;

            Location particleLocation = new Location(player.getWorld(), x, y, z);
            player.getWorld().spawnParticle(Particle.FALLING_LAVA, particleLocation, 1);
        }
    }

    @Override
    public void stop(String segmentGroup, int segmentNumber) {
        if(bloodRainTask != null) {
            Bukkit.getLogger().info("Stopping Blood Rain");
            bloodRainTask.cancel();
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.stopSound(BloodRain, SoundCategory.WEATHER);
            }
            playersHearingSound.clear();
            bloodRainTask = null; // Reset task after canceling
        }
        if (stopBloodRainEffectTask != null) {
            stopBloodRainEffectTask.cancel();
            stopBloodRainEffectTask = null; // Reset task after canceling
        }
        segmentUtils.setEffectActive(segmentGroup, segmentNumber, "BLOOD_RAIN", false);
    }
}