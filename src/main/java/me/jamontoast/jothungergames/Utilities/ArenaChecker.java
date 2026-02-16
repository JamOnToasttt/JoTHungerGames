package me.jamontoast.jothungergames.Utilities;

import me.jamontoast.jothungergames.JoTHungerGames;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

public class ArenaChecker {
    private final Location arenaCenter;
    private final String arenaType;
    private final int arenaHeight;
    private final int arenaRadius;
    private final Location lobbyCenter;
    private final String lobbyType;
    private final int lobbyHeight;
    private final int lobbyRadius;
    private final JoTHungerGames plugin = JoTHungerGames.getInstance();

    public ArenaChecker(String arenaName) {
        FileConfiguration config = plugin.getArenaConfig();
        String basePath = "arenas." + arenaName;

        String arenaCenterString = config.getString(basePath + ".center");
        String lobbyCenterString = config.getString(basePath + ".lobby.center");
        String worldName = config.getString(basePath + ".world");

        if (arenaCenterString == null) {
            Bukkit.getLogger().severe("Arena center coordinates are missing for arena: " + arenaName);
            throw new IllegalArgumentException("Arena center coordinates are missing");
        }

        if (lobbyCenterString == null) {
            Bukkit.getLogger().severe("Lobby center coordinates are missing for arena: " + arenaName);
            throw new IllegalArgumentException("Lobby center coordinates are missing");
        }

        if (worldName == null) {
            Bukkit.getLogger().severe("World is missing for arena: " + arenaName);
            throw new IllegalArgumentException("World is missing");
        }

        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            Bukkit.getLogger().severe("World is null for arena: " + arenaName);
            throw new IllegalArgumentException("World is null");
        }

        String[] arenaCenterCoords = arenaCenterString.split(",");
        String[] lobbyCenterCoords = lobbyCenterString.split(",");
        if (arenaCenterCoords.length != 3) {
            Bukkit.getLogger().severe("Invalid arena center coordinates for arena: " + arenaName);
            throw new IllegalArgumentException("Invalid arena center coordinates");
        }

        if (lobbyCenterCoords.length != 3) {
            Bukkit.getLogger().severe("Invalid lobby center coordinates for arena: " + arenaName);
            throw new IllegalArgumentException("Invalid lobby center coordinates");
        }

        arenaCenter = new Location(world, Integer.parseInt(arenaCenterCoords[0]), Integer.parseInt(arenaCenterCoords[1]), Integer.parseInt(arenaCenterCoords[2]));
        lobbyCenter = new Location(world, Integer.parseInt(lobbyCenterCoords[0]), Integer.parseInt(lobbyCenterCoords[1]), Integer.parseInt(lobbyCenterCoords[2]));

        arenaType = config.getString(basePath + ".type").toUpperCase();
        arenaRadius = config.getInt(basePath + ".radius");
        arenaHeight = config.getInt(basePath + ".height");

        lobbyType = config.getString(basePath + ".lobby.type").toLowerCase();
        lobbyRadius = config.getInt(basePath + ".lobby.radius");
        lobbyHeight = config.getInt(basePath + ".lobby.height");
    }

    public boolean isInsideArena(Location entityLocation) {

        // Check height
        if (entityLocation.getY() < arenaCenter.getY() || entityLocation.getY() > arenaCenter.getY() + arenaHeight) {
            Bukkit.getLogger().info("Entity is not within arena Y bounds");
            return false;
        }

        if (arenaType.equals("CIRCLE")) {
            double dx = entityLocation.getX() - arenaCenter.getX();
            double dz = entityLocation.getZ() - arenaCenter.getZ();
            // Check radial distance
            double radialDistance = Math.sqrt(dx * dx + dz * dz);
            if (radialDistance <= arenaRadius) {
                return true;
            } else {
                Bukkit.getLogger().info("Entity is not inside circular arena");
                return false;
            }

        } else if (arenaType.equals("SQUARE")) {
            double halfSideLength = arenaRadius;

            double minX = arenaCenter.getX() - halfSideLength;
            double maxX = arenaCenter.getX() + halfSideLength;
            double minZ = arenaCenter.getZ() - halfSideLength;
            double maxZ = arenaCenter.getZ() + halfSideLength;

            double x = entityLocation.getX();
            double z = entityLocation.getZ();

            if (x >= minX && x <= maxX && z >= minZ && z <= maxZ) {
                return true;
            } else {
                Bukkit.getLogger().info("Entity is not inside square arena");
                return false;
            }
        }else {
            Bukkit.getLogger().severe("Arena type not read");
            return false;
        }
    }

    public boolean isInsideLobby(Location entityLocation) {

        // Check height
        if (entityLocation.getY() < lobbyCenter.getY() || entityLocation.getY() > lobbyCenter.getY() + lobbyHeight) {
            return false;
        }

        if (lobbyType.equals("circle")) {
            double dx = entityLocation.getX() - lobbyCenter.getX();
            double dz = entityLocation.getZ() - lobbyCenter.getZ();
            // Check radial distance
            double radialDistance = Math.sqrt(dx * dx + dz * dz);
            return (radialDistance <= lobbyRadius);

        } else if (lobbyType.equals("square")) {
            double halfSideLength = lobbyRadius;

            double minX = lobbyCenter.getX() - halfSideLength;
            double maxX = lobbyCenter.getX() + halfSideLength;
            double minZ = lobbyCenter.getZ() - halfSideLength;
            double maxZ = lobbyCenter.getZ() + halfSideLength;

            double x = entityLocation.getX();
            double z = entityLocation.getZ();

            return (x >= minX && x <= maxX && z >= minZ && z <= maxZ);
        }
        return false;
    }
}
