package com.tracelink.prodsec.plugin.veracode.dast.service;

import com.tracelink.prodsec.plugin.veracode.dast.model.VeracodeDastFlawModel;
import com.tracelink.prodsec.plugin.veracode.dast.repository.VeracodeDastFlawRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Handles business logic for storing flaws
 *
 * @author csmith
 */
@Service
public class VeracodeDastFlawService {

	private final VeracodeDastFlawRepository flawRepo;

	public VeracodeDastFlawService(@Autowired VeracodeDastFlawRepository flawRepo) {
		this.flawRepo = flawRepo;
	}

	/**
	 * Saves all flaws in the given list to the {@link VeracodeDastFlawRepository}.
	 *
	 * @param flawModels flaws to save
	 */
	public void saveFlaws(List<VeracodeDastFlawModel> flawModels) {
		flawRepo.saveAll(flawModels);
		flawRepo.flush();
	}

	public VeracodeDastFlawModel getFlawForIssueId(long analysisId, long issueId) {
		return flawRepo.findByAnalysisIdAndIssueId(analysisId, issueId);
	}

}
