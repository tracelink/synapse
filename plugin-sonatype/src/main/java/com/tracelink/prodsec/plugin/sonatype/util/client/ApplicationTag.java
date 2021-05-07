package com.tracelink.prodsec.plugin.sonatype.util.client;

/**
 * DTO to store ApplicationTag info from the Sonatype Nexus IQ API.
 */
public class ApplicationTag {

	private String id;
	private String tagId;
	private String applicationId;

	public void setId(String id) {
		this.id = id;
	}

	public void setTagId(String tagId) {
		this.tagId = tagId;
	}

	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}

}
