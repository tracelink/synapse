package com.tracelink.prodsec.plugin.veracode.sca.repository;

import com.tracelink.prodsec.plugin.veracode.sca.model.VeracodeScaIssue;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for DB integration with the {@link VeracodeScaIssue}.
 *
 * @author mcool
 */
@Repository
public interface VeracodeScaIssueRepository extends JpaRepository<VeracodeScaIssue, UUID> {

	/**
	 * Gets the {@link VeracodeScaIssue} with the earliest recorded date.
	 *
	 * @return the Veracode SCA project with the earliest recorded date, or null
	 */
	VeracodeScaIssue findFirstByOrderByCreatedDateAsc();
}
