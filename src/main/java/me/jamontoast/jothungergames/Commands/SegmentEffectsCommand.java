package me.jamontoast.jothungergames.Commands;

import me.jamontoast.jothungergames.Enums.SegmentEffects;
import me.jamontoast.jothungergames.JoTHungerGames;
import me.jamontoast.jothungergames.Interfaces.SegmentEffectStrategy;
import me.jamontoast.jothungergames.Utilities.SegmentEffectManager;
import org.apache.commons.lang3.math.NumberUtils;
import org.bukkit.Bukkit;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;
import java.util.List;

public class SegmentEffectsCommand implements CommandExecutor, TabCompleter {

    //Constants for subcommands
    private static final String START_SUBCOMMAND = "start";
    private static final String STOP_SUBCOMMAND = "stop";
    private static final String LIST_SUBCOMMAND = "list";

    public boolean isValidEffect(String arg) {
        try {
            SegmentEffects effect = SegmentEffects.valueOf(arg.toUpperCase());
            return true;  // The conversion succeeded, so it's a valid effect
        } catch (IllegalArgumentException e) {
            return false;  // The conversion failed, so it's not a valid effect
        }
    }

    // Map to store effects for quick retrieval
    private final Map<String, SegmentEffectStrategy> effectsMap = new HashMap<>();
    public SegmentEffectsCommand() {
        for (SegmentEffects effect : SegmentEffects.values()) {
            effectsMap.put(effect.name(), effect.getStrategy());
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // Ensure there is at least one argument for the subcommand
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Subcommand required. Usage: " + ChatColor.GOLD + "/hgsegmentEffect " + ChatColor.YELLOW + "<" + ChatColor.GOLD + "start"
                    + ChatColor.YELLOW + "|" + ChatColor.GOLD + "stop" + ChatColor.YELLOW + "|" + ChatColor.GOLD + "list" + ChatColor.YELLOW + ">");
            return true;
        }

        // Determine action based on subcommand
        String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            case START_SUBCOMMAND:_SUBCOMMAND:
                return handleStartEffect(sender, Arrays.copyOfRange(args, 1, args.length));
            case STOP_SUBCOMMAND:
                return handleStopEffect(sender, Arrays.copyOfRange(args, 1, args.length));
            case LIST_SUBCOMMAND:
                return handleListEffects(sender, Arrays.copyOfRange(args, 1, args.length));
            default:
                sender.sendMessage(ChatColor.RED + "Unknown subcommand. Usage: " + ChatColor.GOLD + "/hgsegmentEffect " + ChatColor.YELLOW + "<" + ChatColor.GOLD + "start"
                        + ChatColor.YELLOW + "|" + ChatColor.GOLD + "stop" + ChatColor.YELLOW + "|" + ChatColor.GOLD + "list" + ChatColor.YELLOW + ">");
                return true;
        }
    }

    private boolean handleStartEffect(CommandSender sender, String[] args) {

        if (!validateStartInputArguments(sender, args)) {
            Bukkit.getLogger().info("One or more inputs was invalid.");
            return true;
        }

        String segmentGroup = args[0];
        int segmentNumber = Integer.parseInt(args[1]);
        String segmentEffect = args[2].toUpperCase();

        SegmentEffectStrategy effectStrategy = effectsMap.get(segmentEffect);

        if (effectStrategy == null) {
            sender.sendMessage(ChatColor.RED + "Effect does not exist!");
            return true;
        }

        SegmentEffectManager manager = JoTHungerGames.getInstance().getSegmentEffectManager();

        Long durationOverride = null;
        manager.startEffect(segmentEffect, segmentGroup, segmentNumber, durationOverride);
        return true;
    }

    private boolean handleStopEffect(CommandSender sender, String[] args) {

        if (!validateStopInputArguments(sender, args)) {
            Bukkit.getLogger().info("One or more inputs was invalid.");
            return true;
        }

        String segmentGroup = args[0];
        int segmentNumber = Integer.parseInt(args[1]);
        String segmentEffect = args[2].toUpperCase();

        SegmentEffectStrategy effectStrategy = effectsMap.get(segmentEffect);

        if (effectStrategy == null) {
            sender.sendMessage(ChatColor.RED + "Effect does not exist!");
            return true;
        }

        SegmentEffectManager manager = JoTHungerGames.getInstance().getSegmentEffectManager();

        manager.stopEffect(segmentEffect, segmentGroup, segmentNumber);
        return true;
    }

    private boolean handleListEffects(CommandSender sender, String[] args) {

        if (!validateListInputArguments(sender, args)) {
            Bukkit.getLogger().info("One or more inputs was invalid.");
            return true;
        }

        ConfigurationSection segmentGroups = JoTHungerGames.getInstance().getSegmentConfig().getConfigurationSection("segment groups");

        if (args.length == 0) {

            // Sending the list of effects and their descriptions
            sender.sendMessage(ChatColor.GOLD + "List of Segment Effects:");
            for (SegmentEffects effect : SegmentEffects.values()) {
                // Create a new TextComponent for each effect
                TextComponent message = new TextComponent(effect.name());
                message.setColor(ChatColor.YELLOW);

                // Create the hover text (description)
                TextComponent hoverText = new TextComponent(effect.getDescription());
                hoverText.setColor(ChatColor.WHITE);

                // Add the hover event to the message
                message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{hoverText}));

                // Send the message to the sender
                sender.spigot().sendMessage(message);
            }
        }
        else if (args.length == 1 && args[0].equalsIgnoreCase("active")) {

            if (segmentGroups == null) {
                sender.sendMessage(ChatColor.RED + "No segment groups found for an effect to be active in.");
                return true;
            }

            int numActiveEffects = 0;

            //Sending the list of active effects
            sender.sendMessage(ChatColor.GOLD + "List of Active Effects:");

            // Iterate through each segment group
            for (String groupKey : segmentGroups.getKeys(false)) {
                ConfigurationSection segmentsSection = segmentGroups.getConfigurationSection(groupKey + ".segments");
                if (segmentsSection == null) continue;

                // Iterate through each segment in the group
                for (String segmentKey : segmentsSection.getKeys(false)) {
                    ConfigurationSection activeEffectsSection = segmentsSection.getConfigurationSection(segmentKey + ".activeEffects");
                    if (activeEffectsSection != null) {

                        // Iterate through each active effect in the segment
                        for (String effectKey : activeEffectsSection.getKeys(false)) {
                            boolean isActive = activeEffectsSection.getBoolean(effectKey);
                            if (isActive) {
                                ++numActiveEffects;
                                SegmentEffects effectEnum = SegmentEffects.valueOf(effectKey.toUpperCase());
                                TextComponent message = new TextComponent(ChatColor.GOLD + effectKey + ChatColor.WHITE + " is" + ChatColor.GREEN + " ACTIVE" +
                                        ChatColor.WHITE + " in segment " + ChatColor.YELLOW + segmentKey + ChatColor.WHITE +  " of " +
                                        ChatColor.YELLOW + groupKey + ChatColor.WHITE + ".");

                                // Create the hover text (description)
                                TextComponent hoverText = new TextComponent(effectEnum.getDescription());
                                hoverText.setColor(net.md_5.bungee.api.ChatColor.WHITE);

                                // Add the hover event to the message
                                message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{hoverText}));

                                // Send the message to the sender
                                sender.spigot().sendMessage(message);
                            }
                        }
                    }
                }
            }
            if (numActiveEffects == 0) {
                sender.sendMessage(ChatColor.RED + "NONE");
            }
        }

        return true;
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

    private boolean validateListInputArguments(CommandSender sender, String[] args) {

        // Validation logic here, returning false and sending error messages as required
        String CorrectUsageExample = ChatColor.RED + "Correct usage: " + ChatColor.GOLD + "/hgsegmentEffect "
                + ChatColor.YELLOW + " <" + ChatColor.GOLD + "list" + ChatColor.YELLOW + ">"
                + org.bukkit.ChatColor.YELLOW + " [" + org.bukkit.ChatColor.GOLD + "active" + org.bukkit.ChatColor.YELLOW + "]";

        if (args.length > 1) {
            sender.sendMessage(ChatColor.RED + "You have submitted too many arguments!");
            sender.sendMessage(CorrectUsageExample);
        }else {
            Bukkit.getLogger().info("Argument count valid.");
        }
        if (args.length == 1 && !args[0].equalsIgnoreCase("active")) {
            sender.sendMessage(ChatColor.RED + args[0] + "is not a recognised subcommand!");
            sender.sendMessage(CorrectUsageExample);
        }else {
            Bukkit.getLogger().info("List subcommand valid.");
        }

        return true; // If all validations pass
    }

    //sets up tabCompleter
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

        List<String> completions = new ArrayList<>();


        // First argument: Subcommands
        if (args.length == 1) {
            List<String> subCommands = Arrays.asList(START_SUBCOMMAND, STOP_SUBCOMMAND, LIST_SUBCOMMAND);
            for (String subCommand : subCommands) {
                if (subCommand.startsWith(args[0].toLowerCase())) {
                    completions.add(subCommand);
                }
            }
        }
        // Second argument: Segment Group Names
        else if (args.length == 2 && !(args[0].equalsIgnoreCase("list"))) {
            List<String> segmentGroups = JoTHungerGames.getInstance().getAllSegmentGroups();
            for (String segmentGroup : segmentGroups) {
                if (segmentGroup.toLowerCase().contains(args[1].toLowerCase())) { //uses contains as opposed to startsWith
                    completions.add(segmentGroup);
                }
            }
        }
        // Second argument: List Active
        else if (args.length == 2 && (args[0].equalsIgnoreCase("list"))) {
            String listActiveSubcommand = "active";
            if (listActiveSubcommand.contains(args[1].toLowerCase())) { //uses contains as opposed to startsWith
                completions.add(listActiveSubcommand);
            }
        }
        // Third argument: Segment Numbers for the selected group
        else if (args.length == 3 && JoTHungerGames.getInstance().segmentGroupExists(args[1]) && !(args[0].equalsIgnoreCase("list"))) {
            List<String> segmentNumbers = JoTHungerGames.getInstance().getAllSegmentNumbersForGroup(args[1]);
            for (String segmentNumber : segmentNumbers) {
                if (segmentNumber.startsWith(args[2])) {
                    completions.add(segmentNumber);
                }
            }
        }
        // Fourth argument: Effects
        else if (args.length == 4 && !(args[0].equalsIgnoreCase("list"))) {
            for (SegmentEffects effect : SegmentEffects.values()) {
                String effectName = effect.name().toLowerCase();
                if (effectName.contains(args[3].toLowerCase())) { //uses contains as opposed to startsWith
                    completions.add(effectName);
                }
            }
        }

        return completions;
    }
}





