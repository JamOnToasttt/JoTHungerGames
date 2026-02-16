package me.jamontoast.jothungergames;

import me.jamontoast.jothungergames.Commands.*;
import me.jamontoast.jothungergames.Effects.*;
import me.jamontoast.jothungergames.Enums.SegmentEffects;
import me.jamontoast.jothungergames.Utilities.SegmentEffectManager;
import me.jamontoast.jothungergames.Utilities.SegmentUtils;
import me.jamontoast.jothungergames.Utilities.SpectatorMenu;
import org.bukkit.*;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.loot.LootTable;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;

public final class JoTHungerGames extends JavaPlugin implements Listener{
    private static JoTHungerGames instance;

    private SegmentEffectManager segmentEffectManager;
    private File segmentConfigFile;
    private FileConfiguration segmentConfig;
    private File arenaConfigFile;
    private FileConfiguration arenaConfig;
    private Scoreboard mainScoreboard;
    private Objective killsObjective;
    private Objective winsObjective;
    private ArenaCommand arenaCommand;
    private String resourcePackBase64;

    private LootTable centerLootTable;
    private LootTable centerRefillLootTable;
    private LootTable normalLootTable;
    private LootTable normalRefillLootTable;
    private LootTable primeLootTable;
    private LootTable primeRefillLootTable;
    @Override
    public void onEnable() {
        // Plugin startup logic
        setupPlugin();
        setupCommands();
        registerEffects();
        loadActiveEffects();

        setupScoreboard();
        // Register the main class as a listener
        getServer().getPluginManager().registerEvents(this, this);
    }
    @Override
    public void onDisable() {
        // Plugin shutdown logic
        disableEffectsForShutdown();
        Bukkit.getLogger().info("Shutting Down");

    }

    private void setupPlugin() {
        instance = this;
        saveDefaultConfig(); // It copies the default `config.yml` from your JAR to the plugin folder if it doesn't exist.
        saveDefaultSegments();
        reloadSegmentConfig();
        saveDefaultArenas();
        reloadArenaConfig();

        // Verify resources in JAR
        verifyResourceInJar("hg_datapack");

        setupLootTables();
        extractDataPack();


        // Load loot tables after reload
        Bukkit.getScheduler().runTaskLater(this, () -> {
            getLogger().info("Loading loot tables...");
            loadLootTables();
        }, 20L); // 1 second after reload

        segmentEffectManager = new SegmentEffectManager();
        arenaCommand = new ArenaCommand();
        new SpectatorMenu();
    }

    private void verifyResourceInJar(String resourcePath) {
        try (JarFile jarFile = new JarFile(getFile())) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().startsWith(resourcePath + "/")) {
                    //getLogger().info("Found resource in JAR: " + entry.getName());
                }
            }
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Failed to access JAR file: " + e.getMessage(), e);
        }
    }

    private void setupLootTables() {
        File lootTablesFolder = new File(getDataFolder(), "loot_tables");

        // Create folder if it doesn't exist
        if (!lootTablesFolder.exists()) {
            lootTablesFolder.mkdirs();
            getLogger().info("Created loot_tables folder");
        }

        String[] lootTableFiles = {
                "center.json",
                "center_refill.json",
                "normal.json",
                "normal_refill.json",
                "prime.json",
                "prime_refill.json"
        };

        // ✅ Copy any missing files FROM hg_datapack in JAR TO plugin folder
        for (String fileName : lootTableFiles) {
            File lootTableFile = new File(lootTablesFolder, fileName);

            if (!lootTableFile.exists()) {
                try {
                    String resourcePath = "hg_datapack/data/hg_loot/loot_table/" + fileName;
                    InputStream in = getResource(resourcePath);

                    if (in != null) {
                        copyResource(in, lootTableFile);
                        getLogger().info("Created default loot table: " + fileName);
                    } else {
                        getLogger().warning("Could not find " + resourcePath + " in JAR");
                    }
                } catch (IOException e) {
                    getLogger().severe("Failed to create loot table " + fileName + ": " + e.getMessage());
                }
            }
        }
    }

    private void extractDataPack() {
        for (World world : Bukkit.getWorlds()) {
            File datapackFolder = new File(world.getWorldFolder(), "datapacks/hg_datapack");

            // Delete and recreate datapack
            if (datapackFolder.exists()) {
                if (!deleteDirectory(datapackFolder)) {
                    getLogger().warning("Failed to delete existing datapack folder");
                    continue;
                }
            }

            if (datapackFolder.mkdirs()) {
                copyResourceDirectory("hg_datapack", datapackFolder);

                // ✅ Copy loot tables FROM plugin folder TO datapack
                File datapackLootFolder = new File(datapackFolder, "data/hg_loot/loot_table");
                File pluginLootFolder = new File(getDataFolder(), "loot_tables");

                String[] lootTableFiles = {
                        "center.json",
                        "center_refill.json",
                        "normal.json",
                        "normal_refill.json",
                        "prime.json",
                        "prime_refill.json"
                };

                for (String fileName : lootTableFiles) {
                    File source = new File(pluginLootFolder, fileName);
                    File target = new File(datapackLootFolder, fileName);

                    if (source.exists()) {
                        try {
                            java.nio.file.Files.copy(
                                    source.toPath(),
                                    target.toPath(),
                                    java.nio.file.StandardCopyOption.REPLACE_EXISTING
                            );
                        } catch (IOException e) {
                            getLogger().warning("Failed to copy " + fileName + " to datapack: " + e.getMessage());
                        }
                    }
                }
            }
        }
    }
    // Deletes a directory and its contents recursively
    private boolean deleteDirectory(File directory) {
        if (!directory.exists()) {
            return true; // Directory does not exist, nothing to delete
        }
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    if (!deleteDirectory(file)) {
                        return false; // Failed to delete subdirectory
                    }
                } else if (!file.delete()) {
                    getLogger().warning("Failed to delete file: " + file.getAbsolutePath());
                    return false;
                }
            }
        }
        return directory.delete(); // Delete the now-empty directory itself
    }

    // Copies a resource directory from the plugin JAR to the specified target directory
    private void copyResourceDirectory(String resourcePath, File targetDir) {
        //getLogger().info("Copying resource directory: " + resourcePath + " to " + targetDir.getAbsolutePath());
        try (JarFile jarFile = new JarFile(getFile())) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().startsWith(resourcePath + "/")) {
                    String entryName = entry.getName().substring(resourcePath.length() + 1);
                    File targetFile = new File(targetDir, entryName);
                    //getLogger().info("Processing entry: " + entry.getName());
                    if (entry.isDirectory()) {
                        if (!targetFile.exists()) {
                            targetFile.mkdirs();
                            //getLogger().info("Created directory: " + targetFile.getAbsolutePath());
                        }
                    } else {
                        if (!targetFile.getParentFile().exists()) {
                            targetFile.getParentFile().mkdirs();
                            //getLogger().info("Created parent directory: " + targetFile.getParentFile().getAbsolutePath());
                        }
                        try (InputStream in = jarFile.getInputStream(entry)) {
                            copyResource(in, targetFile);
                            //getLogger().info("Copied file: " + targetFile.getAbsolutePath());
                        }
                    }
                }
            }
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Failed to copy resource directory: " + e.getMessage(), e);
        }
    }

    private void copyResource(InputStream in, File outFile) throws IOException {
        try (FileOutputStream out = new FileOutputStream(outFile)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
        }
    }

    private void loadLootTables() {
        try {
            NamespacedKey centerKey = new NamespacedKey("hg_loot", "center");
            getLogger().info("Loading loot table with key: " + centerKey);
            centerLootTable = Bukkit.getLootTable(centerKey);
            if (centerLootTable == null) {
                getLogger().severe("Failed to load centerLootTable.");
            }

            NamespacedKey centerRefillKey = new NamespacedKey("hg_loot", "center_refill");
            getLogger().info("Loading loot table with key: " + centerRefillKey);
            centerRefillLootTable = Bukkit.getLootTable(centerRefillKey);
            if (centerRefillLootTable == null) {
                getLogger().severe("Failed to load centerRefillLootTable.");
            }

            NamespacedKey normalKey = new NamespacedKey("hg_loot", "normal");
            getLogger().info("Loading loot table with key: " + normalKey);
            normalLootTable = Bukkit.getLootTable(normalKey);
            if (normalLootTable == null) {
                getLogger().severe("Failed to load normalLootTable.");
            }

            NamespacedKey normalRefillKey = new NamespacedKey("hg_loot", "normal_refill");
            getLogger().info("Loading loot table with key: " + normalRefillKey);
            normalRefillLootTable = Bukkit.getLootTable(normalRefillKey);
            if (normalRefillLootTable == null) {
                getLogger().severe("Failed to load normalRefillLootTable.");
            }

            NamespacedKey primeKey = new NamespacedKey("hg_loot", "prime");
            getLogger().info("Loading loot table with key: " + primeKey);
            primeLootTable = Bukkit.getLootTable(primeKey);
            if (primeLootTable == null) {
                getLogger().severe("Failed to load primeLootTable.");
            }

            NamespacedKey primeRefillKey = new NamespacedKey("hg_loot", "prime_refill");
            getLogger().info("Loading loot table with key: " + primeRefillKey);
            primeRefillLootTable = Bukkit.getLootTable(primeRefillKey);
            if (primeRefillLootTable == null) {
                getLogger().severe("Failed to load primeRefillLootTable.");
            }
        }catch (Exception e){
                getLogger().severe("Error loading loot tables: " + e.getMessage());
                e.printStackTrace();
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        // URL of the resource pack
        String resourcePackURL = "https://github.com/JamOnToasttt/HGMusicPack/raw/main/JamOnToast-Hunger-Games.zip";
        //String sha1Hash = "1fbe4b72dcdcc22df4d6a00f5376c5d2564b9f11";
        String sha1Hash = "7814b8d983efb0e6466406518f84e20d7abb8d55";

        // Log player info
        Bukkit.getLogger().info("Sending resource pack to player: " + player.getName());

        // Send the resource pack URL to the player
        try {
            player.setResourcePack(resourcePackURL, hexStringToByteArray(sha1Hash));
        } catch (Exception e) {
            Bukkit.getLogger().severe("Failed to set resource pack: " + e.getMessage());
            e.printStackTrace();
        }
    }
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public LootTable getCenterLootTable() {
        return centerLootTable;
    }

    public LootTable getCenterRefillLootTable() {return centerRefillLootTable;}

    public LootTable getNormalLootTable() {
        return normalLootTable;
    }
    public LootTable getNormalRefillLootTable() {return normalRefillLootTable;}

    public LootTable getPrimeLootTable() {return primeLootTable;}

    public LootTable getPrimeRefillLootTable() {
        return primeRefillLootTable;
    }

    private void setupCommands() {
        Objects.requireNonNull(getCommand("hgsegmentGroup")).setExecutor(new SegmentGroupCommand());
        Objects.requireNonNull(getCommand("hgsegmentGroup")).setTabCompleter(new SegmentGroupCommand());
        Objects.requireNonNull(getCommand("hgsegmentFill")).setExecutor(new SegmentFillCommand());
        Objects.requireNonNull(getCommand("hgsegmentFill")).setTabCompleter(new SegmentFillCommand());
        Objects.requireNonNull(getCommand("hgsegmentEffect")).setExecutor(new SegmentEffectsCommand());
        Objects.requireNonNull(getCommand("hgsegmentEffect")).setTabCompleter(new SegmentEffectsCommand());
        Objects.requireNonNull(getCommand("hgarena")).setExecutor(new ArenaCommand());
        Objects.requireNonNull(getCommand("hgarena")).setTabCompleter(new ArenaCommand());
    }

    private void registerEffects() {
        // Register effects:
        segmentEffectManager.registerEffect("BEAST", new BeastEffect());
        segmentEffectManager.registerEffect("BLOOD_RAIN", new BloodRainEffect());
        segmentEffectManager.registerEffect("EXTREME_COLD", new ExtremeColdEffect());
        segmentEffectManager.registerEffect("FIRESTORM", new FirestormEffect());
        segmentEffectManager.registerEffect("INSECTS", new InsectsEffect());
        segmentEffectManager.registerEffect("JABBERJAYS", new JabberjaysEffect());
        segmentEffectManager.registerEffect("LIGHTNING", new LightningEffect());
        segmentEffectManager.registerEffect("PARALYSING_FOG", new ParalysingFogEffect());
        segmentEffectManager.registerEffect("POISON_FLOWERS", new PoisonFlowersEffect());
        segmentEffectManager.registerEffect("SPIDERS", new SpidersEffect());
        segmentEffectManager.registerEffect("WASPS", new WaspsEffect());
        segmentEffectManager.registerEffect("WAVE", new WaveEffect());
    }

    private void loadActiveEffects() {
        ConfigurationSection segmentGroups = instance.getSegmentConfig().getConfigurationSection("segment groups");
        if (segmentGroups == null) return;

        for (String segmentGroup : segmentGroups.getKeys(false)) {
            ConfigurationSection segments = segmentGroups.getConfigurationSection(segmentGroup + ".segments");
            if (segments == null) continue;

            for (String segmentNumber : segments.getKeys(false)) {
                ConfigurationSection activeEffects = segments.getConfigurationSection(segmentNumber + ".activeEffects");
                if (activeEffects == null) continue;

                for (String effectName : activeEffects.getKeys(false)) {
                    boolean isActive = activeEffects.getBoolean(effectName);

                    SegmentEffectManager manager = JoTHungerGames.getInstance().getSegmentEffectManager();

                    if (isActive) {
                        Long durationOverride = null;
                        if (getConfig().getBoolean("effects master controls.enabled")) {
                            durationOverride = getConfig().getLong("effects master controls.duration");
                        }else {
                            durationOverride = 60L * 20;
                        }
                        // Start the effect based on its name. You might use a switch statement or an if-else chain here.
                        manager.startEffect(effectName.toUpperCase(), segmentGroup, Integer.parseInt(segmentNumber), durationOverride);
                        // Add more effects as needed.
                    }
                }
            }
        }
    }
    private void disableEffectsForShutdown() {
        ConfigurationSection segmentGroups = instance.getSegmentConfig().getConfigurationSection("segment groups");
        if (segmentGroups == null) return;

        for (String segmentGroup : segmentGroups.getKeys(false)) {
            ConfigurationSection segments = segmentGroups.getConfigurationSection(segmentGroup + ".segments");
            if (segments == null) continue;

            for (String segmentNumber : segments.getKeys(false)) {
                ConfigurationSection activeEffects = segments.getConfigurationSection(segmentNumber + ".activeEffects");
                if (activeEffects == null) continue;

                for (String effectName : activeEffects.getKeys(false)) {
                    boolean isActive = activeEffects.getBoolean(effectName);

                    SegmentEffectManager manager = JoTHungerGames.getInstance().getSegmentEffectManager();

                    if (isActive) {
                        // Start the effect based on its name. You might use a switch statement or an if-else chain here.
                        manager.stopEffect(effectName.toUpperCase(), segmentGroup, Integer.parseInt(segmentNumber));
                        SegmentUtils.setEffectActive(segmentGroup, Integer.parseInt(segmentNumber), "BEAST", true);
                        // Add more effects as needed.
                    }
                }
            }
        }
    }

    private void setupScoreboard() {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        assert manager != null;
        mainScoreboard = manager.getMainScoreboard(); // Get the main scoreboard

        // Check if the kills objective already exists
        killsObjective = mainScoreboard.getObjective("Kills");
        if (killsObjective == null) {
            // Create the kills objective if it doesn't exist
            killsObjective = mainScoreboard.registerNewObjective("Kills", "dummy", ChatColor.RED.toString() + ChatColor.BOLD + "Kills");
        }
        winsObjective = mainScoreboard.getObjective("Wins");
        if (winsObjective == null) {
            // Create the kills objective if it doesn't exist
            winsObjective = mainScoreboard.registerNewObjective("Wins", "playerWins", ChatColor.GOLD.toString() + ChatColor.BOLD + "Wins");
        }
    }
    public Scoreboard getScoreboard() {
        return mainScoreboard;
    }
    public Objective getKillsObjective() {
        return killsObjective;
    }
    public Objective getWinsObjective() {
        return winsObjective;
    }
    public void resetScoreboard(String scoreboardObjective) {
        for (String entry : mainScoreboard.getEntries()) {
            if (entry.equals(scoreboardObjective)) {
                mainScoreboard.resetScores(entry);
            }
        }
    }

    private void saveDefaultArenas() {
        File arenaFile = new File(getDataFolder(), "arenas.yml");
        if (!arenaFile.exists()) {
            this.saveResource("arenas.yml", false);
        }
    }
    public void saveArenaConfig() {
        try {
            arenaConfig.save(arenaConfigFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public FileConfiguration getArenaConfig() {
        if (arenaConfig == null) {
            reloadArenaConfig();
        }
        return arenaConfig;
    }
    public void reloadArenaConfig() {
        if (arenaConfigFile == null) {
            arenaConfigFile = new File(getDataFolder(), "arenas.yml");
        }
        arenaConfig = YamlConfiguration.loadConfiguration(arenaConfigFile);
    }

    public void saveChestToConfig(String arenaName, Location chestLocation, String chestType) {
        String basePath = "arenas." + arenaName + ".chests." + chestType + ".locations";
        ConfigurationSection chestTypeChests = getArenaConfig().getConfigurationSection(basePath);

        getLogger().info("Saving chest to config:");
        getLogger().info("Arena: " + arenaName);
        getLogger().info("Location: " + chestLocation.getBlockX() + "," + chestLocation.getBlockY() + "," + chestLocation.getBlockZ());
        getLogger().info("Chest Type: " + chestType);
        getLogger().info("Base Path: " + basePath);

        if (chestTypeChests == null) {
            getLogger().info("Chest type section does not exist, creating new section.");
            chestTypeChests = getArenaConfig().createSection(basePath);
        }

        // Generate the next key in sequence
        int nextKey = chestTypeChests.getKeys(false).size() + 1;
        getLogger().info("Next Key: " + nextKey);

        // Add the new location under the correct key
        chestTypeChests.set(String.valueOf(nextKey), chestLocation.getBlockX() + "," + chestLocation.getBlockY() + "," + chestLocation.getBlockZ());

        saveArenaConfig();
        getLogger().info("Chest saved successfully.");
    }
    public void removeChestFromConfig(String arenaName, Location chestLocation, String chestType) {
        String basePath = "arenas." + arenaName + ".chests." + chestType + ".locations";
        ConfigurationSection chestTypeChests = getArenaConfig().getConfigurationSection(basePath);
        for (String LocKey : chestTypeChests.getKeys(false)) {
            String[] locParts = chestTypeChests.getString(LocKey).split(",");
            if (locParts.length == 3) {
                int x = Integer.parseInt(locParts[0]);
                int y = Integer.parseInt(locParts[1]);
                int z = Integer.parseInt(locParts[2]);
                if (chestLocation.getBlockX() == x && chestLocation.getBlockY() == y && chestLocation.getBlockZ() == z) {
                    chestTypeChests.set(LocKey, null);
                    saveArenaConfig();
                    break;
                }
            }
        }
    }
    //save arenas to config to be used later
    public void saveArenaToConfig(String arenaName, Location center, String arenaType, int radius, int height, int numPlayers) {
        String basePath = "arenas." + arenaName;

        getArenaConfig().set(basePath + ".center", center.getBlockX() + "," + center.getBlockY() + "," + center.getBlockZ());
        getArenaConfig().set(basePath + ".world", Objects.requireNonNull(center.getWorld()).getName());
        getArenaConfig().set(basePath + ".type", arenaType);
        getArenaConfig().set(basePath + ".radius", radius);
        getArenaConfig().set(basePath + ".height", height);

        getArenaConfig().set(basePath + ".grace period length", 0);
        getArenaConfig().set(basePath + ".time of day", 6000);
        getArenaConfig().set(basePath + ".weather", "clear");
        getArenaConfig().set(basePath + ".do fallen display", true);
        getArenaConfig().set(basePath + ".event broadcast.action bar", true);
        getArenaConfig().set(basePath + ".event broadcast.sidebar", true);

        getArenaConfig().set(basePath + ".world border.active", false);
        getArenaConfig().set(basePath + ".world border.start", 300);
        getArenaConfig().set(basePath + ".world border.duration", 60);
        getArenaConfig().set(basePath + ".world border.final radius", 10);
        getArenaConfig().set(basePath + ".world border.random center", false);

        getArenaConfig().set(basePath + ".highlight players.active", false);
        getArenaConfig().set(basePath + ".highlight players.start", 300);

        getArenaConfig().set(basePath + ".enable locator bar.active", false);
        getArenaConfig().set(basePath + ".enable locator bar.start", 300);

        getArenaConfig().set(basePath + ".lobby.center", "");
        getArenaConfig().set(basePath + ".lobby.type", "");
        getArenaConfig().set(basePath + ".lobby.radius", "");
        getArenaConfig().set(basePath + ".lobby.height", "");

        getArenaConfig().set(basePath + ".players.maximum", numPlayers);
        for (int i = 0; i < numPlayers; i++) {
            getArenaConfig().set(basePath + ".players.playerSpawns." + (i+ 1), "");
        }

        getArenaConfig().set(basePath + ".linked segment group", "");
        getArenaConfig().set(basePath + ".effects.clock", false);
        getArenaConfig().set(basePath + ".effects.clock start time", 20);
        getArenaConfig().set(basePath + ".effects.clock duration override", 60);
        getArenaConfig().set(basePath + ".effects.clock effects.BEAST.active", false);
        getArenaConfig().set(basePath + ".effects.clock effects.BEAST.segments", "");
        getArenaConfig().set(basePath + ".effects.clock effects.BLOOD_RAIN.active", false);
        getArenaConfig().set(basePath + ".effects.clock effects.BLOOD_RAIN.segments", "");
        getArenaConfig().set(basePath + ".effects.clock effects.EXTREME_COLD.active", false);
        getArenaConfig().set(basePath + ".effects.clock effects.EXTREME_COLD.segments", "");
        getArenaConfig().set(basePath + ".effects.clock effects.FIRESTORM.active", false);
        getArenaConfig().set(basePath + ".effects.clock effects.FIRESTORM.segments", "");
        getArenaConfig().set(basePath + ".effects.clock effects.INSECTS.active", false);
        getArenaConfig().set(basePath + ".effects.clock effects.INSECTS.segments", "");
        getArenaConfig().set(basePath + ".effects.clock effects.JABBERJAYS.active", false);
        getArenaConfig().set(basePath + ".effects.clock effects.JABBERJAYS.segments", "");
        getArenaConfig().set(basePath + ".effects.clock effects.LIGHTNING.active", false);
        getArenaConfig().set(basePath + ".effects.clock effects.LIGHTNING.segments", "");
        getArenaConfig().set(basePath + ".effects.clock effects.PARALYSING_FOG.active", false);
        getArenaConfig().set(basePath + ".effects.clock effects.PARALYSING_FOG.segments", "");
        getArenaConfig().set(basePath + ".effects.clock effects.POISON_FLOWERS.active", false);
        getArenaConfig().set(basePath + ".effects.clock effects.POISON_FLOWERS.segments", "");
        getArenaConfig().set(basePath + ".effects.clock effects.SPIDERS.active", false);
        getArenaConfig().set(basePath + ".effects.clock effects.SPIDERS.segments", "");
        getArenaConfig().set(basePath + ".effects.clock effects.WASPS.active", false);
        getArenaConfig().set(basePath + ".effects.clock effects.WASPS.segments", "");
        getArenaConfig().set(basePath + ".effects.clock effects.WAVE.active", false);
        getArenaConfig().set(basePath + ".effects.clock effects.WAVE.segments", "");

        getArenaConfig().set(basePath + ".effects.freeform", false);
        getArenaConfig().set(basePath + ".effects.freeform effects." + 1 + ".effect name", "lightning");
        getArenaConfig().set(basePath + ".effects.freeform effects." + 1 + ".start time", 30);
        getArenaConfig().set(basePath + ".effects.freeform effects." + 1 + ".effect duration override", 30);
        getArenaConfig().set(basePath + ".effects.freeform effects." + 1 + ".segments", "1,2");

        getArenaConfig().set(basePath + ".chests.center.refills." + 1, 160);
        getArenaConfig().set(basePath + ".chests.center.locations", "");
        getArenaConfig().set(basePath + ".chests.normal.refills." + 1, 120);
        getArenaConfig().set(basePath + ".chests.normal.locations", "");
        getArenaConfig().set(basePath + ".chests.prime.refills." + 1, 180);
        getArenaConfig().set(basePath + ".chests.prime.locations", "");

        saveArenaConfig();

    }
    public List<String> getAllArenas() {
        // This is just a mock-up. Adjust to your actual way of fetching segment group names.
        ConfigurationSection section = getArenaConfig().getConfigurationSection("arenas");
        if (section != null) {
            return new ArrayList<>(section.getKeys(false));
        }
        return new ArrayList<>();
    }
    //check if input group name exists as a stored group
    public boolean arenaExists(String arenaName) {
        return getArenaConfig().contains("arenas." + arenaName);
    }

    private void saveDefaultSegments() {
        File segmentsFile = new File(getDataFolder(), "segmentGroups.yml");
        if (!segmentsFile.exists()) {
            this.saveResource("segmentGroups.yml", false);
        }
    }
    public void saveSegmentConfig() {
        try {
            segmentConfig.save(segmentConfigFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public FileConfiguration getSegmentConfig() {
        if (segmentConfig == null) {
            reloadSegmentConfig();
        }
        return segmentConfig;
    }
    public void reloadSegmentConfig() {
        if (segmentConfigFile == null) {
            segmentConfigFile = new File(getDataFolder(), "segmentGroups.yml");
        }
        segmentConfig = YamlConfiguration.loadConfiguration(segmentConfigFile);
    }

    public static JoTHungerGames getInstance() {
        return instance;
    }

    public SegmentEffectManager getSegmentEffectManager() {
        return segmentEffectManager;
    }

    //save segments to config to be used later
    public void saveSegmentToConfig(String groupName, Location center, int outerRadius, int innerRadius, int height, List<JoTHungerGames.Segment> segmentsList) {
        String basePath = "segment groups." + groupName;

        getSegmentConfig().set(basePath + ".center", center.getBlockX() + "," + center.getBlockY() + "," + center.getBlockZ());
        getSegmentConfig().set(basePath + ".world", Objects.requireNonNull(center.getWorld()).getName());
        getSegmentConfig().set(basePath + ".outerRadius", outerRadius);
        getSegmentConfig().set(basePath + ".innerRadius", innerRadius);
        getSegmentConfig().set(basePath + ".height", height);

        double segmentAngle = 360.0 / segmentsList.size();
        for (int i = 0; i < segmentsList.size(); i++) {
            getSegmentConfig().set(basePath + ".segments." + (i+ 1) + ".startAngle", i * segmentAngle);
            getSegmentConfig().set(basePath + ".segments." + (i+ 1) + ".endAngle", (i + 1) * segmentAngle);
        }

        saveSegmentConfig();

        for (int i = 0; i < segmentsList.size(); i++) {
            Location segmentCenter = SegmentUtils.getSegmentCenter(groupName, i + 1);
            getSegmentConfig().set(basePath + ".segments." + (i + 1) + ".center", segmentCenter.getBlockX() + "," + segmentCenter.getBlockY() + "," + segmentCenter.getBlockZ());
        }

        saveSegmentConfig();

    }

    //get segments from config to use or change them
    public List<Segment> getSegmentsFromConfig(String groupName) {
        String basePath = "segment groups." + groupName;

        String centerStr = getSegmentConfig().getString(basePath + ".center");
        if(centerStr == null) {
            // Log an error or handle this scenario
            return new ArrayList<>(); // Empty list or however you wish to handle this
        }

        String[] centerCoords = centerStr.split(",");
        World world = Bukkit.getWorld(getSegmentConfig().getString(basePath + ".world"));
        Location center = new Location(world, Integer.parseInt(centerCoords[0]), Integer.parseInt(centerCoords[1]), Integer.parseInt(centerCoords[2]));
        int outerRadius = getSegmentConfig().getInt(basePath + ".outerRadius");
        int innerRadius = getSegmentConfig().getInt(basePath + ".innerRadius");
        int height = getSegmentConfig().getInt(basePath + ".height");

        ConfigurationSection segmentsSection = getSegmentConfig().getConfigurationSection(basePath + ".segments");
        List<Segment> segmentsList = new ArrayList<>();

        for (String key : segmentsSection.getKeys(false)) {
            double startAngle = segmentsSection.getDouble(key + ".startAngle");
            double endAngle = segmentsSection.getDouble(key + ".endAngle");

            // Use these angles, center, radius, and height to reconstruct each segment
            Segment segment = reconstructSegment(Integer.parseInt(key) - 1, center, outerRadius, innerRadius, height, startAngle, endAngle);
            segmentsList.add(segment);
        }

        return segmentsList;
    }

    public Segment reconstructSegment(int index, Location center, int outerRadius, int innerRadius, int height, double startAngle, double endAngle) {
        Segment segment = new Segment(index); // Assuming your Segment constructor allows for an empty instantiation

        World world = center.getWorld();

        int baseY = center.getBlockY();
        int baseX = center.getBlockX();
        int baseZ = center.getBlockZ();

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

                            // Check if the angle is within the segment's angle range
                            if (angle >= startAngle && angle <= endAngle) {
                                segment.addBlock(new Location(world, x, y, z));
                            }
                        }
                    }
                }
            }
        }

        return segment;
    }

    public List<String> getAllSegmentGroups() {
        // This is just a mock-up. Adjust to your actual way of fetching segment group names.
        ConfigurationSection section = getSegmentConfig().getConfigurationSection("segment groups");
        if (section != null) {
            return new ArrayList<>(section.getKeys(false));
        }
        return new ArrayList<>();
    }

    public List<String> getAllSegmentNumbersForGroup(String groupName) {
        // This is just a mock-up. Adjust to your actual way of fetching segment numbers.
        ConfigurationSection section = getSegmentConfig().getConfigurationSection("segment groups." + groupName + ".segments");
        if (section != null) {
            return new ArrayList<>(section.getKeys(false));
        }
        return new ArrayList<>();
    }

    //check if input group name exists as a stored group
    public boolean segmentGroupExists(String groupName) {
        return getSegmentConfig().contains("segment groups." + groupName);
    }

    public boolean segmentExistsWithinGroup(String groupName, int segmentNumber) {
        // Check if the group exists
        if (!getSegmentConfig().contains("segment groups." + groupName)) {
            return false;
        }

        // Check if the segment number exists within the group
        return getSegmentConfig().contains("segment groups." + groupName + ".segments." + segmentNumber);
    }

    public List<Material> getIgnoredMaterials() {
        List<String> materialNames = getConfig().getStringList("getSurfaceBlocks-ignored-materials");
        List<Material> materials = new ArrayList<>();

        for (String name : materialNames) {
            Material material = Material.getMaterial(name);
            if (material != null) {
                materials.add(material);
            } else {
                getLogger().warning("Invalid material in getSurfaceBlocks-ignored-materials list: " + name);
            }
        }

        return materials;
    }

    public List<Material> getFlammableMaterials() {
        List<String> materialNames = getConfig().getStringList("getSurfaceBlocks-flammable-materials");
        List<Material> materials = new ArrayList<>();

        for (String name : materialNames) {
            Material material = Material.getMaterial(name);
            if (material != null) {
                materials.add(material);
            } else {
                getLogger().warning("Invalid material in getSurfaceBlocks-ignored-materials list: " + name);
            }
        }

        return materials;
    }

    public static class Segment {
        private final int index;
        private final List<Location> blocks;

        public Segment(int index) {
            this.index = index;
            this.blocks = new ArrayList<>();
        }

        public void addBlock(Location location) {
            blocks.add(location);
        }

        public List<Location> getBlocks() {
            return blocks;
        }

        public int getIndex() {
            return index;
        }
    }
}


