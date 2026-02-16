package me.jamontoast.jothungergames.Utilities;

import me.jamontoast.jothungergames.JoTHungerGames;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

public class SegmentChecker {
    private final Location center;
    private final double startAngle;
    private final double endAngle;
    private final int height;
    private final int outerRadius;
    private final int innerRadius;
    private final JoTHungerGames plugin = JoTHungerGames.getInstance();


    public SegmentChecker(String segmentGroup, int segmentNumber) {
        FileConfiguration config = plugin.getSegmentConfig();
        String basePath = "segment groups." + segmentGroup;

        String[] centerCoords = config.getString(basePath + ".center").split(",");
        World world = Bukkit.getWorld(config.getString(basePath + ".world"));
        center = new Location(world, Integer.parseInt(centerCoords[0]), Integer.parseInt(centerCoords[1]), Integer.parseInt(centerCoords[2]));

        outerRadius = config.getInt(basePath + ".outerRadius");
        innerRadius = config.getInt(basePath + ".innerRadius");
        height = config.getInt(basePath + ".height");

        String segmentPath = basePath + ".segments." + segmentNumber;
        startAngle = config.getDouble(segmentPath + ".startAngle");
        endAngle = config.getDouble(segmentPath + ".endAngle");
    }

    public boolean isInsideSegment(Location entityLocation) {
        double dx = entityLocation.getX() - center.getX();
        double dz = entityLocation.getZ() - center.getZ();

        double angle = (Math.toDegrees(Math.atan2(dz, dx)) + 360) % 360;

        // Check angle
        if (angle < startAngle || angle > endAngle) {
            return false;
        }

        // Check height
        if (entityLocation.getY() < center.getY() || entityLocation.getY() > center.getY() + height) {
            return false;
        }

        // Check radial distance
        double radialDistance = Math.sqrt(dx * dx + dz * dz);
        return (radialDistance >= innerRadius) && (radialDistance <= outerRadius);
    }
}
