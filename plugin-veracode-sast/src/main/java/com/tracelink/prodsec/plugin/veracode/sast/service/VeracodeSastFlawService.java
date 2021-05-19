package com.tracelink.prodsec.plugin.veracode.sast.service;

import com.tracelink.prodsec.plugin.veracode.sast.model.VeracodeSastFlawModel;
import com.tracelink.prodsec.plugin.veracode.sast.model.VeracodeSastReportModel;
import com.tracelink.prodsec.plugin.veracode.sast.repository.VeracodeSastFlawRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Handles business logic for storing flaws
 *
 * @author csmith
 */
@Service
public class VeracodeSastFlawService {

	private final VeracodeSastFlawRepository flawRepo;

	public VeracodeSastFlawService(@Autowired VeracodeSastFlawRepository flawRepo) {
		this.flawRepo = flawRepo;
	}

	/**
	 * Saves all flaws in the given list to the {@link VeracodeSastFlawRepository}.
	 *
	 * @param flawModels flaws to save
	 */
	public void saveFlaws(List<VeracodeSastFlawModel> flawModels) {
		flawRepo.saveAll(flawModels);
		flawRepo.flush();
	}

	public VeracodeSastFlawModel getFlawForIssueId(long analysisId, long issueId) {
		return flawRepo.findByAnalysisIdAndIssueId(analysisId, issueId);
	}

	/**
	 * Deletes any {@link VeracodeSastFlawModel} associated with the given report.
	 *
	 * @param report the report for which to delete all associated flaws
	 * @throws IllegalArgumentException if the report is null
	 */
	public void deleteFlawsByReport(VeracodeSastReportModel report) {
		// Make sure report is not null
		if (report == null) {
			throw new IllegalArgumentException("Cannot delete flaws for a null report");
		}
		// Delete all flaws with the given report
		flawRepo.deleteByReport(report);
		// Flush before returning
		flawRepo.flush();
	}
}
