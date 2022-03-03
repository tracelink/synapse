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

import java.io.IOException;
import java.util.Objects;

import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import io.swagger.annotations.ApiModelProperty;

/**
 * AppPolicy
 */
public class AppPolicy {
	@SerializedName("guid")
	private String guid = null;

	@SerializedName("is_default")
	private Boolean isDefault = null;

	@SerializedName("name")
	private String name = null;

	/**
	 * The policy compliance status.
	 */
	@JsonAdapter(PolicyComplianceStatusEnum.Adapter.class)
	public enum PolicyComplianceStatusEnum {
		DETERMINING("DETERMINING"),

		NOT_ASSESSED("NOT_ASSESSED"),

		DID_NOT_PASS("DID_NOT_PASS"),

		CONDITIONAL_PASS("CONDITIONAL_PASS"),

		PASSED("PASSED"),

		VENDOR_REVIEW("VENDOR_REVIEW");

		private String value;

		PolicyComplianceStatusEnum(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}

		@Override
		public String toString() {
			return String.valueOf(value);
		}

		public static PolicyComplianceStatusEnum fromValue(String text) {
			for (PolicyComplianceStatusEnum b : PolicyComplianceStatusEnum.values()) {
				if (String.valueOf(b.value).equals(text)) {
					return b;
				}
			}
			return null;
		}

		public static class Adapter extends TypeAdapter<PolicyComplianceStatusEnum> {
			@Override
			public void write(final JsonWriter jsonWriter, final PolicyComplianceStatusEnum enumeration)
					throws IOException {
				jsonWriter.value(enumeration.getValue());
			}

			@Override
			public PolicyComplianceStatusEnum read(final JsonReader jsonReader) throws IOException {
				String value = jsonReader.nextString();
				return PolicyComplianceStatusEnum.fromValue(String.valueOf(value));
			}
		}
	}

	@SerializedName("policy_compliance_status")
	private PolicyComplianceStatusEnum policyComplianceStatus = null;

	public AppPolicy guid(String guid) {
		this.guid = guid;
		return this;
	}

	/**
	 * Get guid
	 * 
	 * @return guid
	 **/
	@ApiModelProperty(value = "")
	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public AppPolicy isDefault(Boolean isDefault) {
		this.isDefault = isDefault;
		return this;
	}

	/**
	 * Get isDefault
	 * 
	 * @return isDefault
	 **/
	@ApiModelProperty(value = "")
	public Boolean isIsDefault() {
		return isDefault;
	}

	public void setIsDefault(Boolean isDefault) {
		this.isDefault = isDefault;
	}

	/**
	 * The policy name.
	 * 
	 * @return name
	 **/
	@ApiModelProperty(value = "The policy name.")
	public String getName() {
		return name;
	}

	/**
	 * The policy compliance status.
	 * 
	 * @return policyComplianceStatus
	 **/
	@ApiModelProperty(value = "The policy compliance status.")
	public PolicyComplianceStatusEnum getPolicyComplianceStatus() {
		return policyComplianceStatus;
	}

	@Override
	public boolean equals(java.lang.Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		AppPolicy appPolicy = (AppPolicy) o;
		return Objects.equals(this.guid, appPolicy.guid) && Objects.equals(this.isDefault, appPolicy.isDefault)
				&& Objects.equals(this.name, appPolicy.name)
				&& Objects.equals(this.policyComplianceStatus, appPolicy.policyComplianceStatus);
	}

	@Override
	public int hashCode() {
		return Objects.hash(guid, isDefault, name, policyComplianceStatus);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class AppPolicy {\n");

		sb.append("    guid: ").append(toIndentedString(guid)).append("\n");
		sb.append("    isDefault: ").append(toIndentedString(isDefault)).append("\n");
		sb.append("    name: ").append(toIndentedString(name)).append("\n");
		sb.append("    policyComplianceStatus: ").append(toIndentedString(policyComplianceStatus)).append("\n");
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