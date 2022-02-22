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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.google.gson.annotations.SerializedName;

import io.swagger.annotations.ApiModelProperty;

/**
 * EmbeddedApplication
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2022-02-13T02:06:38.897Z")
public class EmbeddedApplication {
	@SerializedName("applications")
	private List<Application> applications = null;

	public EmbeddedApplication applications(List<Application> applications) {
		this.applications = applications;
		return this;
	}

	public EmbeddedApplication addApplicationsItem(Application applicationsItem) {
		if (this.applications == null) {
			this.applications = new ArrayList<Application>();
		}
		this.applications.add(applicationsItem);
		return this;
	}

	/**
	 * Get applications
	 * 
	 * @return applications
	 **/
	@ApiModelProperty(value = "")
	public List<Application> getApplications() {
		return applications;
	}

	public void setApplications(List<Application> applications) {
		this.applications = applications;
	}

	@Override
	public boolean equals(java.lang.Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		EmbeddedApplication embeddedApplication = (EmbeddedApplication) o;
		return Objects.equals(this.applications, embeddedApplication.applications);
	}

	@Override
	public int hashCode() {
		return Objects.hash(applications);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class EmbeddedApplication {\n");

		sb.append("    applications: ").append(toIndentedString(applications)).append("\n");
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
