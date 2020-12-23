package com.tracelink.prodsec.plugin.sonatype.util.client;

import java.util.List;

public class Application {

	private String id;
	private String publicId;
	private String name;
	private String organizationId;
	private String contactUserName;
	private List<ApplicationTag> applicationTags = null;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setPublicId(String publicId) {
		this.publicId = publicId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setOrganizationId(String organizationId) {
		this.organizationId = organizationId;
	}

	public void setContactUserName(String contactUserName) {
		this.contactUserName = contactUserName;
	}

	public void setApplicationTags(List<ApplicationTag> applicationTags) {
		this.applicationTags = applicationTags;
	}
}
