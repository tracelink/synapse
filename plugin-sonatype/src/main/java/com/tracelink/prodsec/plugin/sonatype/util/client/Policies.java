package com.tracelink.prodsec.plugin.sonatype.util.client;

import java.util.List;

/**
 * DTO to store a list of Policies from the Sonatype Nexus IQ API.
 */
public class Policies {

	private List<Policy> policies = null;

	public List<Policy> getPolicies() {
		return policies;
	}

	public void setPolicies(List<Policy> policies) {
		this.policies = policies;
	}

}
