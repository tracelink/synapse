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
 * CustomNameValue
 */
public class CustomNameValue {
	@SerializedName("name")
	private String name = null;

	@SerializedName("value")
	private String value = null;

	public CustomNameValue name(String name) {
		this.name = name;
		return this;
	}

	/**
	 * The custom field name.
	 * 
	 * @return name
	 **/
	@ApiModelProperty(value = "The custom field name.")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public CustomNameValue value(String value) {
		this.value = value;
		return this;
	}

	/**
	 * The custom field value. Note: All applications in your organization use the
	 * same set of custom field values.
	 * 
	 * @return value
	 **/
	@ApiModelProperty(value = "The custom field value. Note: All applications in your organization use the same set of custom field values.")
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
		CustomNameValue customNameValue = (CustomNameValue) o;
		return Objects.equals(this.name, customNameValue.name) && Objects.equals(this.value, customNameValue.value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, value);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class CustomNameValue {\n");

		sb.append("    name: ").append(toIndentedString(name)).append("\n");
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
