package com.tracelink.prodsec.plugin.veracode.sca.repository;

import com.tracelink.prodsec.plugin.veracode.sca.model.VeracodeScaClient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for DB integration with the {@link VeracodeScaClient}.
 *
 * @author mcool
 */
@Repository
public interface VeracodeScaClientRepository extends JpaRepository<VeracodeScaClient, Long> {
}
