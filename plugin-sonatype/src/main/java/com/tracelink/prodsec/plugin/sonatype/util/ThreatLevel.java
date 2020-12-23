package com.tracelink.prodsec.plugin.sonatype.util;

import com.tracelink.prodsec.plugin.sonatype.model.SonatypeMetrics;

import java.util.function.ToLongFunction;

/**
 * Represents the severity of a Sonatype violation, broken into human-readable
 * threat levels of high, medium, low and info.
 */
public enum ThreatLevel {
    HIGH("High", 8, 10, SonatypeMetrics::getHighVios, 25), MEDIUM("Medium", 4, 7, SonatypeMetrics::getMedVios, 5),
    LOW("Low", 2, 3, SonatypeMetrics::getLowVios, 2), INFO("Informational", 0, 1, SonatypeMetrics::getInfoVios, 0.5);

    private final String name;
    private final int low;
    private final int high;
    private final ToLongFunction<SonatypeMetrics> viosCallback;
    /**
     * Weight is used to calculate the risk score and can be adjusted
     */
    private final double weight;

    ThreatLevel(String name, int low, int high, ToLongFunction<SonatypeMetrics> viosCallback, double weight) {
        this.name = name;
        this.low = low;
        this.high = high;
        this.viosCallback = viosCallback;
        this.weight = weight;
    }

    public String getName() {
        return name;
    }

    public double getWeight() {
        return weight;
    }

    public ToLongFunction<SonatypeMetrics> getViosCallback() {
        return viosCallback;
    }

    public static ThreatLevel forLevel(int level) {
        for (ThreatLevel tl : ThreatLevel.values()) {
            if (tl.low <= level && tl.high >= level) {
                return tl;
            }
        }
        return null;
    }
}
