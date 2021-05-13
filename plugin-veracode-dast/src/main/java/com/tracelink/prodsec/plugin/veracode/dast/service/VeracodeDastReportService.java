package com.tracelink.prodsec.plugin.veracode.dast.service;

import com.tracelink.prodsec.plugin.veracode.dast.model.VeracodeDastReportModel;
import com.tracelink.prodsec.plugin.veracode.dast.repository.VeracodeDastReportRepository;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Handles business logic for saving and retrieving reports
 *
 * @author csmith
 */
@Service
public class VeracodeDastReportService {

	private final VeracodeDastReportRepository reportRepo;

	public VeracodeDastReportService(@Autowired VeracodeDastReportRepository reportRepo) {
		this.reportRepo = reportRepo;
	}

	// Report Methods

	/**
	 * Saves the given report in the {@link VeracodeDastReportRepository}.
	 *
	 * @param report the report to save
	 * @return the updated report
	 */
	public VeracodeDastReportModel save(VeracodeDastReportModel report) {
		return reportRepo.saveAndFlush(report);
	}

	public Optional<VeracodeDastReportModel> getReportById(long reportId) {
		return reportRepo.findById(reportId);
	}

	public VeracodeDastReportModel getReportForAnalysisAndBuild(long analysisId, long buildId) {
		return reportRepo.findByAnalysisIdAndBuildId(analysisId, buildId);
	}
}
