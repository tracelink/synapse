package com.tracelink.prodsec.plugin.veracode.sca.util.model;

import com.google.gson.annotations.SerializedName;

/**
 * PagedResourcesIssueSummary
 */
public class PagedResourcesIssueSummary extends AbstractPagedResources {

	@SerializedName("_embedded")
	private IssueSummaries _embedded = new IssueSummaries();

	public IssueSummaries getEmbedded() {
		return _embedded;
	}

	public void setEmbedded(IssueSummaries _embedded) {
		this._embedded = _embedded;
	}
}
