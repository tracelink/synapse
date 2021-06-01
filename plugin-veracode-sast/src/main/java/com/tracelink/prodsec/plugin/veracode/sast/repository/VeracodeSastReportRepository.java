package com.tracelink.prodsec.plugin.veracode.sast.repository;

import com.tracelink.prodsec.plugin.veracode.sast.model.VeracodeSastAppModel;
import com.tracelink.prodsec.plugin.veracode.sast.model.VeracodeSastReportModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles DB operations for the {@linkplain VeracodeSastReportModel}
 *
 * @author csmith
 */
@Repository
public interface VeracodeSastReportRepository extends JpaRepository<VeracodeSastReportModel, Long> {

	VeracodeSastReportModel findByAnalysisIdAndBuildId(long analysisId, long buildId);

	/**
	 * Gets a page of {@link VeracodeSastReportModel}s that are associated with the given {@link
	 * VeracodeSastAppModel}.
	 *
	 * @param app      app for which to get reports
	 * @param pageable the page information for the database request
	 * @return page of reports associated with the given app
	 */
	Page<VeracodeSastReportModel> findAllByApp(VeracodeSastAppModel app, Pageable pageable);

	/**
	 * Deletes all {@link VeracodeSastReportModel}s that are associated with the given {@link
	 * VeracodeSastAppModel}.
	 *
	 * @param app app for which to delete reports
	 */
	@Transactional
	void deleteByApp(VeracodeSastAppModel app);
}
