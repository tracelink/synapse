package com.tracelink.prodsec.plugin.veracode.sca.util.model;

import com.google.gson.annotations.SerializedName;
import java.util.UUID;

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
