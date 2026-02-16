package me.jamontoast.jothungergames.Utilities;

import me.jamontoast.jothungergames.Enums.SegmentEffects;
import me.jamontoast.jothungergames.Interfaces.SegmentEffectStrategy;
import me.jamontoast.jothungergames.JoTHungerGames;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang3.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

public class EffectsUtils {

    //Constants for subcommands
    private static final String START_SUBCOMMAND = "start";
    private static final String STOP_SUBCOMMAND = "stop";

    public boolean isValidEffect(String arg) {
        try {
            SegmentEffects effect = SegmentEffects.valueOf(arg.toUpperCase());
            return true;  // The conversion succeeded, so it's a valid effect
        } catch (IllegalArgumentException e) {
            return false;  // The conversion failed, so it's not a valid effect
        }
    }

    // Map to store effects for quick retrieval
    private static final Map<String, SegmentEffectStrategy> effectsMap = new HashMap<>();
    public static void SegmentEffectsHandler(String subCommand, String segmentGroup, String segmentEffect, int segmentNumber, Long durationOverride) {
        for (SegmentEffects effect : SegmentEffects.values()) {
            effectsMap.put(effect.name(), effect.getStrategy());
        }

        switch (subCommand) {
            case START_SUBCOMMAND:
                handleStartEffect(subCommand, segmentGroup, segmentEffect, segmentNumber, durationOverride);
            case STOP_SUBCOMMAND:
                handleStopEffect(subCommand, segmentGroup, segmentEffect, segmentNumber);
            default:
        }
    }

    private static void handleStartEffect(String subCommand, String segmentGroup, String segmentEffect, int segmentNumber, Long durationOverride) {

        SegmentEffectManager manager = JoTHungerGames.getInstance().getSegmentEffectManager();

        manager.startEffect(segmentEffect, segmentGroup, segmentNumber, durationOverride);
    }

    private static void handleStopEffect(String subCommand, String segmentGroup, String segmentEffect, int segmentNumber) {

        SegmentEffectManager manager = JoTHungerGames.getInstance().getSegmentEffectManager();

        manager.stopEffect(segmentEffect, segmentGroup, segmentNumber);
    }

    // Validation logic here, returning false and sending error messages as required
    private boolean validateStartInputArguments(CommandSender sender, String[] args) {
        // Validation logic here, returning false and sending error messages as required
        String CorrectUsageExample = ChatColor.RED + "Correct usage: " + ChatColor.GOLD + "/hgsegmentEffect "
                + ChatColor.YELLOW + " <" + ChatColor.GOLD + "start" + ChatColor.YELLOW + ">"
                + ChatColor.YELLOW + " <" + ChatColor.GOLD + "segment group" + ChatColor.YELLOW + ">"
                + ChatColor.YELLOW + " <" + ChatColor.GOLD + "segment number" + ChatColor.YELLOW + ">"
                + ChatColor.YELLOW + " <" + ChatColor.GOLD + "effect" + ChatColor.YELLOW + ">";

        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "You are missing arguments!");
            sender.sendMessage(CorrectUsageExample);
            return false;
        }else if (args.length > 3) {
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
        //checks that the third arg lists a valid effect
        if (!isValidEffect(args[2].toUpperCase())) {
            sender.sendMessage(ChatColor.RED + "You must enter a valid effect.");
            sender.sendMessage(CorrectUsageExample);
            return true;
        }else {
            Bukkit.getLogger().info("Effect valid.");
        }
        // Add other validation checks...

        return true; // If all validations pass
    }

    private boolean validateStopInputArguments(CommandSender sender, String[] args) {
        // Validation logic here, returning false and sending error messages as required
        String CorrectUsageExample = ChatColor.RED + "Correct usage: " + ChatColor.GOLD + "/hgsegmentEffect "
                + ChatColor.YELLOW + " <" + ChatColor.GOLD + "stop" + ChatColor.YELLOW + ">"
                + ChatColor.YELLOW + " <" + ChatColor.GOLD + "segment group" + ChatColor.YELLOW + ">"
                + ChatColor.YELLOW + " <" + ChatColor.GOLD + "segment number" + ChatColor.YELLOW + ">"
                + ChatColor.YELLOW + " <" + ChatColor.GOLD + "effect" + ChatColor.YELLOW + ">";

        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "You are missing arguments!");
            sender.sendMessage(CorrectUsageExample);
            return false;
        }else if (args.length > 3) {
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

        // Fetch the configuration section for the specified segment's active effects
        String pathToActiveEffects = "segment groups." + args[0] + ".segments." + args[1] + ".activeEffects";
        ConfigurationSection activeEffectsSection = JoTHungerGames.getInstance().getSegmentConfig().getConfigurationSection(pathToActiveEffects);
        //checks that the third arg lists a valid active effect
        if (!isValidEffect(args[2].toUpperCase())) {
            sender.sendMessage(ChatColor.RED + "You must enter a valid effect.");
            sender.sendMessage(CorrectUsageExample);
            return true;
        }else if (activeEffectsSection == null || !activeEffectsSection.getBoolean(args[2].toUpperCase(), false)) {
            sender.sendMessage(ChatColor.RED + "The effect " + args[2].toUpperCase() + " is not active or doesn't exist for segment " + ChatColor.GOLD + args[1]
                    + ChatColor.RED + " in group " + ChatColor.GOLD + args[0] + ChatColor.RED + ".");
            return false;
        }
        else {
            Bukkit.getLogger().info("Effect valid.");
        }
        // Add other validation checks...

        return true; // If all validations pass
    }
}