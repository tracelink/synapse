package com.tracelink.prodsec.lib.veracode.rest.api.model;

import com.google.gson.annotations.SerializedName;

/**
 * PageMetadata
 */
public class PageMetadata {

	@SerializedName("number")
	private long number;

	@SerializedName("size")
	private long size;

	@SerializedName("total_elements")
	private long totalElements;

	@SerializedName("total_pages")
	private long totalPages;

	public long getNumber() {
		return number;
	}

	public void setNumber(long number) {
		this.number = number;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public long getTotalElements() {
		return totalElements;
	}

	public void setTotalElements(long totalElements) {
		this.totalElements = totalElements;
	}

	public long getTotalPages() {
		return totalPages;
	}

	public void setTotalPages(long totalPages) {
		this.totalPages = totalPages;
	}
}
