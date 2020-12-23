package com.tracelink.prodsec.plugin.veracode.sca.service;

import com.tracelink.prodsec.plugin.veracode.sca.exception.VeracodeScaThresholdsException;
import com.tracelink.prodsec.plugin.veracode.sca.model.VeracodeScaThresholds;
import com.tracelink.prodsec.plugin.veracode.sca.repository.VeracodeScaThresholdsRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service to store and retrieve data about Veracode SCA risk thresholds from the {@link
 * VeracodeScaThresholdsRepository}.
 */
@Service
public class VeracodeScaThresholdsService {

	private final VeracodeScaThresholdsRepository thresholdsRepository;

	public VeracodeScaThresholdsService(
		@Autowired VeracodeScaThresholdsRepository thresholdsRepository) {
		this.thresholdsRepository = thresholdsRepository;
	}

	/**
	 * Gets the configured Veracode SCA risk thresholds from the database.
	 *
	 * @return the configured risk thresholds
	 * @throws VeracodeScaThresholdsException if no thresholds are configured
	 */
	public VeracodeScaThresholds getThresholds() throws VeracodeScaThresholdsException {
		List<VeracodeScaThresholds> thresholds = thresholdsRepository.findAll();
		if (thresholds.isEmpty()) {
			throw new VeracodeScaThresholdsException("No Veracode SCA thresholds configured.");
		}
		return thresholds.get(0);
	}

	/**
	 * Sets the values of the Veracode SCA risk thresholds in the database. If no risk thresholds
	 * are currently configured, it will create a new entity. Otherwise, it will update the existing
	 * entity.
	 *
	 * @param greenYellow the threshold between green and yellow traffic lights
	 * @param yellowRed   the threshold between yellow and red traffic lights
	 */
	public void setThresholds(long greenYellow, long yellowRed) {
		List<VeracodeScaThresholds> thresholds = thresholdsRepository.findAll();
		VeracodeScaThresholds threshold;
		if (thresholds.isEmpty()) {
			threshold = new VeracodeScaThresholds();
		} else {
			threshold = thresholds.get(0);
		}
		threshold.setGreenYellow(greenYellow);
		threshold.setYellowRed(yellowRed);
		thresholdsRepository.saveAndFlush(threshold);
	}
}
