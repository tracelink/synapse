package com.tracelink.prodsec.plugin.veracode.dast.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tracelink.prodsec.plugin.veracode.dast.model.VeracodeDastThresholdModel;
import com.tracelink.prodsec.plugin.veracode.dast.repository.VeracodeDastThresholdsRepository;

/**
 * Handles business logic for getting and setting threshold levels for the
 * Scorecard
 * 
 * @author csmith
 *
 */
@Service
public class VeracodeDastThresholdsService {
	private final VeracodeDastThresholdsRepository thresholdsRepository;

	public VeracodeDastThresholdsService(@Autowired VeracodeDastThresholdsRepository thresholdsRepository) {
		this.thresholdsRepository = thresholdsRepository;
	}

	public VeracodeDastThresholdModel getThresholds() {
		List<VeracodeDastThresholdModel> thresholds = thresholdsRepository.findAll();
		if (thresholds.isEmpty()) {
			return null;
		}
		return thresholds.get(0);
	}

	public VeracodeDastThresholdModel setThresholds(int greenYellow, int yellowRed) {
		VeracodeDastThresholdModel threshold = getThresholds();
		if (threshold == null) {
			threshold = new VeracodeDastThresholdModel();
		}
		threshold.setGreenYellow(greenYellow);
		threshold.setYellowRed(yellowRed);
		return thresholdsRepository.saveAndFlush(threshold);
	}
}
