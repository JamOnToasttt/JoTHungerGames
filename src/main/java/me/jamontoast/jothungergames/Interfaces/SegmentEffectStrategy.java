package me.jamontoast.jothungergames.Interfaces;

public interface SegmentEffectStrategy {
    void start(String segmentGroup, int segmentNumber, Long durationOverride);
    void stop(String segmentGroup, int segmentNumber);
}
