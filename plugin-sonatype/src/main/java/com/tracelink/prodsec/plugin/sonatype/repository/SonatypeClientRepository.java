package com.tracelink.prodsec.plugin.sonatype.repository;

import com.tracelink.prodsec.plugin.sonatype.model.SonatypeClient;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for DB integration with the {@link SonatypeClient}.
 *
 * @author mcool
 */
@Repository
public interface SonatypeClientRepository extends JpaRepository<SonatypeClient, Long> {
}
