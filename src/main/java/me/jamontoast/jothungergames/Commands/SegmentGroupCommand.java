package me.jamontoast.jothungergames.Commands;

import me.jamontoast.jothungergames.JoTHungerGames;
import me.jamontoast.jothungergames.Utilities.BlockUtils;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.*;
import org.apache.commons.lang3.math.NumberUtils;
import org.bukkit.command.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SegmentGroupCommand implements CommandExecutor, TabCompleter {


    //Constants for subcommands
    private static final String CREATE_SUBCOMMAND = "create";
    private static final String DELETE_SUBCOMMAND = "delete";
    private static final String LIST_SUBCOMMAND = "list";
    private static final String CLEARCACHE_SUBCOMMAND = "clearcache";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // Ensure there is at least one argument for the subcommand
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Subcommand required. Usage: "+ ChatColor.GOLD + "/hgsegmentGroup " + ChatColor.YELLOW + "<" + ChatColor.GOLD + "create"
                    + ChatColor.YELLOW + "|" + ChatColor.GOLD + "delete" + ChatColor.YELLOW +"|" + ChatColor.GOLD + "list" + ChatColor.YELLOW + ">");
            return true;
        }

        // Determine action based on subcommand
        String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            case CREATE_SUBCOMMAND:
                return handleCreateSegmentGroup(sender, Arrays.copyOfRange(args, 1, args.length));
            case DELETE_SUBCOMMAND:
                return handleDeleteSegmentGroup(sender, Arrays.copyOfRange(args, 1, args.length));
            case LIST_SUBCOMMAND:
                return handleListSegmentGroup(sender, Arrays.copyOfRange(args, 1, args.length));
            case CLEARCACHE_SUBCOMMAND:
                return handleClearCacheSegmentGroup(sender, Arrays.copyOfRange(args, 1, args.length));
            default:
                sender.sendMessage(ChatColor.RED + "Unknown subcommand. Usage: " + ChatColor.GOLD + "/hgsegmentGroup " + ChatColor.YELLOW + "<" + ChatColor.GOLD + "create"
                        + ChatColor.YELLOW + "|" + ChatColor.GOLD + "delete" + ChatColor.YELLOW +"|" + ChatColor.GOLD + "list" + ChatColor.YELLOW + ">");
                return true;
        }
    }

    private boolean handleCreateSegmentGroup(CommandSender sender, String[] args) {
        // Implement the creation logic here
        // Similar to your existing onCommand logic but focused on creation

        // Ensure sender is a player and validate arguments
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command!");
            return true;
        }

        if (!validateCreateInputArguments(sender, args)) {
            Bukkit.getLogger().info("One or more inputs was invalid.");
            return true;
        }

        Player player = (Player) sender;
        // Extracting arguments
        String groupName = args[0];
        int outerRadius = Integer.parseInt(args[1]);
        int numSegments = Integer.parseInt(args[2]);
        int height = Integer.parseInt(args[3]);
        int innerRadius = args.length == 5 ? Integer.parseInt(args[4]) : 0;

        Location center = player.getLocation();

        List<JoTHungerGames.Segment> segments = getGroupBlocks(center, outerRadius, innerRadius, numSegments, height);
        Bukkit.getLogger().info("Collected segments for processing.");  // Debug output

        JoTHungerGames.getInstance().saveSegmentToConfig(groupName, center, outerRadius, innerRadius, height, segments);
        Bukkit.getLogger().info("Segments saved to config.");  // Debug output

        sender.sendMessage(ChatColor.GREEN + "Segment group " + ChatColor.YELLOW + groupName + ChatColor.GREEN + " saved at "
                + ChatColor.YELLOW + center.getBlockX() + "," + center.getBlockY() + "," + center.getBlockZ()
                + ChatColor.GREEN + " with " + ChatColor.YELLOW + numSegments + ChatColor.GREEN + " segments, an outer radius of " + ChatColor.YELLOW + outerRadius
                + ChatColor.GREEN + ", an inner radius of " + ChatColor.YELLOW + innerRadius + ChatColor.GREEN + ", and a height of " + ChatColor.YELLOW + height + ChatColor.GREEN + ".");

        return true; // Return true when creation is successful
    }

    private boolean handleDeleteSegmentGroup(CommandSender sender, String[] args) {

        if (!validateDeleteInputArguments(sender, args)) {
            Bukkit.getLogger().info("One or more inputs was invalid.");
            return true;
        }

        String segmentGroup = args[0];

        FileConfiguration segmentConfig = JoTHungerGames.getInstance().getSegmentConfig();

        segmentConfig.set("segment groups." + segmentGroup, null);
        JoTHungerGames.getInstance().saveSegmentConfig();

        sender.sendMessage(ChatColor.GREEN + "Segment group " + segmentGroup + " deleted.");

        return true; // Return true or false based on success
    }

    private boolean handleClearCacheSegmentGroup(CommandSender sender, String[] args) {

        if (!validateClearCacheInputArguments(sender, args)) {
            Bukkit.getLogger().info("One or more inputs was invalid.");
            return true;
        }

        String segmentGroup = args[0];

        BlockUtils.clearCache(args[1]);

        sender.sendMessage(ChatColor.GREEN + "Block cache for segment group " + segmentGroup + " cleared.");

        return true; // Return true or false based on success
    }

    private boolean handleListSegmentGroup(CommandSender sender, String[] args) {

        if (!validateListInputArguments(sender, args)) {
            Bukkit.getLogger().info("One or more inputs was invalid.");
            return true;
        }

        ConfigurationSection segmentGroups = JoTHungerGames.getInstance().getSegmentConfig().getConfigurationSection("segment groups");

        if (segmentGroups == null) {
            sender.sendMessage(ChatColor.RED + "No segment groups found.");
            return true;
        }

        sender.sendMessage(ChatColor.GOLD + "List of Segment Groups:");

        FileConfiguration config = JoTHungerGames.getInstance().getSegmentConfig();

        // Iterate through each segment group
        for (String groupKey : segmentGroups.getKeys(false)) {

            World world = Bukkit.getWorld(config.getString("segment groups." + groupKey + ".world"));
            String groupCenter = config.getString("segment groups." + groupKey + ".center");
            int outerRadius = config.getInt("segment groups." + groupKey + ".outerRadius");
            int innerRadius = config.getInt("segment groups." + groupKey + ".innerRadius");
            int groupHeight = config.getInt("segment groups." + groupKey + ".height");

            TextComponent message = new TextComponent(groupKey);
            message.setColor(net.md_5.bungee.api.ChatColor.YELLOW);

            // Create the hover text (description)
            TextComponent hoverText = new TextComponent("World: " + world + "\n" +
                    "Center: " + groupCenter + "\n" +
                    "Radius: " + outerRadius + "\n" +
                    "Radius: " + innerRadius + "\n" +
                    "Height: " + groupHeight);
            hoverText.setColor(net.md_5.bungee.api.ChatColor.WHITE);

            // Add the hover event to the message
            message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{hoverText}));

            // Send the message to the sender
            sender.spigot().sendMessage(message);
        }

        return true; // Return true or false based on success
    }

    private boolean validateCreateInputArguments(CommandSender sender, String[] args) {
        // Validation logic here, returning false and sending error messages as required

        //sets the CorrectUsageExample string to be copied into all error messages for the command.
        String CorrectUsageExample = ChatColor.RED + "Correct usage: " + ChatColor.GOLD + "/hgsegmentGroup create "
                + ChatColor.YELLOW + " <" + ChatColor.GOLD + "group name" + ChatColor.YELLOW + ">"
                + ChatColor.YELLOW + " <" + ChatColor.GOLD + "radius" + ChatColor.YELLOW + ">"
                + ChatColor.YELLOW + " <" + ChatColor.GOLD + "segments" + ChatColor.YELLOW + ">"
                + ChatColor.YELLOW + " <" + ChatColor.GOLD + "height" + ChatColor.YELLOW + ">"
                + ChatColor.YELLOW + " [" + ChatColor.GOLD + "inner radius" + ChatColor.YELLOW + "]";

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command!");
            return false;
        }
        if (args.length < 4) {
            sender.sendMessage(ChatColor.RED + "You are missing arguments!");
            sender.sendMessage(CorrectUsageExample);
            return false;
        } else if (args.length > 5) {
            sender.sendMessage(ChatColor.RED + "You have submitted too many arguments!");
            sender.sendMessage(CorrectUsageExample);
        } else {
            Bukkit.getLogger().info("Argument count valid.");
        }
        //checks to see if the first arg is a valid segment group
        if ((JoTHungerGames.getInstance().segmentGroupExists(args[0]))) {
            sender.sendMessage(ChatColor.RED + "A segment group with the name " + args[0] + " already exists!");
            sender.sendMessage(CorrectUsageExample);
            return false;
        }
        if (!NumberUtils.isParsable(args[1]) || Integer.parseInt(args[1]) < 0) {
            sender.sendMessage(ChatColor.RED + "Radius must be a positive integer.");
            sender.sendMessage(CorrectUsageExample);
            return false;
        }
        if (!NumberUtils.isParsable(args[2]) || Integer.parseInt(args[2]) < 1) {
            sender.sendMessage(ChatColor.RED + "Segments must be a positive integer 1 or greater.");
            sender.sendMessage(CorrectUsageExample);
            return false;
        }
        if (!NumberUtils.isParsable(args[3]) || Integer.parseInt(args[3]) < 1) {
            sender.sendMessage(ChatColor.RED + "Height must be a positive integer.");
            sender.sendMessage(CorrectUsageExample);
            return false;
        }
        if (args.length == 5) {
            if (!NumberUtils.isParsable(args[4]) || Integer.parseInt(args[4]) < 0) {
                sender.sendMessage(ChatColor.RED + "Inner radius must be an integer 0 or greater.");
                sender.sendMessage(CorrectUsageExample);
                return false;
            }
        }
        // Add other validation checks...
        return true; // Return true if all checks pass
    }

    private boolean validateDeleteInputArguments(CommandSender sender, String[] args) {
        // Validation logic here, returning false and sending error messages as required

        //sets the CorrectUsageExample string to be copied into all error messages for the command.
        String CorrectUsageExample = ChatColor.RED + "Correct usage: " + ChatColor.GOLD + "/hgsegmentGroup delete"
                + ChatColor.YELLOW + " <" + ChatColor.GOLD + "group name" + ChatColor.YELLOW + ">";

        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "You are missing an argument!");
            sender.sendMessage(CorrectUsageExample);
            return false;
        } else if (args.length > 1) {
            sender.sendMessage(ChatColor.RED + "You have submitted too many arguments!");
            sender.sendMessage(CorrectUsageExample);
            return false;
        } else {
            Bukkit.getLogger().info("Argument count valid.");
        }
        //checks to see if the first arg is a valid segment group
        if (!(JoTHungerGames.getInstance().segmentGroupExists(args[0]))) {
            sender.sendMessage(ChatColor.RED + args[0] + " is not a valid segment group.");
            sender.sendMessage(CorrectUsageExample);
            return false;
        } else {
            Bukkit.getLogger().info("Segment group valid.");
        }
        // Add other validation checks...

        return true; // If all validations pass
    }

    private boolean validateClearCacheInputArguments(CommandSender sender, String[] args) {
        // Validation logic here, returning false and sending error messages as required

        //sets the CorrectUsageExample string to be copied into all error messages for the command.
        String CorrectUsageExample = ChatColor.RED + "Correct usage: " + ChatColor.GOLD + "/hgsegmentGroup clearCache"
                + ChatColor.YELLOW + " <" + ChatColor.GOLD + "group name" + ChatColor.YELLOW + ">";

        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "You are missing an argument!");
            sender.sendMessage(CorrectUsageExample);
            return false;
        } else if (args.length > 1) {
            sender.sendMessage(ChatColor.RED + "You have submitted too many arguments!");
            sender.sendMessage(CorrectUsageExample);
            return false;
        } else {
            Bukkit.getLogger().info("Argument count valid.");
        }
        //checks to see if the first arg is a valid segment group
        if (!(JoTHungerGames.getInstance().segmentGroupExists(args[0]))) {
            sender.sendMessage(ChatColor.RED + args[0] + " is not a valid segment group.");
            sender.sendMessage(CorrectUsageExample);
            return false;
        } else {
            Bukkit.getLogger().info("Segment group valid.");
        }
        // Add other validation checks...

        return true; // If all validations pass
    }

    private boolean validateListInputArguments(CommandSender sender, String[] args) {
        // Validation logic here, returning false and sending error messages as required

        //sets the CorrectUsageExample string to be copied into all error messages for the command.
        String CorrectUsageExample = ChatColor.RED + "Correct usage: " + ChatColor.GOLD + "/hgsegmentGroup list";

        if (args.length > 1) {
            sender.sendMessage(ChatColor.RED + "You have submitted too many arguments!");
            sender.sendMessage(CorrectUsageExample);
            return false;
        } else {
            Bukkit.getLogger().info("Argument count valid.");
        }
        // Add other validation checks...

        return true; // If all validations pass
    }

    private List<JoTHungerGames.Segment> getGroupBlocks(Location center, int outerRadius, int innerRadius, int numSegments, int height) {
        List<JoTHungerGames.Segment> segments = new ArrayList<>();
        for (int i = 0; i < numSegments; i++) {
            segments.add(new JoTHungerGames.Segment(i));
        }
        World world = center.getWorld();

        int baseY = center.getBlockY();
        int baseX = center.getBlockX();
        int baseZ = center.getBlockZ();

        double segmentAngle = 360.0 / numSegments;

        for (int y = baseY; y < baseY + height; y++) {
            for (int x = baseX - outerRadius; x <= baseX + outerRadius; x++) {
                for (int z = baseZ - outerRadius; z <= baseZ + outerRadius; z++) {
                    if ((x - baseX) * (x - baseX) + (z - baseZ) * (z - baseZ) <= outerRadius * outerRadius) {
                        double dx = x - baseX;
                        double dz = z - baseZ;
                        double distanceSquared = dx * dx + dz * dz;

                        // Check if the distance falls within the inner and outer radii
                        if (distanceSquared <= outerRadius * outerRadius && distanceSquared >= innerRadius * innerRadius) {

                            double angle = (Math.toDegrees(Math.atan2(dz, dx)) + 360) % 360;

                            // Determine which segment the block falls into
                            int segmentIndex = (int)(angle / segmentAngle);

                            if (segmentIndex >= 0 && segmentIndex < numSegments) {
                                segments.get(segmentIndex).addBlock(new Location(world, x, y, z));
                            }
                        }
                    }
                }
            }
        }

        return segments;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // User is trying to complete the subcommand
            if ("create".startsWith(args[0].toLowerCase())) {
                completions.add("create");
            }
            if ("delete".startsWith(args[0].toLowerCase())) {
                completions.add("delete");
            }
            if ("list".startsWith(args[0].toLowerCase())) {
                completions.add("list");
            }
            if ("clearCache".startsWith(args[0].toLowerCase())) {
                completions.add("clearCache");
            }
        } else if (args.length == 2) {
            // User has entered a subcommand and is now trying to complete the next argument
            // For the delete command, provide a list of existing segment groups
            if (args[0].equalsIgnoreCase("delete") || args[0].equalsIgnoreCase("clearCache")) {
                for (String segmentGroup : JoTHungerGames.getInstance().getAllSegmentGroups()) {
                    if (segmentGroup.toLowerCase().contains(args[1].toLowerCase())) { // Use contains for a "retroactive" search
                        completions.add(segmentGroup);
                    }
                }
            }
            // For the create command, additional arguments might be handled differently
            // You can add suggestions for creation or handle it as needed
        }
        // Add more conditional blocks for further arguments if necessary

        return completions;
    }
}
