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
 * Details about SCA policy rules for the component.
 */
public class ViolatedRuleList {
	@SerializedName("policy_rule")
	private List<PolicyRule> policyRule = null;

	/**
	 * Get policyRule
	 * 
	 * @return policyRule
	 **/
	public List<PolicyRule> getPolicyRule() {
		return policyRule;
	}

	public void setPolicyRule(List<PolicyRule> policyRule) {
		this.policyRule = policyRule;
	}

	@Override
	public boolean equals(java.lang.Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		ViolatedRuleList violatedRuleList = (ViolatedRuleList) o;
		return Objects.equals(this.policyRule, violatedRuleList.policyRule);
	}

	@Override
	public int hashCode() {
		return Objects.hash(policyRule);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class ViolatedRuleList {\n");

		sb.append("    policyRule: ").append(toIndentedString(policyRule)).append("\n");
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
