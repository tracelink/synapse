package com.tracelink.prodsec.plugin.veracode.dast.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tracelink.prodsec.plugin.veracode.dast.model.VeracodeDastThresholdModel;

/**
 * Handles DB operations for the {@linkplain VeracodeDastThresholdModel}
 * 
 * @author csmith
 *
 */
@Repository
public interface VeracodeDastThresholdsRepository extends JpaRepository<VeracodeDastThresholdModel, Long> {

}
