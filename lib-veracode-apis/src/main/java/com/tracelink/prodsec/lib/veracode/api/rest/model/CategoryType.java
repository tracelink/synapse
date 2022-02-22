/*
 * Veracode Summary Report API
 * Veracode Summary Report API Documentation
 *
 * OpenAPI spec version: v2
 * 
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */

package com.tracelink.prodsec.lib.veracode.api.rest.model;

import java.util.Objects;

import com.google.gson.annotations.SerializedName;

/**
 * Category Type
 */
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaClientCodegen", date = "2022-02-16T18:59:41.331Z[GMT]")
public class CategoryType {
	@SerializedName("categoryname")
	private String categoryName = null;

	@SerializedName("severity")
	private SeverityEnum severity = null;

	@SerializedName("count")
	private Long count = null;

	public CategoryType categoryName(String categoryName) {
		this.categoryName = categoryName;
		return this;
	}

	/**
	 * Name of the severity category.
	 * 
	 * @return categoryName
	 **/
	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

	public CategoryType severity(SeverityEnum severity) {
		this.severity = severity;
		return this;
	}

	/**
	 * Get severity
	 * 
	 * @return severity
	 **/
	public SeverityEnum getSeverity() {
		return severity;
	}

	public void setSeverity(SeverityEnum severity) {
		this.severity = severity;
	}

	public CategoryType count(Long count) {
		this.count = count;
		return this;
	}

	/**
	 * Number of findings in this category.
	 * 
	 * @return count
	 **/
	public Long getCount() {
		return count;
	}

	public void setCount(Long count) {
		this.count = count;
	}

	@Override
	public boolean equals(java.lang.Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		CategoryType categoryType = (CategoryType) o;
		return Objects.equals(this.categoryName, categoryType.categoryName)
				&& Objects.equals(this.severity, categoryType.severity)
				&& Objects.equals(this.count, categoryType.count);
	}

	@Override
	public int hashCode() {
		return Objects.hash(categoryName, severity, count);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class CategoryType {\n");

		sb.append("    categoryName: ").append(toIndentedString(categoryName)).append("\n");
		sb.append("    severity: ").append(toIndentedString(severity)).append("\n");
		sb.append("    count: ").append(toIndentedString(count)).append("\n");
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
