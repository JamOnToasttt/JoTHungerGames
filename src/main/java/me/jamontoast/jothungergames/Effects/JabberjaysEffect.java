package me.jamontoast.jothungergames.Effects;

import me.jamontoast.jothungergames.JoTHungerGames;
import me.jamontoast.jothungergames.Interfaces.SegmentEffectStrategy;
import me.jamontoast.jothungergames.Utilities.SegmentChecker;
import me.jamontoast.jothungergames.Utilities.SegmentUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class JabberjaysEffect implements SegmentEffectStrategy {


    private BukkitRunnable soundTask;
    private BukkitRunnable nauseaTask;
    private BukkitRunnable stopJabberjaysEffectTask;
    private final SegmentUtils segmentUtils = new SegmentUtils();
    private final Random random = new Random();
    private Set<Player> playersInSegment = new HashSet<>();
    private Map<Player, Long> playersHearingSound = new HashMap<>();

    private final List<Sound> jabberjaySounds = Arrays.asList(
            Sound.ENTITY_ALLAY_DEATH,
            Sound.ENTITY_ENDERMAN_SCREAM,
            Sound.ENTITY_GHAST_HURT,
            Sound.ENTITY_HORSE_DEATH,
            Sound.ENTITY_PHANTOM_DEATH
            );

    private final PotionEffect nauseaEffect = new PotionEffect(PotionEffectType.NAUSEA, 161, 0); // Duration just beyond 6 seconds to ensure continuous effect, and amplifier of 1


    @Override
    public void start(String segmentGroup, int segmentNumber, Long durationOverride) {
        // Start effect logic

        //initialise variables from config
        long effectDuration = 60;
        if (durationOverride != null) {
            effectDuration = durationOverride;
        } else if (!JoTHungerGames.getInstance().getConfig().getBoolean("effects master controls.enabled")) {
            effectDuration = JoTHungerGames.getInstance().getConfig().getLong("effects.jabberjays.duration") * 20; //Convert seconds to ticks
        } else {
            effectDuration = JoTHungerGames.getInstance().getConfig().getLong("effects master controls.duration") * 20; //Convert seconds to ticks
        }

        SegmentChecker segmentChecker = new SegmentChecker(segmentGroup, segmentNumber);

        Bukkit.getLogger().info("Starting Jabberjays");
        segmentUtils.setEffectActive(segmentGroup, segmentNumber, "JABBERJAYS", true);

        soundTask = new BukkitRunnable() {
            @Override
            public void run() {

                Set<Player> currentPlayersInSegment = new HashSet<>();

                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (segmentChecker.isInsideSegment(player.getLocation())) {

                        currentPlayersInSegment.add(player); // Add player to the set

                        // Create a random offset from the player's location
                        double offsetX = (random.nextDouble() - 0.5) * 10; // Random offset between -5 and 5
                        double offsetY = random.nextDouble() * 5; // Random offset between 0 and 5
                        double offsetZ = (random.nextDouble() - 0.5) * 10; // Random offset between -5 and 5

                        // Create the location with the offset
                        Location soundLocation = player.getLocation().clone().add(offsetX, offsetY, offsetZ);

                        // Randomly select a sound
                        Sound randomSound = jabberjaySounds.get(random.nextInt(jabberjaySounds.size()));
                        // Play the randomly selected sound
                        player.playSound(soundLocation, randomSound, SoundCategory.AMBIENT, 1.0F, 1.0F);
                    }
                }
                // Detect players who have left the segment
                for (Player previousPlayer : playersInSegment) {
                    if (!currentPlayersInSegment.contains(previousPlayer)) {
                        // This player has left the segment.
                        playersHearingSound.remove(previousPlayer);
                    }
                }
                playersInSegment = currentPlayersInSegment; // Update the set of players currently in the segment
            }
        };
        soundTask.runTaskTimer(JoTHungerGames.getInstance(), 0L,  random.nextInt(15) + 5L);  // Repeats randomly between .25 and 1 second

        nauseaTask = new BukkitRunnable() {
            @Override
            public void run() {

                Set<Player> currentPlayersInSegment = new HashSet<>();

                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (segmentChecker.isInsideSegment(player.getLocation())) {

                        currentPlayersInSegment.add(player); // Add player to the set

                        // Apply nausea effect
                        player.addPotionEffect(nauseaEffect);
                    }
                }
                playersInSegment = currentPlayersInSegment; // Update the set of players currently in the segment
            }

        };
        nauseaTask.runTaskTimer(JoTHungerGames.getInstance(), 0L, random.nextInt(160) + 21L);  // Repeats randomly between 1 and 9 seconds

        stopJabberjaysEffectTask = new BukkitRunnable() {

            @Override
            public void run() {
                stop(segmentGroup, segmentNumber);
            }
        };
        stopJabberjaysEffectTask.runTaskLater(JoTHungerGames.getInstance(), effectDuration);  // Starts after the effect duration has run out

    }

    @Override
    public void stop(String segmentGroup, int segmentNumber) {
        // Stop effect logic
        if(soundTask != null) {
            Bukkit.getLogger().info("Stopping Jabberjays");
            soundTask.cancel();
            playersHearingSound.clear();
            soundTask = null; // Reset task
        }
        if(nauseaTask != null) {
            nauseaTask.cancel();
            nauseaTask = null; // Reset task
        }
        if(stopJabberjaysEffectTask != null) {
            stopJabberjaysEffectTask.cancel();
            stopJabberjaysEffectTask = null; // Reset task
        }
        segmentUtils.setEffectActive(segmentGroup, segmentNumber, "JABBERJAYS", false);
    }


}