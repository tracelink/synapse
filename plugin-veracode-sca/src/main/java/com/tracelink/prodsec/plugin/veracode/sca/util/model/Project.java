package com.tracelink.prodsec.plugin.veracode.sca.util.model;

import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Project
 */
public class Project {

	@SerializedName("branches")
	private List<String> branches = null;

	@SerializedName("id")
	private UUID id = null;

	@JsonAdapter(LanguagesEnum.Adapter.class)
	public enum LanguagesEnum {
		JAVA("JAVA"),
		JS("JS"),
		RUBY("RUBY"),
		PYTHON("PYTHON"),
		OBJECTIVEC("OBJECTIVEC"),
		SWIFT("SWIFT"),
		PHP("PHP"),
		SCALA("SCALA"),
		GO("GO"),
		CPP("CPP"),
		CSHARP("CSHARP"),
		OS("OS");

		private final String value;

		LanguagesEnum(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}

		@Override
		public String toString() {
			return String.valueOf(value);
		}

		public static LanguagesEnum fromValue(String text) {
			for (LanguagesEnum b : LanguagesEnum.values()) {
				if (String.valueOf(b.value).equals(text)) {
					return b;
				}
			}
			return null;
		}

		public static class Adapter extends TypeAdapter<LanguagesEnum> {

			@Override
			public void write(final JsonWriter jsonWriter, final LanguagesEnum enumeration)
				throws IOException {
				jsonWriter.value(enumeration.getValue());
			}

			@Override
			public LanguagesEnum read(final JsonReader jsonReader) throws IOException {
				String value = jsonReader.nextString();
				return LanguagesEnum.fromValue(String.valueOf(value));
			}
		}
	}

	@SerializedName("languages")
	private List<LanguagesEnum> languages = null;

	@SerializedName("last_scan_date")
	private String lastScanDate = null;

	@SerializedName("library_issues_count")
	private int libraryIssuesCount;

	@SerializedName("license_issues_count")
	private int licenseIssuesCount;

	@SerializedName("name")
	private String name = null;

	@SerializedName("site_id")
	private String siteId = null;

	@SerializedName("total_issues_count")
	private int totalIssuesCount;

	@SerializedName("vulnerability_issues_count")
	private int vulnerabilityIssuesCount;

	public Project branches(List<String> branches) {
		this.branches = branches;
		return this;
	}

	public List<String> getBranches() {
		return branches;
	}

	public void setBranches(List<String> branches) {
		this.branches = branches;
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public List<LanguagesEnum> getLanguages() {
		return languages;
	}

	public void setLanguages(List<LanguagesEnum> languages) {
		this.languages = languages;
	}

	public String getLastScanDate() {
		return lastScanDate;
	}

	public void setLastScanDate(String lastScanDate) {
		this.lastScanDate = lastScanDate;
	}

	public int getLibraryIssuesCount() {
		return libraryIssuesCount;
	}

	public void setLibraryIssuesCount(int libraryIssuesCount) {
		this.libraryIssuesCount = libraryIssuesCount;
	}

	public int getLicenseIssuesCount() {
		return licenseIssuesCount;
	}

	public void setLicenseIssuesCount(int licenseIssuesCount) {
		this.licenseIssuesCount = licenseIssuesCount;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSiteId() {
		return siteId;
	}

	public void setSiteId(String siteId) {
		this.siteId = siteId;
	}

	public int getTotalIssuesCount() {
		return totalIssuesCount;
	}

	public void setTotalIssuesCount(int totalIssuesCount) {
		this.totalIssuesCount = totalIssuesCount;
	}

	public int getVulnerabilityIssuesCount() {
		return vulnerabilityIssuesCount;
	}

	public void setVulnerabilityIssuesCount(int vulnerabilityIssuesCount) {
		this.vulnerabilityIssuesCount = vulnerabilityIssuesCount;
	}
}

