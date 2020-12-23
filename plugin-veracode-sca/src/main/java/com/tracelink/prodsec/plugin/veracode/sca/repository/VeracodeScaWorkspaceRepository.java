package com.tracelink.prodsec.plugin.veracode.sca.repository;

import com.tracelink.prodsec.plugin.veracode.sca.model.VeracodeScaWorkspace;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for DB integration with the {@link VeracodeScaWorkspace}.
 *
 * @author mcool
 */
@Repository
public interface VeracodeScaWorkspaceRepository extends JpaRepository<VeracodeScaWorkspace, UUID> {

	/**
	 * Gets the {@link VeracodeScaWorkspace} with the given name.
	 *
	 * @param name the name to search by
	 * @return the Veracode SCA workspace with the given name, or null
	 */
	VeracodeScaWorkspace findByName(String name);
}
