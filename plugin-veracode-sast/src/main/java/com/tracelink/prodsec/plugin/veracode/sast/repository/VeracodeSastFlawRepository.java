package com.tracelink.prodsec.plugin.veracode.sast.repository;

import com.tracelink.prodsec.plugin.veracode.sast.model.VeracodeSastFlawModel;
import com.tracelink.prodsec.plugin.veracode.sast.model.VeracodeSastReportModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles DB operations for the {@linkplain VeracodeSastFlawModel}
 *
 * @author csmith
 */
@Repository
public interface VeracodeSastFlawRepository extends JpaRepository<VeracodeSastFlawModel, Long> {

	VeracodeSastFlawModel findByAnalysisIdAndIssueId(long analysisId, long issueId);

	/**
	 * Deletes all flaws associated with the given {@link VeracodeSastReportModel}.
	 *
	 * @param report the report for which to delete all flaws
	 */
	@Transactional
	void deleteByReport(VeracodeSastReportModel report);
}
