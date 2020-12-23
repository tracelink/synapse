package com.tracelink.prodsec.plugin.sonatype.util.client;

import java.util.List;

public class ApplicationViolation {

	private Application application;
	private List<PolicyViolation> policyViolations = null;

	public Application getApplication() {
		return application;
	}

	public void setApplication(Application application) {
		this.application = application;
	}

	public List<PolicyViolation> getPolicyViolations() {
		return policyViolations;
	}

	public void setPolicyViolations(List<PolicyViolation> policyViolations) {
		this.policyViolations = policyViolations;
	}

}
