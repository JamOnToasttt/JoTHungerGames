package me.jamontoast.jothungergames.Commands;

import me.jamontoast.jothungergames.JoTHungerGames;
import me.jamontoast.jothungergames.Tasks.FallenDisplayTask;
import me.jamontoast.jothungergames.Utilities.*;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang3.math.NumberUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.List;

public class ArenaCommand implements CommandExecutor, TabCompleter {

    private static final String START_SUBCOMMAND = "start";
    private static final String STOP_SUBCOMMAND = "stop";
    private static final String CREATE_SUBCOMMAND = "create";
    private static final String DELETE_SUBCOMMAND = "delete";
    private static final String LIST_SUBCOMMAND = "list";
    private static final String CHEST_SUBCOMMAND = "chest";
    //private static final String SPECTATOR_SUBCOMMAND = "spectator";


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // Ensure there is at least one argument for the subcommand
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Subcommand required. Usage: " + ChatColor.GOLD + "/hgarena "
                    + ChatColor.YELLOW + "<" + ChatColor.GOLD + "start" + ChatColor.YELLOW + "|" + ChatColor.GOLD + "stop"
                    + ChatColor.YELLOW + "|" + ChatColor.GOLD + "create" + ChatColor.YELLOW + "|" + ChatColor.GOLD + "delete"
                    + ChatColor.YELLOW + "|" + ChatColor.GOLD + "list" +  ChatColor.YELLOW + "|" + ChatColor.GOLD + "chest" + ChatColor.YELLOW + ">");
            return true;
        }

        // Determine action based on subcommand
        String subCommand = args[0].toLowerCase();
        return switch (subCommand) {
            case START_SUBCOMMAND -> handleStartArena(sender, Arrays.copyOfRange(args, 1, args.length));
            case STOP_SUBCOMMAND -> handleStopArena(sender, Arrays.copyOfRange(args, 1, args.length));
            case CREATE_SUBCOMMAND -> handleCreateArena(sender, Arrays.copyOfRange(args, 1, args.length));
            case DELETE_SUBCOMMAND -> handleDeleteArena(sender, Arrays.copyOfRange(args, 1, args.length));
            case LIST_SUBCOMMAND -> handleListArena(sender, Arrays.copyOfRange(args, 1, args.length));
            case CHEST_SUBCOMMAND -> {
                if (args.length > 1 && args[1].equalsIgnoreCase("check")) {
                    if (args.length == 2) {
                        yield handleChestCheck(sender);
                    } else {
                        sender.sendMessage(ChatColor.RED + "Too many arguments! Usage: " + ChatColor.GOLD + "/hga chest check");
                        yield true;
                    }
                } else {
                    yield handleChestArena(sender, Arrays.copyOfRange(args, 1, args.length));
                }
            }
            //case SPECTATOR_SUBCOMMAND -> handleSpectatorArena(sender, Arrays.copyOfRange(args, 1, args.length));
            default -> {
                sender.sendMessage(ChatColor.RED + "Unknown subcommand. Usage: " + ChatColor.GOLD + "/hgarena "
                        + ChatColor.YELLOW + "<" + ChatColor.GOLD + "start" + ChatColor.YELLOW + "|" + ChatColor.GOLD + "stop"
                        + ChatColor.YELLOW + "|" + ChatColor.GOLD + "create" + ChatColor.YELLOW + "|" + ChatColor.GOLD + "delete"
                        + ChatColor.YELLOW + "|" + ChatColor.GOLD + "list" + ChatColor.YELLOW + "|" + ChatColor.GOLD + "chest"
                        + "|" + ChatColor.GOLD + "spectator" + ChatColor.YELLOW + ">");
                yield true;
            }
        };
    }

    /*private boolean handleSpectatorArena(CommandSender sender, String[] args) {

        String arenaName = args[0];
        ArenaManager arenaManager = ArenaManager.getInstance();
        ArenaData arenaData = arenaManager.getArenaData(arenaName);

        long displayDuration = (500 / (arenaData.getGamePlayers().size() + 1));// Duration to display in ticks (3 seconds)
        FileConfiguration mainConfig = JoTHungerGames.getInstance().getConfig();
        String musicStyle = mainConfig.getString("music style");
        if ("real".equals(musicStyle)) {
            displayDuration = (680 / (arenaData.getGamePlayers().size() + 1));// Duration to display in ticks (3 seconds)
        }

        Location soundPlayerLoc = arenaData.getArenaCenter().clone().add(0.0,50.0,0.0);

        double heightAboveCenter = 100.0;
        double insetDistance = 15.0;  // How far inside the dome surface

        double effectiveRadius = arenaData.getArenaRadius() - insetDistance;

        if (arenaData.getArenaRadius() <= heightAboveCenter) {
            Bukkit.getLogger().warning("Arena radius too small for fallen display height. Using fallback position.");
            heightAboveCenter = arenaData.getArenaRadius() * 0.5;  // Fallback to half the radius
        }

        double horizontalDistance = Math.sqrt(
                effectiveRadius * effectiveRadius - heightAboveCenter * heightAboveCenter
        );

        // Determine how many displays to spawn
        List<Location> displayLocations = new ArrayList<>();

        if (arenaData.getArenaRadius() > 100) {
            // Large arena: spawn in all 4 cardinal directions
            // North
            displayLocations.add(new Location(
                    arenaData.getArenaCenter().getWorld(),
                    arenaData.getArenaCenter().getX(),
                    arenaData.getArenaCenter().getY() + heightAboveCenter,
                    arenaData.getArenaCenter().getZ() - horizontalDistance
            ));
            // South
            displayLocations.add(new Location(
                    arenaData.getArenaCenter().getWorld(),
                    arenaData.getArenaCenter().getX(),
                    arenaData.getArenaCenter().getY() + heightAboveCenter,
                    arenaData.getArenaCenter().getZ() + horizontalDistance
            ));
            // East
            displayLocations.add(new Location(
                    arenaData.getArenaCenter().getWorld(),
                    arenaData.getArenaCenter().getX() + horizontalDistance,
                    arenaData.getArenaCenter().getY() + heightAboveCenter,
                    arenaData.getArenaCenter().getZ()
            ));
            // West
            displayLocations.add(new Location(
                    arenaData.getArenaCenter().getWorld(),
                    arenaData.getArenaCenter().getX() - horizontalDistance,
                    arenaData.getArenaCenter().getY() + heightAboveCenter,
                    arenaData.getArenaCenter().getZ()
            ));

            Bukkit.getLogger().info("Fallen Display running at 4 cardinal locations for large arena (radius: " + arenaData.getArenaRadius() + ")");
        }else {
            // Small arena: single display at north
            displayLocations.add(new Location(
                    arenaData.getArenaCenter().getWorld(),
                    arenaData.getArenaCenter().getX(),
                    arenaData.getArenaCenter().getY() + heightAboveCenter,
                    arenaData.getArenaCenter().getZ() - horizontalDistance
            ));

            Bukkit.getLogger().info("Fallen Display running at north location for small arena (radius: " + arenaData.getArenaRadius() + ")");
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            if ("real".equals(musicStyle)) {
                player.playSound(soundPlayerLoc, "jothungergames.hornofplenty_real_oldrecord", 20.0f, 1.0f);
            }else {
                player.playSound(soundPlayerLoc, "jothungergames.hornofplenty_noteblocks_oldrecord", 20.0f, 1.0f);
            }
            Bukkit.getLogger().info("Playing anthem for " + player);
        }

        List<String> deadPlayers = new ArrayList<>();
        deadPlayers.add(0, "JamOnToast_");
        
        FallenDisplayTask fallenDisplayTask = new FallenDisplayTask(
                deadPlayers,
                arenaData.getArenaCenter(),
                arenaData.getArenaRadius(),
                displayLocations,
                displayDuration
        );
        arenaData.setFallenDisplayTask(fallenDisplayTask);
        arenaData.getFallenDisplayTask().runTaskTimer(JoTHungerGames.getInstance(), 0L, displayDuration);
        return true;
    }*/

    private boolean handleStartArena(CommandSender sender, String[] args) {

        try {

            if (!validateStartInputArguments(sender, args)) {
                Bukkit.getLogger().info("One or more inputs was invalid.");
                return true;
            }
            String arenaName = args[0];
            ArenaManager arenaManager = ArenaManager.getInstance();
            arenaManager.addArena(arenaName, false);
            ArenaData arenaData = arenaManager.getArenaData(arenaName);


            if (!arenaManager.initializeArena(arenaName)) {
                sender.sendMessage(ChatColor.RED + "Failed to initialize arena.");
                return true;
            }

            arenaManager.setArenaActive(arenaName, true);

            ArenaChecker arenaChecker = new ArenaChecker(arenaName);

            FileConfiguration config = JoTHungerGames.getInstance().getArenaConfig();
            String basePath = "arenas." + arenaName;

            //reset player sets and maps
            arenaData.setTotalDeadPlayers(new HashSet<>());

            arenaData.setCurrentDay(1);
            arenaData.setDeadPlayersByDay(new HashMap<>());
            arenaData.getDeadPlayersByDay().put(1, new ArrayList<>());

            int numPlayers = 0;
            int maxPlayers = arenaData.getMaxPlayers();

            // Populate currentPlayersInLobby with players inside the lobby
            Set<Player> currentPlayersInLobby = new HashSet<>();
            for (Player player : Bukkit.getOnlinePlayers()) {

                if (arenaChecker.isInsideLobby(player.getLocation())) {
                    currentPlayersInLobby.add(player); // Add player to the set
                }
            }
            if (args.length > 1) {
                numPlayers = Integer.parseInt(args[1]);

                if (currentPlayersInLobby.size() < numPlayers) {
                    sender.sendMessage(ChatColor.RED + "There are fewer than " + numPlayers + " players in the " + arenaName + " lobby!");
                    return false;
                }
            }else {
                numPlayers = Math.min(currentPlayersInLobby.size(), maxPlayers);

                if (numPlayers == 0) {
                    sender.sendMessage(ChatColor.RED + "There are no players in the " + arenaName + " lobby with which to start a game!");
                    return false;
                }
            }

            // Select players for the game
            Iterator<Player> playerIterator = currentPlayersInLobby.iterator();
            Set<UUID> gamePlayers = new HashSet<>();
            for (int i = 0; i < numPlayers && playerIterator.hasNext(); i++) {
                gamePlayers.add(playerIterator.next().getUniqueId());
            }
            arenaData.setGamePlayers(new HashSet<>(gamePlayers));
            arenaData.setAlivePlayers(new HashSet<>(gamePlayers));


            //Set up player stats and scoreboard
            JoTHungerGames.getInstance().resetScoreboard("Kills");

            Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
            Team spectatorTeam = scoreboard.getTeam("Spectators");
            if (spectatorTeam == null) {
                spectatorTeam = scoreboard.registerNewTeam("Spectators");
                spectatorTeam.setPrefix(ChatColor.GRAY.toString() + ChatColor.BOLD + "SPECTATOR " + ChatColor.GRAY);
                spectatorTeam.setCanSeeFriendlyInvisibles(true);
            }
            for (UUID uuid : gamePlayers) {
                Player player = Bukkit.getPlayer(uuid);
                if (player == null || !player.isOnline()) continue;

                player.setGameMode(GameMode.ADVENTURE);
                player.setFlying(false);
                player.setAllowFlight(false);
                player.setCollidable(true);
                player.setCanPickupItems(true);
                for (PotionEffect effect : player.getActivePotionEffects()) {
                    player.removePotionEffect(effect.getType());
                }
                for (Player pl: Bukkit.getOnlinePlayers()){
                    pl.showPlayer(player);
                }
                SpectatorCurrency.clearCurrency(player);
                spectatorTeam.removeEntry(player.getName());

                player.setRespawnLocation(arenaData.getArenaDeathSpawn());
                Bukkit.getLogger().info("set respawn for " + player + " to " + arenaData.getArenaDeathSpawn());
                player.getInventory().clear();
                player.setHealth(20);
                player.setFoodLevel(20);
                player.setExp(0);
                player.setLevel(0);

                if (arenaData.isEventBroadcastSidebar()) {
                    player.setScoreboard(ScoreboardBuilder.getOrCreate(player)
                            .setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "Hunger Games")

                            .setLine(1, "Kills: " + ChatColor.RED + "0")
                            .setLine(2, " ")
                            .setLine(3, "Players left: " + ChatColor.GREEN + gamePlayers.size())
                            .setLine(4, "  ")
                            .setLine(5, ChatColor.GREEN + "Game starting!")
                            .setLine(6, "Next event:")
                            .setLine(7, "     ")
                            .setLine(8, "Game time: " + ChatColor.YELLOW + "00:00:00")
                            .setLine(9, "      ")
                            .build()
                    );
                } else {
                    player.setScoreboard(ScoreboardBuilder.getOrCreate(player)
                            .setDisplayName(ChatColor.BOLD.toString() + ChatColor.YELLOW + "Hunger Games")

                            .setLine(1, "Kills: " + ChatColor.RED + "0")
                            .setLine(2, " ")
                            .setLine(3, "Players left: " + ChatColor.GREEN + gamePlayers.size())
                            .setLine(4, "   ")
                            .setLine(5, "Game time: " + ChatColor.YELLOW + "00:00:00")
                            .setLine(6, "    ")
                            .build()
                    );
                }
            }

            //reset chests
            ChestUtilities chestUtilities = new ChestUtilities();
            chestUtilities.refillChestsByType(arenaName, "center");
            chestUtilities.refillChestsByType(arenaName, "normal");
            chestUtilities.refillChestsByType(arenaName, "prime");

            //register Death Listener
            arenaManager.registerDeathListener(arenaName);
            arenaManager.registerPlayerRejoinHandler(arenaName);
            arenaManager.registerSpectatorListener(arenaName);

            Bukkit.getLogger().info(arenaName + " is now running an ACTIVE game.");
            // Start countdown
            arenaManager.teleportCountdown(arenaName);

            return true;
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "An error occurred while starting the arena. Check server logs for details.");
            Bukkit.getLogger().severe("Error in handleStartArena: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private List<Long> getConfigRefillTimes(FileConfiguration config, String path) {
        List<Long> refillTimes = new ArrayList<>();
        ConfigurationSection section = config.getConfigurationSection(path);
        if (section != null) {
            for (String key : section.getKeys(false)) {
                refillTimes.add(section.getLong(key) * 20); // Convert seconds to ticks
            }
        }
        return refillTimes;
    }
    private boolean handleStopArena(CommandSender sender, String[] args) {

        if (!validateStopInputArguments(sender, args)) {
            Bukkit.getLogger().info("One or more inputs was invalid.");
            return false;
        }
        String arenaName = args[0];
        ArenaManager arenaManager = ArenaManager.getInstance();
        ArenaData arenaData = arenaManager.getArenaData(arenaName);
        if (arenaData == null || !arenaData.isActive()) {
            sender.sendMessage(ChatColor.RED + "Arena " + arenaName + " is not active!");
            Bukkit.getLogger().info("Arena stop sequence not possible, " + arenaName + " is not active.");
            return false;
        }

        Bukkit.getLogger().info("Stopping arena " + arenaName);
        arenaManager.stopArenaTasks(arenaName);
        arenaManager.resetArenaSpectators(arenaName);
        World world = arenaData.getArenaWorld();
        WorldBorder worldBorder = world.getWorldBorder();
        worldBorder.setSize(30000000);
        worldBorder.setCenter(new Location(world, 0, 255, 0));

        for (UUID uuid : arenaData.getGamePlayers()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline()) continue;

            player.setGameMode(GameMode.ADVENTURE);

            player.setFlying(false);
            player.setAllowFlight(false);
            player.setCollidable(true);
            player.setCanPickupItems(true);
            player.setInvulnerable(false);

            player.getInventory().clear();

            for (PotionEffect effect : player.getActivePotionEffects()) {
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

        //reset friendly fire
        Bukkit.getScoreboardManager().getMainScoreboard().getTeam("ArenaPlayers").setAllowFriendlyFire(true);

        Bukkit.getScheduler().runTaskLater(JoTHungerGames.getInstance(), () -> {
            arenaData.clearGamePlayers();
        }, 2L);


        sender.sendMessage(ChatColor.RED + "Stopped game in arena " + arenaName);

        return true;
    }

    private boolean handleCreateArena(CommandSender sender, String[] args) {
        // Implement the creation logic here
        // Similar to your existing onCommand logic but focused on creation

        // Ensure sender is a player and validate arguments
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command!");
            return false;
        }

        if (!validateCreateInputArguments(sender, args)) {
            Bukkit.getLogger().info("One or more inputs was invalid.");
            return false;
        }

        // Extracting arguments
        String arenaName = args[0];
        String arenaType = args[1].toUpperCase();
        int radius = Integer.parseInt(args[2]);
        int height = Integer.parseInt(args[3]);
        int numPlayers = Integer.parseInt(args[4]);

        Location center = player.getLocation();

        JoTHungerGames.getInstance().saveArenaToConfig(arenaName, center, arenaType, radius, height, numPlayers);
        Bukkit.getLogger().info("Arena saved to config.");  // Debug output
        ArenaManager arenaManager = ArenaManager.getInstance();
        arenaManager.addArena(arenaName, false);

        sender.sendMessage(ChatColor.YELLOW + arenaType + ChatColor.GREEN + " arena " + ChatColor.YELLOW + arenaName + ChatColor.GREEN + " saved at "
                + ChatColor.YELLOW + center.getBlockX() + "," + center.getBlockY() + "," + center.getBlockZ()
                + ChatColor.GREEN + ", with a radius of " + ChatColor.YELLOW + radius + ChatColor.GREEN + ", a height of "
                + ChatColor.YELLOW + height + ChatColor.GREEN + ", and a player limit of " + ChatColor.YELLOW + numPlayers
                + ChatColor.GREEN + ". Please find the arenas.yml config file and fill out the missing information to ensure that your arena is fully set up! Some arena-related commands may not work until the config is completely filled out and your server has been reloaded/restarted.");

        return true; // Return true when creation is successful
    }

    private boolean handleDeleteArena(CommandSender sender, String[] args) {

        if (!validateDeleteInputArguments(sender, args)) {
            Bukkit.getLogger().info("One or more inputs was invalid.");
            return false;
        }

        String arena = args[0];

        FileConfiguration arenaConfig = JoTHungerGames.getInstance().getArenaConfig();

        arenaConfig.set("arenas." + arena, null);
        JoTHungerGames.getInstance().saveArenaConfig();

        sender.sendMessage(ChatColor.GREEN + "Arena " + arena + " deleted.");

        return true; // Return true or false based on success
    }

    private boolean handleListArena(CommandSender sender, String[] args) {

        if (!validateListInputArguments(sender, args)) {
            Bukkit.getLogger().info("One or more inputs was invalid.");
            return false;
        }

        ConfigurationSection arenas = JoTHungerGames.getInstance().getArenaConfig().getConfigurationSection("arenas");

        if (arenas == null) {
            sender.sendMessage(ChatColor.RED + "No arenas found.");
            return false;
        }

        sender.sendMessage(ChatColor.GOLD + "List of Arenas:");

        FileConfiguration config = JoTHungerGames.getInstance().getArenaConfig();


        // Iterate through each arena
        for (String arenaKey : arenas.getKeys(false)) {


            World world = Bukkit.getWorld(Objects.requireNonNull(config.getString("arenas." + arenaKey + ".world")));
            String arenaType = config.getString("arenas." + arenaKey + ".type");
            int maxPlayers = config.getInt("arenas." + arenaKey + ".players.maximum");
            int arenaRadius = config.getInt("arenas." + arenaKey + ".radius");
            String arenaCenter = config.getString("arenas." + arenaKey + ".center");
            int arenaHeight = config.getInt("arenas." + arenaKey + ".height");
            String linkedGroup = config.getString("arenas." + arenaKey + ".linked segment group");
            TextComponent message = new TextComponent(arenaKey);
            message.setColor(net.md_5.bungee.api.ChatColor.YELLOW);

            // Create the hover text (description)
            TextComponent hoverText = new TextComponent("World: " + world + "\n" +
                    "Type: " + arenaType + "\n" +
                    "Center: " + arenaCenter + "\n" +
                    "Max Players: " + maxPlayers + "\n" +
                    "Radius: " + arenaRadius + "\n" +
                    "Height: " + arenaHeight + "\n" +
                    "Linked group: " + linkedGroup);
            hoverText.setColor(net.md_5.bungee.api.ChatColor.WHITE);

            // Add the hover event to the message
            message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{hoverText}));
            // Send the message to the sender
            sender.spigot().sendMessage(message);
        }

        return true; // Return true or false based on success
    }
    private boolean handleChestCheck(CommandSender sender) {
        // Must be a player
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command!");
            return false;
        }

        // Get the block the player is looking at (up to 5 blocks away)
        Block targetBlock = player.getTargetBlockExact(5);

        if (targetBlock == null || targetBlock.getType() != Material.CHEST) {
            player.sendMessage(ChatColor.RED + "You are not looking at a chest!");
            return false;
        }

        // Get chest location
        Location chestLoc = targetBlock.getLocation();
        String chestLocationKey = chestLoc.getBlockX() + "," +
                chestLoc.getBlockY() + "," +
                chestLoc.getBlockZ();

        // Search all arenas for this chest
        FileConfiguration config = JoTHungerGames.getInstance().getArenaConfig();
        ConfigurationSection arenas = config.getConfigurationSection("arenas");

        if (arenas == null) {
            player.sendMessage(ChatColor.RED + "No arenas found.");
            return false;
        }

        Set<String> arenaNames = arenas.getKeys(false);

        for (String arenaName : arenaNames) {
            // Check CENTER chests
            ConfigurationSection centerSection = config.getConfigurationSection("arenas." + arenaName + ".chests.center.locations");
            if (centerSection != null) {
                for (String key : centerSection.getKeys(false)) {
                    String storedLocation = centerSection.getString(key);
                    if (storedLocation != null && storedLocation.equals(chestLocationKey)) {
                        player.sendMessage(ChatColor.GOLD + "This is a CENTER chest for arena: " +
                                ChatColor.YELLOW + arenaName);
                        return true;
                    }
                }
            }

            // Check NORMAL chests
            ConfigurationSection normalSection = config.getConfigurationSection("arenas." + arenaName + ".chests.normal.locations");
            if (normalSection != null) {
                for (String key : normalSection.getKeys(false)) {
                    String storedLocation = normalSection.getString(key);
                    if (storedLocation != null && storedLocation.equals(chestLocationKey)) {
                        player.sendMessage(ChatColor.GOLD + "This is a NORMAL chest for arena: " +
                                ChatColor.YELLOW + arenaName);
                        return true;
                    }
                }
            }

            // Check PRIME chests
            ConfigurationSection primeSection = config.getConfigurationSection("arenas." + arenaName + ".chests.prime.locations");
            if (primeSection != null) {
                for (String key : primeSection.getKeys(false)) {
                    String storedLocation = primeSection.getString(key);
                    if (storedLocation != null && storedLocation.equals(chestLocationKey)) {
                        player.sendMessage(ChatColor.GOLD + "This is a PRIME chest for arena: " +
                                ChatColor.YELLOW + arenaName);
                        return true;
                    }
                }
            }
        }

        // Not found in any arena
        player.sendMessage(ChatColor.RED + "This chest is not registered to any arena");
        return false;
    }

    private boolean handleChestArena(CommandSender sender, String[] args){
        if (!validateChestInputArguments(sender, args)) {
            Bukkit.getLogger().info("One or more inputs was invalid.");
            return true;
        }

        JoTHungerGames plugin = JoTHungerGames.getInstance();
        String commandType = args[0];
        String chestType = args[1].split("_")[0];
        String chestTypeModifier = args[1];
        FileConfiguration config = JoTHungerGames.getInstance().getArenaConfig();
        ConfigurationSection arenas = JoTHungerGames.getInstance().getArenaConfig().getConfigurationSection("arenas");
        String arenaName = null;
        World world = null;
        Location chestLocation = null;
        int x = 0;
        int y= 0;
        int z= 0;


        //if argument count is 2, checks that player is in a valid arena
        if (args.length == 2) {
            if (!(sender instanceof Player player)) {
                if (args[0].equals("add") || args[0].equals("remove")) {
                    sender.sendMessage(ChatColor.RED + "Only players inside an arena can use this command without specifying an arena and coordinates!");
                } else if (args[0].equals("refill") || args[0].equals("list")) {
                    sender.sendMessage(ChatColor.RED + "Only players inside an arena can use this command without specifying an arena!");
                }
                return false;
            }
            chestLocation = player.getLocation();
            x = chestLocation.getBlockX();
            y = chestLocation.getBlockY();
            z = chestLocation.getBlockZ();
            world = chestLocation.getWorld();
            chestLocation = new Location(world, x, y, z);

            // Iterate through each arena
            boolean arenaValidate = false;
            for (String arenaKey : arenas.getKeys(false)) {
                try {

                    ArenaChecker arenaChecker = new ArenaChecker(arenaKey);
                    Bukkit.getLogger().info("Testing for arena " + arenaKey);
                    if (arenaChecker.isInsideArena(chestLocation)) {
                        Bukkit.getLogger().info("Chest location is inside arena " + arenaKey);
                        arenaName = arenaKey;
                        arenaValidate = true;
                        break;
                    } else {
                        Bukkit.getLogger().warning("Chest location is NOT inside arena " + arenaKey);
                    }
                } catch (IllegalArgumentException e) {
                    // Skip arenas with invalid/incomplete configuration
                    Bukkit.getLogger().warning("Skipping arena '" + arenaKey + "' - invalid/incomplete config: " + e.getMessage());
                    continue;
                }

            }
            if (!arenaValidate) {
                if (args[0].equals("add") || args[0].equals("remove")) {
                    sender.sendMessage(ChatColor.RED + "You must be inside an arena to use this command without specifying an arena and coordinates!");
                } else if (args[0].equals("refill") || args[0].equals("list")) {
                    sender.sendMessage(ChatColor.RED + "You must be inside an arena to use this command without specifying an arena!");
                }
                return false;
            }

        }else if (args.length == 3) {
            arenaName = args[2];
            world = Bukkit.getWorld(arenas.getString(arenaName + ".world"));

            if (commandType.equals("add") || commandType.equals("remove")) {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(ChatColor.RED + "Only players can use this command without coordinates!");
                    return false;
                }

                // Get player's current location
                Location playerLoc = player.getLocation();
                x = playerLoc.getBlockX();
                y = playerLoc.getBlockY();
                z = playerLoc.getBlockZ();
                chestLocation = new Location(world, x, y, z);
            }
        }else if (args.length == 6) {
            arenaName = args[2];
            world = Bukkit.getWorld(arenas.getString(arenaName + ".world"));
            x = Integer.parseInt(args[3]);
            y = Integer.parseInt(args[4]);
            z = Integer.parseInt(args[5]);
            chestLocation = new Location(world, x, y, z);
        }
        ChestUtilities chestUtilities = new ChestUtilities();

        ConfigurationSection chestLocationSection = config.getConfigurationSection("arenas." + arenaName + ".chests." + chestType + ".locations");
        String chestLocationKey = x + "," + y + "," + z;
        Bukkit.getLogger().info("Generated chest location key: " + chestLocationKey);

        switch (commandType) {
            case "add" -> {
                ConfigurationSection centerLocationSection = config.getConfigurationSection("arenas." + arenaName + ".chests." + "center" + ".locations");
                ConfigurationSection normalLocationSection = config.getConfigurationSection("arenas." + arenaName + ".chests." + "normal" + ".locations");
                ConfigurationSection primeLocationSection = config.getConfigurationSection("arenas." + arenaName + ".chests." + "prime" + ".locations");

                if (chestLocation != null) {


                    // *** FIX: Check VALUES not keys ***
                    boolean alreadyExists = false;

                    // Check center chests
                    if (centerLocationSection != null) {
                        for (String key : centerLocationSection.getKeys(false)) {
                            if (centerLocationSection.getString(key).equals(chestLocationKey)) {
                                alreadyExists = true;
                                break;
                            }
                        }
                    }

                    // Check normal chests
                    if (!alreadyExists && normalLocationSection != null) {
                        for (String key : normalLocationSection.getKeys(false)) {
                            if (normalLocationSection.getString(key).equals(chestLocationKey)) {
                                alreadyExists = true;
                                break;
                            }
                        }
                    }

                    // Check prime chests
                    if (!alreadyExists && primeLocationSection != null) {
                        for (String key : primeLocationSection.getKeys(false)) {
                            if (primeLocationSection.getString(key).equals(chestLocationKey)) {
                                alreadyExists = true;
                                break;
                            }
                        }
                    }

                    if (alreadyExists) {
                        sender.sendMessage(ChatColor.RED + "A chest already exists at this location!");
                        return false;
                    }

                    plugin.saveChestToConfig(arenaName, chestLocation, chestType);
                    plugin.saveArenaConfig();
                    sender.sendMessage(ChatColor.YELLOW + chestType.toUpperCase() + ChatColor.GREEN + " chest successfully logged at [" + ChatColor.YELLOW
                            + chestLocation.getX() + ChatColor.WHITE + "," + ChatColor.YELLOW + chestLocation.getY() + ChatColor.WHITE + "," + ChatColor.YELLOW
                            + chestLocation.getZ() + ChatColor.GREEN + "] in arena " + ChatColor.YELLOW + arenaName);
                }
            }
            case "remove" -> {
                // Check if the chest location exists
                boolean keyExists = false;
                String matchingKey = null;
                assert chestLocationSection != null;
                for (String key : chestLocationSection.getKeys(false)) {
                    if (chestLocationSection.getString(key).equals(chestLocationKey)) {
                        keyExists = true;
                        matchingKey = key;
                        break;
                    }
                }
                if (!keyExists) {
                    sender.sendMessage(ChatColor.RED + "Chest does not exist!");
                    Bukkit.getLogger().info("Available keys: " + chestLocationSection.getKeys(false).toString());
                    return false;
                } else {
                    plugin.removeChestFromConfig(arenaName, chestLocation, chestType);
                    plugin.saveArenaConfig();
                    // Reindex the configuration section after removing the chest
                    reindexConfigSection(chestLocationSection);
                    plugin.saveArenaConfig();

                    sender.sendMessage(ChatColor.YELLOW + chestType.toUpperCase() + ChatColor.GREEN + " chest successfully" + ChatColor.RED + " removed"
                            + ChatColor.GREEN + " at [" + ChatColor.YELLOW + x + ChatColor.WHITE + "," + ChatColor.YELLOW + y
                            + ChatColor.WHITE + "," + ChatColor.YELLOW + z + ChatColor.GREEN + "] in arena " + ChatColor.YELLOW + arenaName);
                }
            }
            case "refill" -> {
                String basePath = "arenas." + arenaName;
                ConfigurationSection section = config.getConfigurationSection(basePath);
                if (section == null) {
                    sender.sendMessage(ChatColor.RED + "Arena or chest type configuration section not found.");
                    return false;
                }
                Set<Location> chestLocationsSet = chestUtilities.getChestLocations(arenaName, chestType);
                if (chestLocationsSet == null) {
                    sender.sendMessage(ChatColor.RED + "No chest locations found for this arena and chest type.");
                    return false;
                }
                chestUtilities.refillChestsByType(arenaName, chestTypeModifier);
            }
            case "list" -> {
                Set<Location> chestLocationsSet = chestUtilities.getChestLocations(arenaName, chestType);

                sender.sendMessage(ChatColor.GOLD + chestType.toUpperCase() + " chest locations:");
                for (Location location : chestLocationsSet) {
                    String locationString = location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ();
                    TextComponent message = new TextComponent(locationString);
                    message.setColor(net.md_5.bungee.api.ChatColor.YELLOW);

                    // Create the click event for teleportation
                    message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                            "/tp " + sender.getName() + " " + location.getBlockX() + " " + location.getBlockY() + " " + location.getBlockZ()));

                    // Add hover text for the message
                    message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{new TextComponent("Click to teleport to this location")}));

                    sender.spigot().sendMessage(message);
                }
            }
        }

        return true;
    }
    public void reindexConfigSection(ConfigurationSection section) {
        if (section == null) return;

        Map<String, Object> items = section.getValues(false);
        int index = 1;
        for (String key : items.keySet()) {
            Object value = items.get(key);
            section.set(key, null);  // Remove the old key
            section.set(String.valueOf(index), value);  // Set the new indexed key
            index++;
        }
    }

    //VALIDATION CODE
    private boolean validateStartInputArguments(CommandSender sender, String[] args) {
        // Validation logic here, returning false and sending error messages as required

        //sets the CorrectUsageExample string to be copied into all error messages for the command.
        String CorrectUsageExample = ChatColor.RED + "Correct usage: " + ChatColor.GOLD + "/hgarena start"
                + ChatColor.YELLOW + " <" + ChatColor.GOLD + "arena name" + ChatColor.YELLOW + ">"
                + ChatColor.YELLOW + " [" + ChatColor.GOLD + "number of players" + ChatColor.YELLOW + "]";

        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "You must specify an arena name!");
            sender.sendMessage(CorrectUsageExample);
            return false;
        }
        else if (args.length > 2) {
            sender.sendMessage(ChatColor.RED + "You have submitted too many arguments!");
            sender.sendMessage(CorrectUsageExample);
            return false;
        } else {
            Bukkit.getLogger().info("Argument count valid.");
        }
        //checks to see if the first arg is a valid arena
        if (!(JoTHungerGames.getInstance().arenaExists(args[0]))) {
            sender.sendMessage(ChatColor.RED + args[0] + " is not a valid arena.");
            sender.sendMessage(CorrectUsageExample);
            return false;
        } else {
            Bukkit.getLogger().info("Arena valid.");
        }
        //checks to see if the second arg is a valid integer
        if (args.length == 2){
            if (!NumberUtils.isParsable(args[1])) {
                sender.sendMessage(ChatColor.RED + args[1] + " is not a valid positive integer within the set bounds.");
                sender.sendMessage(CorrectUsageExample);
                return false;
            } else if (Integer.parseInt(args[1]) < 1) {
                sender.sendMessage(ChatColor.RED + "You cannot start a game with fewer than 1 player!");
                sender.sendMessage(CorrectUsageExample);
                return false;

            } else if (Integer.parseInt(args[1]) > JoTHungerGames.getInstance().getArenaConfig().getInt("arenas." + args[0] + ".players.maximum")) {
                sender.sendMessage(ChatColor.RED + args[1] + " is higher than the maximum number of players set for this arena!");
                sender.sendMessage(CorrectUsageExample);
                return false;
            } else {
                Bukkit.getLogger().info("Player count valid.");
            }
        }
        // Add other validation checks...

        return true; // If all validations pass
    }

    private boolean validateStopInputArguments(CommandSender sender, String[] args) {
        // Validation logic here, returning false and sending error messages as required

        //sets the CorrectUsageExample string to be copied into all error messages for the command.
        String CorrectUsageExample = ChatColor.RED + "Correct usage: " + ChatColor.GOLD + "/hgarena stop"
                + ChatColor.YELLOW + " <" + ChatColor.GOLD + "arena name" + ChatColor.YELLOW + ">";

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
        //checks to see if the first arg is a valid arena
        if (!(JoTHungerGames.getInstance().arenaExists(args[0]))) {
            sender.sendMessage(ChatColor.RED + args[0] + " is not a valid arena.");
            sender.sendMessage(CorrectUsageExample);
            return false;
        } else {
            Bukkit.getLogger().info("Arena valid.");
        }
        // Add other validation checks...

        return true; // If all validations pass
    }

    private boolean validateCreateInputArguments(CommandSender sender, String[] args) {
        // Validation logic here, returning false and sending error messages as required

        //sets the CorrectUsageExample string to be copied into all error messages for the command.
        String CorrectUsageExample = ChatColor.RED + "Correct usage: " + ChatColor.GOLD + "/hgarena create "
                + ChatColor.YELLOW + " <" + ChatColor.GOLD + "arena name" + ChatColor.YELLOW + ">"
                + ChatColor.YELLOW + " <" + ChatColor.GOLD + "arena type" + ChatColor.YELLOW + ">"
                + ChatColor.YELLOW + " <" + ChatColor.GOLD + "radius" + ChatColor.YELLOW + ">"
                + ChatColor.YELLOW + " <" + ChatColor.GOLD + "height" + ChatColor.YELLOW + ">"
                + ChatColor.YELLOW + " <" + ChatColor.GOLD + "maximum number of players" + ChatColor.YELLOW + ">";

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command!");
            return false;
        }
        if (args.length < 5) {
            sender.sendMessage(ChatColor.RED + "You are missing arguments!");
            sender.sendMessage(CorrectUsageExample);
            return false;
        } else if (args.length > 5) {
            sender.sendMessage(ChatColor.RED + "You have submitted too many arguments!");
            sender.sendMessage(CorrectUsageExample);
        } else {
            Bukkit.getLogger().info("Argument count valid.");
        }
        //checks to see if the first arg is a valid arena
        if ((JoTHungerGames.getInstance().arenaExists(args[0]))) {
            sender.sendMessage(ChatColor.RED + "An arena with the name " + args[0] + " already exists!");
            sender.sendMessage(CorrectUsageExample);
            return false;
        }
        if (!(args[1].equalsIgnoreCase("circle") || args[1].equalsIgnoreCase("square"))) {
            sender.sendMessage(ChatColor.RED + "Accepted arena types are 'circle' and 'square.'");
            sender.sendMessage(CorrectUsageExample);
            return false;
        }
        if (!NumberUtils.isParsable(args[2]) || Integer.parseInt(args[2]) < 0) {
            sender.sendMessage(ChatColor.RED + "Radius must be a positive integer.");
            sender.sendMessage(CorrectUsageExample);
            return false;
        }
        if (!NumberUtils.isParsable(args[3]) || Integer.parseInt(args[3]) < 1) {
            sender.sendMessage(ChatColor.RED + "Height must be a positive integer.");
            sender.sendMessage(CorrectUsageExample);
            return false;
        }
        if (args.length == 5) {
            if (!NumberUtils.isParsable(args[4]) || Integer.parseInt(args[4]) < 1) {
                sender.sendMessage(ChatColor.RED + "Maximum number of players must be an integer 1 or greater.");
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
        String CorrectUsageExample = ChatColor.RED + "Correct usage: " + ChatColor.GOLD + "/hgarena delete"
                + ChatColor.YELLOW + " <" + ChatColor.GOLD + "arena name" + ChatColor.YELLOW + ">";

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
        //checks to see if the first arg is a valid arena
        if (!(JoTHungerGames.getInstance().arenaExists(args[0]))) {
            sender.sendMessage(ChatColor.RED + args[0] + " is not a valid arena.");
            sender.sendMessage(CorrectUsageExample);
            return false;
        } else {
            Bukkit.getLogger().info("Arena valid.");
        }
        // Add other validation checks...

        return true; // If all validations pass
    }

    private boolean validateListInputArguments(CommandSender sender, String[] args) {
        // Validation logic here, returning false and sending error messages as required

        //sets the CorrectUsageExample string to be copied into all error messages for the command.
        String CorrectUsageExample = ChatColor.RED + "Correct usage: " + ChatColor.GOLD + "/hgarena list";

        if (args.length > 0) {
            sender.sendMessage(ChatColor.RED + "You have submitted too many arguments!");
            sender.sendMessage(CorrectUsageExample);
            return false;
        } else {
            Bukkit.getLogger().info("Argument count valid.");
        }
        // Add other validation checks...

        return true; // If all validations pass
    }

    private boolean validateChestInputArguments(CommandSender sender, String[] args) {
        //sets the CorrectUsageExample string to be copied into all error messages for the command.
        String CorrectUsageExample = ChatColor.RED + "Correct usage: " + ChatColor.GOLD + "/hgarena chest "
                + ChatColor.YELLOW + "<" + ChatColor.GOLD + "add" + ChatColor.YELLOW + "|" + ChatColor.GOLD
                + "remove" + ChatColor.YELLOW + "|" + ChatColor.GOLD + "refill" + ChatColor.YELLOW + "|"
                + ChatColor.GOLD + "list" + "> " + ChatColor.YELLOW + "<" + ChatColor.GOLD + "center"
                + ChatColor.YELLOW + "|" + ChatColor.GOLD + "normal" + ChatColor.YELLOW + "|"
                + ChatColor.GOLD + "prime" + ChatColor.YELLOW + "> [" + ChatColor.GOLD + "arena name"
                + ChatColor.YELLOW + "] [" + ChatColor.GOLD + "x y z" + ChatColor.YELLOW + "]";

        ConfigurationSection arenas = JoTHungerGames.getInstance().getArenaConfig().getConfigurationSection("arenas");
        if (arenas == null) {
            sender.sendMessage(ChatColor.RED + "No arenas found. Please create an arena before interacting with chests");
            return false;
        }

        //Check if arg count is an outlier
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "You are missing an argument!");
            sender.sendMessage(CorrectUsageExample);
            return false;
        } else if (args.length > 6) {
            sender.sendMessage(ChatColor.RED + "You have submitted too many arguments!");
            sender.sendMessage(CorrectUsageExample);
            return false;
        }
        //checks to see if the first arg is a command
        String commandType = args[0].toLowerCase();
        if (!commandType.equalsIgnoreCase("add") && !commandType.equalsIgnoreCase("remove")
                && !commandType.equalsIgnoreCase("refill") && !commandType.equalsIgnoreCase("list")) {
            sender.sendMessage(ChatColor.RED + args[0] + " is not a valid command.");
            sender.sendMessage(CorrectUsageExample);
            return false;
        } else {
            Bukkit.getLogger().info("Command type valid.");
        }
        //checks to see if the second arg is a chest type
        String chestType = args[1].toLowerCase();
        if (commandType.equals("refill")) {
            if (!chestType.equals("normal") && !chestType.equals("center") && !chestType.equals("prime")
                    && !chestType.equals("normal_refill") && !chestType.equals("center_refill") && !chestType.equals("prime_refill")) {
                sender.sendMessage(ChatColor.RED + args[1] + " is not a valid chest type.");
                sender.sendMessage(CorrectUsageExample);
                return false;
            } else {
                Bukkit.getLogger().info("Chest type valid.");
            }
        }else {
            if (!chestType.equals("normal") && !chestType.equals("center") && !chestType.equals("prime")) {
                sender.sendMessage(ChatColor.RED + args[1] + " is not a valid chest type.");
                sender.sendMessage(CorrectUsageExample);
                return false;
            } else {
                Bukkit.getLogger().info("Chest type valid.");
            }
        }

        // Validate arena name and coordinates
        if (args.length >= 3) {
            String arenaName = args[2];
            if (!JoTHungerGames.getInstance().arenaExists(arenaName)) {
                sender.sendMessage(ChatColor.RED + arenaName + " is not a valid arena.");
                sender.sendMessage(CorrectUsageExample);
                return false;
            }

            if (args.length == 6) {
                for (int i = 3; i < 6; i++) {
                    if (!NumberUtils.isParsable(args[i])) {
                        sender.sendMessage(ChatColor.RED + "Coordinate value must be a valid number.");
                        sender.sendMessage(CorrectUsageExample);
                        return false;
                    }
                }
            }
        }

        String commandSwitch = null;
        if (args[0].equals("add") || args[0].equals("remove")) {
            commandSwitch = "add/remove";
        }else if (args[0].equals("refill") || args[0].equals("list")) {
            commandSwitch = "refill/list";
        }
        switch (commandSwitch) {

            case "add/remove" : {
                //check for incomplete coordinate section
                if (args.length > 3 && args.length < 6) {
                    sender.sendMessage(ChatColor.RED + "You are missing an argument!");
                    sender.sendMessage(CorrectUsageExample);
                    return false;
                }
                Bukkit.getLogger().info("Argument count valid.");
                //check for sender being Player if only 2 args
                if (args.length == 2) {
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(ChatColor.RED + "Only players inside an arena can use this command without specifying an arena and coordinates!");
                        return false;
                    }
                }else if (args.length == 3) {
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(ChatColor.RED + "Only players inside an arena can use this command without specifying an arena and coordinates!");
                        return false;
                    }
                }
                break;
            }
            case "refill/list" : {
                if (args.length > 3) {
                    sender.sendMessage(ChatColor.RED + "You have submitted too many arguments!");
                    sender.sendMessage(CorrectUsageExample);
                    return false;
                }
                Bukkit.getLogger().info("Argument count valid.");
                //check for sender being Player if only 2 args
                if (args.length == 2) {
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(ChatColor.RED + "Only players inside an arena can use this command without specifying an arena!");
                        return false;
                    }
                }
                break;
            }
        }
        // Add other validation checks...

        return true; // If all validations pass
    }
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> completions = new ArrayList<>();

        // First argument: Subcommands
        if (args.length == 1) {
            List<String> subCommands = Arrays.asList(START_SUBCOMMAND, STOP_SUBCOMMAND, CREATE_SUBCOMMAND, DELETE_SUBCOMMAND, LIST_SUBCOMMAND, CHEST_SUBCOMMAND);
            for (String subCommand : subCommands) {
                if (subCommand.startsWith(args[0].toLowerCase())) {
                    completions.add(subCommand);
                }
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase(CHEST_SUBCOMMAND)) {
                List<String> subSubCommands = Arrays.asList("add", "remove", "refill", "list", "check");
                for (String subSubCommand : subSubCommands) {
                    if (subSubCommand.toLowerCase().startsWith(args[1].toLowerCase())) {
                        completions.add(subSubCommand);
                    }
                }
            }else if (!args[0].equalsIgnoreCase(LIST_SUBCOMMAND) && !args[0].equalsIgnoreCase(CREATE_SUBCOMMAND)) {
                for (String arena : JoTHungerGames.getInstance().getAllArenas()) {
                    if (arena.toLowerCase().contains(args[1].toLowerCase())) { // Use contains for a "retroactive" search
                        completions.add(arena);
                    }
                }
            }
        } else if (args.length == 3 && args[0].equals(CHEST_SUBCOMMAND)) {
            List<String> chestTypes;
            if (args[1].equals("refill")) {
                chestTypes = Arrays.asList("normal", "center", "prime", "normal_refill", "center_refill", "prime_refill");
            }else {
                chestTypes = Arrays.asList("normal", "center", "prime");
            }
            for (String chestType : chestTypes) {
                if (chestType.toLowerCase().contains(args[2].toLowerCase())) {
                    completions.add(chestType);
                }
            }
        } else if (args.length == 4 && args[0].equalsIgnoreCase(CHEST_SUBCOMMAND)) {
            for (String arena : JoTHungerGames.getInstance().getAllArenas()) {
                if (arena.toLowerCase().contains(args[3].toLowerCase())) { // Use contains for a "retroactive" search
                    completions.add(arena);
                }
            }
        }
        // Add more conditional blocks for further arguments if necessary

        return completions;
    }

    public BufferedImage fetchPlayerSkin(String username) throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        // Fetch player UUID
        try {
            HttpRequest uuidRequest = HttpRequest.newBuilder()
                    .uri(new URI("https://api.mojang.com/users/profiles/minecraft/" + username))
                    .build();
            HttpResponse<String> uuidResponse = client.send(uuidRequest, HttpResponse.BodyHandlers.ofString());

            if (uuidResponse.statusCode() != 200) {
                throw new Exception("Failed to fetch UUID: HTTP status " + uuidResponse.statusCode());
            }

            JSONObject uuidJson = new JSONObject(uuidResponse.body());
            String uuid = uuidJson.getString("id");

            // Fetch player skin URL
            HttpRequest skinRequest = HttpRequest.newBuilder()
                    .uri(new URI("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid))
                    .build();
            HttpResponse<String> skinResponse = client.send(skinRequest, HttpResponse.BodyHandlers.ofString());

            if (skinResponse.statusCode() != 200) {
                throw new Exception("Failed to fetch skin data: HTTP status " + skinResponse.statusCode());
            }

            JSONObject skinJson = new JSONObject(skinResponse.body());
            String skinBase64 = skinJson.getJSONArray("properties").getJSONObject(0).getString("value");
            String skinDecoded = new String(Base64.getDecoder().decode(skinBase64));

            JSONObject skinData = new JSONObject(skinDecoded);
            String skinUrl = skinData.getJSONObject("textures").getJSONObject("SKIN").getString("url");

            // Fetch the skin image
            HttpRequest imageRequest = HttpRequest.newBuilder()
                    .uri(new URI(skinUrl))
                    .build();
            HttpResponse<byte[]> imageResponse = client.send(imageRequest, HttpResponse.BodyHandlers.ofByteArray());

            if (imageResponse.statusCode() != 200) {
                throw new Exception("Failed to fetch skin image: HTTP status " + imageResponse.statusCode());
            }

            ByteArrayInputStream bis = new ByteArrayInputStream(imageResponse.body());

            return ImageIO.read(bis);
        } catch (Exception e) {
            System.err.println("Error fetching player skin for username: " + username);
            e.printStackTrace();
            throw e;
        }
    }

    public String processSkin(BufferedImage skinImage) {

        //define shades of aqua
        Color baseColor = new Color(0x55FFFF);
        Color blackColor = new Color(0x000000);
        Color whiteColor = new Color(0xFFFFFF);

        Color[] aquaShades = new Color[10];
        for (int i = 0; i < 5; i++) {
            float ratio = (float) i / 4;  // Ratio ranges from 0.0 to 1.0 for blending
            aquaShades[4 - i] = new Color(
                    (int) (baseColor.getRed() * (1 - ratio) + blackColor.getRed() * ratio),
                    (int) (baseColor.getGreen() * (1 - ratio) + blackColor.getGreen() * ratio),
                    (int) (baseColor.getBlue() * (1 - ratio) + blackColor.getBlue() * ratio)
            );
            aquaShades[5 + i] = new Color(
                    (int) (baseColor.getRed() * (1 - ratio) + whiteColor.getRed() * ratio),
                    (int) (baseColor.getGreen() * (1 - ratio) + whiteColor.getGreen() * ratio),
                    (int) (baseColor.getBlue() * (1 - ratio) + whiteColor.getBlue() * ratio)
            );
        }



        // Extract the 8x8 portion of the head
        BufferedImage head = skinImage.getSubimage(8, 8, 8, 8);

        // Scale the head to 32x32
        BufferedImage scaledHead = new BufferedImage(29, 29, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = scaledHead.createGraphics();
        g2d.drawImage(head.getScaledInstance(29, 29, Image.SCALE_DEFAULT), 0, 0, null);
        g2d.dispose();

        // Convert to grayscale and map to aqua shades
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("[");

        for (int y = 0; y < 29; y++) {
            for (int x = 0; x < 29; x++) {
                Color color = new Color(scaledHead.getRGB(x, y), true);
                int gray = (color.getRed() + color.getGreen() + color.getBlue()) / 3;
                int shadeIndex = Math.min(gray / 26, 9);  // Map grayscale to 10 shades and ensure index is within bounds
                Color aquaColor = aquaShades[shadeIndex];
                String hexColor = String.format("#%02x%02x%02x", aquaColor.getRed(), aquaColor.getGreen(), aquaColor.getBlue());
                jsonBuilder.append("{\"text\":\"█\",\"color\":\"").append(hexColor).append("\"}");
                if (x < 28) {
                    jsonBuilder.append(",");
                }
            }
            if (y < 28) {
                jsonBuilder.append(",{\"text\":\"\n\"},");
            }
        }

        jsonBuilder.append("]");
        return jsonBuilder.toString();
    }
}