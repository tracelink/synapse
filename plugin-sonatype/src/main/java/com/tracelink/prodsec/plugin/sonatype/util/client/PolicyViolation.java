package com.tracelink.prodsec.plugin.sonatype.util.client;

import java.util.List;

/**
 * DTO to store PolicyViolation info from the Sonatype Nexus IQ API.
 */
public class PolicyViolation {

	private String policyId;
	private String policyName;
	private String policyViolationId;
	private int threatLevel;
	private List<ConstraintViolation> constraintViolations = null;
	private String stageId;
	private String reportUrl;
	private Component component;

	public void setPolicyId(String policyId) {
		this.policyId = policyId;
	}

	public void setPolicyName(String policyName) {
		this.policyName = policyName;
	}

	public void setPolicyViolationId(String policyViolationId) {
		this.policyViolationId = policyViolationId;
	}

	public int getThreatLevel() {
		return threatLevel;
	}

	public void setThreatLevel(int threatLevel) {
		this.threatLevel = threatLevel;
	}

	public void setConstraintViolations(List<ConstraintViolation> constraintViolations) {
		this.constraintViolations = constraintViolations;
	}

	public String getStageId() {
		return stageId;
	}

	public void setStageId(String stageId) {
		this.stageId = stageId;
	}

	public void setReportUrl(String reportUrl) {
		this.reportUrl = reportUrl;
	}

	public void setComponent(Component component) {
		this.component = component;
	}
}
