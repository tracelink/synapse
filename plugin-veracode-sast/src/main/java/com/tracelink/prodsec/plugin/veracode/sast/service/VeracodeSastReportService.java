package com.tracelink.prodsec.plugin.veracode.sast.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tracelink.prodsec.plugin.veracode.sast.model.VeracodeSastAppModel;
import com.tracelink.prodsec.plugin.veracode.sast.model.VeracodeSastReportModel;
import com.tracelink.prodsec.plugin.veracode.sast.repository.VeracodeSastReportRepository;

/**
 * Handles business logic for saving and retrieving reports
 *
 * @author csmith
 */
@Service
public class VeracodeSastReportService {

	private final VeracodeSastReportRepository reportRepo;

	public VeracodeSastReportService(@Autowired VeracodeSastReportRepository reportRepo) {
		this.reportRepo = reportRepo;
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
	 * Deletes any {@link VeracodeSastReportModel} associated with the given
	 * {@link VeracodeSastAppModel}. Also deletes any {@link VeracodeSastFlawModel}
	 * that is associated with the reports to avoid orphaned flaws.
	 *
	 * @param app the app for which to delete all associated reports
	 * @throws IllegalArgumentException if the app is null
	 */
	public void deleteReportsByApp(VeracodeSastAppModel app) throws IllegalArgumentException {
		// Make sure the app is not null
		if (app == null) {
			throw new IllegalArgumentException("Cannot delete reports for a null app");
		}
		// Delete all the reports
		reportRepo.deleteByApp(app);
		// Flush before returning
		reportRepo.flush();
	}
}
