package com.tracelink.prodsec.plugin.veracode.sca.repository;

import com.tracelink.prodsec.plugin.veracode.sca.model.VeracodeScaThresholds;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for DB integration with the {@link VeracodeScaThresholds}.
 *
 * @author mcool
 */
@Repository
public interface VeracodeScaThresholdsRepository extends
	JpaRepository<VeracodeScaThresholds, Long> {

}
