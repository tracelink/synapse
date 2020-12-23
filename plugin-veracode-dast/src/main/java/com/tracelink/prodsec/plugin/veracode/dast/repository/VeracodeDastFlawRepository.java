package com.tracelink.prodsec.plugin.veracode.dast.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tracelink.prodsec.plugin.veracode.dast.model.VeracodeDastFlawModel;

/**
 * Handles DB operations for the {@linkplain VeracodeDastFlawModel}
 * 
 * @author csmith
 *
 */
@Repository
public interface VeracodeDastFlawRepository extends JpaRepository<VeracodeDastFlawModel, Long> {

	VeracodeDastFlawModel findByAnalysisIdAndIssueId(long analysisId, long issueId);

}
