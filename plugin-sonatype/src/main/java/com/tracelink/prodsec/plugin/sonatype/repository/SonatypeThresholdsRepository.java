package com.tracelink.prodsec.plugin.sonatype.repository;

import com.tracelink.prodsec.plugin.sonatype.model.SonatypeThresholds;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for DB integration with the {@link SonatypeThresholds}.
 *
 * @author mcool
 */
@Repository
public interface SonatypeThresholdsRepository extends JpaRepository<SonatypeThresholds, Long> {
}
