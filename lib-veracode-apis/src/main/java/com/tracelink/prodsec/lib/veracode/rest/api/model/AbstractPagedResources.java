package com.tracelink.prodsec.lib.veracode.rest.api.model;

import com.google.gson.annotations.SerializedName;

/**
 * Abstract class to hold page metadata for all paged resources.
 */
public abstract class AbstractPagedResources {

	@SerializedName("page")
	private PageMetadata page = null;

	public PageMetadata getPage() {
		return page;
	}

	public void setPage(PageMetadata page) {
		this.page = page;
	}
}
