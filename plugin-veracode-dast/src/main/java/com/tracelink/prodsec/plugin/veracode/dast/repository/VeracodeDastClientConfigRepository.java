package com.tracelink.prodsec.plugin.veracode.dast.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tracelink.prodsec.plugin.veracode.dast.model.VeracodeDastClientConfigModel;

/**
 * Handles DB operations for the {@linkplain VeracodeDastClientConfigModel}
 * 
 * @author csmith
 *
 */
@Repository
public interface VeracodeDastClientConfigRepository extends JpaRepository<VeracodeDastClientConfigModel, Long> {

}
