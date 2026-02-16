package me.jamontoast.jothungergames.Tasks;

import me.jamontoast.jothungergames.JoTHungerGames;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

public class FallenDisplayTask extends BukkitRunnable {

    private int countdown;
    private List<String> deadPlayers;
    private final float scale = 50f;

    private final Location arenaCenter;
    private final List<Location> displayLocations;

    //FADING LOGIC
    private static final String BANNER_TAG  = "fallen_banner";
    private static final String GRAPHIC_TAG = "fallen_graphic";
    private static final int fade_ticks = 5;    // How many ticks to spend fading in (and out)
    private final long displayDuration;     // in ticks

    private final String instanceId;
    private final String bannerTag;
    private final String graphicTag;

    private final String HGMusic = "jothungergames.hornofplenty_noteblocks_oldrecord";
    private final String HGMusicOG = "jothungergames.hornofplenty_real_oldrecord";



    public FallenDisplayTask(List<String> deadPlayers, Location arenaCenter, int arenaRadius, List<Location> displayLocations, long displayDuration) {

        this.displayDuration = displayDuration;

        this.deadPlayers = deadPlayers;
        countdown = (deadPlayers.size() + 1);
        this.displayLocations = displayLocations;
        this.arenaCenter = arenaCenter;
        this.instanceId = UUID.randomUUID().toString().substring(0, 8);
        this.bannerTag = BANNER_TAG + "_" + instanceId;
        this.graphicTag = GRAPHIC_TAG + "_" + instanceId;

    }

    @Override
    public void run() {

        String deadPlayerName = null;
        String deadPlayerImg = null;
        String bannerNBT = null;
        String graphicNBT = null;
        String defaultGraphicNBT = null;


        try {
            if (countdown == deadPlayers.size() + 1) {
                //prepare text
                //deadPlayerImg = "[{\"text\":\"         ▇            \",\"color\":\"#40dfdf\"},{\"text\":\"▇\",\"color\":\"#3cd2d2\"},{\"text\":\"▇\n       \",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#56ffff\"},{\"text\":\"▇\",\"color\":\"#54ffff\"},{\"text\":\"▇\",\"color\":\"#42cccc\"},{\"text\":\"▇           \",\"color\":\"#56ffff\"},{\"text\":\"▇\",\"color\":\"#54ffff\"},{\"text\":\"▇\",\"color\":\"#4eeded\"},{\"text\":\"▇\",\"color\":\"#53fafa\"},{\"text\":\"▇\n                      \",\"color\":\"#4ae3e3\"},{\"text\":\"▇  \",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#3abfbf\"},{\"text\":\"▇\n     \",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#55eeee\"},{\"text\":\"▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#56ffff\"},{\"text\":\"▇               \",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#44d9d9\"},{\"text\":\"▇ \",\"color\":\"#4ae9e9\"},{\"text\":\"▇▇\n   \",\"color\":\"#55ffff\"},{\"text\":\"▇▇\",\"color\":\"#54ffff\"},{\"text\":\"▇\",\"color\":\"#58ffff\"},{\"text\":\"▇\",\"color\":\"#55ffff\"},{\"text\":\"▇     \",\"color\":\"#51f2f2\"},{\"text\":\"▇▇  \",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#40bfbf\"},{\"text\":\"▇\",\"color\":\"#55ffff\"},{\"text\":\"▇▇    \",\"color\":\"#51ffff\"},{\"text\":\"▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#55eaea\"},{\"text\":\"▇▇\",\"color\":\"#54ffff\"},{\"text\":\"▇\n   \",\"color\":\"#55ffff\"},{\"text\":\"▇▇\",\"color\":\"#54ffff\"},{\"text\":\"▇\",\"color\":\"#58ffff\"},{\"text\":\"▇\",\"color\":\"#55ffff\"},{\"text\":\"▇     \",\"color\":\"#51f2f2\"},{\"text\":\"▇▇  \",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#40bfbf\"},{\"text\":\"▇\",\"color\":\"#55ffff\"},{\"text\":\"▇▇    \",\"color\":\"#51ffff\"},{\"text\":\"▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#55eaea\"},{\"text\":\"▇▇\",\"color\":\"#54ffff\"},{\"text\":\"▇\n  ▇\",\"color\":\"#55ffff\"},{\"text\":\"▇▇\",\"color\":\"#51f5f5\"},{\"text\":\"▇\",\"color\":\"#57ffff\"},{\"text\":\"▇   \",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#54fbfb\"},{\"text\":\"▇▇\",\"color\":\"#50f3f3\"},{\"text\":\"▇▇   ▇▇▇▇    \",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#44d7d7\"},{\"text\":\"▇▇▇\n   \",\"color\":\"#54ffff\"},{\"text\":\"▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇   \",\"color\":\"#54ffff\"},{\"text\":\"▇\",\"color\":\"#53fafa\"},{\"text\":\"▇\",\"color\":\"#55ffff\"},{\"text\":\"▇▇\",\"color\":\"#53f9f9\"},{\"text\":\"▇▇   ▇\",\"color\":\"#55ffff\"},{\"text\":\"▇▇\",\"color\":\"#4ef1f1\"},{\"text\":\"▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇   \",\"color\":\"#35c4c4\"},{\"text\":\"▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\n \",\"color\":\"#52fbfb\"},{\"text\":\"▇      ▇▇▇▇▇▇▇   ▇▇▇▇▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇      \",\"color\":\"#54ffff\"},{\"text\":\"▇\n ▇▇▇▇  \",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#4ef5f5\"},{\"text\":\"▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#53fafa\"},{\"text\":\"▇▇▇▇   ▇▇▇▇▇▇▇    \",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#52f9f9\"},{\"text\":\"▇▇\n\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#4cf0f0\"},{\"text\":\"▇▇    ▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#56ffff\"},{\"text\":\"▇▇▇▇▇▇   ▇▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#54fefe\"},{\"text\":\"▇▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇    \",\"color\":\"#56ffff\"},{\"text\":\"▇▇\n▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#57ffff\"},{\"text\":\"▇   \",\"color\":\"#54ffff\"},{\"text\":\"▇\",\"color\":\"#50ecec\"},{\"text\":\"▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#4ff3f3\"},{\"text\":\"▇\",\"color\":\"#55ffff\"},{\"text\":\"▇▇\",\"color\":\"#54fefe\"},{\"text\":\"▇\",\"color\":\"#54ffff\"},{\"text\":\"▇   ▇▇▇▇▇▇▇▇    \",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#54ffff\"},{\"text\":\"▇\n\",\"color\":\"#4de7e7\"},{\"text\":\"▇▇    \",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#4eefef\"},{\"text\":\"▇▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#54fdfd\"},{\"text\":\"▇▇▇▇   ▇\",\"color\":\"#55ffff\"},{\"text\":\"▇▇\",\"color\":\"#54ffff\"},{\"text\":\"▇▇▇▇▇    ▇▇\n\",\"color\":\"#55ffff\"},{\"text\":\"▇▇    \",\"color\":\"#54ffff\"},{\"text\":\"▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#54fdfd\"},{\"text\":\"▇▇▇▇▇▇▇▇ ▇▇▇▇▇▇▇▇     ▇\n▇▇    ▇▇▇▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇▇\",\"color\":\"#54ffff\"},{\"text\":\"▇▇ ▇ ▇▇▇▇▇▇▇▇     ▇\n▇▇    ▇▇▇▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇▇\",\"color\":\"#54ffff\"},{\"text\":\"▇▇ ▇ ▇▇▇▇▇▇▇▇     ▇\n▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇   \",\"color\":\"#60ffff\"},{\"text\":\"▇▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#49e4e4\"},{\"text\":\"▇▇▇\",\"color\":\"#56ffff\"},{\"text\":\"▇▇▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#54ffff\"},{\"text\":\"▇▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#56ffff\"},{\"text\":\"▇\",\"color\":\"#4ef1f1\"},{\"text\":\"▇\",\"color\":\"#51f6f6\"},{\"text\":\"▇▇    ▇▇\n▇▇▇   ▇▇▇▇▇▇▇▇▇▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#54ffff\"},{\"text\":\"▇▇▇▇▇▇▇▇    ▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\n\",\"color\":\"#56ffff\"},{\"text\":\"▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇          \",\"color\":\"#56ffff\"},{\"text\":\"▇▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#56ffff\"},{\"text\":\"▇\",\"color\":\"#54ffff\"},{\"text\":\"▇           ▇▇\n\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#54ffff\"},{\"text\":\"▇▇            ▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#56ffff\"},{\"text\":\"▇             \",\"color\":\"#54ffff\"},{\"text\":\"▇\n\",\"color\":\"#56ffff\"},{\"text\":\"▇\",\"color\":\"#54ffff\"},{\"text\":\"▇▇     ▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#4ef0f0\"},{\"text\":\"▇▇  ▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#56ffff\"},{\"text\":\"▇ ▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#3bd8d8\"},{\"text\":\"▇\",\"color\":\"#3ed2d2\"},{\"text\":\"▇      \",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#54ffff\"},{\"text\":\"▇\n ▇▇      ▇▇▇▇▇▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#53fafa\"},{\"text\":\"▇\",\"color\":\"#54ffff\"},{\"text\":\"▇▇▇▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇      \",\"color\":\"#57ffff\"},{\"text\":\"▇▇\n \",\"color\":\"#55ffff\"},{\"text\":\"▇ \",\"color\":\"#54ffff\"},{\"text\":\"▇▇     ▇\",\"color\":\"#55ffff\"},{\"text\":\"▇▇\",\"color\":\"#54ffff\"},{\"text\":\"▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#4ae7e7\"},{\"text\":\"▇▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇▇\",\"color\":\"#53ffff\"},{\"text\":\"▇       \",\"color\":\"#55ffff\"},{\"text\":\"▇ \",\"color\":\"#41d0d0\"},{\"text\":\"▇\n  \",\"color\":\"#53ffff\"},{\"text\":\"▇▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇       \",\"color\":\"#4cecec\"},{\"text\":\"▇▇▇▇▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇▇      \",\"color\":\"#52ffff\"},{\"text\":\"▇▇\",\"color\":\"#52f6f6\"},{\"text\":\"▇▇\n  ▇▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇       \",\"color\":\"#4cecec\"},{\"text\":\"▇▇▇▇▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇▇      \",\"color\":\"#52ffff\"},{\"text\":\"▇▇\",\"color\":\"#52f6f6\"},{\"text\":\"▇▇\n  \",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#57ffff\"},{\"text\":\"▇▇\",\"color\":\"#4ef1f1\"},{\"text\":\"▇\",\"color\":\"#55ffff\"},{\"text\":\"▇      \",\"color\":\"#53ffff\"},{\"text\":\"▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#55fcfc\"},{\"text\":\"▇▇▇▇       \",\"color\":\"#55ffff\"},{\"text\":\"▇  \",\"color\":\"#54fdfd\"},{\"text\":\"▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\n   \",\"color\":\"#43d7d7\"},{\"text\":\"▇▇▇▇▇                 ▇▇▇▇▇\n     ▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#39c6c6\"},{\"text\":\"▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#46dbdb\"},{\"text\":\"▇            \",\"color\":\"#53ffff\"},{\"text\":\"▇\",\"color\":\"#51f5f5\"},{\"text\":\"▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#4ff0f0\"},{\"text\":\"▇\",\"color\":\"#55ffff\"},{\"text\":\"▇▇\n      \",\"color\":\"#55fefe\"},{\"text\":\"▇▇ ▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#57ffff\"},{\"text\":\"▇▇        ▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#54ffff\"},{\"text\":\"▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\n       \",\"color\":\"#51f7f7\"},{\"text\":\"▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#54ffff\"},{\"text\":\"▇\",\"color\":\"#57ffff\"},{\"text\":\"▇▇      \",\"color\":\"#55ffff\"},{\"text\":\"▇▇\",\"color\":\"#54fbfb\"},{\"text\":\"▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#4eebeb\"},{\"text\":\"▇▇\n         \",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#59ffff\"},{\"text\":\"▇▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇    \",\"color\":\"#53fcfc\"},{\"text\":\"▇\",\"color\":\"#54fbfb\"},{\"text\":\"▇▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\n              \",\"color\":\"#54fefe\"},{\"text\":\"▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#4eeeee\"},{\"text\":\"▇\",\"color\":\"#55ffff\"}]";
                deadPlayerImg = "[{\"text\":\"         ▇            \",\"color\":\"#40dfdf\"},{\"text\":\"▇\",\"color\":\"#3cd2d2\"},{\"text\":\"▇\\n       \",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#56ffff\"},{\"text\":\"▇\",\"color\":\"#54ffff\"},{\"text\":\"▇\",\"color\":\"#42cccc\"},{\"text\":\"▇           \",\"color\":\"#56ffff\"},{\"text\":\"▇\",\"color\":\"#54ffff\"},{\"text\":\"▇\",\"color\":\"#4eeded\"},{\"text\":\"▇\",\"color\":\"#53fafa\"},{\"text\":\"▇\\n                      \",\"color\":\"#4ae3e3\"},{\"text\":\"▇  \",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#3abfbf\"},{\"text\":\"▇\\n     \",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#55eeee\"},{\"text\":\"▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#56ffff\"},{\"text\":\"▇               \",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#44d9d9\"},{\"text\":\"▇ \",\"color\":\"#4ae9e9\"},{\"text\":\"▇▇\\n   \",\"color\":\"#55ffff\"},{\"text\":\"▇▇\",\"color\":\"#54ffff\"},{\"text\":\"▇\",\"color\":\"#58ffff\"},{\"text\":\"▇\",\"color\":\"#55ffff\"},{\"text\":\"▇     \",\"color\":\"#51f2f2\"},{\"text\":\"▇▇  \",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#40bfbf\"},{\"text\":\"▇\",\"color\":\"#55ffff\"},{\"text\":\"▇▇    \",\"color\":\"#51ffff\"},{\"text\":\"▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#55eaea\"},{\"text\":\"▇▇\",\"color\":\"#54ffff\"},{\"text\":\"▇\\n   \",\"color\":\"#55ffff\"},{\"text\":\"▇▇\",\"color\":\"#54ffff\"},{\"text\":\"▇\",\"color\":\"#58ffff\"},{\"text\":\"▇\",\"color\":\"#55ffff\"},{\"text\":\"▇     \",\"color\":\"#51f2f2\"},{\"text\":\"▇▇  \",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#40bfbf\"},{\"text\":\"▇\",\"color\":\"#55ffff\"},{\"text\":\"▇▇    \",\"color\":\"#51ffff\"},{\"text\":\"▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#55eaea\"},{\"text\":\"▇▇\",\"color\":\"#54ffff\"},{\"text\":\"▇\\n  ▇\",\"color\":\"#55ffff\"},{\"text\":\"▇▇\",\"color\":\"#51f5f5\"},{\"text\":\"▇\",\"color\":\"#57ffff\"},{\"text\":\"▇   \",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#54fbfb\"},{\"text\":\"▇▇\",\"color\":\"#50f3f3\"},{\"text\":\"▇▇   ▇▇▇▇    \",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#44d7d7\"},{\"text\":\"▇▇▇\\n   \",\"color\":\"#54ffff\"},{\"text\":\"▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇   \",\"color\":\"#54ffff\"},{\"text\":\"▇\",\"color\":\"#53fafa\"},{\"text\":\"▇\",\"color\":\"#55ffff\"},{\"text\":\"▇▇\",\"color\":\"#53f9f9\"},{\"text\":\"▇▇   ▇\",\"color\":\"#55ffff\"},{\"text\":\"▇▇\",\"color\":\"#4ef1f1\"},{\"text\":\"▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇   \",\"color\":\"#35c4c4\"},{\"text\":\"▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\\n \",\"color\":\"#52fbfb\"},{\"text\":\"▇      ▇▇▇▇▇▇▇   ▇▇▇▇▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇      \",\"color\":\"#54ffff\"},{\"text\":\"▇\\n ▇▇▇▇  \",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#4ef5f5\"},{\"text\":\"▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#53fafa\"},{\"text\":\"▇▇▇▇   ▇▇▇▇▇▇▇    \",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#52f9f9\"},{\"text\":\"▇▇\\n\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#4cf0f0\"},{\"text\":\"▇▇    ▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#56ffff\"},{\"text\":\"▇▇▇▇▇▇   ▇▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#54fefe\"},{\"text\":\"▇▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇    \",\"color\":\"#56ffff\"},{\"text\":\"▇▇\\n▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#57ffff\"},{\"text\":\"▇   \",\"color\":\"#54ffff\"},{\"text\":\"▇\",\"color\":\"#50ecec\"},{\"text\":\"▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#4ff3f3\"},{\"text\":\"▇\",\"color\":\"#55ffff\"},{\"text\":\"▇▇\",\"color\":\"#54fefe\"},{\"text\":\"▇\",\"color\":\"#54ffff\"},{\"text\":\"▇   ▇▇▇▇▇▇▇▇    \",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#54ffff\"},{\"text\":\"▇\\n\",\"color\":\"#4de7e7\"},{\"text\":\"▇▇    \",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#4eefef\"},{\"text\":\"▇▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#54fdfd\"},{\"text\":\"▇▇▇▇   ▇\",\"color\":\"#55ffff\"},{\"text\":\"▇▇\",\"color\":\"#54ffff\"},{\"text\":\"▇▇▇▇▇    ▇▇\\n\",\"color\":\"#55ffff\"},{\"text\":\"▇▇    \",\"color\":\"#54ffff\"},{\"text\":\"▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#54fdfd\"},{\"text\":\"▇▇▇▇▇▇▇▇ ▇▇▇▇▇▇▇▇     ▇\\n▇▇    ▇▇▇▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇▇\",\"color\":\"#54ffff\"},{\"text\":\"▇▇ ▇ ▇▇▇▇▇▇▇▇     ▇\\n▇▇    ▇▇▇▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇▇\",\"color\":\"#54ffff\"},{\"text\":\"▇▇ ▇ ▇▇▇▇▇▇▇▇     ▇\\n▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇   \",\"color\":\"#60ffff\"},{\"text\":\"▇▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#49e4e4\"},{\"text\":\"▇▇▇\",\"color\":\"#56ffff\"},{\"text\":\"▇▇▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#54ffff\"},{\"text\":\"▇▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#56ffff\"},{\"text\":\"▇\",\"color\":\"#4ef1f1\"},{\"text\":\"▇\",\"color\":\"#51f6f6\"},{\"text\":\"▇▇    ▇▇\\n▇▇▇   ▇▇▇▇▇▇▇▇▇▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#54ffff\"},{\"text\":\"▇▇▇▇▇▇▇▇    ▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\\n\",\"color\":\"#56ffff\"},{\"text\":\"▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇          \",\"color\":\"#56ffff\"},{\"text\":\"▇▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#56ffff\"},{\"text\":\"▇\",\"color\":\"#54ffff\"},{\"text\":\"▇           ▇▇\\n\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#54ffff\"},{\"text\":\"▇▇            ▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#56ffff\"},{\"text\":\"▇             \",\"color\":\"#54ffff\"},{\"text\":\"▇\\n\",\"color\":\"#56ffff\"},{\"text\":\"▇\",\"color\":\"#54ffff\"},{\"text\":\"▇▇     ▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#4ef0f0\"},{\"text\":\"▇▇  ▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#56ffff\"},{\"text\":\"▇ ▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#3bd8d8\"},{\"text\":\"▇\",\"color\":\"#3ed2d2\"},{\"text\":\"▇      \",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#54ffff\"},{\"text\":\"▇\\n ▇▇      ▇▇▇▇▇▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#53fafa\"},{\"text\":\"▇\",\"color\":\"#54ffff\"},{\"text\":\"▇▇▇▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇      \",\"color\":\"#57ffff\"},{\"text\":\"▇▇\\n \",\"color\":\"#55ffff\"},{\"text\":\"▇ \",\"color\":\"#54ffff\"},{\"text\":\"▇▇     ▇\",\"color\":\"#55ffff\"},{\"text\":\"▇▇\",\"color\":\"#54ffff\"},{\"text\":\"▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#4ae7e7\"},{\"text\":\"▇▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇▇\",\"color\":\"#53ffff\"},{\"text\":\"▇       \",\"color\":\"#55ffff\"},{\"text\":\"▇ \",\"color\":\"#41d0d0\"},{\"text\":\"▇\\n  \",\"color\":\"#53ffff\"},{\"text\":\"▇▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇       \",\"color\":\"#4cecec\"},{\"text\":\"▇▇▇▇▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇▇      \",\"color\":\"#52ffff\"},{\"text\":\"▇▇\",\"color\":\"#52f6f6\"},{\"text\":\"▇▇\\n  ▇▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇       \",\"color\":\"#4cecec\"},{\"text\":\"▇▇▇▇▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇▇      \",\"color\":\"#52ffff\"},{\"text\":\"▇▇\",\"color\":\"#52f6f6\"},{\"text\":\"▇▇\\n  \",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#57ffff\"},{\"text\":\"▇▇\",\"color\":\"#4ef1f1\"},{\"text\":\"▇\",\"color\":\"#55ffff\"},{\"text\":\"▇      \",\"color\":\"#53ffff\"},{\"text\":\"▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#55fcfc\"},{\"text\":\"▇▇▇▇       \",\"color\":\"#55ffff\"},{\"text\":\"▇  \",\"color\":\"#54fdfd\"},{\"text\":\"▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\\n   \",\"color\":\"#43d7d7\"},{\"text\":\"▇▇▇▇▇                 ▇▇▇▇▇\\n     ▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#39c6c6\"},{\"text\":\"▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#46dbdb\"},{\"text\":\"▇            \",\"color\":\"#53ffff\"},{\"text\":\"▇\",\"color\":\"#51f5f5\"},{\"text\":\"▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#4ff0f0\"},{\"text\":\"▇\",\"color\":\"#55ffff\"},{\"text\":\"▇▇\\n      \",\"color\":\"#55fefe\"},{\"text\":\"▇▇ ▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#57ffff\"},{\"text\":\"▇▇        ▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#54ffff\"},{\"text\":\"▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\\n       \",\"color\":\"#51f7f7\"},{\"text\":\"▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#54ffff\"},{\"text\":\"▇\",\"color\":\"#57ffff\"},{\"text\":\"▇▇      \",\"color\":\"#55ffff\"},{\"text\":\"▇▇\",\"color\":\"#54fbfb\"},{\"text\":\"▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#4eebeb\"},{\"text\":\"▇▇\\n         \",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#59ffff\"},{\"text\":\"▇▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇    \",\"color\":\"#53fcfc\"},{\"text\":\"▇\",\"color\":\"#54fbfb\"},{\"text\":\"▇▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\\n              \",\"color\":\"#54fefe\"},{\"text\":\"▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#4eeeee\"},{\"text\":\"▇\",\"color\":\"#55ffff\"}]";


                //deadPlayerImg = "[{\"text\":\"         ▇            \",\"color\":\"#40dfdf\"},{\"text\":\"▇\",\"color\":\"#3cd2d2\"},{\"text\":\"▇\\n       \",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#56ffff\"},{\"text\":\"▇\",\"color\":\"#54ffff\"},{\"text\":\"▇\",\"color\":\"#42cccc\"},{\"text\":\"▇           \",\"color\":\"#56ffff\"},{\"text\":\"▇\",\"color\":\"#54ffff\"},{\"text\":\"▇\",\"color\":\"#4eeded\"},{\"text\":\"▇\",\"color\":\"#53fafa\"},{\"text\":\"▇\\n                      \",\"color\":\"#4ae3e3\"},{\"text\":\"▇  \",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#3abfbf\"},{\"text\":\"▇\\n     \",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#55eeee\"},{\"text\":\"▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#56ffff\"},{\"text\":\"▇               \",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#44d9d9\"},{\"text\":\"▇ \",\"color\":\"#4ae9e9\"},{\"text\":\"▇▇\\n   \",\"color\":\"#55ffff\"},{\"text\":\"▇▇\",\"color\":\"#54ffff\"},{\"text\":\"▇\",\"color\":\"#58ffff\"},{\"text\":\"▇\",\"color\":\"#55ffff\"},{\"text\":\"▇     \",\"color\":\"#51f2f2\"},{\"text\":\"▇▇  \",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#40bfbf\"},{\"text\":\"▇\",\"color\":\"#55ffff\"},{\"text\":\"▇▇    \",\"color\":\"#51ffff\"},{\"text\":\"▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#55eaea\"},{\"text\":\"▇▇\",\"color\":\"#54ffff\"},{\"text\":\"▇\\n   \",\"color\":\"#55ffff\"},{\"text\":\"▇▇\",\"color\":\"#54ffff\"},{\"text\":\"▇\",\"color\":\"#58ffff\"},{\"text\":\"▇\",\"color\":\"#55ffff\"},{\"text\":\"▇     \",\"color\":\"#51f2f2\"},{\"text\":\"▇▇  \",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#40bfbf\"},{\"text\":\"▇\",\"color\":\"#55ffff\"},{\"text\":\"▇▇    \",\"color\":\"#51ffff\"},{\"text\":\"▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#55eaea\"},{\"text\":\"▇▇\",\"color\":\"#54ffff\"},{\"text\":\"▇\\n  ▇\",\"color\":\"#55ffff\"},{\"text\":\"▇▇\",\"color\":\"#51f5f5\"},{\"text\":\"▇\",\"color\":\"#57ffff\"},{\"text\":\"▇   \",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#54fbfb\"},{\"text\":\"▇▇\",\"color\":\"#50f3f3\"},{\"text\":\"▇▇   ▇▇▇▇    \",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#44d7d7\"},{\"text\":\"▇▇▇\\n   \",\"color\":\"#54ffff\"},{\"text\":\"▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇   \",\"color\":\"#54ffff\"},{\"text\":\"▇\",\"color\":\"#53fafa\"},{\"text\":\"▇\",\"color\":\"#55ffff\"},{\"text\":\"▇▇\",\"color\":\"#53f9f9\"},{\"text\":\"▇▇   ▇\",\"color\":\"#55ffff\"},{\"text\":\"▇▇\",\"color\":\"#4ef1f1\"},{\"text\":\"▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇   \",\"color\":\"#35c4c4\"},{\"text\":\"▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\\n \",\"color\":\"#52fbfb\"},{\"text\":\"▇      ▇▇▇▇▇▇▇   ▇▇▇▇▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇      \",\"color\":\"#54ffff\"},{\"text\":\"▇\\n ▇▇▇▇  \",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#4ef5f5\"},{\"text\":\"▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#53fafa\"},{\"text\":\"▇▇▇▇   ▇▇▇▇▇▇▇    \",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#52f9f9\"},{\"text\":\"▇▇\\n\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#4cf0f0\"},{\"text\":\"▇▇    ▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#56ffff\"},{\"text\":\"▇▇▇▇▇▇   ▇▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#54fefe\"},{\"text\":\"▇▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇    \",\"color\":\"#56ffff\"},{\"text\":\"▇▇\\n▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#57ffff\"},{\"text\":\"▇   \",\"color\":\"#54ffff\"},{\"text\":\"▇\",\"color\":\"#50ecec\"},{\"text\":\"▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#4ff3f3\"},{\"text\":\"▇\",\"color\":\"#55ffff\"},{\"text\":\"▇▇\",\"color\":\"#54fefe\"},{\"text\":\"▇\",\"color\":\"#54ffff\"},{\"text\":\"▇   ▇▇▇▇▇▇▇▇    \",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#54ffff\"},{\"text\":\"▇\\n\",\"color\":\"#4de7e7\"},{\"text\":\"▇▇    \",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#4eefef\"},{\"text\":\"▇▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#54fdfd\"},{\"text\":\"▇▇▇▇   ▇\",\"color\":\"#55ffff\"},{\"text\":\"▇▇\",\"color\":\"#54ffff\"},{\"text\":\"▇▇▇▇▇    ▇▇\\n\",\"color\":\"#55ffff\"},{\"text\":\"▇▇    \",\"color\":\"#54ffff\"},{\"text\":\"▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#54fdfd\"},{\"text\":\"▇▇▇▇▇▇▇▇ ▇▇▇▇▇▇▇▇     ▇\\n▇▇    ▇▇▇▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇▇\",\"color\":\"#54ffff\"},{\"text\":\"▇▇ ▇ ▇▇▇▇▇▇▇▇     ▇\\n▇▇    ▇▇▇▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇▇\",\"color\":\"#54ffff\"},{\"text\":\"▇▇ ▇ ▇▇▇▇▇▇▇▇     ▇\\n▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇   \",\"color\":\"#60ffff\"},{\"text\":\"▇▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#49e4e4\"},{\"text\":\"▇▇▇\",\"color\":\"#56ffff\"},{\"text\":\"▇▇▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#54ffff\"},{\"text\":\"▇▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#56ffff\"},{\"text\":\"▇\",\"color\":\"#4ef1f1\"},{\"text\":\"▇\",\"color\":\"#51f6f6\"},{\"text\":\"▇▇    ▇▇\\n▇▇▇   ▇▇▇▇▇▇▇▇▇▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#54ffff\"},{\"text\":\"▇▇▇▇▇▇▇▇    ▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\\n\",\"color\":\"#56ffff\"},{\"text\":\"▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇          \",\"color\":\"#56ffff\"},{\"text\":\"▇▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#56ffff\"},{\"text\":\"▇\",\"color\":\"#54ffff\"},{\"text\":\"▇           ▇▇\\n\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#54ffff\"},{\"text\":\"▇▇            ▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#56ffff\"},{\"text\":\"▇             \",\"color\":\"#54ffff\"},{\"text\":\"▇\\n\",\"color\":\"#56ffff\"},{\"text\":\"▇\",\"color\":\"#54ffff\"},{\"text\":\"▇▇     ▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#4ef0f0\"},{\"text\":\"▇▇  ▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#56ffff\"},{\"text\":\"▇ ▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#3bd8d8\"},{\"text\":\"▇\",\"color\":\"#3ed2d2\"},{\"text\":\"▇      \",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#54ffff\"},{\"text\":\"▇\\n ▇▇      ▇▇▇▇▇▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#53fafa\"},{\"text\":\"▇\",\"color\":\"#54ffff\"},{\"text\":\"▇▇▇▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇      \",\"color\":\"#57ffff\"},{\"text\":\"▇▇\\n \",\"color\":\"#55ffff\"},{\"text\":\"▇ \",\"color\":\"#54ffff\"},{\"text\":\"▇▇     ▇\",\"color\":\"#55ffff\"},{\"text\":\"▇▇\",\"color\":\"#54ffff\"},{\"text\":\"▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#4ae7e7\"},{\"text\":\"▇▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇▇\",\"color\":\"#53ffff\"},{\"text\":\"▇       \",\"color\":\"#55ffff\"},{\"text\":\"▇ \",\"color\":\"#41d0d0\"},{\"text\":\"▇\\n  \",\"color\":\"#53ffff\"},{\"text\":\"▇▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇       \",\"color\":\"#4cecec\"},{\"text\":\"▇▇▇▇▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇▇      \",\"color\":\"#52ffff\"},{\"text\":\"▇▇\",\"color\":\"#52f6f6\"},{\"text\":\"▇▇\\n  ▇▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇       \",\"color\":\"#4cecec\"},{\"text\":\"▇▇▇▇▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇▇      \",\"color\":\"#52ffff\"},{\"text\":\"▇▇\",\"color\":\"#52f6f6\"},{\"text\":\"▇▇\\n  \",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#57ffff\"},{\"text\":\"▇▇\",\"color\":\"#4ef1f1\"},{\"text\":\"▇\",\"color\":\"#55ffff\"},{\"text\":\"▇      \",\"color\":\"#53ffff\"},{\"text\":\"▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#55fcfc\"},{\"text\":\"▇▇▇▇       \",\"color\":\"#55ffff\"},{\"text\":\"▇  \",\"color\":\"#54fdfd\"},{\"text\":\"▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\\n   \",\"color\":\"#43d7d7\"},{\"text\":\"▇▇▇▇▇                 ▇▇▇▇▇\\n     ▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#39c6c6\"},{\"text\":\"▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#46dbdb\"},{\"text\":\"▇            \",\"color\":\"#53ffff\"},{\"text\":\"▇\",\"color\":\"#51f5f5\"},{\"text\":\"▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#4ff0f0\"},{\"text\":\"▇\",\"color\":\"#55ffff\"},{\"text\":\"▇▇\\n      \",\"color\":\"#55fefe\"},{\"text\":\"▇▇ ▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#57ffff\"},{\"text\":\"▇▇        ▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#54ffff\"},{\"text\":\"▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\\n       \",\"color\":\"#51f7f7\"},{\"text\":\"▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#54ffff\"},{\"text\":\"▇\",\"color\":\"#57ffff\"},{\"text\":\"▇▇      \",\"color\":\"#55ffff\"},{\"text\":\"▇▇\",\"color\":\"#54fbfb\"},{\"text\":\"▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#4eebeb\"},{\"text\":\"▇▇\\n         \",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#59ffff\"},{\"text\":\"▇▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇    \",\"color\":\"#53fcfc\"},{\"text\":\"▇\",\"color\":\"#54fbfb\"},{\"text\":\"▇▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\\n              \",\"color\":\"#54fefe\"},{\"text\":\"▇▇\",\"color\":\"#55ffff\"},{\"text\":\"▇\",\"color\":\"#4eeeee\"},{\"text\":\"▇\",\"color\":\"#55ffff\"}]";
                //deadPlayerImg = "[" + "{text:'         ▇            ',color:'#40dfdf'},{text':'▇',color:'#3cd2d2'},{'text':'▇\n       ',color:'#55ffff'},{'text':'▇',color:'#56ffff'},{'text':'▇',color:'#54ffff'},{'text':'▇',color:'#42cccc'},{'text':'▇           ',color:'#56ffff'},{'text':'▇',color:'#54ffff'},{'text':'▇',color:'#4eeded'},{'text':'▇',color:'#53fafa'},{'text':'▇\n                      ',color:'#4ae3e3'},{'text':'▇  ',color:'#55ffff'},{'text':'▇',color:'#3abfbf'},{'text':'▇\n     ',color:'#55ffff'},{'text':'▇',color:'#55eeee'},{'text':'▇',color:'#55ffff'},{'text':'▇',color:'#56ffff'},{'text':'▇               ',color:'#55ffff'},{'text':'▇',color:'#44d9d9'},{'text':'▇ ',color:'#4ae9e9'},{'text':'▇▇\n   ',color:'#55ffff'},{'text':'▇▇',color:'#54ffff'},{'text':'▇',color:'#58ffff'},{'text':'▇',color:'#55ffff'},{'text':'▇     ',color:'#51f2f2'},{'text':'▇▇  ',color:'#55ffff'},{'text':'▇',color:'#40bfbf'},{'text':'▇',color:'#55ffff'},{'text':'▇▇    ',color:'#51ffff'},{'text':'▇',color:'#55ffff'},{'text':'▇',color:'#55eaea'},{'text':'▇▇',color:'#54ffff'},{'text':'▇\n   ',color:'#55ffff'},{'text':'▇▇',color:'#54ffff'},{'text':'▇',color:'#58ffff'},{'text':'▇',color:'#55ffff'},{'text':'▇     ',color:'#51f2f2'},{'text':'▇▇  ',color:'#55ffff'},{'text':'▇',color:'#40bfbf'},{'text':'▇',color:'#55ffff'},{'text':'▇▇    ',color:'#51ffff'},{'text':'▇',color:'#55ffff'},{'text':'▇',color:'#55eaea'},{'text':'▇▇',color:'#54ffff'},{'text':'▇\n  ▇',color:'#55ffff'},{'text':'▇▇',color:'#51f5f5'},{'text':'▇',color:'#57ffff'},{'text':'▇   ',color:'#55ffff'},{'text':'▇',color:'#54fbfb'},{'text':'▇▇',color:'#50f3f3'},{'text':'▇▇   ▇▇▇▇    ',color:'#55ffff'},{'text':'▇',color:'#44d7d7'},{'text':'▇▇▇\n   ',color:'#54ffff'},{'text':'▇▇',color:'#55ffff'},{'text':'▇   ',color:'#54ffff'},{'text':'▇',color:'#53fafa'},{'text':'▇',color:'#55ffff'},{'text':'▇▇',color:'#53f9f9'},{'text':'▇▇   ▇',color:'#55ffff'},{'text':'▇▇',color:'#4ef1f1'},{'text':'▇▇',color:'#55ffff'},{'text':'▇   ',color:'#35c4c4'},{'text':'▇▇',color:'#55ffff'},{'text':'▇\n ',color:'#52fbfb'},{'text':'▇      ▇▇▇▇▇▇▇   ▇▇▇▇▇▇',color:'#55ffff'},{'text':'▇      ',color:'#54ffff'},{'text':'▇\n ▇▇▇▇  ',color:'#55ffff'},{'text':'▇',color:'#4ef5f5'},{'text':'▇▇',color:'#55ffff'},{'text':'▇',color:'#53fafa'},{'text':'▇▇▇▇   ▇▇▇▇▇▇▇    ',color:'#55ffff'},{'text':'▇',color:'#52f9f9'},{'text':'▇▇\n',color:'#55ffff'},{'text':'▇',color:'#4cf0f0'},{'text':'▇▇    ▇',color:'#55ffff'},{'text':'▇',color:'#56ffff'},{'text':'▇▇▇▇▇▇   ▇▇▇',color:'#55ffff'},{'text':'▇',color:'#54fefe'},{'text':'▇▇▇',color:'#55ffff'},{'text':'▇    ',color:'#56ffff'},{'text':'▇▇\n▇',color:'#55ffff'},{'text':'▇',color:'#57ffff'},{'text':'▇   ',color:'#54ffff'},{'text':'▇',color:'#50ecec'},{'text':'▇▇',color:'#55ffff'},{'text':'▇',color:'#4ff3f3'},{'text':'▇',color:'#55ffff'},{'text':'▇▇',color:'#54fefe'},{'text':'▇',color:'#54ffff'},{'text':'▇   ▇▇▇▇▇▇▇▇    ',color:'#55ffff'},{'text':'▇',color:'#54ffff'},{'text':'▇\n',color:'#4de7e7'},{'text':'▇▇    ',color:'#55ffff'},{'text':'▇',color:'#4eefef'},{'text':'▇▇▇',color:'#55ffff'},{'text':'▇',color:'#54fdfd'},{'text':'▇▇▇▇   ▇',color:'#55ffff'},{'text':'▇▇',color:'#54ffff'},{'text':'▇▇▇▇▇    ▇▇\n',color:'#55ffff'},{'text':'▇▇    ',color:'#54ffff'},{'text':'▇▇',color:'#55ffff'},{'text':'▇',color:'#54fdfd'},{'text':'▇▇▇▇▇▇▇▇ ▇▇▇▇▇▇▇▇     ▇\n▇▇    ▇▇▇▇▇',color:'#55ffff'},{'text':'▇▇',color:'#54ffff'},{'text':'▇▇ ▇ ▇▇▇▇▇▇▇▇     ▇\n▇▇    ▇▇▇▇▇',color:'#55ffff'},{'text':'▇▇',color:'#54ffff'},{'text':'▇▇ ▇ ▇▇▇▇▇▇▇▇     ▇\n▇▇',color:'#55ffff'},{'text':'▇   ',color:'#60ffff'},{'text':'▇▇▇',color:'#55ffff'},{'text':'▇',color:'#49e4e4'},{'text':'▇▇▇',color:'#56ffff'},{'text':'▇▇▇▇',color:'#55ffff'},{'text':'▇',color:'#54ffff'},{'text':'▇▇▇',color:'#55ffff'},{'text':'▇',color:'#56ffff'},{'text':'▇',color:'#4ef1f1'},{'text':'▇',color:'#51f6f6'},{'text':'▇▇    ▇▇\n▇▇▇   ▇▇▇▇▇▇▇▇▇▇▇',color:'#55ffff'},{'text':'▇',color:'#54ffff'},{'text':'▇▇▇▇▇▇▇▇    ▇',color:'#55ffff'},{'text':'▇\n',color:'#56ffff'},{'text':'▇▇',color:'#55ffff'},{'text':'▇          ',color:'#56ffff'},{'text':'▇▇▇',color:'#55ffff'},{'text':'▇',color:'#56ffff'},{'text':'▇',color:'#54ffff'},{'text':'▇           ▇▇\n',color:'#55ffff'},{'text':'▇',color:'#54ffff'},{'text':'▇▇            ▇',color:'#55ffff'},{'text':'▇',color:'#56ffff'},{'text':'▇             ',color:'#54ffff'},{'text':'▇\n',color:'#56ffff'},{'text':'▇',color:'#54ffff'},{'text':'▇▇     ▇▇',color:'#55ffff'},{'text':'▇',color:'#4ef0f0'},{'text':'▇▇  ▇',color:'#55ffff'},{'text':'▇',color:'#56ffff'},{'text':'▇ ▇▇',color:'#55ffff'},{'text':'▇',color:'#3bd8d8'},{'text':'▇',color:'#3ed2d2'},{'text':'▇      ',color:'#55ffff'},{'text':'▇',color:'#54ffff'},{'text':'▇\n ▇▇      ▇▇▇▇▇▇▇',color:'#55ffff'},{'text':'▇',color:'#53fafa'},{'text':'▇',color:'#54ffff'},{'text':'▇▇▇▇▇',color:'#55ffff'},{'text':'▇      ',color:'#57ffff'},{'text':'▇▇\n ',color:'#55ffff'},{'text':'▇ ',color:'#54ffff'},{'text':'▇▇     ▇',color:'#55ffff'},{'text':'▇▇',color:'#54ffff'},{'text':'▇▇',color:'#55ffff'},{'text':'▇',color:'#4ae7e7'},{'text':'▇▇▇',color:'#55ffff'},{'text':'▇▇',color:'#53ffff'},{'text':'▇       ',color:'#55ffff'},{'text':'▇ ',color:'#41d0d0'},{'text':'▇\n  ',color:'#53ffff'},{'text':'▇▇▇',color:'#55ffff'},{'text':'▇       ',color:'#4cecec'},{'text':'▇▇▇▇▇▇',color:'#55ffff'},{'text':'▇▇      ',color:'#52ffff'},{'text':'▇▇',color:'#52f6f6'},{'text':'▇▇\n  ▇▇▇',color:'#55ffff'},{'text':'▇       ',color:'#4cecec'},{'text':'▇▇▇▇▇▇',color:'#55ffff'},{'text':'▇▇      ',color:'#52ffff'},{'text':'▇▇',color:'#52f6f6'},{'text':'▇▇\n  ',color:'#55ffff'},{'text':'▇',color:'#57ffff'},{'text':'▇▇',color:'#4ef1f1'},{'text':'▇',color:'#55ffff'},{'text':'▇      ',color:'#53ffff'},{'text':'▇',color:'#55ffff'},{'text':'▇',color:'#55fcfc'},{'text':'▇▇▇▇       ',color:'#55ffff'},{'text':'▇  ',color:'#54fdfd'},{'text':'▇',color:'#55ffff'},{'text':'▇\n   ',color:'#43d7d7'},{'text':'▇▇▇▇▇                 ▇▇▇▇▇\n     ▇',color:'#55ffff'},{'text':'▇',color:'#39c6c6'},{'text':'▇',color:'#55ffff'},{'text':'▇',color:'#46dbdb'},{'text':'▇            ',color:'#53ffff'},{'text':'▇',color:'#51f5f5'},{'text':'▇',color:'#55ffff'},{'text':'▇',color:'#4ff0f0'},{'text':'▇',color:'#55ffff'},{'text':'▇▇\n      ',color:'#55fefe'},{'text':'▇▇ ▇',color:'#55ffff'},{'text':'▇',color:'#57ffff'},{'text':'▇▇        ▇▇',color:'#55ffff'},{'text':'▇',color:'#54ffff'},{'text':'▇▇',color:'#55ffff'},{'text':'▇\n       ',color:'#51f7f7'},{'text':'▇▇',color:'#55ffff'},{'text':'▇',color:'#54ffff'},{'text':'▇',color:'#57ffff'},{'text':'▇▇      ',color:'#55ffff'},{'text':'▇▇',color:'#54fbfb'},{'text':'▇',color:'#55ffff'},{'text':'▇',color:'#4eebeb'},{'text':'▇▇\n         ',color:'#55ffff'},{'text':'▇',color:'#59ffff'},{'text':'▇▇▇',color:'#55ffff'},{'text':'▇    ',color:'#53fcfc'},{'text':'▇',color:'#54fbfb'},{'text':'▇▇▇',color:'#55ffff'},{'text':'▇\n              ',color:'#54fefe'},{'text':'▇▇',color:'#55ffff'},{'text':'▇',color:'#4eeeee'},{'text':'▇',color:'#55ffff'}]";


                bannerNBT = "{"
                        + "Glowing:1b,"
                        + "view_range:1024f,"
                        + "billboard:\"horizontal\","
                        + "text_opacity:0,"
                        + "background:0,"
                        + "Rotation:[0F, 0F],"
                        + "transformation:{left_rotation:[0f,0f,0f,1f],right_rotation:[0f,0f,0f,1f],translation:[0f,0f,0f],"
                        + "scale:[" + scale + "," + scale + "," + scale + "]},"
                        + "text:{\"color\":\"#55FFFF\",\"bold\":true," +
                        "\"text\":\"The Fallen\"}"
                        + "}";
                graphicNBT = "{"
                        + "Glowing:1b,"
                        + "view_range:1024f,"
                        + "billboard:\"horizontal\","
                        + "line_width:2147483647,"
                        + "text_opacity:0,"
                        + "background:0,"
                        + "alignment:\"left\","
                        + "Rotation:[0F, 0F],"
                        + "transformation:{left_rotation:[0f,0f,0f,1f],right_rotation:[0f,0f,0f,1f],translation:[0f,0f,0f],"
                        + "scale:[" + (scale/3) + "," + (scale/3 * 0.5555556) + "," + (scale/3) + "]},"
                        + "text:" + deadPlayerImg.replace("'", "\"")
                        + "}";
                defaultGraphicNBT = "{"
                        + "Glowing:1b,"
                        + "view_range:1024f,"
                        + "billboard:\"horizontal\","
                        + "line_width:2147483647,"
                        + "text_opacity:0,"
                        + "background:0,"
                        + "alignment:\"left\","
                        + "Rotation:[0F, 0F],"
                        + "transformation:{left_rotation:[0f,0f,0f,1f],right_rotation:[0f,0f,0f,1f],translation:[0f,0f,0f],"
                        + "scale:[" + (scale/3) + "," + (scale/3 * 0.5555556) + "," + (scale/3) + "]},"
                        + "text:" + deadPlayerImg.replace("'", "\"")
                        + "}";


            } else if (countdown > 0) {
                cleanupCurrentDisplays();

                deadPlayerName = deadPlayers.get(countdown - 1);

                bannerNBT = "{"
                        + "Glowing:1b,"
                        + "view_range:1024f,"
                        + "billboard:\"horizontal\","
                        + "text_opacity:0,"
                        + "background:0,"
                        + "Rotation:[0F, 0F],"
                        + "transformation:{left_rotation:[0f,0f,0f,1f],right_rotation:[0f,0f,0f,1f],translation:[0f,0f,0f],"
                        + "scale:[" + scale + "," + scale + "," + scale + "]},"
                        + "text:{\"color\":\"#55FFFF\",\"bold\":true," +
                        "\"text\":\"" + deadPlayerName + "\"}"
                        + "}";

                BufferedImage skinImage = null;
                try {
                    skinImage = fetchPlayerSkin(deadPlayerName);
                    deadPlayerImg = processSkin(skinImage);

                } catch (Exception e) {
                    Bukkit.getLogger().severe("Failed to display player head for " + deadPlayerName + ": " + e.getMessage());
                    throw new RuntimeException(e);
                }
                if (countdown == deadPlayers.size() + 1) {
                    graphicNBT = "{"
                            + "Glowing:1b,"
                            + "view_range:1024f,"
                            + "billboard:\"horizontal\","
                            + "line_width:2147483647,"
                            + "text_opacity:0,"
                            + "background:0,"
                            + "alignment:\"left\","
                            + "Rotation:[0F, 0F],"
                            + "transformation:{left_rotation:[0f,0f,0f,1f],right_rotation:[0f,0f,0f,1f],translation:[0f,0f,0f],"
                            + "scale:[" + (scale/3) + "," + (scale/3 * 0.5555556) + "," + (scale/3) + "]},"
                            + "text:" + deadPlayerImg.replace("'", "\"")
                            + "}";
                }else {
                    graphicNBT = "{"
                            + "Glowing:1b,"
                            + "view_range:1024f,"
                            + "billboard:\"horizontal\","
                            + "line_width:2147483647,"
                            + "text_opacity:0,"
                            + "background:0,"
                            + "alignment:\"left\","
                            + "Rotation:[0F, 0F],"
                            + "transformation:{left_rotation:[0f,0f,0f,1f],right_rotation:[0f,0f,0f,1f],translation:[0f,0f,0f],"
                            + "scale:[" + (scale/6) + "," + (scale/6) + "," + (scale/6) + "]},"
                            + "text:" + deadPlayerImg.replace("'", "\"")
                            + "}";
                }


            } else {
                cleanupAllDisplays();

                for (Player Eachplayer : Bukkit.getOnlinePlayers()) {
                    Eachplayer.stopSound(HGMusic);
                    Eachplayer.stopSound(HGMusicOG);
                }
                cancel();
                return;
            }
            if (bannerNBT != null) {

                for (Location loc : displayLocations) {
                    double x = loc.getX();
                    double y = loc.getY();
                    double z = loc.getZ();

                    String rotation = "[0F, 0F]";
                    if (z < arenaCenter.getZ()) {
                        rotation = "[0F, 0F]";
                    }else if (z > arenaCenter.getZ()) {
                        rotation = "[180F, 0F]";
                    }else if (x > arenaCenter.getX()) {
                        rotation = "[90F, 0F]";
                    }else if (x < arenaCenter.getX()) {
                        rotation = "[270F, 0F]";
                    }

                    if (deadPlayers.size() > 0) {
                        String rotatedBannerNBT = bannerNBT.replace(
                                "Rotation:[0F, 0F]",
                                "Rotation:" + rotation
                        );

                        Bukkit.dispatchCommand(
                                Bukkit.getConsoleSender(),
                                "summon minecraft:text_display " + x + " " + y + " " + z + " " + rotatedBannerNBT.replace("}", ",Tags:[\"" + bannerTag + "\"]}")
                        );
                    }

                    String graphicToUse = (countdown == deadPlayers.size() + 1) ? defaultGraphicNBT : graphicNBT;
                    String rotatedGraphicNBT = graphicToUse.replace(
                            "Rotation:[0F, 0F]",
                            "Rotation:" + rotation
                    );
                    Bukkit.dispatchCommand(
                            Bukkit.getConsoleSender(),
                            "summon minecraft:text_display " + x + " " + (y + 15) + " " + (z + 15) + " " + rotatedGraphicNBT.replace("}", ",Tags:[\"" + graphicTag + "\"]}")
                    );
                }
                scheduleFade(bannerTag);
                scheduleFade(graphicTag);
            }
        }catch (Exception e) {
            Bukkit.getLogger().severe("FallenDisplayTask error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            countdown--;
        }
    }

    private void cleanupCurrentDisplays() {
        Bukkit.dispatchCommand(
                Bukkit.getConsoleSender(),
                "kill @e[type=minecraft:text_display,tag=" + bannerTag + "]"
        );
        Bukkit.dispatchCommand(
                Bukkit.getConsoleSender(),
                "kill @e[type=minecraft:text_display,tag=" + graphicTag + "]"
        );
    }

    private void cleanupAllDisplays() {
        cleanupCurrentDisplays();
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

        /*for (int y = 0; y < 29; y++) {
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
         */
        StringBuilder sb = new StringBuilder("[");
        for (int yy = 0; yy < 29; yy++) {
            for (int xx = 0; xx < 29; xx++) {
                Color c = new Color(scaledHead.getRGB(xx, yy), true);
                int gray = (c.getRed() + c.getGreen() + c.getBlue()) / 3;
                int idx = Math.min(gray / 26, 9);
                String hex = String.format("#%02x%02x%02x", aquaShades[idx].getRed(), aquaShades[idx].getGreen(), aquaShades[idx].getBlue());
                sb.append("{text:\"█\",color:\"").append(hex).append("\"}");
                if (xx < 28) sb.append(",");
            }
            if (yy < 28) sb.append(",{text:\"\\n\"},");
        }
        sb.append("]");
        return sb.toString();
    }
    private void scheduleFade(String tag) {
        // FADE-IN
        for (int t = 0; t <= fade_ticks; t++) {
            final int alphaByte = Math.round(255 * (t / (float) fade_ticks));
            Bukkit.getScheduler().runTaskLater(JoTHungerGames.getInstance(), () ->
                            Bukkit.dispatchCommand(
                                    Bukkit.getConsoleSender(),
                                    "execute as @e[type=minecraft:text_display,tag=" + tag + "] run data merge entity @s {text_opacity:" + alphaByte + "}"

                            ),
                    t);
        }

        // FADE-OUT
        long fadeStart = displayDuration - fade_ticks;
        for (int t = 0; t <= fade_ticks; t++) {
            final int alphaByte = Math.round(255 * (1f - t / (float) fade_ticks));
            Bukkit.getScheduler().runTaskLater(JoTHungerGames.getInstance(), () ->
                            Bukkit.dispatchCommand(
                                    Bukkit.getConsoleSender(),
                                    "execute as @e[type=minecraft:text_display,tag=" + tag + "] run data merge entity @s {text_opacity:" + alphaByte + "}"

                            ),
                    fadeStart + t);
        }
    }
}
