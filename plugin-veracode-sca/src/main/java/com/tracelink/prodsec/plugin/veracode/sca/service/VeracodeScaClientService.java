package com.tracelink.prodsec.plugin.veracode.sca.service;

import com.tracelink.prodsec.plugin.veracode.sca.exception.VeracodeScaClientException;
import com.tracelink.prodsec.plugin.veracode.sca.model.VeracodeScaClient;
import com.tracelink.prodsec.plugin.veracode.sca.model.VeracodeScaProject;
import com.tracelink.prodsec.plugin.veracode.sca.model.VeracodeScaWorkspace;
import com.tracelink.prodsec.plugin.veracode.sca.repository.VeracodeScaClientRepository;
import com.tracelink.prodsec.plugin.veracode.sca.util.api.VeracodeScaApiWrapper;
import com.tracelink.prodsec.plugin.veracode.sca.util.api.VeracodeScaPagedResourcesIterator;
import com.tracelink.prodsec.plugin.veracode.sca.util.model.IssueSummary;
import com.tracelink.prodsec.plugin.veracode.sca.util.model.PagedResourcesIssueSummary;
import com.tracelink.prodsec.plugin.veracode.sca.util.model.PagedResourcesProject;
import com.tracelink.prodsec.plugin.veracode.sca.util.model.PagedResourcesWorkspace;
import com.tracelink.prodsec.plugin.veracode.sca.util.model.Project;
import com.tracelink.prodsec.plugin.veracode.sca.util.model.Workspace;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service to store and retrieve data about the Veracode SCA API client from the {@link
 * VeracodeScaClientRepository}. Also handles logic to fetch data from the Veracode SCA server.
 */
@Service
public class VeracodeScaClientService {

	private static final Logger LOGGER = LoggerFactory.getLogger(VeracodeScaClientService.class);
	private final VeracodeScaWorkspaceService workspaceService;
	private final VeracodeScaProjectService projectService;
	private final VeracodeScaIssueService issueService;
	private final VeracodeScaClientRepository clientRepository;
	private final VeracodeScaApiWrapper apiWrapper;

	public VeracodeScaClientService(@Autowired VeracodeScaWorkspaceService workspaceService,
			@Autowired VeracodeScaProjectService projectService,
			@Autowired VeracodeScaIssueService issueService,
			@Autowired VeracodeScaClientRepository clientRepository) {
		this.workspaceService = workspaceService;
		this.projectService = projectService;
		this.issueService = issueService;
		this.clientRepository = clientRepository;
		this.apiWrapper = new VeracodeScaApiWrapper();
	}

	/**
	 * Determines whether all API calls can be made to the Veracode SCA server, using the
	 * configured API client settings.
	 */
	public void testConnection() {
		VeracodeScaClient client;
		try {
			client = getClient();
			apiWrapper.setClient(client);
			PagedResourcesWorkspace workspaces = apiWrapper.getWorkspaces(0);
			for (Workspace workspace : workspaces.getEmbedded().getWorkspaces()) {
				PagedResourcesProject projects = apiWrapper.getProjects(workspace.getId(), 0);
				for (Project project : projects.getEmbedded().getProjects()) {
					PagedResourcesIssueSummary issueSummaries = apiWrapper
							.getIssue(workspace.getId(), project.getId());
					for (IssueSummary issueSummary : issueSummaries.getEmbedded().getIssues()) {
						if (issueSummary != null) {
							return; // Successfully tested all API calls
						}
					}
				}
			}
		} catch (Exception e) {
			throw new VeracodeScaClientException(e.getMessage());
		}
		throw new VeracodeScaClientException(
				"Client has access to Veracode but cannot view issues");
	}

	/**
	 * Fetches current issues data from the Veracode SCA server. Stores Veracode SCA workspaces,
	 * projects and issues in the database.
	 */
	public void fetchData() {
		LOGGER.info("Starting data fetch for Veracode SCA");
		VeracodeScaClient client;
		try {
			client = getClient();
			apiWrapper.setClient(client);
		} catch (VeracodeScaClientException e) {
			LOGGER.info(e.getMessage());
			return;
		}

		try {
			fetchWorkspaceData();
		} catch (Exception e) {
			LOGGER.error("An error occurred while fetching Veracode SCA data", e);
		}
		LOGGER.info("Finished data fetch for Veracode SCA");
	}

	/**
	 * Gets the configured Veracode SCA API client from the database.
	 *
	 * @return the configured API client
	 * @throws VeracodeScaClientException if no API client is configured
	 */
	public VeracodeScaClient getClient() throws VeracodeScaClientException {
		List<VeracodeScaClient> clients = clientRepository.findAll();
		if (clients.isEmpty()) {
			throw new VeracodeScaClientException("No Veracode SCA client configured.");
		}
		return clients.get(0);
	}

	/**
	 * Sets the values of the Veracode SCA API client in the database. If no API client is currently
	 * configured, it will create a new entity. Otherwise, it will update the existing entity.
	 *
	 * @param apiId        API ID for the Veracode SCA server
	 * @param apiSecretKey API secret key for the Veracode SCA server
	 * @return true if the API client is set, false otherwise
	 */
	public boolean setClient(String apiId, String apiSecretKey) {
		if (apiId == null || apiSecretKey == null) {
			return false;
		}

		List<VeracodeScaClient> clients = clientRepository.findAll();
		VeracodeScaClient client;
		if (clients.isEmpty()) {
			client = new VeracodeScaClient();
		} else {
			client = clients.get(0);
		}
		client.setApiId(apiId);
		client.setApiSecretKey(apiSecretKey);
		clientRepository.saveAndFlush(client);
		return true;
	}

	/**
	 * Helper for the {@link VeracodeScaClientService#fetchData()} method. Gets workspaces from the
	 * Veracode SCA API and fetches project data for each workspace.
	 */
	private void fetchWorkspaceData() {
		VeracodeScaPagedResourcesIterator<PagedResourcesWorkspace> workspacesIterator = new VeracodeScaPagedResourcesIterator<>(
				apiWrapper::getWorkspaces);
		// Get all workspaces, one page at a time
		while (workspacesIterator.hasNext()) {
			List<Workspace> workspaces = workspacesIterator.next().getEmbedded()
					.getWorkspaces();
			List<VeracodeScaWorkspace> workspaceModels = workspaceService
					.updateWorkspaces(workspaces);
			fetchProjectData(workspaceModels);
		}
	}

	/**
	 * Helper for the {@link VeracodeScaClientService#fetchData()} method. Gets projects from the
	 * Veracode SCA API and fetches issue and branch data for each project.
	 *
	 * @param workspaceModels the list of workspaces for which to fetch projects
	 */
	private void fetchProjectData(List<VeracodeScaWorkspace> workspaceModels) {
		for (VeracodeScaWorkspace workspaceModel : workspaceModels) {
			VeracodeScaPagedResourcesIterator<PagedResourcesProject> projectsIterator = new VeracodeScaPagedResourcesIterator<>(
					page -> apiWrapper
							.getProjects(workspaceModel.getId(), page));
			// Get all projects for this workspace, one page at a time
			while (projectsIterator.hasNext()) {
				List<Project> projects = projectsIterator.next().getEmbedded()
						.getProjects();
				Map<UUID, String> branches = getVisibleBranches(workspaceModel, projects);
				List<VeracodeScaProject> projectModels = projectService
						.updateProjects(projects, workspaceModel, branches);
				fetchIssuesData(workspaceModel, projectModels);
			}
		}
	}

	/**
	 * Helper for the {@link VeracodeScaClientService#fetchData()} method. Gets a single issue for
	 * each project in the given list to determine the visible branch for each project.
	 *
	 * @param workspaceModel the workspace of the projects
	 * @param projects       the list of projects for which to determine the visible branch
	 * @return map from project ID to visible branch
	 */
	private Map<UUID, String> getVisibleBranches(VeracodeScaWorkspace workspaceModel,
			List<Project> projects) {
		Map<UUID, String> branches = new HashMap<>();
		for (Project project : projects) {
			PagedResourcesIssueSummary pagedResourcesIssueSummary = apiWrapper
					.getIssue(workspaceModel.getId(), project.getId());
			List<IssueSummary> issues = pagedResourcesIssueSummary.getEmbedded().getIssues();
			if (issues.isEmpty()) {
				LOGGER.debug("No issues associated with visible branch for project " + project
						.getName());
			} else {
				String branch = issues.get(0).getProjectBranch();
				branches.put(project.getId(), branch);
			}
		}
		return branches;
	}

	/**
	 * Helper for the {@link VeracodeScaClientService#fetchData()} method. Gets issues from the
	 * Veracode SCA API for the given workspace and projects.
	 *
	 * @param workspaceModel the workspace of the projects
	 * @param projectModels  the list of projects for which to fetch issues
	 */
	private void fetchIssuesData(VeracodeScaWorkspace workspaceModel,
			List<VeracodeScaProject> projectModels) {
		for (VeracodeScaProject projectModel : projectModels) {
			VeracodeScaPagedResourcesIterator<PagedResourcesIssueSummary> issuesIterator = new VeracodeScaPagedResourcesIterator<>(
					page -> apiWrapper.getIssues(workspaceModel.getId(), projectModel.getId(),
							projectModel.getVisibleBranch(), page));
			// Get all issues for this project and branch combo, one page at a time
			while (issuesIterator.hasNext()) {
				List<IssueSummary> issues = issuesIterator.next().getEmbedded().getIssues();
				issueService.updateIssues(issues, projectModel);
			}
		}
	}
}
