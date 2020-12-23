package com.tracelink.prodsec.plugin.veracode.sca.util.model;

import com.google.gson.annotations.SerializedName;

/**
 * LicenseSummary
 */
public class LicenseSummary {

	@SerializedName("id")
	private String id = null;

	@SerializedName("name")
	private String name = null;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}

