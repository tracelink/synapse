package com.tracelink.prodsec.plugin.veracode.sast.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tracelink.prodsec.plugin.veracode.sast.model.VeracodeSastThresholdModel;
import com.tracelink.prodsec.plugin.veracode.sast.repository.VeracodeSastThresholdsRepository;

/**
 * Handles business logic for getting and setting threshold levels for the
 * Scorecard
 * 
 * @author csmith
 *
 */
@Service
public class VeracodeSastThresholdsService {
	private final VeracodeSastThresholdsRepository thresholdsRepository;

	public VeracodeSastThresholdsService(@Autowired VeracodeSastThresholdsRepository thresholdsRepository) {
		this.thresholdsRepository = thresholdsRepository;
	}

	public VeracodeSastThresholdModel getThresholds() {
		List<VeracodeSastThresholdModel> thresholds = thresholdsRepository.findAll();
		if (thresholds.isEmpty()) {
			return null;
		}
		return thresholds.get(0);
	}

	public VeracodeSastThresholdModel setThresholds(int greenYellow, int yellowRed) {
		VeracodeSastThresholdModel threshold = getThresholds();
		if (threshold == null) {
			threshold = new VeracodeSastThresholdModel();
		}
		threshold.setGreenYellow(greenYellow);
		threshold.setYellowRed(yellowRed);
		return thresholdsRepository.saveAndFlush(threshold);
	}
}
