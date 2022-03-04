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
 * Information about the type of module that Veracode scanned.
 */
public class ModuleType {
	@SerializedName("name")
	private String name = null;

	@SerializedName("compiler")
	private String compiler = null;

	@SerializedName("os")
	private String os = null;

	@SerializedName("architecture")
	private String architecture = null;

	@SerializedName("loc")
	private Long loc = null;

	@SerializedName("score")
	private Long score = null;

	@SerializedName("numflawssev0")
	private Long numflawssev0 = null;

	@SerializedName("numflawssev1")
	private Long numflawssev1 = null;

	@SerializedName("numflawssev2")
	private Long numflawssev2 = null;

	@SerializedName("numflawssev3")
	private Long numflawssev3 = null;

	@SerializedName("numflawssev4")
	private Long numflawssev4 = null;

	@SerializedName("numflawssev5")
	private Long numflawssev5 = null;

	@SerializedName("target_url")
	private String targetUrl = null;

	@SerializedName("domain")
	private String domain = null;

	/**
	 * Name of the scanned module. For a dynamic analysis, the name is blank.
	 * 
	 * @return name
	 **/
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Compiler that compiled the scanned module. For a dynamic analysis, the value
	 * is blank.
	 * 
	 * @return compiler
	 **/
	public String getCompiler() {
		return compiler;
	}

	public void setCompiler(String compiler) {
		this.compiler = compiler;
	}

	/**
	 * Operating system for which the scanned module is targetted. For a dynamic
	 * analysis, the value is blank.
	 * 
	 * @return os
	 **/
	public String getOs() {
		return os;
	}

	public void setOs(String os) {
		this.os = os;
	}

	/**
	 * Target architecture for which the scanned module is targeted. For a dynamic
	 * analysis, the value is blank.
	 * 
	 * @return architecture
	 **/
	public String getArchitecture() {
		return architecture;
	}

	public void setArchitecture(String architecture) {
		this.architecture = architecture;
	}

	/**
	 * Lines of codes. For a dynamic analysis or non-debug modules, the value is
	 * blank.
	 * 
	 * @return loc
	 **/
	public Long getLoc() {
		return loc;
	}

	public void setLoc(Long loc) {
		this.loc = loc;
	}

	/**
	 * Module-specific security score, which contributes toward the analysis scores
	 * for the application.
	 * 
	 * @return score
	 **/
	public Long getScore() {
		return score;
	}

	public void setScore(Long score) {
		this.score = score;
	}

	/**
	 * Number of severity-0 findings. These findings are the lowest severity and are
	 * usually informational only.
	 * 
	 * @return numflawssev0
	 **/
	public Long getNumflawssev0() {
		return numflawssev0;
	}

	public void setNumflawssev0(Long numflawssev0) {
		this.numflawssev0 = numflawssev0;
	}

	/**
	 * Number of severity-1 findings.
	 * 
	 * @return numflawssev1
	 **/
	public Long getNumflawssev1() {
		return numflawssev1;
	}

	public void setNumflawssev1(Long numflawssev1) {
		this.numflawssev1 = numflawssev1;
	}

	/**
	 * Number of severity-2 findings.
	 * 
	 * @return numflawssev2
	 **/
	public Long getNumflawssev2() {
		return numflawssev2;
	}

	public void setNumflawssev2(Long numflawssev2) {
		this.numflawssev2 = numflawssev2;
	}

	/**
	 * Number of severity-3 findings.
	 * 
	 * @return numflawssev3
	 **/
	public Long getNumflawssev3() {
		return numflawssev3;
	}

	public void setNumflawssev3(Long numflawssev3) {
		this.numflawssev3 = numflawssev3;
	}

	/**
	 * Number of severity-4 findings.
	 * 
	 * @return numflawssev4
	 **/
	public Long getNumflawssev4() {
		return numflawssev4;
	}

	public void setNumflawssev4(Long numflawssev4) {
		this.numflawssev4 = numflawssev4;
	}

	/**
	 * Number of severity-5 findings. These findings are the highest severity and
	 * Veracode recommends that you fix them immediately.
	 * 
	 * @return numflawssev5
	 **/
	public Long getNumflawssev5() {
		return numflawssev5;
	}

	public void setNumflawssev5(Long numflawssev5) {
		this.numflawssev5 = numflawssev5;
	}

	/**
	 * For a dynamic analysis, the URL for the application you scanned.
	 * 
	 * @return targetUrl
	 **/
	public String getTargetUrl() {
		return targetUrl;
	}

	public void setTargetUrl(String targetUrl) {
		this.targetUrl = targetUrl;
	}

	/**
	 * For a dynamic analysis, the domain for the application you scanned.
	 * 
	 * @return domain
	 **/
	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	@Override
	public boolean equals(java.lang.Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		ModuleType moduleType = (ModuleType) o;
		return Objects.equals(this.name, moduleType.name) && Objects.equals(this.compiler, moduleType.compiler)
				&& Objects.equals(this.os, moduleType.os) && Objects.equals(this.architecture, moduleType.architecture)
				&& Objects.equals(this.loc, moduleType.loc) && Objects.equals(this.score, moduleType.score)
				&& Objects.equals(this.numflawssev0, moduleType.numflawssev0)
				&& Objects.equals(this.numflawssev1, moduleType.numflawssev1)
				&& Objects.equals(this.numflawssev2, moduleType.numflawssev2)
				&& Objects.equals(this.numflawssev3, moduleType.numflawssev3)
				&& Objects.equals(this.numflawssev4, moduleType.numflawssev4)
				&& Objects.equals(this.numflawssev5, moduleType.numflawssev5)
				&& Objects.equals(this.targetUrl, moduleType.targetUrl)
				&& Objects.equals(this.domain, moduleType.domain);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, compiler, os, architecture, loc, score, numflawssev0, numflawssev1, numflawssev2,
				numflawssev3, numflawssev4, numflawssev5, targetUrl, domain);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class ModuleType {\n");

		sb.append("    name: ").append(toIndentedString(name)).append("\n");
		sb.append("    compiler: ").append(toIndentedString(compiler)).append("\n");
		sb.append("    os: ").append(toIndentedString(os)).append("\n");
		sb.append("    architecture: ").append(toIndentedString(architecture)).append("\n");
		sb.append("    loc: ").append(toIndentedString(loc)).append("\n");
		sb.append("    score: ").append(toIndentedString(score)).append("\n");
		sb.append("    numflawssev0: ").append(toIndentedString(numflawssev0)).append("\n");
		sb.append("    numflawssev1: ").append(toIndentedString(numflawssev1)).append("\n");
		sb.append("    numflawssev2: ").append(toIndentedString(numflawssev2)).append("\n");
		sb.append("    numflawssev3: ").append(toIndentedString(numflawssev3)).append("\n");
		sb.append("    numflawssev4: ").append(toIndentedString(numflawssev4)).append("\n");
		sb.append("    numflawssev5: ").append(toIndentedString(numflawssev5)).append("\n");
		sb.append("    targetUrl: ").append(toIndentedString(targetUrl)).append("\n");
		sb.append("    domain: ").append(toIndentedString(domain)).append("\n");
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
