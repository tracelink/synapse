package com.tracelink.prodsec.plugin.sonatype.util.client;

import java.util.List;

public class ApplicationViolations {

	private List<ApplicationViolation> applicationViolations = null;

	public List<ApplicationViolation> getApplicationViolations() {
		return applicationViolations;
	}

	public void setApplicationViolations(List<ApplicationViolation> applicationViolations) {
		this.applicationViolations = applicationViolations;
	}

}
