package com.tracelink.prodsec.plugin.veracode.sca.model;

import com.tracelink.prodsec.plugin.veracode.sca.VeracodeScaPlugin;
import com.tracelink.prodsec.synapse.products.model.ProjectModel;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
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
	 * The visible branch for this project. Statistics will be gathered only for the issues
	 * associated with the visible branch of this project. The visible branch is either the default
	 * branch for the project or the last scanned branch, depending on the settings configured in
	 * Veracode. If there are no open issues for the default branch (or last branch scanned),
	 * Synapse cannot detect the visible branch, and this value will be null.
	 */
	@Column(name = "visible_branch")
	private String visibleBranch;

	/**
	 * The workspace this project belongs to.
	 */
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "workspace_id")
	private VeracodeScaWorkspace workspace;

	/**
	 * Whether the issues associated with this project should be included by Synapse. If excluded,
	 * this project and the issues associated with it will not be displayed in graphs, summary
	 * statistics, or the issues page. Note that even if this value is true, this project will be
	 * excluded if the workspace it is associated with is excluded.
	 */
	@Column(name = "included")
	private boolean included = true;

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

	public String getVisibleBranch() {
		return visibleBranch;
	}

	public void setVisibleBranch(String visibleBranch) {
		this.visibleBranch = visibleBranch;
	}

	public VeracodeScaWorkspace getWorkspace() {
		return workspace;
	}

	public void setWorkspace(VeracodeScaWorkspace workspace) {
		this.workspace = workspace;
	}

	public boolean isIncluded() {
		return included;
	}

	public void setIncluded(boolean included) {
		this.included = included;
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
	 * Gets all issues associated with this project whose branch is the visible branch of this
	 * project. If no visible branch is set, returns an empty list.
	 *
	 * @return list of issues associated with the visible branch of this project
	 */
	public List<VeracodeScaIssue> getIssuesForVisibleBranch() {
		if (visibleBranch == null) {
			return Collections.emptyList();
		}
		return getIssues().stream()
				.filter(i -> i.getProjectBranch().equals(visibleBranch))
				.collect(Collectors.toList());
	}

	/**
	 * Gets all unresolved issues associated with this project whose branch is the visible branch of
	 * this project. If no visible branch is set, returns an empty list.
	 *
	 * @return list of unresolved issues associated with the visible branch of this project
	 */
	public List<VeracodeScaIssue> getUnresolvedIssuesForVisibleBranch() {
		return getIssuesForVisibleBranch().stream().filter(i -> !i.isResolved())
				.collect(Collectors.toList());
	}

	/**
	 * Determines whether this project is vulnerable. A project is vulnerable if it has any
	 * unresolved issues associated with its visible branch.
	 *
	 * @return true if there are unresolved issues, false otherwise
	 */
	public boolean isVulnerable() {
		return !getUnresolvedIssuesForVisibleBranch().isEmpty();
	}

	/**
	 * Gets a project display name, which includes both the workspace name and project name for
	 * clarity.
	 *
	 * @return display name of the project
	 */
	public String getDisplayName() {
		return workspace.getName() + " - " + getName();
	}
}

