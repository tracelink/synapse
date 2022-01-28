package com.tracelink.prodsec.lib.veracode.rest.api.model;

import com.google.gson.annotations.SerializedName;
import java.util.Date;

/**
 * LibrarySummary
 */
public class LibrarySummary {

	@SerializedName("direct")
	private boolean direct;

	@SerializedName("id")
	private String id = null;

	@SerializedName("latest_version")
	private String latestVersion = null;

	@SerializedName("latest_version_release_date")
	private Date latestVersionReleaseDate = null;

	@SerializedName("name")
	private String name = null;

	@SerializedName("release_date")
	private Date releaseDate = null;

	@SerializedName("transitive")
	private boolean transitive;

	@SerializedName("version")
	private String version = null;

	public boolean isDirect() {
		return direct;
	}

	public void setDirect(boolean direct) {
		this.direct = direct;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getLatestVersion() {
		return latestVersion;
	}

	public void setLatestVersion(String latestVersion) {
		this.latestVersion = latestVersion;
	}

	public Date getLatestVersionReleaseDate() {
		return latestVersionReleaseDate;
	}

	public void setLatestVersionReleaseDate(Date latestVersionReleaseDate) {
		this.latestVersionReleaseDate = latestVersionReleaseDate;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getReleaseDate() {
		return releaseDate;
	}

	public void setReleaseDate(Date releaseDate) {
		this.releaseDate = releaseDate;
	}

	public boolean isTransitive() {
		return transitive;
	}

	public void setTransitive(boolean transitive) {
		this.transitive = transitive;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}
}

