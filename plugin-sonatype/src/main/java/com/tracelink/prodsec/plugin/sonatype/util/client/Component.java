package com.tracelink.prodsec.plugin.sonatype.util.client;

/**
 * DTO to store Component info from the Sonatype Nexus IQ API.
 */
public class Component {

	private String packageUrl;
	private String hash;
	private ComponentIdentifier componentIdentifier;
	private boolean proprietary;

	public void setPackageUrl(String packageUrl) {
		this.packageUrl = packageUrl;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public void setComponentIdentifier(ComponentIdentifier componentIdentifier) {
		this.componentIdentifier = componentIdentifier;
	}

	public void setProprietary(boolean proprietary) {
		this.proprietary = proprietary;
	}
}
