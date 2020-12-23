package com.tracelink.prodsec.plugin.sonatype.service;

import com.tracelink.prodsec.plugin.sonatype.model.SonatypeApp;
import com.tracelink.prodsec.plugin.sonatype.model.SonatypeMetrics;
import com.tracelink.prodsec.plugin.sonatype.repository.SonatypeMetricsRepository;
import com.tracelink.prodsec.plugin.sonatype.util.ThreatLevel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;

/**
 * Service to store and retrieve data about Sonatype metrics from the {@link
 * SonatypeMetricsRepository}.
 */
@Service
public class SonatypeMetricsService {
    private final SonatypeMetricsRepository metricsRepository;
    private LocalDate earliestMetricsDate;

    public SonatypeMetricsService(@Autowired SonatypeMetricsRepository metricsRepository) {
        this.metricsRepository = metricsRepository;
    }

    /**
     * Gets the earliest recorded date for any {@link SonatypeMetrics} in the
     * repository. If there are no metrics, returns the current date. Caches
     * the value after the first retrieval.
     *
     * @return earliest recorded date for any metrics in the database
     */
    public LocalDate getEarliestMetricsDate() {
        if (earliestMetricsDate != null) {
            return earliestMetricsDate;
        }

        SonatypeMetrics metrics = metricsRepository.findFirstByOrderByRecordedDateAsc();
        if (metrics == null) {
            return LocalDate.now();
        }
        earliestMetricsDate = metrics.getRecordedDate();
        return earliestMetricsDate;
    }

    /**
     * Stores the given violations data in a {@link SonatypeMetrics} entity in
     * the database. If there already exists a metrics in the database
     * associated with the given {@link SonatypeApp} and with today's recorded
     * date, it overwrites the violation data. Otherwise, it creates a new
     * metrics object.
     *
     * @param app          the Sonatype app associated with the violations
     * @param threatLevels map containing violations data for various threat
     *                     levels
     */
    public void storeMetrics(SonatypeApp app, Map<ThreatLevel, Integer> threatLevels) {
        // Get most recent metrics from the database for this app
        SonatypeMetrics metrics = metricsRepository.findFirstByAppOrderByRecordedDateDesc(app);
        // If no metrics available or metrics is older than the start of today, create a
        // new one
        if (metrics == null || metrics.getRecordedDate().isBefore(LocalDate.now())) {
            metrics = new SonatypeMetrics();
            metrics.setApp(app);
            metrics.setRecordedDate(LocalDate.now());
        }
        // Set all values for metrics
        metrics.setHighVios(threatLevels.getOrDefault(ThreatLevel.HIGH, 0));
        metrics.setMedVios(threatLevels.getOrDefault(ThreatLevel.MEDIUM, 0));
        metrics.setLowVios(threatLevels.getOrDefault(ThreatLevel.LOW, 0));
        metrics.setInfoVios(threatLevels.getOrDefault(ThreatLevel.INFO, 0));

        metricsRepository.saveAndFlush(metrics);
    }
}
