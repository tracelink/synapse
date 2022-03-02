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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.google.gson.annotations.SerializedName;

/**
 * Details about the vulnerable components.
 */
public class VulnerableComponentList {
	@SerializedName("component")
	private List<Component> component = null;

	public VulnerableComponentList component(List<Component> component) {
		this.component = component;
		return this;
	}

	public VulnerableComponentList addComponentItem(Component componentItem) {
		if (this.component == null) {
			this.component = new ArrayList<Component>();
		}
		this.component.add(componentItem);
		return this;
	}

	/**
	 * Get component
	 * 
	 * @return component
	 **/
	public List<Component> getComponent() {
		return component;
	}

	public void setComponent(List<Component> component) {
		this.component = component;
	}

	@Override
	public boolean equals(java.lang.Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		VulnerableComponentList vulnerableComponentList = (VulnerableComponentList) o;
		return Objects.equals(this.component, vulnerableComponentList.component);
	}

	@Override
	public int hashCode() {
		return Objects.hash(component);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class VulnerableComponentList {\n");

		sb.append("    component: ").append(toIndentedString(component)).append("\n");
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
