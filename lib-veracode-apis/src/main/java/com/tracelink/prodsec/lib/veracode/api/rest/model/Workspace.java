package com.tracelink.prodsec.lib.veracode.api.rest.model;

import java.util.UUID;

import com.google.gson.annotations.SerializedName;

/**
 * Workspace
 */
public class Workspace {

	@SerializedName("id")
	private UUID id = null;

	@SerializedName("name")
	private String name = null;

	@SerializedName("site_id")
	private String siteId = null;

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSiteId() {
		return siteId;
	}

	public void setSiteId(String siteId) {
		this.siteId = siteId;
	}
}
