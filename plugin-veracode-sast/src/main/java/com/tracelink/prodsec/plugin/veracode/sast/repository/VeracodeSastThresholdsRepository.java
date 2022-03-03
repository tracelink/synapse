package com.tracelink.prodsec.plugin.veracode.sast.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tracelink.prodsec.plugin.veracode.sast.model.VeracodeSastThresholdModel;

/**
 * Handles DB operations for the {@linkplain VeracodeSastThresholdModel}
 * @author csmith
 *
 */
@Repository
public interface VeracodeSastThresholdsRepository extends JpaRepository<VeracodeSastThresholdModel, Long> {

}
