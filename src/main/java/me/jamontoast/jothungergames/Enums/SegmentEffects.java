package me.jamontoast.jothungergames.Enums;

import me.jamontoast.jothungergames.Effects.*;
import me.jamontoast.jothungergames.Interfaces.SegmentEffectStrategy;
import org.bukkit.Material;

import java.util.Collections;
import java.util.List;

public enum SegmentEffects {

    BEAST(
            "Spawns a beast that is limited to the segment",
            Collections.emptyList(),
            new BeastEffect()
    ),
    BLOOD_RAIN(
            "Causes a blood rain that blinds all players caught in it.",
            Collections.emptyList(),
            new BloodRainEffect()
    ),
    EXTREME_COLD(
            "Lowers the temperature in the segment, causing players to start freezing.",
            Collections.emptyList(),
            new ExtremeColdEffect()
    ),
    FIRESTORM(
            "Burns all flammable materials",
            Collections.emptyList(),
            new FirestormEffect()
    ),
    INSECTS(
            "Spawns deadly insects limited to the segment.",
            Collections.emptyList(),
            new InsectsEffect()
    ),
    JABBERJAYS(
            "Plays loud, distressing sounds to all players within the segment. Transparent barrier trapping all players inside may be added later.",
            Collections.emptyList(),
            new JabberjaysEffect()
    ),
    LIGHTNING(
            "Lightning strikes randomly within the segment. It is recommended to turn FireTick off.",
            Collections.emptyList(),
            new LightningEffect()
    ),
    PARALYSING_FOG(
            "Creates a paralyzing fog that slows and poisons players.",
            Collections.emptyList(),
            new ParalysingFogEffect()
    ),
    POISON_FLOWERS(
            "Giant flowers appear that release a deadly poison.",
            Collections.emptyList(),
            new PoisonFlowersEffect()
    ),
    SPIDERS(
            "Spiders.",
            Collections.emptyList(),
            new SpidersEffect()
    ),
    WASPS(
            "Spawns enraged wasps limited to the segment.",
            Collections.emptyList(),
            new WaspsEffect()
    ),
    WAVE(
            "A massive wave fills entire segment.",
            Collections.emptyList(),
            new WaveEffect()
    );
    private final String description;
    private final List<Material> affectedBlocks;
    private final SegmentEffectStrategy strategy;

    SegmentEffects(String description, List<Material> affectedBlocks, SegmentEffectStrategy strategy){
        this.affectedBlocks = affectedBlocks;
        this.description = description;
        this.strategy = strategy;
    }

    public List<Material> getAffectedBlocks() {
        return affectedBlocks;
    }
    public String getDescription() {
        return description;
    }
    public SegmentEffectStrategy getStrategy() {
        return strategy;
    }

}
