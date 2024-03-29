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
 * Filepath for the component.
 */
public class FilePath {
	@SerializedName("value")
	private String value = null;

	/**
	 * Filepath for the component.
	 * 
	 * @return value
	 **/
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public boolean equals(java.lang.Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		FilePath filePath = (FilePath) o;
		return Objects.equals(this.value, filePath.value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(value);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class FilePath {\n");

		sb.append("    value: ").append(toIndentedString(value)).append("\n");
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
