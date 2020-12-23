package com.tracelink.prodsec.plugin.jira.service;

import com.tracelink.prodsec.plugin.jira.exception.JiraAllowedSlaException;
import com.tracelink.prodsec.plugin.jira.model.JiraAllowedSla;
import com.tracelink.prodsec.plugin.jira.repo.JiraAllowedSlaRepo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Handles the business logic for storing and retrieving the days allowed
 * in SLA for a given severity.
 *
 * @author bhoran
 */

@Service
public class JiraAllowedSlaService {

	private final JiraAllowedSlaRepo allowedSlaRepo;

	/**
	 * Creates this JiraAllowedSlaService with the pre-configured database
	 * {@link JiraAllowedSlaRepo} to interact and add {@link JiraAllowedSla}
	 * instances
	 *
	 * @param allowedSlaRepo The repo containing allowed time in SLA for each severity
	 */
	public JiraAllowedSlaService(@Autowired JiraAllowedSlaRepo allowedSlaRepo) {
		this.allowedSlaRepo = allowedSlaRepo;
	}

	public List<JiraAllowedSla> getAllAllowedSla() throws JiraAllowedSlaException {
		List<JiraAllowedSla> allowedDays = allowedSlaRepo.findAll();
		if (allowedDays.isEmpty()) {
			throw new JiraAllowedSlaException("No allowed days configured for SLA.");
		}
		return allowedDays;
	}

	/**
	 * Given a severity level value and a number of days, sets the database entity for
	 * that severity level to the time in SLA specified
	 *
	 * @param issueLevel the severity value to be changed
	 * @param days       integer of the number of days the given severity will be
	 *                   changed to allow
	 */
	public void setAllowedSla(String issueLevel, Integer days) {
		List<JiraAllowedSla> knownSlaPeriod = allowedSlaRepo.findAll();
		JiraAllowedSla updateSlaPeriod = null;
		if (knownSlaPeriod.isEmpty()) {
			updateSlaPeriod = new JiraAllowedSla();
		} else {
			for (JiraAllowedSla j : knownSlaPeriod) {
				if (j.getSeverity().equals(issueLevel)) {
					updateSlaPeriod = j;
					break;
				} else {
					updateSlaPeriod = new JiraAllowedSla();
				}
			}
		}
		updateSlaPeriod.setAllowedDays(days);
		updateSlaPeriod.setSeverity(issueLevel);
		allowedSlaRepo.saveAndFlush(updateSlaPeriod);
	}

	/**
	 * Given a severity value, returns the time an issue is allowed to spent in SLA
	 * <p>
	 * getAllowedTimeBySev() can return null, as null is representative of no time
	 * specified, and the caller should respond to null accordingly
	 *
	 * @param severity string representing an approved severity value
	 * @return integer representing the number of days a vulnerability is allowed
	 * to exist based on the company's Service Level Agreement
	 */
	public Integer getAllowedTimeBySev(String severity) {
		JiraAllowedSla severityType = allowedSlaRepo.findOneBySeverityEquals(severity);
		if (severityType == null) {
			return null;
		}
		return severityType.getAllowedDays();
	}
}
