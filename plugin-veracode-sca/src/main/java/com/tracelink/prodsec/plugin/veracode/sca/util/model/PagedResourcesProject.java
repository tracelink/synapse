package com.tracelink.prodsec.plugin.veracode.sca.util.model;

import com.google.gson.annotations.SerializedName;

/**
 * PagedResourcesProject
 */
public class PagedResourcesProject extends AbstractPagedResources {

	@SerializedName("_embedded")
	private Projects _embedded = new Projects();

	public Projects getEmbedded() {
		return _embedded;
	}

	public void setEmbedded(Projects _embedded) {
		this._embedded = _embedded;
	}
}
