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
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
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

	public VeracodeScaProjectService(@Autowired VeracodeScaProjectRepository projectRepository) {
		this.projectRepository = projectRepository;
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
		return projectRepository.findAll().stream().filter(p -> p.getWorkspace().isIncluded())
				.collect(Collectors.toList());
	}

	/**
	 * Gets all {@link VeracodeScaProject}s that are mapped to a Synapse {@link ProjectModel}.
	 *
	 * @return list of mapped Veracode SCA projects
	 */
	public List<VeracodeScaProject> getMappedProjects() {
		return projectRepository.findAllBySynapseProjectNotNull().stream()
				.filter(p -> p.getWorkspace().isIncluded())
				.collect(Collectors.toList());
	}

	/**
	 * Gets all {@link VeracodeScaProject}s that are not mapped to a Synapse {@link ProjectModel}.
	 *
	 * @return list of unmapped Veracode SCA projects
	 */
	public List<VeracodeScaProject> getUnmappedProjects() {
		return projectRepository.findAllBySynapseProjectIsNull().stream()
				.filter(p -> p.getWorkspace().isIncluded())
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
	 * @param workspace name of workspace the projects belongs to
	 */
	public void updateProjects(List<Project> projects, VeracodeScaWorkspace workspace) {
		List<VeracodeScaProject> projectModels = new ArrayList<>();
		projects.forEach(project -> {
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
			try {
				populateProjectModel(projectModel, project, workspace);
				projectModels.add(projectModel);
			} catch (VeracodeScaProductException e) {
				// Do not update project if default branch cannot be set
			}
		});
		projectRepository.saveAll(projectModels);
		projectRepository.flush();
	}

	private void populateProjectModel(VeracodeScaProject projectModel, Project project,
			VeracodeScaWorkspace workspace)
			throws VeracodeScaProductException {
		projectModel.setName(project.getName());
		projectModel.setSiteId(project.getSiteId());
		projectModel.setLastScanDate(LocalDateTime.parse(project.getLastScanDate(),
				dateTimeFormatter));
		// Do not overwrite existing branches so as not to orphan issues
		projectModel.addBranches(new HashSet<>(project.getBranches()));
		// Do not overwrite existing default branch settings
		if (projectModel.getDefaultBranch() == null) {
			String defaultBranch;
			if (project.getBranches().contains(DEVELOP_BRANCH)) {
				defaultBranch = DEVELOP_BRANCH;
			} else if (project.getBranches().contains(MASTER_BRANCH)) {
				defaultBranch = MASTER_BRANCH;
			} else if (!project.getBranches().isEmpty()) {
				// Randomly pick first branch
				defaultBranch = project.getBranches().get(0);
			} else {
				throw new VeracodeScaProductException("No branches for project");
			}
			projectModel.setDefaultBranch(defaultBranch);
		}
		projectModel.setWorkspace(workspace);
	}

	/*
	 * * * * * * * * * * * Scorecard methods * * * * * * * * *
	 */

	/**
	 * Gets the unresolved {@link VeracodeScaIssue}s for the default branch of the {@link
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
	 * Gets the unresolved {@link VeracodeScaIssue}s for the default branch of the {@link
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
			scaIssues.addAll(project.getUnresolvedIssuesForDefaultBranch());
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

	/**
	 * Sets the default branch for the {@link VeracodeScaProject} associated with the given project
	 * name. If the project does not exist or if the project does not have a branch with the given
	 * name, throws an exception.
	 *
	 * @param projectName   the name of the Veracode SCA project to set the default branch for
	 * @param defaultBranch the name of the branch to set as default
	 * @throws VeracodeScaProductException if the project does not exist or does not have the given
	 *                                     branch
	 */
	public void setDefaultBranch(String projectName, String defaultBranch)
			throws VeracodeScaProductException {
		VeracodeScaProject project = projectRepository.findByName(projectName);
		if (project == null) {
			throw new VeracodeScaProductException("No project found with the name: " + projectName);
		}
		if (defaultBranch == null || !project.getBranches().contains(defaultBranch)) {
			throw new VeracodeScaProductException(
					"No branch found with the name: " + defaultBranch);
		}
		project.setDefaultBranch(defaultBranch);
		projectRepository.saveAndFlush(project);
	}
}
