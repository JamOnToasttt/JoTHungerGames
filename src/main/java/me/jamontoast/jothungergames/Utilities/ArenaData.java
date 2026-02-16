package me.jamontoast.jothungergames.Utilities;

import me.jamontoast.jothungergames.Tasks.GameTimerTask;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class ArenaData {
    private String arenaName;
    private World arenaWorld;
    private Location arenaCenter;
    private Location arenaLobby;
    private int arenaHeight;
    private int arenaRadius;
    private Location arenaDeathSpawn;
    private String arenaType;
    private int maxPlayers;
    private long borderStartTime;
    private long highlightStartTime;
    private long locatorBarStartTime;
    private Location borderCenter;

    private Set<UUID> gamePlayers;
    private Set<UUID> alivePlayers;
    private Set<UUID> totalDeadPlayers = new HashSet<>();
    private Set<Location> spawnLocations = new HashSet<>();
    private Map<UUID, Location> playerSpawnMap = new HashMap<>();
    private Map<Integer, List<String>> deadPlayersByDay = new HashMap<>();
    private int currentDay;

    private long gracePeriod;
    private boolean doFallenDisplay;
    private boolean eventBroadcastActionbar;
    private boolean eventBroadcastSidebar;

    private Set<Location> centerChestLocations = new HashSet<>();
    private Set<Location> normalChestLocations = new HashSet<>();
    private Set<Location> primeChestLocations = new HashSet<>();

    private String linkedGroup;

    private Map<Long, List<BukkitRunnable>> effectStartTasks = new HashMap<Long, List<BukkitRunnable>>();
    private BukkitRunnable teleportCountdownTask;
    private BukkitRunnable spawnLockTask;
    private BukkitRunnable spawnLockMessageTask;
    private BukkitRunnable gracePeriodTask;
    private BukkitRunnable dayEndCheckTask;
    private BukkitRunnable fallenDisplayTask;

    private BukkitRunnable gameTimerTask;
    private Map<UUID, Integer> playerDeathTimes = new HashMap<>();

    private BukkitRunnable eventTimerTask;
    private List<BukkitRunnable> refillTasks;
    private BukkitRunnable borderShrinkTask;
    private BukkitRunnable highlightPlayersTask;
    private BukkitRunnable locatorBarTask;

    private boolean isActive;

    //constructor
    public ArenaData(String arenaName, boolean isActive) {
        this.arenaName = arenaName;
        this.isActive = isActive;

        this.gamePlayers = new HashSet<>();
        this.alivePlayers    = new HashSet<>();
        this.totalDeadPlayers= new HashSet<>();
        this.deadPlayersByDay= new HashMap<>();
        this.refillTasks     = new ArrayList<>();
    }

    // Getters and setters for arenaName
    public String getArenaName() {
        return arenaName;
    }
    public void setArenaName(String arenaName) {
        this.arenaName = arenaName;
    }

    // Getters and setters for arenaLobby
    public Location getArenaLobby() {
        return arenaLobby;
    }
    public void setArenaLobby(Location arenaLobby) {
        this.arenaLobby = arenaLobby;
    }

    // Getters and setters for arenaCenter
    public Location getArenaCenter() {
        return arenaCenter;
    }
    public void setArenaCenter(Location arenaCenter) {
        this.arenaCenter = arenaCenter;
    }

    // Getters and setters for arenaCenter
    public int getArenaHeight() {
        return arenaHeight;
    }
    public void setArenaHeight(int arenaHeight) {
        this.arenaHeight = arenaHeight;
    }

    // Getters and setters for arenaRadius
    public int getArenaRadius() {
        return arenaRadius;
    }
    public void setArenaRadius(int arenaRadius) {
        this.arenaRadius = arenaRadius;
    }

    // Getters and setters for arenaWorld
    public World getArenaWorld() {
        return arenaWorld;
    }
    public void setArenaWorld(World arenaWorld) {
        this.arenaWorld = arenaWorld;
    }

    // Getters and setters for arenaDeathSpawn
    public Location getArenaDeathSpawn() {
        return arenaDeathSpawn;
    }
    public void setArenaDeathSpawn(Location arenaDeathSpawn) {
        this.arenaDeathSpawn = arenaDeathSpawn;
    }

    // Getters and setters for arenaType
    public String getArenaType() {
        return arenaType;
    }
    public void setArenaType(String arenaType) {
        this.arenaType = arenaType;
    }

    // Getters and setters for maxPlayers
    public int getMaxPlayers() {
        return maxPlayers;
    }
    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }


    // Getters and setters for gamePlayers
    public Set<UUID> getGamePlayers() {
        return gamePlayers;
    }
    public void setGamePlayers(Set<UUID> gamePlayers) {
        this.gamePlayers = gamePlayers;
    }



    // Getters and setters for alivePlayers
    public Set<UUID> getAlivePlayers() {
        return alivePlayers;
    }
    public void setAlivePlayers(Set<UUID> alivePlayers) {
        this.alivePlayers = alivePlayers;
    }

    // Getters and setters for totalDeadPlayers
    public Set<UUID> getTotalDeadPlayers() {
        return totalDeadPlayers;
    }
    public void setTotalDeadPlayers(Set<UUID> totalDeadPlayers) {
        this.totalDeadPlayers = totalDeadPlayers;
    }

    // Getters and setters for spawnLocations
    public Set<Location> getSpawnLocations() {
        return spawnLocations;
    }
    public void setSpawnLocations(Set<Location> spawnLocations) {
        this.spawnLocations = spawnLocations;
    }

    // Getters and setters for playerSpawnMap
    public Map<UUID, Location> getPlayerSpawnMap() {
        return playerSpawnMap;
    }
    public void setPlayerSpawnMap(Map<UUID, Location> playerSpawnMap) {
        this.playerSpawnMap = playerSpawnMap;
    }

    // Getters and setters for deadPlayersByDay
    public Map<Integer, List<String>> getDeadPlayersByDay() {
        return deadPlayersByDay;
    }
    public void setDeadPlayersByDay(Map<Integer, List<String>> deadPlayersByDay) {
        this.deadPlayersByDay = deadPlayersByDay;
    }

    // Getters and setters for currentDay
    public int getCurrentDay() {
        return currentDay;
    }
    public void setCurrentDay(int currentDay) {
        this.currentDay = currentDay;
    }

    // Getters and setters for gracePeriod
    public long getGracePeriod() {
        return gracePeriod;
    }
    public void setGracePeriod(long gracePeriod) {
        this.gracePeriod = gracePeriod;
    }

    // Getters and setters for eventBroadcastSidebar
    public boolean isDoFallenDisplay() {
        return doFallenDisplay;
    }
    public void setDoFallenDisplay(boolean doFallenDisplay) {
        this.doFallenDisplay = doFallenDisplay;
    }

    // Getters and setters for eventBroadcastActionbar
    public boolean isEventBroadcastActionbar() {
        return eventBroadcastActionbar;
    }
    public void setEventBroadcastActionbar(boolean eventBroadcastActionbar) {
        this.eventBroadcastActionbar = eventBroadcastActionbar;
    }

    // Getters and setters for eventBroadcastSidebar
    public boolean isEventBroadcastSidebar() {
        return eventBroadcastSidebar;
    }
    public void setEventBroadcastSidebar(boolean eventBroadcastSidebar) {
        this.eventBroadcastSidebar = eventBroadcastSidebar;
    }

    // Getters and setters for centerChestLocations
    public Set<Location> getCenterChestLocations() {
        return centerChestLocations;
    }
    public void setCenterChestLocations(Set<Location> centerChestLocations) {
        this.centerChestLocations = centerChestLocations;
    }

    // Getters and setters for normalChestLocations
    public Set<Location> getNormalChestLocations() {
        return normalChestLocations;
    }
    public void setNormalChestLocations(Set<Location> normalChestLocations) {
        this.normalChestLocations = normalChestLocations;
    }

    // Getters and setters for primeChestLocations
    public Set<Location> getPrimeChestLocations() {
        return primeChestLocations;
    }
    public void setPrimeChestLocations(Set<Location> primeChestLocations) {
        this.primeChestLocations = primeChestLocations;
    }

    // Getters and setters for linkedGroup
    public String getLinkedGroup() {
        return linkedGroup;
    }
    public void setLinkedGroup(String linkedGroup) {
        this.linkedGroup = linkedGroup;
    }

    // Getters and setters for effectStartTasks
    public Map<Long, List<BukkitRunnable>> getEffectStartTasks() {
        return effectStartTasks;
    }
    public void setEffectStartTasks(Map<Long, List<BukkitRunnable>> effectStartTasks) {
        this.effectStartTasks = effectStartTasks;
    }

    // Getters and setters for teleportCountdownTask
    public BukkitRunnable getTeleportCountdownTask() {
        return teleportCountdownTask;
    }
    public void setTeleportCountdownTask(BukkitRunnable teleportCountdownTask) {
        this.teleportCountdownTask = teleportCountdownTask;
    }

    // Getters and setters for spawnLockTask
    public BukkitRunnable getSpawnLockTask() {
        return spawnLockTask;
    }
    public void setSpawnLockTask(BukkitRunnable spawnLockTask) {
        this.spawnLockTask = spawnLockTask;
    }

    // Getters and setters for spawnLockMessageTask
    public BukkitRunnable getSpawnLockMessageTask() {
        return spawnLockMessageTask;
    }
    public void setSpawnLockMessageTask(BukkitRunnable spawnLockMessageTask) {
        this.spawnLockMessageTask = spawnLockMessageTask;
    }

    // Getters and setters for gracePeriodTask
    public BukkitRunnable getGracePeriodTask() {
        return gracePeriodTask;
    }
    public void setGracePeriodTask(BukkitRunnable gracePeriodTask) {
        this.gracePeriodTask = gracePeriodTask;
    }

    // Getters and setters for dayEndCheckTask
    public BukkitRunnable getDayEndCheckTask() {
        return dayEndCheckTask;
    }
    public void setDayEndCheckTask(BukkitRunnable dayEndCheckTask) {
        this.dayEndCheckTask = dayEndCheckTask;
    }

    // Getters and setters for fallenDisplayTask
    public BukkitRunnable getFallenDisplayTask() {
        return fallenDisplayTask;
    }
    public void setFallenDisplayTask(BukkitRunnable fallenDisplayTask) {
        this.fallenDisplayTask = fallenDisplayTask;
    }

    // Getters and setters for gameTimerTask
    public BukkitRunnable getGameTimerTask() {
        return gameTimerTask;
    }
    public void setGameTimerTask(BukkitRunnable gameTimerTask) {
        this.gameTimerTask = gameTimerTask;
    }

    public int getGameTime() {
        if (gameTimerTask instanceof GameTimerTask) {
            return ((GameTimerTask) gameTimerTask).getArenaTime();
        }
        return 0;
    }
    //Record when a player died//
    public void recordPlayerDeath(Player player) {
        int currentGameTime = getGameTime();
        playerDeathTimes.put(player.getUniqueId(), currentGameTime);
    }
    //Get how long a player survived (in seconds) Returns the time they died, or current time if still alive//
    public int getPlayerSurvivalTime(Player player) {
        return playerDeathTimes.getOrDefault(player.getUniqueId(), getGameTime());
    }
    public void clearPlayerDeathTimes() {
        playerDeathTimes.clear();
    }



    // Getters and setters for eventTimerTask
    public BukkitRunnable getEventTimerTask() {
        return eventTimerTask;
    }
    public void setEventTimerTask(BukkitRunnable eventTimerTask) {
        this.eventTimerTask = eventTimerTask;
    }

    // Getters and setters for refillTasks
    public List<BukkitRunnable> getRefillTasks() {
        return refillTasks;
    }
    public void setRefillTasks(List<BukkitRunnable> refillTasks) {
        this.refillTasks = refillTasks;
    }

    // Getters and setters for borderShrinkTask
    public BukkitRunnable getBorderShrinkTask() {
        return borderShrinkTask;
    }
    public void setBorderShrinkTask(BukkitRunnable borderShrinkTask) {
        this.borderShrinkTask = borderShrinkTask;
    }

    // Getters and setters for borderStartTime
    public Long getBorderStartTime() {
        return borderStartTime;
    }
    public void setBorderStartTime(Long borderStartTime) {
        this.borderStartTime = borderStartTime;
    }

    // Getters and setters for borderShrinkTask
    public Location getBorderCenter() {
        return borderCenter;
    }
    public void setBorderCenter(Location borderCenter) {
        this.borderCenter = borderCenter;
    }

    // Getters and setters for highlightPlayersTask
    public BukkitRunnable getHighlightPlayersTask() {
        return highlightPlayersTask;
    }
    public Long getHighlightStartTime() {
        return highlightStartTime;
    }
    public void setHighlightStartTime(Long highlightStartTime) {
        this.highlightStartTime = highlightStartTime;
    }
    public void setHighlightPlayersTask(BukkitRunnable highlightPlayersTask) {
        this.highlightPlayersTask = highlightPlayersTask;
    }

    public BukkitRunnable getLocatorBarTask() {
        return locatorBarTask;
    }
    public Long getLocatorBarStartTime() {
        return locatorBarStartTime;
    }
    public void setLocatorBarStartTime(Long locatorBarStartTime) {
        this.locatorBarStartTime = locatorBarStartTime;
    }
    public void setLocatorBarTask(BukkitRunnable locatorBarTask) {
        this.locatorBarTask = locatorBarTask;
    }

    public void clearGamePlayers() {
        this.gamePlayers.clear();
    }

    public boolean isActive() {
        return isActive;
    }
    public void setActive(boolean active) {
        this.isActive = active;
    }
}
