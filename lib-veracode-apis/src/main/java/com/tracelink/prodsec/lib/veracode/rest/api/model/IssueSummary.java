package com.tracelink.prodsec.lib.veracode.rest.api.model;

import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.UUID;

/**
 * IssueSummary
 */
public class IssueSummary {

	@SerializedName("created_date")
	private String createdDate = null;

	@SerializedName("id")
	private UUID id = null;

	@SerializedName("ignored")
	private boolean ignored;

	/**
	 * IssueStatusEnum
	 */
	@JsonAdapter(IssueStatusEnum.Adapter.class)
	public enum IssueStatusEnum {
		FIXED("fixed"),
		OPEN("open");

		private final String value;

		IssueStatusEnum(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}

		@Override
		public String toString() {
			return String.valueOf(value);
		}

		/**
		 * Gets the {@link IssueStatusEnum} associated with the given value.
		 *
		 * @param text the value to get the issue status for
		 * @return the issue status
		 */
		public static IssueStatusEnum fromValue(String text) {
			for (IssueStatusEnum b : IssueStatusEnum.values()) {
				if (String.valueOf(b.value).equals(text)) {
					return b;
				}
			}
			return null;
		}

		/**
		 * An adapter class to handle JSON translation of the {@link IssueStatusEnum}.
		 */
		public static class Adapter extends TypeAdapter<IssueStatusEnum> {

			@Override
			public void write(final JsonWriter jsonWriter, final IssueStatusEnum enumeration)
					throws IOException {
				jsonWriter.value(enumeration.getValue());
			}

			@Override
			public IssueStatusEnum read(final JsonReader jsonReader) throws IOException {
				String value = jsonReader.nextString();
				return IssueStatusEnum.fromValue(String.valueOf(value));
			}
		}
	}

	@SerializedName("issue_status")
	private IssueStatusEnum issueStatus = null;

	/**
	 * IssueTypeEnum
	 */
	@JsonAdapter(IssueTypeEnum.Adapter.class)
	public enum IssueTypeEnum {
		LIBRARY("library"),
		LICENSE("license"),
		VULNERABILITY("vulnerability");

		private final String value;

		IssueTypeEnum(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}

		@Override
		public String toString() {
			return String.valueOf(value);
		}

		/**
		 * Gets the {@link IssueTypeEnum} associated with the given value.
		 *
		 * @param text the value to get the issue type for
		 * @return the issue type
		 */
		public static IssueTypeEnum fromValue(String text) {
			for (IssueTypeEnum b : IssueTypeEnum.values()) {
				if (String.valueOf(b.value).equals(text)) {
					return b;
				}
			}
			return null;
		}

		/**
		 * An adapter class to handle JSON translation of the {@link IssueTypeEnum}.
		 */
		public static class Adapter extends TypeAdapter<IssueTypeEnum> {

			@Override
			public void write(final JsonWriter jsonWriter, final IssueTypeEnum enumeration)
					throws IOException {
				jsonWriter.value(enumeration.getValue());
			}

			@Override
			public IssueTypeEnum read(final JsonReader jsonReader) throws IOException {
				String value = jsonReader.nextString();
				return IssueTypeEnum.fromValue(String.valueOf(value));
			}
		}
	}

	@SerializedName("issue_type")
	private IssueTypeEnum issueType = null;

	@SerializedName("library")
	private LibrarySummary library = null;

	@SerializedName("library_updated_release_date")
	private String libraryUpdatedReleaseDate = null;

	@SerializedName("library_updated_version")
	private String libraryUpdatedVersion = null;

	@SerializedName("license")
	private LicenseSummary license = null;

	@SerializedName("license_count")
	private int licenseCount;

	@SerializedName("project_branch")
	private String projectBranch = null;

	@SerializedName("project_id")
	private UUID projectId = null;

	@SerializedName("project_name")
	private String projectName = null;

	@SerializedName("project_tag")
	private String projectTag = null;

	@SerializedName("severity")
	private float severity;

	@SerializedName("site_id")
	private long siteId;

	@SerializedName("vulnerability")
	private VulnerabilitySummary vulnerability = null;

	@SerializedName("vulnerable_method")
	private boolean vulnerableMethod;

	@SerializedName("workspace_id")
	private UUID workspaceId = null;

	public String getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(String createdDate) {
		this.createdDate = createdDate;
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public boolean isIgnored() {
		return ignored;
	}

	public void setIgnored(boolean ignored) {
		this.ignored = ignored;
	}

	public IssueStatusEnum getIssueStatus() {
		return issueStatus;
	}

	public void setIssueStatus(IssueStatusEnum issueStatus) {
		this.issueStatus = issueStatus;
	}

	public IssueTypeEnum getIssueType() {
		return issueType;
	}

	public void setIssueType(IssueTypeEnum issueType) {
		this.issueType = issueType;
	}

	public LibrarySummary getLibrary() {
		return library;
	}

	public void setLibrary(LibrarySummary library) {
		this.library = library;
	}

	public String getLibraryUpdatedReleaseDateDate() {
		return libraryUpdatedReleaseDate;
	}

	public void setLibraryUpdatedReleaseDate(String libraryUpdatedReleaseDate) {
		this.libraryUpdatedReleaseDate = libraryUpdatedReleaseDate;
	}

	public String getLibraryUpdatedVersion() {
		return libraryUpdatedVersion;
	}

	public void setLibraryUpdatedVersion(String libraryUpdatedVersion) {
		this.libraryUpdatedVersion = libraryUpdatedVersion;
	}

	public LicenseSummary getLicense() {
		return license;
	}

	public void setLicense(LicenseSummary license) {
		this.license = license;
	}

	public int getLicenseCount() {
		return licenseCount;
	}

	public void setLicenseCount(int licenseCount) {
		this.licenseCount = licenseCount;
	}

	public String getProjectBranch() {
		return projectBranch;
	}

	public void setProjectBranch(String projectBranch) {
		this.projectBranch = projectBranch;
	}

	public UUID getProjectId() {
		return projectId;
	}

	public void setProjectId(UUID projectId) {
		this.projectId = projectId;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public String getProjectTag() {
		return projectTag;
	}

	public void setProjectTag(String projectTag) {
		this.projectTag = projectTag;
	}

	public float getSeverity() {
		return severity;
	}

	public void setSeverity(float severity) {
		this.severity = severity;
	}

	public long getSiteId() {
		return siteId;
	}

	public void setSiteId(long siteId) {
		this.siteId = siteId;
	}

	public VulnerabilitySummary getVulnerability() {
		return vulnerability;
	}

	public void setVulnerability(VulnerabilitySummary vulnerability) {
		this.vulnerability = vulnerability;
	}

	public boolean isVulnerableMethod() {
		return vulnerableMethod;
	}

	public void setVulnerableMethod(boolean vulnerableMethod) {
		this.vulnerableMethod = vulnerableMethod;
	}

	public UUID getWorkspaceId() {
		return workspaceId;
	}

	public void setWorkspaceId(UUID workspaceId) {
		this.workspaceId = workspaceId;
	}
}

