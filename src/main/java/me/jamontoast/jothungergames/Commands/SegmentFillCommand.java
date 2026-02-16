package me.jamontoast.jothungergames.Commands;

import me.jamontoast.jothungergames.JoTHungerGames;
import me.jamontoast.jothungergames.Utilities.SegmentUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.entity.Player;


import java.util.*;


public class SegmentFillCommand implements CommandExecutor, TabCompleter {

    //sets the CorrectUsageExample string to be copied into all error messages for the command.
    String CorrectUsageExample = ChatColor.RED + "Correct usage: " + ChatColor.GOLD + "/hgsegmentFill "
            + ChatColor.YELLOW + " <" + ChatColor.GOLD + "segment group" + ChatColor.YELLOW + ">"
            + ChatColor.YELLOW + " <" + ChatColor.GOLD + "segment number" + ChatColor.YELLOW + ">"
            + ChatColor.YELLOW + " <" + ChatColor.GOLD + "block type" + ChatColor.YELLOW + ">"
            + ChatColor.YELLOW + " [" + ChatColor.GOLD + "layer" + ChatColor.YELLOW + "]";


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!validateInputArguments(sender, args)) {
            Bukkit.getLogger().info("One or more inputs was invalid.");
            return true;
        }

        Player player = (Player) sender;
        String segmentGroup = args[0];
        int segmentNumber = Integer.parseInt(args[1]);
        Material fillMaterial = Material.getMaterial(args[2].toUpperCase());
        Integer relativeLayer = null;

        if (args.length == 4) {
            relativeLayer = Integer.parseInt(args[3]);
        }
        else {
            relativeLayer = 1;
        }
        int segmentHeight = SegmentUtils.getHeight(segmentGroup);
        int centerY = Integer.parseInt(SegmentUtils.getGroupCenter(segmentGroup)[1]);

        // Validate relativeLayer if it's provided
        if (relativeLayer != null && (relativeLayer < 1 || relativeLayer > segmentHeight)) {
            sender.sendMessage(ChatColor.RED + "The relative layer must be within the segment height.");
            sender.sendMessage(CorrectUsageExample);
            return true;
        }
        // Calculate the actual Y-level to fill based on the relativeLayer and centerY
        int actualY = centerY + relativeLayer - 1;

        //actually do the fill stuff
        List<JoTHungerGames.Segment> segmentsList = JoTHungerGames.getInstance().getSegmentsFromConfig(segmentGroup);
        JoTHungerGames.Segment segmentToFill = segmentsList.get(segmentNumber - 1);  // Subtract 1 to account for 0-based indexing

        for (Location blockLoc : segmentToFill.getBlocks()) {
            if (relativeLayer == null || blockLoc.getBlockY() == actualY) {
                blockLoc.getBlock().setType(fillMaterial);
            }
        }
        player.sendMessage(ChatColor.GREEN + "Segment " + ChatColor.YELLOW + segmentNumber + ChatColor.GREEN + " of " + ChatColor.YELLOW + segmentGroup
                + ChatColor.GREEN + " filled with " + ChatColor.YELLOW + fillMaterial + ChatColor.GREEN + ".");

        return true;
    }

    // Validation logic here, returning false and sending error messages as required
    private boolean validateInputArguments(CommandSender sender, String[] args) {
        // Validation logic here, returning false and sending error messages as required

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command!");
            return false;
        }
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "You are missing arguments!");
            sender.sendMessage(CorrectUsageExample);
            return false;
        }else if (args.length > 4) {
            sender.sendMessage(ChatColor.RED + "You have submitted too many arguments!");
            sender.sendMessage(CorrectUsageExample);
        }else {
            Bukkit.getLogger().info("Argument count valid.");
        }
        //checks to see if the first arg is a valid segment group
        if (!(JoTHungerGames.getInstance().segmentGroupExists(args[0]))) {
            sender.sendMessage(ChatColor.RED + args[0] + " is not a valid segment group.");
            sender.sendMessage(CorrectUsageExample);
            return false;
        }else {
            Bukkit.getLogger().info("Segment group valid.");
        }
        //checks to see if the second arg is a valid integer, and if that integer is 1 or greater
        if (!NumberUtils.isParsable(args[1]) || Integer.parseInt(args[1]) < 1) {
            sender.sendMessage(ChatColor.RED + "Segment number must be a positive integer 1 or greater.");
            sender.sendMessage(CorrectUsageExample);
            return false;
        }else {
            Bukkit.getLogger().info("Segment number valid integer.");
        }
        //checks to see if the second arg is a valid segment number within our chosen group
        if (!(JoTHungerGames.getInstance().segmentExistsWithinGroup(args[0], Integer.parseInt(args[1])))) {
            sender.sendMessage(ChatColor.RED + "Segment number must be a valid segment.");
            sender.sendMessage(CorrectUsageExample);
            return false;
        }else {
            Bukkit.getLogger().info("Segment valid.");
        }
        //checks to see if the third arg is a valid block type
        if (Material.getMaterial((args[2].toUpperCase())) == null) {
            sender.sendMessage(ChatColor.RED + "You must enter a valid block material.");
            sender.sendMessage(CorrectUsageExample);
            return false;
        }
        if (args.length == 4) {
            if (!NumberUtils.isParsable(args[3]) || Integer.parseInt(args[3]) < 1) {
                sender.sendMessage(ChatColor.RED + "Layer must be a valid layer.");
                sender.sendMessage(CorrectUsageExample);
                return false;
            }
        }
        // Add other validation checks...

        return true; // If all validations pass
    }

    //sets up tabCompleter
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

        List<String> completions = new ArrayList<>();

        // If it's the first argument (group name)
        if (args.length == 1) {
            for (String segmentGroup : JoTHungerGames.getInstance().getAllSegmentGroups()) {
                if (segmentGroup.toLowerCase().contains(args[0].toLowerCase())) { // Use contains for a "retroactive" search
                    completions.add(segmentGroup);
                }
            }
        }
        // If it's the second argument (segment number)
        else if (args.length == 2 && JoTHungerGames.getInstance().segmentGroupExists(args[0])) {
            List<String> segments = JoTHungerGames.getInstance().getAllSegmentNumbersForGroup(args[0]);
            for (String segmentNumber : segments) {
                if (segmentNumber.startsWith(args[1])) {
                    completions.add(segmentNumber);
                }
            }
        }
        // If it's the third argument (block type)
        else if (args.length == 3) {
            for (Material mat : Material.values()) {
                if (mat.isBlock() && mat.name().toLowerCase().contains(args[2].toLowerCase())) { // Use contains for a "retroactive" search
                    completions.add(mat.name().toLowerCase());
                }
            }
        }
// If it's the fourth argument
        // If it's the fourth argument (layer)
        else if (args.length == 4 && JoTHungerGames.getInstance().segmentGroupExists(args[0])) {
            // Fetch the height for the segment group
            int height = SegmentUtils.getHeight(args[0]);
            for (int i = 1; i <= height; i++) {
                completions.add(String.valueOf(i));
            }
        }

        return completions;
    }

}
