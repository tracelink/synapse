package com.tracelink.prodsec.plugin.sonatype.util.client;

import java.util.List;

/**
 * DTO to store a list of ApplicationViolations from the Sonatype Nexus IQ API.
 */
public class ApplicationViolations {

	private List<ApplicationViolation> applicationViolations = null;

	public List<ApplicationViolation> getApplicationViolations() {
		return applicationViolations;
	}

	public void setApplicationViolations(List<ApplicationViolation> applicationViolations) {
		this.applicationViolations = applicationViolations;
	}

}
