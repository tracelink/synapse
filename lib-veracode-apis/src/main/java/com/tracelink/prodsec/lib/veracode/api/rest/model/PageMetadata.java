/*
 * Veracode Applications API Specification
 * Veracode Applications API Documentation  Use one of the following base URLs depending on the region for your account: * https://api.veracode.com/ - Veracode US Region (default) * https://api.veracode.eu/ - Veracode European Region * https://api.veracode.us/ - Veracode US Federal Region
 *
 * OpenAPI spec version: 1.0
 * 
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */

package com.tracelink.prodsec.lib.veracode.api.rest.model;

import java.util.Objects;

import com.google.gson.annotations.SerializedName;

import io.swagger.annotations.ApiModelProperty;

/**
 * PageMetadata
 */
public class PageMetadata {
	@SerializedName("number")
	private Long number = null;

	@SerializedName("size")
	private Long size = null;

	@SerializedName("total_elements")
	private Long totalElements = null;

	@SerializedName("total_pages")
	private Long totalPages = null;

	/**
	 * Get number
	 * 
	 * @return number
	 **/
	@ApiModelProperty(value = "")
	public Long getNumber() {
		return number;
	}

	public void setNumber(Long number) {
		this.number = number;
	}

	/**
	 * Get size
	 * 
	 * @return size
	 **/
	@ApiModelProperty(value = "")
	public Long getSize() {
		return size;
	}

	public void setSize(Long size) {
		this.size = size;
	}

	/**
	 * Get totalElements
	 * 
	 * @return totalElements
	 **/
	@ApiModelProperty(value = "")
	public Long getTotalElements() {
		return totalElements;
	}

	public void setTotalElements(Long totalElements) {
		this.totalElements = totalElements;
	}

	/**
	 * Get totalPages
	 * 
	 * @return totalPages
	 **/
	@ApiModelProperty(value = "")
	public Long getTotalPages() {
		return totalPages;
	}

	public void setTotalPages(Long totalPages) {
		this.totalPages = totalPages;
	}

	@Override
	public boolean equals(java.lang.Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		PageMetadata pageMetadata = (PageMetadata) o;
		return Objects.equals(this.number, pageMetadata.number) && Objects.equals(this.size, pageMetadata.size)
				&& Objects.equals(this.totalElements, pageMetadata.totalElements)
				&& Objects.equals(this.totalPages, pageMetadata.totalPages);
	}

	@Override
	public int hashCode() {
		return Objects.hash(number, size, totalElements, totalPages);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class PageMetadata {\n");

		sb.append("    number: ").append(toIndentedString(number)).append("\n");
		sb.append("    size: ").append(toIndentedString(size)).append("\n");
		sb.append("    totalElements: ").append(toIndentedString(totalElements)).append("\n");
		sb.append("    totalPages: ").append(toIndentedString(totalPages)).append("\n");
		sb.append("}");
		return sb.toString();
	}

	/**
	 * Convert the given object to string with each line indented by 4 spaces
	 * (except the first line).
	 */
	private String toIndentedString(java.lang.Object o) {
		if (o == null) {
			return "null";
		}
		return o.toString().replace("\n", "\n    ");
	}

}
