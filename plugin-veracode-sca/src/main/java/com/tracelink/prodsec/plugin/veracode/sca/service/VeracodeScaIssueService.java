package com.tracelink.prodsec.plugin.veracode.sca.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tracelink.prodsec.lib.veracode.rest.api.model.IssueSummary;
import com.tracelink.prodsec.plugin.veracode.sca.model.VeracodeScaIssue;
import com.tracelink.prodsec.plugin.veracode.sca.model.VeracodeScaProject;
import com.tracelink.prodsec.plugin.veracode.sca.model.issue.IssueStatus;
import com.tracelink.prodsec.plugin.veracode.sca.model.issue.IssueType;
import com.tracelink.prodsec.plugin.veracode.sca.repository.VeracodeScaIssueRepository;

/**
 * Service to store and retrieve data about Veracode SCA issues from the {@link
 * VeracodeScaIssueRepository}.
 */
@Service
public class VeracodeScaIssueService {

	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter
			.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

	private final VeracodeScaIssueRepository issueRepository;

	public VeracodeScaIssueService(@Autowired VeracodeScaIssueRepository issueRepository) {
		this.issueRepository = issueRepository;
	}

	/**
	 * Gets the earliest created date for any {@link VeracodeScaIssue} in the repository. If there
	 * are no issues, returns the current date. Caches the value after the first retrieval.
	 *
	 * @return earliest recorded date for any issue in the database
	 */
	public LocalDateTime getEarliestIssueDate() {
		VeracodeScaIssue issue = issueRepository.findFirstByOrderByCreatedDateAsc();
		if (issue == null) {
			return LocalDateTime.now();
		}
		return issue.getCreatedDate();
	}

	/**
	 * Updates the issue data stored in {@link VeracodeScaIssue} entities in the database. If there
	 * already exists an issue in the database associated with the given issue ID, updates that
	 * entity. Otherwise, it creates a new issue object and links it to the given {@link
	 * VeracodeScaProject}.
	 *
	 * @param issues  the list of current issues from the Veracode SCA server
	 * @param project the project the issues are associated with
	 */
	public void updateIssues(List<IssueSummary> issues, VeracodeScaProject project) {
		// If arguments are null, return
		if (issues == null || project == null) {
			return;
		}
		Set<VeracodeScaIssue> issueModels = new HashSet<>();
		issues.forEach(issue -> {
			// Get issue model with matching ID, or create a new one
			VeracodeScaIssue issueModel;
			Optional<VeracodeScaIssue> optionalIssueModel = issueRepository.findById(issue.getId());
			if (optionalIssueModel.isPresent()) {
				issueModel = optionalIssueModel.get();
			} else {
				issueModel = new VeracodeScaIssue();
				issueModel.setId(issue.getId());
			}
			// Update the issue model fields
			try {
				populateIssueModel(issueModel, issue, project);
				issueModels.add(issueModel);
			} catch (Exception e) {
				// Do not update issue if there is an exception
			}
		});
		issueRepository.saveAll(issueModels);
		issueRepository.flush();
	}

	/**
	 * Deletes any {@link VeracodeScaIssue} associated with the given project.
	 *
	 * @param project the project for which to delete all associated issues
	 * @throws IllegalArgumentException if the project is null
	 */
	public void deleteIssuesByProject(VeracodeScaProject project) {
		// Make sure project is not null
		if (project == null) {
			throw new IllegalArgumentException("Cannot delete issues for a null project");
		}
		// Delete all issues with the given project
		issueRepository.deleteByProject(project);
		// Flush before returning
		issueRepository.flush();
	}

	/**
	 * Populates the given {@link VeracodeScaIssue} with data from the given {@link IssueSummary}
	 * retrieved from the Veracode SCA server. Links the issue model with the given {@link
	 * VeracodeScaProject}.
	 *
	 * @param issueModel the Veracode SCA issue stored in Synapse
	 * @param issue      the issue summary retrieved from the Veracode SCA server
	 * @param project    the Veracode SCA project stored in Synapse that is associated with the
	 *                   given issue
	 */
	private void populateIssueModel(VeracodeScaIssue issueModel, IssueSummary issue,
			VeracodeScaProject project) {
		// Set issue type for the issue model and any fields specific to the issue type
		IssueType issueType;
		switch (issue.getIssueType()) {
			case LIBRARY:
				issueType = IssueType.LIBRARY;
				break;
			case LICENSE:
				issueType = IssueType.LICENSE;
				break;
			case VULNERABILITY:
				issueType = IssueType.VULNERABILITY;
				issueModel.setVulnerability(issue.getVulnerability().getTitle());
				issueModel.setVulnerableMethod(issue.isVulnerableMethod());
				break;
			default:
				throw new IllegalArgumentException("Invalid issue type");
		}
		issueModel.setIssueType(issueType);

		handleIssueResolution(issueModel, issue);
		issueModel.setCreatedDate(LocalDateTime.parse(issue.getCreatedDate(), DATE_TIME_FORMATTER));
		issueModel.setLastUpdatedDate(LocalDateTime.now());
		issueModel.setProject(project);
		issueModel.setProjectBranch(issue.getProjectBranch());
		issueModel.setSeverity(issue.getSeverity());
	}

	/**
	 * Updates the given {@link VeracodeScaIssue} with the data from the {@link IssueSummary} from
	 * the Veracode SCA server to make sure that the issue status aligns with the fixed or ignored
	 * date. Handles different cases of an issue being resolved or unresolved.
	 *
	 * @param issueModel the Veracode SCA issue stored in Synapse
	 * @param issue      the issue summary retrieved from the Veracode SCA server
	 */
	private void handleIssueResolution(VeracodeScaIssue issueModel, IssueSummary issue) {
		// If there is an update to the ignored status
		if (issueModel.isIgnored() != issue.isIgnored()) {
			// Set ignored date to now if the issue is newly ignored
			if (issue.isIgnored()) {
				issueModel.setIgnoredDate(LocalDateTime.now());
				// Set ignored date to null	if the issue is newly un-ignored
			} else {
				issueModel.setIgnoredDate(null);
			}
		}
		// Set the ignored status of the issue model
		issueModel.setIgnored(issue.isIgnored());

		// Get the issue status from the issue summary
		IssueStatus issueStatus;
		switch (issue.getIssueStatus()) {
			case FIXED:
				issueStatus = IssueStatus.FIXED;
				break;
			case OPEN:
				issueStatus = IssueStatus.OPEN;
				break;
			default:
				throw new IllegalArgumentException("Invalid issue status");
		}

		// If there is no issue status for the model or if there is an update to the issue status
		if (issueModel.getIssueStatus() == null || !issueModel.getIssueStatus()
				.equals(issueStatus)) {
			switch (issueStatus) {
				// Set fixed date to now if the issue is newly fixed
				case FIXED:
					issueModel.setFixedDate(LocalDateTime.now());
					break;
				// Set fixed date to null if the issue is newly re-opened
				case OPEN:
				default:
					issueModel.setFixedDate(null);
					break;
			}
		}
		// Set the issue status of the issue model
		issueModel.setIssueStatus(issueStatus);
	}
}
