package com.tracelink.prodsec.plugin.veracode.sast.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tracelink.prodsec.plugin.veracode.sast.model.VeracodeSastFlawModel;
import com.tracelink.prodsec.plugin.veracode.sast.repository.VeracodeSastFlawRepository;

/**
 * Handles business logic for storing flaws
 * 
 * @author csmith
 *
 */
@Service
public class VeracodeSastFlawService {

	private final VeracodeSastFlawRepository flawRepo;

	public VeracodeSastFlawService(@Autowired VeracodeSastFlawRepository flawRepo) {
		this.flawRepo = flawRepo;
	}

	public void saveFlaws(List<VeracodeSastFlawModel> flawModels) {
		flawRepo.saveAll(flawModels);
		flawRepo.flush();
	}

	public VeracodeSastFlawModel getFlawForIssueId(long analysisId, long issueId) {
		return flawRepo.findByAnalysisIdAndIssueId(analysisId, issueId);
	}

}
