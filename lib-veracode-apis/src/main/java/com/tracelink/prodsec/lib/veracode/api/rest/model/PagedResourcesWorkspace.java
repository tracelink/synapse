package com.tracelink.prodsec.lib.veracode.api.rest.model;

import com.google.gson.annotations.SerializedName;

/**
 * PagedResourcesWorkspace
 */
public class PagedResourcesWorkspace extends AbstractPagedResources {

	@SerializedName("_embedded")
	private Workspaces _embedded = new Workspaces();

	public Workspaces getEmbedded() {
		return _embedded;
	}

	public void setEmbedded(Workspaces _embedded) {
		this._embedded = _embedded;
	}
}
