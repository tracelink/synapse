package com.tracelink.prodsec.plugin.veracode.sast.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tracelink.prodsec.plugin.veracode.sast.model.VeracodeSastFlawModel;

/**
 * Handles DB operations for the {@linkplain VeracodeSastFlawModel}
 * 
 * @author csmith
 *
 */
@Repository
public interface VeracodeSastFlawRepository extends JpaRepository<VeracodeSastFlawModel, Long> {

	VeracodeSastFlawModel findByAnalysisIdAndIssueId(long analysisId, long issueId);

}
