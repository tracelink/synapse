package com.tracelink.prodsec.plugin.jira.service;

import com.tracelink.prodsec.plugin.jira.exception.JiraThresholdsException;
import com.tracelink.prodsec.plugin.jira.model.JiraThresholds;
import com.tracelink.prodsec.plugin.jira.repo.JiraThresholdsRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service to store and retrieve data about Jira risk thresholds from the {@link
 * JiraThresholdsRepository}.
 *
 * @author bhoran
 */
@Service
public class JiraThresholdsService {

	private final JiraThresholdsRepository thresholdsRepository;

	/**
	 * Creates this thresholds service with the pre-configured {@link
	 * JiraThresholdsRepository} to interact with the database.
	 *
	 * @param thresholdsRepository the Jira thresholds repository
	 */
	public JiraThresholdsService(@Autowired JiraThresholdsRepository thresholdsRepository) {
		this.thresholdsRepository = thresholdsRepository;
	}

	/**
	 * Gets the configured Jira risk thresholds from the database.
	 *
	 * @return the configured risk thresholds
	 * @throws JiraThresholdsException if no thresholds are configured
	 */
	public JiraThresholds getThresholds() throws JiraThresholdsException {
		List<JiraThresholds> thresholds = thresholdsRepository.findAll();
		if (thresholds.isEmpty()) {
			throw new JiraThresholdsException("No Jira thresholds configured.");
		}
		return thresholds.get(0);
	}

	/**
	 * Sets the values of the Jira risk thresholds in the database. If no
	 * risk thresholds are currently configured, it will create a new entity.
	 * Otherwise, it will update the existing entity.
	 *
	 * @param greenYellow the threshold between green and yellow traffic lights
	 * @param yellowRed   the threshold between yellow and red traffic lights
	 */
	public void setThresholds(long greenYellow, long yellowRed) {
		List<JiraThresholds> thresholds = thresholdsRepository.findAll();
		JiraThresholds threshold;
		if (thresholds.isEmpty()) {
			threshold = new JiraThresholds();
		} else {
			threshold = thresholds.get(0);
		}
		threshold.setGreenYellow(greenYellow);
		threshold.setYellowRed(yellowRed);
		thresholdsRepository.saveAndFlush(threshold);
	}
}
