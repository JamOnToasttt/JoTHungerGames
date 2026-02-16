package me.jamontoast.jothungergames.Utilities;

import me.jamontoast.jothungergames.Interfaces.SegmentEffectStrategy;

import java.util.HashMap;
import java.util.Map;

public class SegmentEffectManager {

    private Map<String, SegmentEffectStrategy> activeEffects = new HashMap<>();

    public void registerEffect(String effectName, SegmentEffectStrategy effectStrategy) {
        activeEffects.put(effectName, effectStrategy);
    }

    public void startEffect(String effectName, String segmentGroup, int segmentNumber, Long durationOverride) {
        if (activeEffects.containsKey(effectName)) {
            activeEffects.get(effectName).start(segmentGroup, segmentNumber, durationOverride);
        }
    }

    public void stopEffect(String effectName, String segmentGroup, int segmentNumber) {
        if (activeEffects.containsKey(effectName)) {
            activeEffects.get(effectName).stop(segmentGroup, segmentNumber);
        }
    }
}
