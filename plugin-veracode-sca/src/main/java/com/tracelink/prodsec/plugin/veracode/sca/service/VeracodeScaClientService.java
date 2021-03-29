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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
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
	 * Determines whether a connection can be established with the Veracode SCA server, using the
	 * configured API client settings.
	 *
	 * @return true if data can be fetched, false if no API client is configured or if API client
	 * ID or secret key are incorrect
	 */
	public boolean testConnection() {
		VeracodeScaClient client;
		try {
			client = getClient();
			apiWrapper.setClient(client);
			apiWrapper.getWorkspaces(0);
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			return false;
		}
		return true;
	}

	/**
	 * Fetches current issues data from the Veracode SCA server. Stores Veracode SCA projects and
	 * issues in the database.
	 */
	public void fetchData() {
		VeracodeScaClient client;
		try {
			client = getClient();
			apiWrapper.setClient(client);
		} catch (VeracodeScaClientException e) {
			LOGGER.error(e.getMessage());
			return;
		}

		try {
			VeracodeScaPagedResourcesIterator<PagedResourcesWorkspace> workspacesIterator = new VeracodeScaPagedResourcesIterator<>(
					apiWrapper::getWorkspaces);
			// Get all workspaces, one page at a time
			while (workspacesIterator.hasNext()) {
				List<Workspace> workspaces = workspacesIterator.next().getEmbedded()
						.getWorkspaces();
				List<VeracodeScaWorkspace> workspaceModels = workspaceService
						.updateWorkspaces(workspaces);
				for (VeracodeScaWorkspace workspaceModel : workspaceModels) {
					VeracodeScaPagedResourcesIterator<PagedResourcesProject> projectsIterator = new VeracodeScaPagedResourcesIterator<>(
							page -> apiWrapper
									.getProjects(workspaceModel.getId().toString(), page));
					// Get all projects for this workspace, one page at a time
					while (projectsIterator.hasNext()) {
						List<Project> projects = projectsIterator.next().getEmbedded()
								.getProjects();
						projectService.updateProjects(projects, workspaceModel);
					}
					VeracodeScaPagedResourcesIterator<PagedResourcesIssueSummary> issuesIterator = new VeracodeScaPagedResourcesIterator<>(
							page -> apiWrapper.getIssues(workspaceModel.getId().toString(), page));
					// Get all issues for this workspace, one page at a time
					while (issuesIterator.hasNext()) {
						List<IssueSummary> issues = issuesIterator.next().getEmbedded().getIssues();
						List<VeracodeScaProject> projects = projectService.getProjects(
								issues.stream().map(IssueSummary::getProjectId).distinct()
										.collect(Collectors.toList()));
						issueService.updateIssues(issues, projects);
					}
				}
			}
		} catch (Exception e) {
			LOGGER.error("An error occurred while fetching Veracode SCA data: " + e.getMessage());
		}
	}

	/**
	 * Gets the configured Veracode SCA API client from the database.
	 *
	 * @return the configured API client
	 * @throws VeracodeScaClientException if no API client is configured
	 */
	public VeracodeScaClient getClient() throws VeracodeScaClientException {
		List<VeracodeScaClient> clients;
		try {
			clients = clientRepository.findAll();
			/* Exception is transformed by the Spring exception handling logic, so we have to catch
			 * a generic RuntimeException here
			 */
		} catch (RuntimeException e) {
			/*
			 * This allows us to at least render the UI, but we still cannot save a new value to
			 * the client via the UI.
			 */
			clients = Collections.emptyList();
		}
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
}
