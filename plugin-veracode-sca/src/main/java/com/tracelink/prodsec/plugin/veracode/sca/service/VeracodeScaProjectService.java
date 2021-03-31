package com.tracelink.prodsec.plugin.veracode.sca.service;

import com.tracelink.prodsec.plugin.veracode.sca.exception.VeracodeScaProductException;
import com.tracelink.prodsec.plugin.veracode.sca.model.VeracodeScaIssue;
import com.tracelink.prodsec.plugin.veracode.sca.model.VeracodeScaProject;
import com.tracelink.prodsec.plugin.veracode.sca.model.VeracodeScaWorkspace;
import com.tracelink.prodsec.plugin.veracode.sca.repository.VeracodeScaProjectRepository;
import com.tracelink.prodsec.plugin.veracode.sca.util.model.Project;
import com.tracelink.prodsec.synapse.products.model.ProductLineModel;
import com.tracelink.prodsec.synapse.products.model.ProjectModel;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Service to store and retrieve data about Veracode SCA projects from the {@link
 * VeracodeScaProjectRepository}.
 *
 * @author mcool
 */
@Service
public class VeracodeScaProjectService {

	private static final String DEVELOP_BRANCH = "develop";
	private static final String MASTER_BRANCH = "master";
	private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter
			.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

	private final VeracodeScaProjectRepository projectRepository;
	private final VeracodeScaIssueService issueService;

	public VeracodeScaProjectService(@Autowired VeracodeScaProjectRepository projectRepository,
			@Autowired VeracodeScaIssueService issueService) {
		this.projectRepository = projectRepository;
		this.issueService = issueService;
	}

	/*
	 * * * * * * * * * * * * Repository methods * * * * * * * * * *
	 */

	/**
	 * Gets all {@link VeracodeScaProject}s.
	 *
	 * @return list of Veracode SCA projects
	 */
	public List<VeracodeScaProject> getProjects() {
		return projectRepository.findAll();
	}

	/**
	 * Gets all {@link VeracodeScaProject}s whose ID is in the given list.
	 *
	 * @param projectIds list of project IDs to get projects for
	 * @return list of Veracode SCA projects
	 */
	public List<VeracodeScaProject> getProjects(List<UUID> projectIds) {
		return projectRepository.findByIdIn(projectIds);
	}

	/**
	 * Gets all {@link VeracodeScaProject}s.
	 *
	 * @return list of included Veracode SCA projects
	 */
	public List<VeracodeScaProject> getIncludedProjects() {
		return projectRepository.findAll().stream()
				.filter(p -> p.getWorkspace().isIncluded() && p.isIncluded())
				.collect(Collectors.toList());
	}

	/**
	 * Gets all {@link VeracodeScaProject}s that are mapped to a Synapse {@link ProjectModel}.
	 *
	 * @return list of mapped Veracode SCA projects
	 */
	public List<VeracodeScaProject> getMappedProjects() {
		return projectRepository.findAllBySynapseProjectNotNull().stream()
				.filter(p -> p.getWorkspace().isIncluded() && p.isIncluded())
				.collect(Collectors.toList());
	}

	/**
	 * Gets all {@link VeracodeScaProject}s that are not mapped to a Synapse {@link ProjectModel}.
	 *
	 * @return list of unmapped Veracode SCA projects
	 */
	public List<VeracodeScaProject> getUnmappedProjects() {
		return projectRepository.findAllBySynapseProjectIsNull().stream()
				.filter(p -> p.getWorkspace().isIncluded() && p.isIncluded())
				.collect(Collectors.toList());
	}

	/**
	 * Gets the {@link VeracodeScaProject} with the given ID, or returns null.
	 *
	 * @param id of the project to get
	 * @return project with the given ID, or null
	 */
	public VeracodeScaProject getProject(UUID id) {
		return projectRepository.findById(id).orElse(null);
	}

	/**
	 * Updates each {@link VeracodeScaProject} stored in the database whose ID matches a project in
	 * the given list. Updates all values of the project model according to to the values of the
	 * given project.
	 *
	 * @param projects  list of projects to use to update the project models stored in the database
	 * @param workspace workspace the projects belongs to
	 * @param branches  map from project ID to visible branch to set on each project model
	 * @return the updated project models for each project in the given list
	 */
	public List<VeracodeScaProject> updateProjects(List<Project> projects,
			VeracodeScaWorkspace workspace, Map<UUID, String> branches) {
		List<VeracodeScaProject> projectModels = new ArrayList<>();
		for (Project project : projects) {
			// Get project model with matching ID, or create a new one
			VeracodeScaProject projectModel;
			Optional<VeracodeScaProject> optionalProjectModel = projectRepository
					.findById(project.getId());
			if (optionalProjectModel.isPresent()) {
				projectModel = optionalProjectModel.get();
			} else {
				projectModel = new VeracodeScaProject();
				projectModel.setId(project.getId());
			}
			// Update the project model fields
			populateProjectModel(projectModel, project, workspace,
					branches.getOrDefault(project.getId(), null));
			projectModels.add(projectModel);
		}
		projectModels = projectRepository.saveAll(projectModels);
		projectRepository.flush();
		return projectModels;
	}

	/**
	 * Includes all {@link VeracodeScaProject}s with the given project IDs. Excludes any project
	 * whose ID is not in the list.
	 *
	 * @param projectIds list of IDs of the Veracode SCA projects to set as included
	 * @throws IllegalArgumentException if any of the given IDs are null
	 */
	public void setIncluded(List<UUID> projectIds) throws IllegalArgumentException {
		// Make sure all projectIds are not null
		if (projectIds == null || projectIds.stream().anyMatch(Objects::isNull)) {
			throw new IllegalArgumentException("Please provide non-null project IDs to include");
		}
		// For each page of projects, update whether or not the project is included and save all
		Page<VeracodeScaProject> pagedProjects = null;
		do {
			Pageable pageRequest = (pagedProjects == null) ? PageRequest.of(0, 50)
					: pagedProjects.nextPageable();
			pagedProjects = projectRepository.findAll(pageRequest);
			pagedProjects.forEach(
					project -> project.setIncluded(projectIds.contains(project.getId())));
			projectRepository.saveAll(pagedProjects);
		} while (pagedProjects.hasNext());
		// Flush before returning
		projectRepository.flush();
	}

	/**
	 * Deletes the {@link VeracodeScaProject} with the given ID. Also deletes any {@link
	 * VeracodeScaIssue} that is associated with the project to avoid orphaned issues.
	 *
	 * @param projectId the ID of the project to delete
	 * @throws IllegalArgumentException    if the project ID is null
	 * @throws VeracodeScaProductException if there is no project with the given ID
	 */
	public void deleteProject(UUID projectId)
			throws IllegalArgumentException, VeracodeScaProductException {
		// Make sure the project ID is not null
		if (projectId == null) {
			throw new IllegalArgumentException("Please provide a non-null project ID to delete");
		}
		// Make sure the project exists
		VeracodeScaProject project = projectRepository.findById(projectId).orElse(null);
		if (project == null) {
			throw new VeracodeScaProductException("No project with the given ID exists");
		}
		// Delete issues associated with the project
		issueService.deleteIssuesByProject(project);
		// Delete the project
		projectRepository.delete(project);
		// Flush before returning
		projectRepository.flush();
	}

	/**
	 * Deletes any {@link VeracodeScaProject} associated with the given {@link
	 * VeracodeScaWorkspace}. Also deletes any {@link VeracodeScaIssue} that is associated with the
	 * projects to avoid orphaned issues.
	 *
	 * @param workspace the workspace for which to delete all associated projects
	 * @throws IllegalArgumentException if the workspace is null
	 */
	public void deleteProjectsByWorkspace(VeracodeScaWorkspace workspace)
			throws IllegalArgumentException {
		// Make sure the workspace is not null
		if (workspace == null) {
			throw new IllegalArgumentException("Cannot delete projects for a null workspace");
		}
		// Iterate through pages of projects
		Page<VeracodeScaProject> projectsPage = null;
		do {
			Pageable pageRequest = (projectsPage == null) ? PageRequest.of(0, 100)
					: projectsPage.nextPageable();
			projectsPage = projectRepository.findAllByWorkspace(workspace, pageRequest);
			// Delete all issues associated with these projects
			projectsPage.forEach(issueService::deleteIssuesByProject);
			// Delete all the projects
			projectRepository.deleteAll(projectsPage);
		} while (projectsPage.hasNext());
		// Flush before returning
		projectRepository.flush();
	}

	private void populateProjectModel(VeracodeScaProject projectModel, Project project,
			VeracodeScaWorkspace workspace, String visibleBranch) {
		projectModel.setName(project.getName());
		projectModel.setSiteId(project.getSiteId());
		projectModel.setLastScanDate(LocalDateTime.parse(project.getLastScanDate(),
				dateTimeFormatter));
		projectModel.setVisibleBranch(visibleBranch);
		projectModel.setWorkspace(workspace);
	}

	/*
	 * * * * * * * * * * * Scorecard methods * * * * * * * * *
	 */

	/**
	 * Gets the unresolved {@link VeracodeScaIssue}s for the visible branch of the {@link
	 * VeracodeScaProject} mapped to each Synapse {@link ProjectModel} in the given Synapse {@link
	 * ProductLineModel}. List will be null if none of the projects in the given product line are
	 * mapped.
	 *
	 * @param synapseProductLine Synapse product line to gather metrics for
	 * @return list of unresolved Veracode SCA issues associated with the given product line
	 */
	public List<VeracodeScaIssue> getUnresolvedIssuesForProductLine(
			ProductLineModel synapseProductLine) {
		List<List<VeracodeScaIssue>> issuesLists = synapseProductLine.getProjects().stream()
				.map(this::getUnresolvedIssuesForProject).filter(Objects::nonNull)
				.collect(Collectors.toList());
		if (issuesLists.isEmpty()) {
			return null;
		}
		return issuesLists.stream().flatMap(List::stream).collect(Collectors.toList());
	}

	/**
	 * Gets the unresolved {@link VeracodeScaIssue}s for the visible branch of the {@link
	 * VeracodeScaProject} mapped to the given Synapse {@link ProjectModel}. Will return null if
	 * the {@link ProjectModel} is not mapped.
	 *
	 * @param synapseProject Synapse project to gather metrics for
	 * @return list of Veracode SCA metrics associated with the given project
	 */
	public List<VeracodeScaIssue> getUnresolvedIssuesForProject(ProjectModel synapseProject) {
		List<VeracodeScaProject> projects = projectRepository.findBySynapseProject(synapseProject);
		if (projects.isEmpty()) {
			return null;
		}
		List<VeracodeScaIssue> scaIssues = new ArrayList<>();
		for (VeracodeScaProject project : projects) {
			scaIssues.addAll(project.getUnresolvedIssuesForVisibleBranch());
		}
		return scaIssues;
	}

	/*
	 * * * * * * * * * * Mapping methods * * * * * * * *
	 */

	/**
	 * Creates a mapping between the given {@link ProjectModel} and the {@link VeracodeScaProject}
	 * associated with the given project name. If the project does not exist, no mapping is created.
	 *
	 * @param synapseProject the Synapse project to be mapped
	 * @param projectName    the name of the Veracode SCA project to be mapped
	 */
	public void createMapping(ProjectModel synapseProject, String projectName) {

		VeracodeScaProject project = projectRepository.findByName(projectName);
		if (project != null && synapseProject != null) {
			project.setSynapseProject(synapseProject);
			projectRepository.saveAndFlush(project);
		}
	}

	/**
	 * Deletes the mapping for the {@link VeracodeScaProject} associated with the given project
	 * name. If the project does not exist, does nothing.
	 *
	 * @param projectName the name of the Veracode SCA project to be unmapped
	 */
	public void deleteMapping(String projectName) {
		VeracodeScaProject project = projectRepository.findByName(projectName);
		if (project != null) {
			project.setSynapseProject(null);
			projectRepository.saveAndFlush(project);
		}
	}
}
