package com.tracelink.prodsec.plugin.sonatype.service;

import com.tracelink.prodsec.plugin.sonatype.exception.SonatypeThresholdsException;
import com.tracelink.prodsec.plugin.sonatype.model.SonatypeThresholds;
import com.tracelink.prodsec.plugin.sonatype.repository.SonatypeThresholdsRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service to store and retrieve data about Sonatype risk thresholds from the {@link
 * SonatypeThresholdsRepository}.
 */
@Service
public class SonatypeThresholdsService {
    private final SonatypeThresholdsRepository thresholdsRepository;

    public SonatypeThresholdsService(@Autowired SonatypeThresholdsRepository thresholdsRepository) {
        this.thresholdsRepository = thresholdsRepository;
    }

    /**
     * Gets the configured Sonatype risk thresholds from the database.
     *
     * @return the configured risk thresholds
     * @throws SonatypeThresholdsException if no thresholds are configured
     */
    public SonatypeThresholds getThresholds() throws SonatypeThresholdsException {
        List<SonatypeThresholds> thresholds = thresholdsRepository.findAll();
        if (thresholds.isEmpty()) {
            throw new SonatypeThresholdsException("No Sonatype thresholds configured.");
        }
        return thresholds.get(0);
    }

    /**
     * Sets the values of the Sonatype risk thresholds in the database. If no
     * risk thresholds are currently configured, it will create a new entity.
     * Otherwise, it will update the existing entity.
     *
     * @param greenYellow the threshold between green and yellow traffic lights
     * @param yellowRed   the threshold between yellow and red traffic lights
     */
    public void setThresholds(long greenYellow, long yellowRed) {
        List<SonatypeThresholds> thresholds = thresholdsRepository.findAll();
        SonatypeThresholds threshold;
        if (thresholds.isEmpty()) {
            threshold = new SonatypeThresholds();
        } else {
            threshold = thresholds.get(0);
        }
        threshold.setGreenYellow(greenYellow);
        threshold.setYellowRed(yellowRed);
        thresholdsRepository.saveAndFlush(threshold);
    }
}
