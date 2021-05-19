package com.tracelink.prodsec.plugin.veracode.sast.service;

import com.tracelink.prodsec.plugin.veracode.sast.model.VeracodeSastAppModel;
import com.tracelink.prodsec.plugin.veracode.sast.model.VeracodeSastFlawModel;
import com.tracelink.prodsec.plugin.veracode.sast.model.VeracodeSastReportModel;
import com.tracelink.prodsec.plugin.veracode.sast.repository.VeracodeSastReportRepository;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Handles business logic for saving and retrieving reports
 *
 * @author csmith
 */
@Service
public class VeracodeSastReportService {

	private final VeracodeSastReportRepository reportRepo;
	private final VeracodeSastFlawService flawService;

	public VeracodeSastReportService(@Autowired VeracodeSastReportRepository reportRepo,
			@Autowired VeracodeSastFlawService flawService) {
		this.reportRepo = reportRepo;
		this.flawService = flawService;
	}

	// Report Methods

	/**
	 * Saves the given report in the {@link VeracodeSastReportRepository}.
	 *
	 * @param report the report to save
	 * @return the updated report
	 */
	public VeracodeSastReportModel save(VeracodeSastReportModel report) {
		return reportRepo.saveAndFlush(report);
	}

	public Optional<VeracodeSastReportModel> getReportById(long reportId) {
		return reportRepo.findById(reportId);
	}

	public VeracodeSastReportModel getReportForAnalysisAndBuild(long analysisId, long buildId) {
		return reportRepo.findByAnalysisIdAndBuildId(analysisId, buildId);
	}

	// Data management methods

	/**
	 * Deletes any {@link VeracodeSastReportModel} associated with the given {@link
	 * VeracodeSastAppModel}. Also deletes any {@link VeracodeSastFlawModel} that is associated with
	 * the reports to avoid orphaned flaws.
	 *
	 * @param app the app for which to delete all associated reports
	 * @throws IllegalArgumentException if the app is null
	 */
	public void deleteReportsByApp(VeracodeSastAppModel app)
			throws IllegalArgumentException {
		// Make sure the app is not null
		if (app == null) {
			throw new IllegalArgumentException("Cannot delete reports for a null app");
		}
		// Iterate through pages of reports
		Page<VeracodeSastReportModel> reportsPage = null;
		do {
			Pageable pageRequest = (reportsPage == null) ? PageRequest.of(0, 100)
					: reportsPage.nextPageable();
			reportsPage = reportRepo.findAllByApp(app, pageRequest);
			// Delete all flaws associated with these apps
			reportsPage.forEach(flawService::deleteFlawsByReport);
		} while (reportsPage.hasNext());
		// Delete all the reports
		reportRepo.deleteByApp(app);
		// Flush before returning
		reportRepo.flush();
	}
}
