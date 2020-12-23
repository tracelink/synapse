package com.tracelink.prodsec.plugin.veracode.sca.model;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tracelink.prodsec.plugin.veracode.sca.VeracodeScaPlugin;
import com.tracelink.prodsec.synapse.products.model.ProjectModel;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

/**
 * The Database entity for the Veracode SCA project.
 * <p>
 * Demonstrates how to relate this object to a Synapse Core {@link ProjectModel}.
 *
 * @author mcool
 */
@Entity
@Table(schema = VeracodeScaPlugin.SCHEMA, name = "veracode_sca_projects")
public class VeracodeScaProject {

	private static final Gson GSON = new Gson();
	private static final TypeToken<Set<String>> SET_TYPE_TOKEN = new TypeToken<Set<String>>() {
	};

	/**
	 * The ID of this project, which is a UUID.
	 */
	@Id
	@Column(name = "project_id")
	private UUID id;

	/**
	 * The name of this project.
	 */
	@Column(name = "name")
	private String name;

	/**
	 * The site ID of this project.
	 */
	@Column(name = "site_id")
	private String siteId;

	/**
	 * The date and time of the last scan performed on this project.
	 */
	@Column(name = "lastScanDate")
	private LocalDateTime lastScanDate;

	/**
	 * A JSON string representing the branches of this project that have been scanned by Veracode.
	 */
	@Column(name = "branches")
	private String branches = "[]";

	/**
	 * The default branch for this project. Statistics will be gathered only for the issues
	 * associated with the default branch of this project. The default branch is configurable by the
	 * user.
	 */
	@Column(name = "default_branch")
	private String defaultBranch;

	/**
	 * The workspace this project belongs to.
	 */
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "workspace_id")
	private VeracodeScaWorkspace workspace;

	/**
	 * The list of {@link VeracodeScaIssue}s associated with this project. May contain issues from
	 * more than one project branch.
	 */
	@OneToMany(mappedBy = "project", fetch = FetchType.EAGER)
	@Fetch(value = FetchMode.SUBSELECT)
	@OrderBy("severity desc")
	private List<VeracodeScaIssue> issues;

	/**
	 * The Synapse {@link ProjectModel} that this project is mapped to. The Synapse project mapping
	 * is configurable by the user.
	 */
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "synapse_project")
	private ProjectModel synapseProject;

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
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

	public LocalDateTime getLastScanDate() {
		return lastScanDate;
	}

	public void setLastScanDate(LocalDateTime lastScanDate) {
		this.lastScanDate = lastScanDate;
	}

	public Set<String> getBranches() {
		Set<String> encodedBranches = GSON.fromJson(branches, SET_TYPE_TOKEN.getType());
		return encodedBranches.stream().map(VeracodeScaProject::decodeField)
			.collect(Collectors.toSet());
	}

	public void addBranches(Set<String> branches) {
		// Do not overwrite existing branches so as not to orphan issues
		Set<String> newBranches = getBranches();
		newBranches.addAll(branches);
		this.branches = newBranches.stream().map(VeracodeScaProject::encodeField)
			.collect(Collectors.toSet()).toString();
	}

	public String getDefaultBranch() {
		return defaultBranch;
	}

	public void setDefaultBranch(String defaultBranch) {
		this.defaultBranch = defaultBranch;
	}

	public VeracodeScaWorkspace getWorkspace() {
		return workspace;
	}

	public void setWorkspace(VeracodeScaWorkspace workspace) {
		this.workspace = workspace;
	}

	public List<VeracodeScaIssue> getIssues() {
		return issues;
	}

	public void setIssues(List<VeracodeScaIssue> issues) {
		this.issues = issues;
	}

	public ProjectModel getSynapseProject() {
		return synapseProject;
	}

	public void setSynapseProject(ProjectModel synapseProject) {
		this.synapseProject = synapseProject;
	}

	/**
	 * Gets all issues associated with this project whose branch is the default branch of this
	 * project. If no default branch is set, returns an empty list.
	 *
	 * @return list of issues associated with the default branch of this project
	 */
	public List<VeracodeScaIssue> getIssuesForDefaultBranch() {
		if (defaultBranch == null) {
			return Collections
				.emptyList();
		}
		return getIssues().stream()
			.filter(i -> i.getProjectBranch().equals(defaultBranch))
			.collect(Collectors.toList());
	}

	/**
	 * Gets all unresolved issues associated with this project whose branch is the default branch of
	 * this project. If no default branch is set, returns an empty list.
	 *
	 * @return list of unresolved issues associated with the default branch of this project
	 */
	public List<VeracodeScaIssue> getUnresolvedIssuesForDefaultBranch() {
		return getIssuesForDefaultBranch().stream().filter(i -> !i.isResolved())
			.collect(Collectors.toList());
	}

	/**
	 * Determines whether this project is vulnerable. A project is vulnerable if it has any
	 * unresolved issues associated with its default branch.
	 *
	 * @return true if there are unresolved issues, false otherwise
	 */
	public boolean isVulnerable() {
		return !getUnresolvedIssuesForDefaultBranch().isEmpty();
	}

	public String getDisplayName() {
		return workspace.getName() + " - " + getName();
	}

	private static String decodeField(String field) {
		String decodedField;
		try {
			decodedField = new String(Hex.decodeHex(field), StandardCharsets.UTF_8);
		} catch (DecoderException e) {
			decodedField = "";
		}
		return decodedField;
	}

	private static String encodeField(String field) {
		return Hex.encodeHexString(field.getBytes());
	}
}

