package com.tracelink.prodsec.plugin.sonatype.repository;

import com.tracelink.prodsec.plugin.sonatype.model.SonatypeApp;
import com.tracelink.prodsec.plugin.sonatype.model.SonatypeMetrics;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for DB integration with the {@link SonatypeMetrics}.
 *
 * @author mcool
 */
@Repository
public interface SonatypeMetricsRepository extends JpaRepository<SonatypeMetrics, Long> {
    /**
     * Gets the {@link SonatypeMetrics} with the earliest recorded date.
     *
     * @return the Sonatype app with the earliest recorded date, or null
     */
    SonatypeMetrics findFirstByOrderByRecordedDateAsc();

    /**
     * Gets the {@link SonatypeMetrics} with the most recent recorded date for
     * the given {@link SonatypeApp}.
     *
     * @param app the Sonatype app to search by
     * @return the most recent Sonatype metrics for the Sonatype app, or null
     */
    SonatypeMetrics findFirstByAppOrderByRecordedDateDesc(SonatypeApp app);
}
