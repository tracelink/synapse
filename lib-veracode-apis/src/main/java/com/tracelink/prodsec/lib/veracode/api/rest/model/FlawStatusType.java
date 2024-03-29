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
 * Information about the status of discovered findings.
 */
public class FlawStatusType {
	@SerializedName("new")
	private Long _new = null;

	@SerializedName("reopen")
	private Long reopen = null;

	@SerializedName("open")
	private Long open = null;

	@SerializedName("fixed")
	private Long fixed = null;

	@SerializedName("total")
	private Long total = null;

	@SerializedName("not_mitigated")
	private Long notMitigated = null;

	@SerializedName("sev-1-change")
	private Long sev1Change = null;

	@SerializedName("sev-2-change")
	private Long sev2Change = null;

	@SerializedName("sev-3-change")
	private Long sev3Change = null;

	@SerializedName("sev-4-change")
	private Long sev4Change = null;

	@SerializedName("sev-5-change")
	private Long sev5Change = null;

	@SerializedName("conforms-to-guidelines")
	private Long conformsToGuidelines = null;

	@SerializedName("deviates-from-guidelines")
	private Long deviatesFromGuidelines = null;

	@SerializedName("total-reviewed-mitigations")
	private Long totalReviewedMitigations = null;

	/**
	 * Number of findings discovered during the first build of this application.
	 * 
	 * @return _new
	 **/
	public Long getNew() {
		return _new;
	}

	public void setNew(Long _new) {
		this._new = _new;
	}

	/**
	 * Number of findings discovered in a prior build of this application that were
	 * not new, but Veracode discovered them in the build immediately prior to this
	 * build.
	 * 
	 * @return reopen
	 **/
	public Long getReopen() {
		return reopen;
	}

	public void setReopen(Long reopen) {
		this.reopen = reopen;
	}

	/**
	 * Number of findings discovered in this build that Veracode also discovered in
	 * the build immediately prior to this build.
	 * 
	 * @return open
	 **/
	public Long getOpen() {
		return open;
	}

	public void setOpen(Long open) {
		this.open = open;
	}

	/**
	 * Number of findings discovered in the prior build that Veracode did not
	 * discover in the current build. For a dyanamic analysis, Veracode verifies the
	 * findings as fixed.
	 * 
	 * @return fixed
	 **/
	public Long getFixed() {
		return fixed;
	}

	public void setFixed(Long fixed) {
		this.fixed = fixed;
	}

	/**
	 * Total number of findings discovered in this build.
	 * 
	 * @return total
	 **/
	public Long getTotal() {
		return total;
	}

	public void setTotal(Long total) {
		this.total = total;
	}

	/**
	 * Total number of findings discovered in this build that are not mitigated.
	 * 
	 * @return notMitigated
	 **/
	public Long getNotMitigated() {
		return notMitigated;
	}

	public void setNotMitigated(Long notMitigated) {
		this.notMitigated = notMitigated;
	}

	/**
	 * Number of severity-1 findings discovered in this build, minus the number of
	 * severity-1 findings discovered in the build immediately prior to this build.
	 * 
	 * @return sev1Change
	 **/
	public Long getSev1Change() {
		return sev1Change;
	}

	public void setSev1Change(Long sev1Change) {
		this.sev1Change = sev1Change;
	}

	/**
	 * Number of severity-2 findings discvoered in this build, minus the number of
	 * severity-2 findings discovered in the build immediately prior to this build.
	 * 
	 * @return sev2Change
	 **/
	public Long getSev2Change() {
		return sev2Change;
	}

	public void setSev2Change(Long sev2Change) {
		this.sev2Change = sev2Change;
	}

	/**
	 * Number of severity-3 findings discvoered in this build, minus the number of
	 * severity-3 findings discovered in the build immediately prior to this build.
	 * 
	 * @return sev3Change
	 **/
	public Long getSev3Change() {
		return sev3Change;
	}

	public void setSev3Change(Long sev3Change) {
		this.sev3Change = sev3Change;
	}

	/**
	 * Number of severity-4 findings discvoered in this build, minus the number of
	 * severity-4 findings discovered in the build immediately prior to this build.
	 * 
	 * @return sev4Change
	 **/
	public Long getSev4Change() {
		return sev4Change;
	}

	public void setSev4Change(Long sev4Change) {
		this.sev4Change = sev4Change;
	}

	/**
	 * Number of severity-5 findings discvoered in this build, minus the number of
	 * severity-5 findings discovered in the build immediately prior to this build.
	 * 
	 * @return sev5Change
	 **/
	public Long getSev5Change() {
		return sev5Change;
	}

	public void setSev5Change(Long sev5Change) {
		this.sev5Change = sev5Change;
	}

	/**
	 * Number of mitigations that adhere to your risk tolerance guidelines based on
	 * Veracode review.
	 * 
	 * @return conformsToGuidelines
	 **/
	public Long getConformsToGuidelines() {
		return conformsToGuidelines;
	}

	public void setConformsToGuidelines(Long conformsToGuidelines) {
		this.conformsToGuidelines = conformsToGuidelines;
	}

	/**
	 * Number of mitigations that either do not provide enough information or do not
	 * adhere to your the risk tolerance guidelines, based on Veracode review.
	 * 
	 * @return deviatesFromGuidelines
	 **/
	public Long getDeviatesFromGuidelines() {
		return deviatesFromGuidelines;
	}

	public void setDeviatesFromGuidelines(Long deviatesFromGuidelines) {
		this.deviatesFromGuidelines = deviatesFromGuidelines;
	}

	/**
	 * Total number of mitigations that Veracode reviewed. The value may not add up
	 * to the total number of all proposed or accepted mitigations.
	 * 
	 * @return totalReviewedMitigations
	 **/
	public Long getTotalReviewedMitigations() {
		return totalReviewedMitigations;
	}

	public void setTotalReviewedMitigations(Long totalReviewedMitigations) {
		this.totalReviewedMitigations = totalReviewedMitigations;
	}

	@Override
	public boolean equals(java.lang.Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		FlawStatusType flawStatusType = (FlawStatusType) o;
		return Objects.equals(this._new, flawStatusType._new) && Objects.equals(this.reopen, flawStatusType.reopen)
				&& Objects.equals(this.open, flawStatusType.open) && Objects.equals(this.fixed, flawStatusType.fixed)
				&& Objects.equals(this.total, flawStatusType.total)
				&& Objects.equals(this.notMitigated, flawStatusType.notMitigated)
				&& Objects.equals(this.sev1Change, flawStatusType.sev1Change)
				&& Objects.equals(this.sev2Change, flawStatusType.sev2Change)
				&& Objects.equals(this.sev3Change, flawStatusType.sev3Change)
				&& Objects.equals(this.sev4Change, flawStatusType.sev4Change)
				&& Objects.equals(this.sev5Change, flawStatusType.sev5Change)
				&& Objects.equals(this.conformsToGuidelines, flawStatusType.conformsToGuidelines)
				&& Objects.equals(this.deviatesFromGuidelines, flawStatusType.deviatesFromGuidelines)
				&& Objects.equals(this.totalReviewedMitigations, flawStatusType.totalReviewedMitigations);
	}

	@Override
	public int hashCode() {
		return Objects.hash(_new, reopen, open, fixed, total, notMitigated, sev1Change, sev2Change, sev3Change,
				sev4Change, sev5Change, conformsToGuidelines, deviatesFromGuidelines, totalReviewedMitigations);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class FlawStatusType {\n");

		sb.append("    _new: ").append(toIndentedString(_new)).append("\n");
		sb.append("    reopen: ").append(toIndentedString(reopen)).append("\n");
		sb.append("    open: ").append(toIndentedString(open)).append("\n");
		sb.append("    fixed: ").append(toIndentedString(fixed)).append("\n");
		sb.append("    total: ").append(toIndentedString(total)).append("\n");
		sb.append("    notMitigated: ").append(toIndentedString(notMitigated)).append("\n");
		sb.append("    sev1Change: ").append(toIndentedString(sev1Change)).append("\n");
		sb.append("    sev2Change: ").append(toIndentedString(sev2Change)).append("\n");
		sb.append("    sev3Change: ").append(toIndentedString(sev3Change)).append("\n");
		sb.append("    sev4Change: ").append(toIndentedString(sev4Change)).append("\n");
		sb.append("    sev5Change: ").append(toIndentedString(sev5Change)).append("\n");
		sb.append("    conformsToGuidelines: ").append(toIndentedString(conformsToGuidelines)).append("\n");
		sb.append("    deviatesFromGuidelines: ").append(toIndentedString(deviatesFromGuidelines)).append("\n");
		sb.append("    totalReviewedMitigations: ").append(toIndentedString(totalReviewedMitigations)).append("\n");
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
