package com.tracelink.prodsec.plugin.veracode.sast.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tracelink.prodsec.plugin.veracode.sast.model.VeracodeSastReportModel;

/**
 * Handles DB operations for the {@linkplain VeracodeSastReportModel}
 * 
 * @author csmith
 *
 */
@Repository
public interface VeracodeSastReportRepository extends JpaRepository<VeracodeSastReportModel, Long> {

	VeracodeSastReportModel findByAnalysisIdAndBuildId(long analysisId, long buildId);

}
