package com.tracelink.prodsec.plugin.veracode.dast.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tracelink.prodsec.plugin.veracode.dast.model.VeracodeDastReportModel;

/**
 * Handles DB operations for the {@linkplain VeracodeDastReportModel}
 * 
 * @author csmith
 *
 */
@Repository
public interface VeracodeDastReportRepository extends JpaRepository<VeracodeDastReportModel, Long> {

	VeracodeDastReportModel findByAnalysisIdAndBuildId(long analysisId, long buildId);

}
