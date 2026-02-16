package me.jamontoast.jothungergames.Utilities;

import me.jamontoast.jothungergames.Commands.ArenaCommand;
import me.jamontoast.jothungergames.JoTHungerGames;
import me.jamontoast.jothungergames.Listeners.DeathListener;
import me.jamontoast.jothungergames.Listeners.PlayerRejoinHandler;
import me.jamontoast.jothungergames.Listeners.SpectatorListener;
import me.jamontoast.jothungergames.Tasks.*;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.*;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

import java.util.*;

public class ArenaManager {
    private static ArenaManager instance;
    private Map<String, ArenaData> arenas;
    private Map<String, DeathListener> deathListeners;
    private Map<String, PlayerRejoinHandler> playerRejoinHandlers;
    private Map<String, SpectatorListener> spectatorListeners;
    private final String HGMusic = "jothungergames.hornofplenty_noteblocks_oldrecord";
    private final String HGMusicOG = "jothungergames.hornofplenty_real_oldrecord";

    // Constructor
    private ArenaManager() {
        arenas = new HashMap<>();
        deathListeners = new HashMap<>();
        playerRejoinHandlers = new HashMap<>();
        spectatorListeners = new HashMap<>();
    }

    public static ArenaManager getInstance() {
        if (instance == null) {
            instance = new ArenaManager();
        }
        return instance;
    }

    public boolean startArena(String arenaName) {
        ArenaData arenaData = getArenaData(arenaName);

        if (initializeArena(arenaName)) {
            arenaData.setActive(true);

            return true;
        }else {
            arenaData.setActive(false);

            return false;
        }
    }
    public void handleGameWin(Player winner, String arenaName) {

        ArenaData arenaData = getArenaData(arenaName);

        Bukkit.getLogger().info("Handling game win");

        //reset world border
        World world = arenaData.getArenaWorld();
        WorldBorder worldBorder = world.getWorldBorder();
        worldBorder.setSize(30000000);
        worldBorder.setCenter(new Location(world, 0, 255, 0));

        JoTHungerGames plugin = JoTHungerGames.getInstance();
        //send win message
        if (winner == null) {
            for (UUID uuid : arenaData.getGamePlayers()) {
                Player player = Bukkit.getPlayer(uuid);
                if (player == null || !player.isOnline()) continue;
                player.sendTitle(ChatColor.RED + "ALL TRIBUTES", "have perished", 10, 60, 10);
            }
        }else {
            for (UUID uuid : arenaData.getGamePlayers()) {
                Player player = Bukkit.getPlayer(uuid);
                if (player == null || !player.isOnline()) continue;
                if (!player.equals(winner)) {
                    player.sendTitle(ChatColor.BOLD.toString() + ChatColor.YELLOW + winner.getName(), ChatColor.GOLD + "has won!", 10, 60, 10);
                }
            }
            Score score = plugin.getWinsObjective().getScore(winner.getName());
            int newScore = score.getScore() + 1;
            score.setScore(newScore);
            winner.sendTitle(ChatColor.BOLD.toString() + ChatColor.GOLD + "CONGRATULATIONS!", ChatColor.BOLD.toString() + ChatColor.GOLD + "You have won!", 10, 60, 10);
        }
        //reset friendly fire
        Bukkit.getScoreboardManager().getMainScoreboard().getTeam("ArenaPlayers").setAllowFriendlyFire(true);

        EndGameScoreboard.displayEndGameScoreboard(arenaName, winner);

        Bukkit.getLogger().info("Stopping arena " + arenaName);
        resetArenaSpectators(arenaName);
        stopArenaTasks(arenaName);

    }

    public boolean initializeArena(String arenaName) {
        // Logic to initialize the arena and populate ArenaData
        ArenaData arenaData = getArenaData(arenaName);
        // Populate arenaData with necessary information (e.g., from configuration)
        FileConfiguration config = JoTHungerGames.getInstance().getArenaConfig();
        String basePath = "arenas." + arenaName;


        Location arenaCenter;
        Location arenaLobby;
        Location arenaDeathSpawn;

        try {
            //verify and set arenaWorld
            World world = Bukkit.getWorld(Objects.requireNonNull(config.getString(basePath + ".world")));
            if (world == null) {
                Bukkit.getLogger().severe("World is null for arena: " + arenaName);
                return false;
            }
            arenaData.setArenaWorld(world);

            //verify and set arenaCenter
            String centerString = config.getString(basePath + ".center");
            if (centerString == null) {
                Bukkit.getLogger().severe("Center coordinates are missing for arena: " + arenaName);
                return false;
            }
            String[] centerCoords = centerString.split(",");
            if (centerCoords.length != 3) {
                Bukkit.getLogger().severe("Invalid center coordinates for arena: " + arenaName);
                return false;
            }

            String lobbyString = config.getString(basePath + ".lobby.center");
            if (lobbyString == null) {
                Bukkit.getLogger().severe("Lobby coordinates are missing for arena: " + arenaName);
                return false;
            }
            String[] lobbyCoords = lobbyString.split(",");
            if (lobbyCoords.length != 3) {
                Bukkit.getLogger().severe("Invalid lobby coordinates for arena: " + arenaName);
                return false;
            }

            arenaCenter = new Location(world,
                    Integer.parseInt(centerCoords[0]),
                    Integer.parseInt(centerCoords[1]),
                    Integer.parseInt(centerCoords[2]));
            Bukkit.getLogger().info("Arena center: " + arenaCenter);
            arenaData.setArenaCenter(arenaCenter);

            arenaLobby = new Location(world,
                    Integer.parseInt(lobbyCoords[0]),
                    Integer.parseInt(lobbyCoords[1]),
                    Integer.parseInt(lobbyCoords[2]));
            Bukkit.getLogger().info("Arena lobby center: " + arenaLobby);
            arenaData.setArenaLobby(arenaLobby);

            //set arenaDeathSpawn
            arenaDeathSpawn = arenaCenter.add(0.0, 50.0, 0.0);
            Bukkit.getLogger().info("arena death spawn: " + arenaDeathSpawn);
            arenaData.setArenaDeathSpawn(arenaDeathSpawn);

            String arenaType = config.getString(basePath + ".type");
            if (arenaType == null) {
                Bukkit.getLogger().severe("Arena type is null for arena: " + arenaName);
                return false;
            }
            arenaData.setArenaType(arenaType);

            Bukkit.dispatchCommand(
                    Bukkit.getConsoleSender(),
                    "kill @e[type=item]"
            );

            world.setGameRule(GameRule.LOCATOR_BAR, false);

            world.setGameRule(GameRule.TNT_EXPLODES, false);

            int timeOfDay = config.getInt(basePath + ".time of day");
            world.setTime(timeOfDay);

            String weatherType = Objects.requireNonNull(config.getString(basePath + ".weather")).toLowerCase();
            switch (Objects.requireNonNull(weatherType)) {
                case "rain":
                    world.setStorm(true);
                    break;
                case "storm":
                    world.setThundering(true);
                    break;
                default:
                    world.setStorm(false);
                    break;
            }

            //verify and set arenaRadius
            int arenaRadius = config.getInt(basePath + ".radius");
            arenaData.setArenaRadius(arenaRadius);

            int arenaHeight = config.getInt(basePath + ".height");
            arenaData.setArenaHeight(arenaHeight);

            int maxPlayers = config.getInt(basePath + ".players.maximum");
            arenaData.setMaxPlayers(maxPlayers);

            //verify and set gracePeriod
            long gracePeriod = config.getLong(basePath + ".grace period length") * 20;
            arenaData.setGracePeriod(gracePeriod);

            //verify and set eventBroadcasts
            boolean eventBroadcastActionbar = config.getBoolean(basePath + ".event broadcast.action bar");
            arenaData.setEventBroadcastActionbar(eventBroadcastActionbar);
            boolean eventBroadcastSidebar = config.getBoolean(basePath + ".event broadcast.sidebar");
            arenaData.setEventBroadcastSidebar(eventBroadcastSidebar);

            //verify and set fallenDisplay
            boolean doFallenDisplay = config.getBoolean(basePath + ".do fallen display");
            arenaData.setDoFallenDisplay(doFallenDisplay);

            //verify and set chest locations
            ChestUtilities chestUtilities = new ChestUtilities();
            Set<Location> centerChestLocations = chestUtilities.getChestLocations(arenaName, "center");
            if (centerChestLocations == null) {
                Bukkit.getLogger().severe("Center chest location set is null for arena: " + arenaName);
                return false;
            }
            arenaData.setCenterChestLocations(centerChestLocations);

            Set<Location> normalChestLocations = chestUtilities.getChestLocations(arenaName, "normal");
            if (normalChestLocations == null) {
                Bukkit.getLogger().severe("Normal chest location set is null for arena: " + arenaName);
                return false;
            }
            arenaData.setNormalChestLocations(normalChestLocations);

            Set<Location> primeChestLocations = chestUtilities.getChestLocations(arenaName, "prime");
            if (primeChestLocations == null) {
                Bukkit.getLogger().severe("Prime chest location set is null for arena: " + arenaName);
                return false;
            }
            arenaData.setPrimeChestLocations(primeChestLocations);

            // reset Spawn Locations for arena
            Set<Location> spawnLocations = new HashSet<>();
            arenaData.setSpawnLocations(spawnLocations);

            // iterate through maximum number of players
            for (int i = 0; i < maxPlayers; i++) {
                String playerSpawnString = config.getString(basePath + ".players.playerSpawns." + (i + 1));
                if (playerSpawnString == null) {
                    Bukkit.getLogger().severe("Player spawn coordinates are missing for player " + (i + 1) + " in arena " + arenaName);
                    return false;
                }
                String[] spawnCoords = playerSpawnString.split(",");
                if (spawnCoords.length != 3) {
                    Bukkit.getLogger().severe("Invalid spawn coordinates for player " + (i + 1) + " in arena " + arenaName);
                    return false;
                }
                Location spawnLocation = new Location(world,
                        Double.parseDouble(spawnCoords[0]),
                        Double.parseDouble(spawnCoords[1]),
                        Double.parseDouble(spawnCoords[2]));

                arenaData.getSpawnLocations().add(spawnLocation);
                Bukkit.getLogger().info("Spawn location added at: " + spawnLocation);
            }

            // set world border task
            if (config.getBoolean(basePath + ".world border.active")) {
                Bukkit.getLogger().info("World border active");
                long borderStartTime = config.getLong(basePath + ".world border.start") * 20;
                arenaData.setBorderStartTime(borderStartTime);
                long borderDuration = config.getLong(basePath + ".world border.duration") * 20;
                int borderRadius = config.getInt(basePath + ".world border.final radius");
                Location borderCenter = arenaCenter;
                if (config.getBoolean(basePath + ".world border.random center")) {
                    Location randomLocation = null; // Define outside to use it later
                    if (arenaType.equals("CIRCLE")) {
                        boolean isValidLocation = false;

                        while (!isValidLocation) {
                            // Generate random angle and radius
                            double angle = Math.random() * 2 * Math.PI;
                            double radius = Math.random() * arenaRadius;

                            // Calculate x and z coordinates based on the angle and radius
                            double randomX = arenaCenter.getX() + radius * Math.cos(angle);
                            double randomZ = arenaCenter.getZ() + radius * Math.sin(angle);

                            // Use y-coordinate of the arena center or find the highest block at that location
                            double randomY = arenaCenter.getY(); // You might want to adjust this based on your needs

                            // Create the random location
                            randomLocation = new Location(arenaCenter.getWorld(), randomX, randomY, randomZ);

                            // Check if the random location is within the bounds
                            double dx = randomLocation.getX() - arenaCenter.getX();
                            double dz = randomLocation.getZ() - arenaCenter.getZ();
                            double radialDistance = Math.sqrt(dx * dx + dz * dz);

                            if (radialDistance <= arenaRadius) {
                                isValidLocation = true;
                            }
                        }

                    } else if (arenaType.equals("SQUARE")) {
                        boolean isValidLocation = false;

                        while (!isValidLocation) {
                            // Generate random x and z coordinates within the square bounds
                            double randomX = arenaCenter.getX() + (Math.random() * 2 - 1) * arenaRadius;
                            double randomZ = arenaCenter.getZ() + (Math.random() * 2 - 1) * arenaRadius;

                            // Use y-coordinate of the arena center or find the highest block at that location
                            double randomY = arenaCenter.getY(); // You might want to adjust this based on your needs

                            // Check if the random location is within the square bounds
                            double minX = arenaCenter.getX() - (double) arenaRadius;
                            double maxX = arenaCenter.getX() + (double) arenaRadius;
                            double minZ = arenaCenter.getZ() - (double) arenaRadius;
                            double maxZ = arenaCenter.getZ() + (double) arenaRadius;

                            if (randomX >= minX && randomX <= maxX && randomZ >= minZ && randomZ <= maxZ) {
                                // Create the random location
                                randomLocation = new Location(arenaCenter.getWorld(), randomX, randomY, randomZ);
                                isValidLocation = true;
                            }
                        }
                    }

                    // Now the randomLocation can be used
                    if (randomLocation != null) {
                        Bukkit.getLogger().info("Generated random border centre: " + randomLocation);
                        borderCenter = randomLocation;
                    }
                }
                arenaData.setBorderCenter(borderCenter);
                WorldBorder border = world.getWorldBorder();

                //start the task
                arenaData.setBorderShrinkTask(new BorderShrinkTask(arenaName, border, borderRadius, borderDuration, eventBroadcastActionbar));
            }else {
                Bukkit.getLogger().info("World border inactive");
            }

            // set highlight players task
            if (config.getBoolean(basePath + ".highlight players.active")) {
                long highlightStartTime = config.getLong(basePath + ".highlight players.start") * 20;
                arenaData.setHighlightStartTime(highlightStartTime);
                //start the task
                arenaData.setHighlightPlayersTask(new HighlightPlayersTask(arenaName));
            }
            // set locator bar task
            if (config.getBoolean(basePath + ".enable locator bar.active")) {
                long locatorBarStartTime = config.getLong(basePath + ".enable locator bar.start") * 20;
                arenaData.setLocatorBarStartTime(locatorBarStartTime);
                //start the task
                arenaData.setLocatorBarTask(new LocatorBarTask(arenaName));
            }



            Bukkit.getLogger().info("Initialized arena: " + arenaName);

        } catch (Exception e) {
            Bukkit.getLogger().severe("Error initializing arena: " + e.getMessage());
            return false;
        }

        arenaData.setEffectStartTasks(new HashMap<>());
        String linkedGroup = config.getString(basePath + ".linked segment group");

        if (linkedGroup != null) {
            arenaData.setLinkedGroup(linkedGroup);

            try {
                String effectsType = null;
                if (config.getBoolean(basePath + ".effects.clock")) {
                    effectsType = "clock";
                } else if (config.getBoolean(basePath + ".effects.freeform")) {
                    effectsType = "freeform";
                }
                if (effectsType == null) {
                    Bukkit.getLogger().warning("No segment effect type has been selected despite a group being specified. " +
                            "No effects will activate for this game.");
                    return true;
                }
                switch (effectsType) {
                    case "clock" -> {
                        long startTime = config.getLong(basePath + ".effects.clock start time");
                        Long effectDurationOverride = config.getLong(basePath + ".effects.clock duration override");
                        if (effectDurationOverride == null) {
                            effectDurationOverride = 60L * 20;
                        }

                        Configuration segmentConfig = JoTHungerGames.getInstance().getSegmentConfig();
                        ConfigurationSection numSegmentsLocationSection = segmentConfig.getConfigurationSection("segment groups." + linkedGroup + ".segments");
                        int numSegments = numSegmentsLocationSection.getKeys(false).size();
                        Map<Integer, List<String>> clockSegmentsMap = null;
                        for (int i = 1; i <= numSegments; i++) {
                            List<String> effects = null;
                            clockSegmentsMap.put(i, effects);
                        }

                        ConfigurationSection clockEffectsLocationSection = config.getConfigurationSection(basePath + ".effects.clock effects");
                        assert clockEffectsLocationSection != null;
                        for (String key : clockEffectsLocationSection.getKeys(false)) {
                            if (config.getBoolean(clockEffectsLocationSection + "." + key + ".active")) {
                                String effectName = key.toLowerCase();
                                String[] segments = Objects.requireNonNull(config.getString(basePath + ".effects.clock effects." + key + ".segments")).split(",");
                                for (String segment : segments) {
                                    Integer segmentNumber = Integer.parseInt(segment);
                                    assert clockSegmentsMap != null;
                                    if (clockSegmentsMap.containsKey(segmentNumber)) {
                                        List<String> effects = clockSegmentsMap.get(segmentNumber);
                                        effects.add(effectName);
                                        clockSegmentsMap.replace(segmentNumber, effects);
                                    }
                                }
                            }
                        }

                        int segmentCounter = 0;
                        for (int i = 1; i <= Objects.requireNonNull(clockSegmentsMap).size(); i++) {
                            List<String> effects = clockSegmentsMap.get(i);
                            startTime = startTime + (effectDurationOverride * segmentCounter);
                            Integer segmentNumber = i;
                            for (String effect : effects) {
                                Long finalEffectDurationOverride = effectDurationOverride;
                                BukkitRunnable task = new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        EffectsUtils.SegmentEffectsHandler("start", linkedGroup, effect, segmentNumber, finalEffectDurationOverride);

                                    }
                                };
                                arenaData.getEffectStartTasks().computeIfAbsent(startTime, k -> new ArrayList<>()).add(task);
                            }
                            segmentCounter++;
                        }
                    }
                    case "freeform" -> {
                        ConfigurationSection freeformEffectsLocationSection = config.getConfigurationSection(basePath + ".effects.freeform effects");
                        assert freeformEffectsLocationSection != null;
                        for (String key : freeformEffectsLocationSection.getKeys(false)) {
                            String effectName = config.getString(basePath + ".effects.freeform effects." + key + ".effect name");
                            long startTime = config.getLong(basePath + ".effects.freeform effects." + key + ".start time") * 20;
                            long effectDurationOverride = config.getLong(basePath + ".effects.freeform effects." + key + ".effect duration override") * 20;
                            String[] segments = Objects.requireNonNull(config.getString(basePath + ".effects.freeform effects." + key + ".segments")).split(",");

                            for (String segment : segments) {
                                int segmentNumber = Integer.parseInt(segment);
                                BukkitRunnable task = new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        EffectsUtils.SegmentEffectsHandler("start", linkedGroup, effectName, segmentNumber, effectDurationOverride);
                                    }
                                };
                                arenaData.getEffectStartTasks().computeIfAbsent(startTime, k -> new ArrayList<>()).add(task);
                            }
                        }
                    }

                    default -> throw new IllegalStateException("Unexpected value: " + effectsType);
                }
                Bukkit.getLogger().info("Initialized arena effects for arena " + arenaName);
            } catch (Exception e) {
                Bukkit.getLogger().severe("Error initializing arena effects: " + e.getMessage());
                return false;
            }
        }

        arenas.put(arenaName, arenaData);
        return true;
    }

    public void startArenaTasks(String arenaName) {
        Bukkit.getLogger().info("Starting arena tasks");

        ArenaData arenaData = arenas.get(arenaName);
        boolean eventBroadcastSidebar = arenaData.isEventBroadcastSidebar();
        boolean doFallenDisplay = arenaData.isDoFallenDisplay();

        //reset all Kills to 0
        Objective kills = JoTHungerGames.getInstance()
                        .getKillsObjective();
        Scoreboard scoreboard = Bukkit.getScoreboardManager()
                        .getMainScoreboard();
        for (UUID uuid : arenaData.getGamePlayers()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline()) continue;

            scoreboard.resetScores(player.getName());
            if (kills != null) {
                kills.getScore(player.getName()).setScore(0);
            }
        }

        startGameTimer(arenaName);
        if (eventBroadcastSidebar) {
            startEventTimer(arenaName);
        }
        startRefillTasks(arenaName, "center");
        startRefillTasks(arenaName, "normal");
        startRefillTasks(arenaName, "prime");
        startEffectTasks(arenaName);

        if (doFallenDisplay) {
            dayEndChecker(arenaName);
        }

        WorldBorder border = arenaData.getArenaWorld().getWorldBorder();
        border.setSize(arenaData.getArenaRadius() * 4);


        if (arenaData.getBorderCenter() != null) {
            border.setCenter(arenaData.getBorderCenter());
        } else {
            border.setCenter(arenaData.getArenaCenter());
        }
        if (arenaData.getBorderShrinkTask() != null) {
            arenaData.getBorderShrinkTask().runTaskLater(JoTHungerGames.getInstance(), arenaData.getBorderStartTime());
        }

        if (arenaData.getHighlightPlayersTask() != null) {
            arenaData.getHighlightPlayersTask().runTaskLater(JoTHungerGames.getInstance(), arenaData.getHighlightStartTime());
        }
        if (arenaData.getLocatorBarTask() != null) {
            arenaData.getLocatorBarTask().runTaskLater(JoTHungerGames.getInstance(), arenaData.getLocatorBarStartTime());
        }
    }
    public void resetArenaSpectators(String arenaName) {
        Bukkit.getLogger().info("Resetting arena spectators");

        ArenaData arenaData = arenas.get(arenaName);
        Set<UUID> gamePlayers = arenaData.getGamePlayers();
        Set<UUID> deadPlayers = arenaData.getTotalDeadPlayers();
        Location arenaLobby = arenaData.getArenaLobby();
        Location arenaLobbyTeleport = new Location(arenaData.getArenaWorld(),
                (arenaLobby.getX() + 3),
                arenaLobby.getY(),
                arenaLobby.getZ());

        for (UUID uuid : deadPlayers) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline()) continue;

            player.setGameMode(GameMode.ADVENTURE);

            player.teleport(arenaLobbyTeleport);

            player.setFlying(false);
            player.setAllowFlight(false);
            player.setCollidable(true);
            player.setCanPickupItems(true);
            player.setInvulnerable(false);

            player.getInventory().clear();

            for (org.bukkit.potion.PotionEffect effect : player.getActivePotionEffects()) {
                player.removePotionEffect(effect.getType());
            }

            player.closeInventory();

            for (Player pl: Bukkit.getOnlinePlayers()){
                pl.showPlayer(player);
            }

            SpectatorCurrency.clearCurrency(player);
            Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
            Team spectatorTeam = scoreboard.getTeam("Spectators");
            if (spectatorTeam == null) {
                spectatorTeam = scoreboard.registerNewTeam("Spectators");
                spectatorTeam.setPrefix(ChatColor.GRAY.toString() + ChatColor.BOLD + "SPECTATOR " + ChatColor.GRAY);
                spectatorTeam.setCanSeeFriendlyInvisibles(true);
            }
            spectatorTeam.removeEntry(player.getName());
        }
    }
    public void stopArenaTasks(String arenaName) {
        Bukkit.getLogger().info("Stopping arena tasks");

        ArenaData arenaData = arenas.get(arenaName);
        Set<UUID> gamePlayers = arenaData.getGamePlayers();

        for (UUID uuid : gamePlayers) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline()) continue;

            player.stopSound(HGMusic);
            player.stopSound(HGMusicOG);
        }
        if (arenaData.getTeleportCountdownTask() != null) {
            arenaData.getTeleportCountdownTask().cancel();
        }
        if (arenaData.getSpawnLockTask() != null) {
            arenaData.getSpawnLockTask().cancel();
        }
        if (arenaData.getSpawnLockMessageTask() != null) {
            arenaData.getSpawnLockMessageTask().cancel();
        }
        if (arenaData.getDayEndCheckTask() != null) {
            arenaData.getDayEndCheckTask().cancel();
        }
        if (arenaData.getFallenDisplayTask() != null) {
            arenaData.getFallenDisplayTask().cancel();
        }
        if (arenaData.getGracePeriodTask() != null) {
            arenaData.getGracePeriodTask().cancel();
        }
        if (arenaData.getBorderShrinkTask() != null) {
            arenaData.getBorderShrinkTask().cancel();
        }
        if (arenaData.getHighlightPlayersTask() != null) {
            arenaData.getHighlightPlayersTask().cancel();
        }
        if (arenaData.getLocatorBarTask() != null) {
            arenaData.getLocatorBarTask().cancel();
        }
        if (arenaData.getGameTimerTask() != null) {
            arenaData.getGameTimerTask().cancel();
        }
        if (arenaData.getEventTimerTask() != null) {
            arenaData.getEventTimerTask().cancel();
        }
        stopEffectTasks(arenaName);
        stopRefillTasks(arenaName);

        //remove player effects
        for (UUID uuid : arenaData.getGamePlayers()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline()) continue;

            player.getActivePotionEffects().clear();
        }
        deregisterDeathListener(arenaName);
        deregisterPlayerRejoinHandler(arenaName);
        deregisterSpectatorListener(arenaName);
        arenaData.clearPlayerDeathTimes();

        setArenaActive(arenaName, false);
    }
    public void teleportCountdown(String arenaName) {
        int teleportCountdown = 3;
        ArenaData arenaData = arenas.get(arenaName);

        arenaData.setTeleportCountdownTask(new TeleportCountdownTask(arenaData.getGamePlayers(), arenaData.getSpawnLocations(), arenaName, teleportCountdown));
        arenaData.getTeleportCountdownTask().runTaskTimer(JoTHungerGames.getInstance(), 0L, 20L);
    }
    public void spawnLock(String arenaName) {
        int restrictionTime = 10;
        ArenaData arenaData = arenas.get(arenaName);
        Set<UUID> gamePlayers = arenaData.getGamePlayers();

        arenaData.setSpawnLockTask(new SpawnLockTask(gamePlayers, arenaData.getPlayerSpawnMap(), arenaName, restrictionTime));
        arenaData.getSpawnLockTask().runTaskTimer(JoTHungerGames.getInstance(), 0L, 1L);

        arenaData.setSpawnLockMessageTask(new SpawnLockMessageTask(gamePlayers, restrictionTime));
        arenaData.getSpawnLockMessageTask().runTaskTimer(JoTHungerGames.getInstance(), 0L, 20L);
    }
    public void gracePeriodStart(String arenaName) {
        ArenaData arenaData = arenas.get(arenaName);
        Set<UUID> gamePlayers = arenaData.getGamePlayers();


        ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
        assert scoreboardManager != null;
        Scoreboard scoreboard = scoreboardManager.getMainScoreboard();
        long gracePeriod = arenaData.getGracePeriod();

        // Create or get the team
        Team team = scoreboard.getTeam("ArenaPlayers");
        if (team == null) {
            team = scoreboard.registerNewTeam("ArenaPlayers");
        }
        //reset team
        for (String entry : new HashSet<>(team.getEntries())) {
            team.removeEntry(entry);
        }
        // Disable friendly fire
        team.setAllowFriendlyFire(false);
        // populate team
        for (UUID uuid : gamePlayers) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline()) continue;
            team.addEntry(player.getName());
        }

        Team finalTeam = team;

        arenaData.setGracePeriodTask(new BukkitRunnable() {
            @Override
            public void run() {
                finalTeam.setAllowFriendlyFire(true);
            }
        });
        arenaData.getGracePeriodTask().runTaskLater(JoTHungerGames.getInstance(), gracePeriod);
    }
    public void startGameTimer(String arenaName) {
        ArenaData arenaData = arenas.get(arenaName);
        Set<UUID> gamePlayers = arenaData.getGamePlayers();
        boolean eventBroadcastSidebar = arenaData.isEventBroadcastSidebar();

        arenaData.setGameTimerTask(new GameTimerTask(gamePlayers, eventBroadcastSidebar));
        arenaData.getGameTimerTask().runTaskTimer(JoTHungerGames.getInstance(), 0L, 20L);
    }
    public void startEventTimer(String arenaName) {
        ArenaData arenaData = arenas.get(arenaName);
        Set<UUID> gamePlayers = arenaData.getGamePlayers();

        TreeMap<Long, String> eventsMap = new TreeMap<>();

        List<String> chestTypes = Arrays.asList("normal", "center", "prime");
        FileConfiguration config = JoTHungerGames.getInstance().getArenaConfig();

        for (String type : chestTypes) {
            String basePath = "arenas." + arenaName + ".chests." + type + ".refills";
            ConfigurationSection section = config.getConfigurationSection(basePath);

            if (section != null) {
                for (String key : section.getKeys(false)) {
                    eventsMap.put(section.getLong(key) * 20, StringUtils.capitalize(type) + " refill "); // Convert seconds to ticks
                }
            }
        }
        String borderPath = "arenas." + arenaName + ".world border";
        String highlightPath = "arenas." + arenaName + ".highlight players";
        String locatorBarPath = "arenas." + arenaName + ".enable locator bar";
        if (config.getBoolean(borderPath + ".active")) {
            eventsMap.put(config.getLong(borderPath + ".start") * 20, "Border shrinks ");
        }
        if (config.getBoolean(highlightPath + ".active")) {
            eventsMap.put(config.getLong(highlightPath + ".start") * 20, "Locations revealed ");
        }
        if (config.getBoolean(locatorBarPath + ".active")) {
            eventsMap.put(config.getLong(locatorBarPath + ".start") * 20, "Locator bar enabled ");
        }

        long gameStartTime = System.currentTimeMillis();

        arenaData.setEventTimerTask(new EventTimerTask(gamePlayers, eventsMap));
        arenaData.getEventTimerTask().runTaskTimer(JoTHungerGames.getInstance(), 0L, 20L);
    }
    public void startRefillTasks(String arenaName, String type) {
        ArenaData arenaData = arenas.get(arenaName);
        ChestUtilities chestUtilities = new ChestUtilities();

        List<Long> refillTimes = chestUtilities.getRefillTimes(arenaName, type);
        List<BukkitRunnable> tasks = new ArrayList<>();

        for (long refillTime : refillTimes) {
            BukkitRunnable task = new BukkitRunnable() {
                @Override
                public void run() {
                    chestUtilities.refillChestsByType(arenaName, type + "_refill");
                }
            };
            task.runTaskLater(JoTHungerGames.getInstance(), refillTime);
            tasks.add(task);
        }

        arenaData.setRefillTasks(tasks);
    }
    public void stopRefillTasks(String arenaName) {
        ArenaData arenaData = arenas.get(arenaName);
        if (arenaData == null) return;

        List<BukkitRunnable> refillTasks = arenaData.getRefillTasks();
        if (refillTasks != null) {
            for (BukkitRunnable task : refillTasks) {
                cancelTask(task);
            }
            refillTasks.clear();
        }
    }
    public void startEffectTasks(String arenaName) {
        ArenaData arenaData = arenas.get(arenaName);

        for (Long startTime : arenaData.getEffectStartTasks().keySet()) {
            for (BukkitRunnable task : arenaData.getEffectStartTasks().get(startTime)) {
                task.runTaskLater(JoTHungerGames.getInstance(), startTime);
            }
        }
    }
    public void stopEffectTasks(String arenaName) {
        ArenaData arenaData = arenas.get(arenaName);

        for (Long startTime : arenaData.getEffectStartTasks().keySet()) {
            for (BukkitRunnable task : arenaData.getEffectStartTasks().get(startTime)) {
                task.cancel();
            }
        }
    }
    private void cancelTask(BukkitRunnable task) {
        if (task != null && !task.isCancelled()) {
            task.cancel();
        }
    }
    private void dayEndChecker(String arenaName) {
        ArenaData arenaData = arenas.get(arenaName);

        //runnable every night to display "The Fallen"
        arenaData.setDayEndCheckTask(new DayEndCheckTask(arenaName));
        arenaData.getDayEndCheckTask().runTaskTimer(JoTHungerGames.getInstance(), 0L, 100L); //check every 5 seconds
    }
    public void addDeadPlayer(Player player, String arenaName) {
        ArenaData arenaData = arenas.get(arenaName);
        int currentDay = arenaData.getCurrentDay();

        arenaData.getTotalDeadPlayers().add(player.getUniqueId());
        arenaData.getAlivePlayers().remove(player.getUniqueId());
        List<String> deadPlayersToday = arenaData.getDeadPlayersByDay().computeIfAbsent(currentDay, k -> new ArrayList<>());
        deadPlayersToday.add(player.getName());
    }
    public void displayDeadPlayers(String arenaName, int currentDay) {
        ArenaData arenaData = arenas.get(arenaName);
        Location arenaCenter = arenaData.getArenaCenter();
        int arenaRadius = arenaData.getArenaRadius();

        if (arenaData.getFallenDisplayTask() != null) {
            arenaData.getFallenDisplayTask().cancel();
            // Clean up any remaining text displays from previous runs
            Bukkit.dispatchCommand(
                    Bukkit.getConsoleSender(),
                    "kill @e[type=minecraft:text_display,tag=fallen_banner]"
            );
            Bukkit.dispatchCommand(
                    Bukkit.getConsoleSender(),
                    "kill @e[type=minecraft:text_display,tag=fallen_graphic]"
            );
        }


        List<String> deadPlayers = arenaData.getDeadPlayersByDay()
                .getOrDefault(currentDay, Collections.emptyList()); //defaults to empty list if no players have died

        long displayDuration = (500 / (deadPlayers.size() + 1));// Duration to display in ticks (3 seconds)
        FileConfiguration mainConfig = JoTHungerGames.getInstance().getConfig();
        String musicStyle = mainConfig.getString("music style");
        if ("real".equals(musicStyle)) {
            displayDuration = (680 / (deadPlayers.size() + 1));// Duration to display in ticks (3 seconds)
        }

        Location soundPlayerLoc = arenaCenter.clone().add(0.0,50.0,0.0);

        double heightAboveCenter = 100.0;
        double insetDistance = 15.0;  // How far inside the dome surface

        double effectiveRadius = arenaRadius - insetDistance;

        if (arenaRadius <= heightAboveCenter) {
            Bukkit.getLogger().warning("Arena radius too small for fallen display height. Using fallback position.");
            heightAboveCenter = arenaRadius * 0.5;  // Fallback to half the radius
        }

        double horizontalDistance = Math.sqrt(
                effectiveRadius * effectiveRadius - heightAboveCenter * heightAboveCenter
        );

        // Determine how many displays to spawn
        List<Location> displayLocations = new ArrayList<>();

        if (arenaRadius > 100) {
            // Large arena: spawn in all 4 cardinal directions
            // North
            displayLocations.add(new Location(
                    arenaCenter.getWorld(),
                    arenaCenter.getX(),
                    arenaCenter.getY() + heightAboveCenter,
                    arenaCenter.getZ() - horizontalDistance
            ));
            // South
            displayLocations.add(new Location(
                    arenaCenter.getWorld(),
                    arenaCenter.getX(),
                    arenaCenter.getY() + heightAboveCenter,
                    arenaCenter.getZ() + horizontalDistance
            ));
            // East
            displayLocations.add(new Location(
                    arenaCenter.getWorld(),
                    arenaCenter.getX() + horizontalDistance,
                    arenaCenter.getY() + heightAboveCenter,
                    arenaCenter.getZ()
            ));
            // West
            displayLocations.add(new Location(
                    arenaCenter.getWorld(),
                    arenaCenter.getX() - horizontalDistance,
                    arenaCenter.getY() + heightAboveCenter,
                    arenaCenter.getZ()
            ));

            Bukkit.getLogger().info("Fallen Display running at 4 cardinal locations for large arena (radius: " + arenaRadius + ")");
        }else {
            // Small arena: single display at north
            displayLocations.add(new Location(
                    arenaCenter.getWorld(),
                    arenaCenter.getX(),
                    arenaCenter.getY() + heightAboveCenter,
                    arenaCenter.getZ() - horizontalDistance
            ));

            Bukkit.getLogger().info("Fallen Display running at north location for small arena (radius: " + arenaRadius + ")");
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            if ("real".equals(musicStyle)) {
                player.playSound(soundPlayerLoc, HGMusicOG, 20.0f, 1.0f);
            }else {
                player.playSound(soundPlayerLoc, HGMusic, 20.0f, 1.0f);
            }
            Bukkit.getLogger().info("Playing anthem for " + player);
        }

        FallenDisplayTask fallenDisplayTask = new FallenDisplayTask(
                deadPlayers,
                arenaCenter,
                arenaRadius,
                displayLocations,
                displayDuration
        );
        arenaData.setFallenDisplayTask(fallenDisplayTask);
        arenaData.getFallenDisplayTask().runTaskTimer(JoTHungerGames.getInstance(), 0L, displayDuration);
    }
    // Method to register death listener
    public void registerDeathListener(String arenaName) {
        ArenaData arenaData = getArenaData(arenaName);
        if (arenaData != null) {
            DeathListener deathListener = new DeathListener(arenaName);
            Bukkit.getPluginManager().registerEvents(deathListener, JoTHungerGames.getInstance());
            deathListeners.put(arenaName, deathListener);
        }
    }
    // Method to deregister death listener
    public void deregisterDeathListener(String arenaName) {
        DeathListener deathListener = deathListeners.remove(arenaName);
        if (deathListener != null) {
            HandlerList.unregisterAll(deathListener);
        }
    }

    public void registerPlayerRejoinHandler(String arenaName) {
        ArenaData arenaData = getArenaData(arenaName);
        if (arenaData != null) {
            PlayerRejoinHandler playerRejoinHandler = new PlayerRejoinHandler(arenaName);
            Bukkit.getPluginManager().registerEvents(playerRejoinHandler, JoTHungerGames.getInstance());
            playerRejoinHandlers.put(arenaName, playerRejoinHandler);
        }
    }
    public void deregisterPlayerRejoinHandler(String arenaName) {
        PlayerRejoinHandler playerRejoinHandler = playerRejoinHandlers.remove(arenaName);
        if (playerRejoinHandler != null) {
            HandlerList.unregisterAll(playerRejoinHandler);
        }
    }

    public void registerSpectatorListener(String arenaName) {
        ArenaData arenaData = getArenaData(arenaName);
        if (arenaData != null) {
            SpectatorListener spectatorListener = new SpectatorListener(arenaName);
            Bukkit.getPluginManager().registerEvents(spectatorListener, JoTHungerGames.getInstance());
            spectatorListeners.put(arenaName, spectatorListener);
        }
    }
    public void deregisterSpectatorListener(String arenaName) {
        SpectatorListener spectatorListener = spectatorListeners.remove(arenaName);
        if (spectatorListener != null) {
            HandlerList.unregisterAll(spectatorListener);
        }
    }

    // Method to add or update an arena
    public void addArena(String arenaName, boolean isActive) {
        if (arenas.containsKey(arenaName)) {
            // Arena exists, update the existing ArenaData
            ArenaData arenaData = arenas.get(arenaName);
            arenaData.setActive(isActive);
        } else {
            // Arena does not exist, create and add a new ArenaData
            arenas.put(arenaName, new ArenaData(arenaName, isActive));
        }
    }

    public String getArenaForPlayer(Player player) {
        for (String arenaName : arenas.keySet()) {
            ArenaData arenaData = getArenaData(arenaName);
            if (arenaData != null && arenaData.getGamePlayers().contains(player.getUniqueId())) {
                return arenaName;
            }
        }
        return null;
    }

    // Method to get an arena's data by name
    public ArenaData getArenaData(String arenaName) {
        return arenas.get(arenaName);
    }

    public void setArenaActive(String arenaName, boolean isActive) {
        ArenaData arenaData = arenas.get(arenaName);
        if (arenaData != null) {
            arenaData.setActive(isActive);
        }
    }

    public void setGamePlayers(String arenaName, Set<UUID> gamePlayers) {
        ArenaData arenaData = arenas.get(arenaName);
        if (arenaData != null) {
            arenaData.setGamePlayers(gamePlayers);
        }
    }

    public Set<UUID> getGamePlayers(String arenaName) {
        ArenaData arenaData = arenas.get(arenaName);
        if (arenaData != null) {
            return arenaData.getGamePlayers();
        }
        return null;
    }
    public void clearGamePlayers(String arenaName) {
        ArenaData arenaData = arenas.get(arenaName);
        if (arenaData != null) {
            arenaData.clearGamePlayers();
        }
    }

    public boolean isArenaActive(String arenaName) {
        ArenaData arenaData = arenas.get(arenaName);
        return arenaData != null && arenaData.isActive();
    }
}
