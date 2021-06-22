package com.tracelink.prodsec.plugin.veracode.sca.service;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.mockito.Mockito.times;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.google.gson.Gson;
import com.tracelink.prodsec.plugin.veracode.sca.exception.VeracodeScaClientException;
import com.tracelink.prodsec.plugin.veracode.sca.mock.LoggerRule;
import com.tracelink.prodsec.plugin.veracode.sca.model.VeracodeScaClient;
import com.tracelink.prodsec.plugin.veracode.sca.model.VeracodeScaProject;
import com.tracelink.prodsec.plugin.veracode.sca.model.VeracodeScaWorkspace;
import com.tracelink.prodsec.plugin.veracode.sca.repository.VeracodeScaClientRepository;
import com.tracelink.prodsec.plugin.veracode.sca.util.model.IssueSummaries;
import com.tracelink.prodsec.plugin.veracode.sca.util.model.IssueSummary;
import com.tracelink.prodsec.plugin.veracode.sca.util.model.PageMetadata;
import com.tracelink.prodsec.plugin.veracode.sca.util.model.PagedResourcesIssueSummary;
import com.tracelink.prodsec.plugin.veracode.sca.util.model.PagedResourcesProject;
import com.tracelink.prodsec.plugin.veracode.sca.util.model.PagedResourcesWorkspace;
import com.tracelink.prodsec.plugin.veracode.sca.util.model.Project;
import com.tracelink.prodsec.plugin.veracode.sca.util.model.Projects;
import com.tracelink.prodsec.plugin.veracode.sca.util.model.Workspace;
import com.tracelink.prodsec.plugin.veracode.sca.util.model.Workspaces;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class VeracodeScaClientServiceTest {

	@MockBean
	private VeracodeScaWorkspaceService workspaceService;

	@MockBean
	private VeracodeScaProjectService projectService;

	@MockBean
	private VeracodeScaIssueService issueService;

	@MockBean
	private VeracodeScaClientRepository clientRepository;

	@Rule
	public final LoggerRule loggerRule = LoggerRule.forClass(VeracodeScaClientService.class);

	@Rule
	public final WireMockRule wireMockRule = new WireMockRule(
			WireMockConfiguration.wireMockConfig().dynamicPort());

	private static final Gson GSON = new Gson();
	private static final PageMetadata page = new PageMetadata();
	private static final Map<String, StringValuePattern> queryParams = new HashMap<>();
	private VeracodeScaClientService clientService;
	private VeracodeScaClient client;

	@Before
	public void setup() {
		clientService = new VeracodeScaClientService(workspaceService, projectService, issueService,
				clientRepository);
		client = new VeracodeScaClient();
		client.setApiUrl(wireMockRule.baseUrl());
		client.setApiId("abcdef123456");
		client.setApiSecretKey("abcdef1234567890");

		page.setNumber(0);
		page.setSize(1);
		page.setTotalElements(1);
		page.setTotalPages(1);

		queryParams.put("page", equalTo("0"));
		queryParams.put("size", equalTo("50"));
	}

	@Test
	public void testTestConnectionFailClient() {
		BDDMockito.when(clientRepository.findAll()).thenReturn(Collections.emptyList());
		try {
			clientService.testConnection();
			Assert.fail("Exception should have been thrown");
		} catch (VeracodeScaClientException e) {
			Assert.assertEquals("No Veracode SCA client configured.", e.getMessage());
		}
	}

	@Test
	public void testTestConnectionFailVeracode() {
		BDDMockito.when(clientRepository.findAll()).thenReturn(Collections.singletonList(client));
		WireMock.stubFor(WireMock.get(urlPathEqualTo("/v3/workspaces")).withQueryParams(queryParams)
				.willReturn(WireMock.aResponse().withStatus(401)));
		try {
			clientService.testConnection();
			Assert.fail("Exception should have been thrown");
		} catch (VeracodeScaClientException e) {
			Assert.assertTrue(e.getMessage().contains("Received status code 401"));
		}
	}

	@Test
	public void testTestConnection() {
		BDDMockito.when(clientRepository.findAll()).thenReturn(Collections.singletonList(client));

		PagedResourcesWorkspace pagedWorkspaces = new PagedResourcesWorkspace();
		Workspaces workspacesList = new Workspaces();
		Workspace workspace = new Workspace();
		workspace.setId(UUID.randomUUID());
		workspacesList.setWorkspaces(Collections.singletonList(workspace));
		pagedWorkspaces.setEmbedded(workspacesList);
		pagedWorkspaces.setPage(page);

		PagedResourcesProject pagedProjects = new PagedResourcesProject();
		Projects projectsList = new Projects();
		Project project = new Project();
		project.setId(UUID.randomUUID());
		projectsList.setProjects(Collections.singletonList(project));
		pagedProjects.setEmbedded(projectsList);
		pagedProjects.setPage(page);

		PagedResourcesIssueSummary pagedIssues = new PagedResourcesIssueSummary();
		IssueSummaries issueSummaries = new IssueSummaries();
		IssueSummary issue = new IssueSummary();
		issue.setId(UUID.randomUUID());
		issueSummaries.setIssues(Collections.singletonList(issue));
		pagedIssues.setEmbedded(issueSummaries);
		pagedIssues.setPage(page);

		WireMock.stubFor(WireMock.get(urlPathEqualTo("/v3/workspaces")).withQueryParams(queryParams)
				.willReturn(WireMock.okJson(GSON.toJson(pagedWorkspaces))));

		WireMock.stubFor(WireMock.get(
				urlPathEqualTo(String.format("/v3/workspaces/%s/projects", workspace.getId())))
				.withQueryParams(queryParams)
				.willReturn(WireMock.okJson(GSON.toJson(pagedProjects))));

		Map<String, StringValuePattern> issuesQueryParams = new HashMap<>(queryParams);
		issuesQueryParams.put("size", equalTo("1"));
		issuesQueryParams.put("project_id", equalTo(project.getId().toString()));
		issuesQueryParams.put("status", equalTo("open,fixed"));

		WireMock.stubFor(WireMock.get(
				urlPathEqualTo(String.format("/v3/workspaces/%s/issues", workspace.getId())))
				.withQueryParams(issuesQueryParams)
				.willReturn(WireMock.okJson(GSON.toJson(pagedIssues))));

		clientService.testConnection(); // No exception is thrown
	}

	@Test
	public void testTestConnectionNullIssue() {
		BDDMockito.when(clientRepository.findAll()).thenReturn(Collections.singletonList(client));

		PagedResourcesWorkspace pagedWorkspaces = new PagedResourcesWorkspace();
		Workspaces workspacesList = new Workspaces();
		Workspace workspace = new Workspace();
		workspace.setId(UUID.randomUUID());
		workspacesList.setWorkspaces(Collections.singletonList(workspace));
		pagedWorkspaces.setEmbedded(workspacesList);
		pagedWorkspaces.setPage(page);

		PagedResourcesProject pagedProjects = new PagedResourcesProject();
		Projects projectsList = new Projects();
		Project project = new Project();
		project.setId(UUID.randomUUID());
		projectsList.setProjects(Collections.singletonList(project));
		pagedProjects.setEmbedded(projectsList);
		pagedProjects.setPage(page);

		PagedResourcesIssueSummary pagedIssues = new PagedResourcesIssueSummary();
		IssueSummaries issueSummaries = new IssueSummaries();
		issueSummaries.setIssues(Collections.singletonList(null));
		pagedIssues.setEmbedded(issueSummaries);
		pagedIssues.setPage(page);

		WireMock.stubFor(WireMock.get(urlPathEqualTo("/v3/workspaces")).withQueryParams(queryParams)
				.willReturn(WireMock.okJson(GSON.toJson(pagedWorkspaces))));

		WireMock.stubFor(WireMock.get(
				urlPathEqualTo(String.format("/v3/workspaces/%s/projects", workspace.getId())))
				.withQueryParams(queryParams)
				.willReturn(WireMock.okJson(GSON.toJson(pagedProjects))));

		Map<String, StringValuePattern> issuesQueryParams = new HashMap<>(queryParams);
		issuesQueryParams.put("size", equalTo("1"));
		issuesQueryParams.put("project_id", equalTo(project.getId().toString()));
		issuesQueryParams.put("status", equalTo("open,fixed"));

		WireMock.stubFor(WireMock.get(
				urlPathEqualTo(String.format("/v3/workspaces/%s/issues", workspace.getId())))
				.withQueryParams(issuesQueryParams)
				.willReturn(WireMock.okJson(GSON.toJson(pagedIssues))));

		try {
			clientService.testConnection();
			Assert.fail("Should have thrown exception");
		} catch (VeracodeScaClientException e) {
			Assert.assertEquals("Client has access to Veracode but cannot view issues",
					e.getMessage());
		}
	}

	@Test
	public void testTestConnectionNoIssues() {
		BDDMockito.when(clientRepository.findAll()).thenReturn(Collections.singletonList(client));

		PagedResourcesWorkspace pagedWorkspaces = new PagedResourcesWorkspace();
		Workspaces workspacesList = new Workspaces();
		Workspace workspace = new Workspace();
		workspace.setId(UUID.randomUUID());
		workspacesList.setWorkspaces(Collections.singletonList(workspace));
		pagedWorkspaces.setEmbedded(workspacesList);
		pagedWorkspaces.setPage(page);

		PagedResourcesProject pagedProjects = new PagedResourcesProject();
		Projects projectsList = new Projects();
		Project project = new Project();
		project.setId(UUID.randomUUID());
		projectsList.setProjects(Collections.singletonList(project));
		pagedProjects.setEmbedded(projectsList);
		pagedProjects.setPage(page);

		PagedResourcesIssueSummary pagedIssues = new PagedResourcesIssueSummary();
		IssueSummaries issueSummaries = new IssueSummaries();
		issueSummaries.setIssues(Collections.emptyList());
		pagedIssues.setEmbedded(issueSummaries);
		pagedIssues.setPage(page);

		WireMock.stubFor(WireMock.get(urlPathEqualTo("/v3/workspaces")).withQueryParams(queryParams)
				.willReturn(WireMock.okJson(GSON.toJson(pagedWorkspaces))));

		WireMock.stubFor(WireMock.get(
				urlPathEqualTo(String.format("/v3/workspaces/%s/projects", workspace.getId())))
				.withQueryParams(queryParams)
				.willReturn(WireMock.okJson(GSON.toJson(pagedProjects))));

		Map<String, StringValuePattern> issuesQueryParams = new HashMap<>(queryParams);
		issuesQueryParams.put("size", equalTo("1"));
		issuesQueryParams.put("project_id", equalTo(project.getId().toString()));
		issuesQueryParams.put("status", equalTo("open,fixed"));

		WireMock.stubFor(WireMock.get(
				urlPathEqualTo(String.format("/v3/workspaces/%s/issues", workspace.getId())))
				.withQueryParams(issuesQueryParams)
				.willReturn(WireMock.okJson(GSON.toJson(pagedIssues))));

		try {
			clientService.testConnection();
			Assert.fail("Should have thrown exception");
		} catch (VeracodeScaClientException e) {
			Assert.assertEquals("Client has access to Veracode but cannot view issues",
					e.getMessage());
		}
	}

	@Test
	public void testFetchDataFailClient() {
		BDDMockito.when(clientRepository.findAll()).thenReturn(Collections.emptyList());
		clientService.fetchData();
		Assert.assertEquals("Starting data fetch for Veracode SCA",
				loggerRule.getMessages().get(0));
		Assert.assertEquals("No Veracode SCA client configured.", loggerRule.getMessages().get(1));
	}

	@Test
	public void testFetchDataFailVeracodeScaWorkspaces() {
		BDDMockito.when(clientRepository.findAll()).thenReturn(Collections.singletonList(client));
		WireMock.stubFor(WireMock.get(urlPathEqualTo("/v3/workspaces"))
				.withQueryParams(queryParams)
				.willReturn(WireMock.aResponse().withBody("invalid")));

		clientService.fetchData();

		@SuppressWarnings("unchecked")
		ArgumentCaptor<List<Workspace>> workspacesCaptor = ArgumentCaptor.forClass(List.class);
		BDDMockito.verify(workspaceService).updateWorkspaces(workspacesCaptor.capture());
		Assert.assertTrue(workspacesCaptor.getValue().isEmpty());

		Assert.assertEquals(2, loggerRule.getMessages().size());
		Assert.assertEquals("Starting data fetch for Veracode SCA",
				loggerRule.getMessages().get(0));
		Assert.assertEquals("Finished data fetch for Veracode SCA",
				loggerRule.getMessages().get(1));
	}

	@Test
	public void testFetchDataFailVeracodeScaProjects() {
		BDDMockito.when(clientRepository.findAll()).thenReturn(Collections.singletonList(client));

		Workspace workspace = new Workspace();
		workspace.setId(UUID.randomUUID());
		workspace.setName("Workspace1");

		PagedResourcesWorkspace pagedWorkspaces = new PagedResourcesWorkspace();
		Workspaces workspacesList = new Workspaces();
		workspacesList.setWorkspaces(Collections.singletonList(workspace));
		pagedWorkspaces.setEmbedded(workspacesList);
		pagedWorkspaces.setPage(page);

		WireMock.stubFor(WireMock.get(urlPathEqualTo("/v3/workspaces")).withQueryParams(queryParams)
				.willReturn(WireMock.okJson(GSON.toJson(pagedWorkspaces))));

		WireMock.stubFor(WireMock
				.get(urlPathEqualTo(String.format("/v3/workspaces/%s/projects", workspace.getId())))
				.withQueryParams(queryParams)
				.willReturn(WireMock.aResponse().withBody("invalid")));

		VeracodeScaWorkspace savedWorkspace = new VeracodeScaWorkspace();
		savedWorkspace.setId(workspace.getId());

		BDDMockito.when(workspaceService.updateWorkspaces(BDDMockito.anyList()))
				.thenReturn(Collections.singletonList(savedWorkspace));

		clientService.fetchData();
		BDDMockito.verify(workspaceService, times(1))
				.updateWorkspaces(BDDMockito.anyList());
		@SuppressWarnings("unchecked")
		ArgumentCaptor<List<Project>> projectsCaptor = ArgumentCaptor.forClass(List.class);
		@SuppressWarnings("unchecked")
		ArgumentCaptor<Map<UUID, String>> branchesCaptor = ArgumentCaptor.forClass(Map.class);
		BDDMockito.verify(projectService).updateProjects(projectsCaptor.capture(),
				BDDMockito.any(VeracodeScaWorkspace.class),
				branchesCaptor.capture());
		Assert.assertTrue(projectsCaptor.getValue().isEmpty());
		Assert.assertTrue(branchesCaptor.getValue().isEmpty());
	}

	@Test
	public void testFetchDataFailDetectBranch() {
		BDDMockito.when(clientRepository.findAll()).thenReturn(Collections.singletonList(client));

		Workspace workspace = new Workspace();
		workspace.setId(UUID.randomUUID());
		workspace.setName("Workspace1");

		Project project = new Project();
		project.setId(UUID.randomUUID());
		project.setName("Project1");

		PagedResourcesWorkspace pagedWorkspaces = new PagedResourcesWorkspace();
		Workspaces workspacesList = new Workspaces();
		workspacesList.setWorkspaces(Collections.singletonList(workspace));
		pagedWorkspaces.setEmbedded(workspacesList);
		pagedWorkspaces.setPage(page);

		PagedResourcesProject pagedProjects = new PagedResourcesProject();
		Projects projectsList = new Projects();
		projectsList.setProjects(Collections.singletonList(project));
		pagedProjects.setEmbedded(projectsList);
		pagedProjects.setPage(page);

		WireMock.stubFor(
				WireMock.get(urlPathEqualTo("/v3/workspaces")).withQueryParams(queryParams)
						.willReturn(WireMock.okJson(GSON.toJson(pagedWorkspaces))));

		WireMock.stubFor(WireMock
				.get(urlPathEqualTo(String.format("/v3/workspaces/%s/projects", workspace.getId())))
				.withQueryParams(queryParams)
				.willReturn(WireMock.okJson(GSON.toJson(pagedProjects))));

		Map<String, StringValuePattern> issuesQueryParams = new HashMap<>();
		issuesQueryParams.put("page", equalTo("0"));
		issuesQueryParams.put("size", equalTo("1"));
		issuesQueryParams.put("status", equalTo("open,fixed"));
		issuesQueryParams.put("project_id", equalTo(project.getId().toString()));

		WireMock.stubFor(WireMock
				.get(urlPathEqualTo(String.format("/v3/workspaces/%s/issues", workspace.getId())))
				.withQueryParams(issuesQueryParams)
				.willReturn(WireMock.aResponse().withBody("invalid")));

		VeracodeScaWorkspace workspaceModel = new VeracodeScaWorkspace();
		workspaceModel.setId(workspace.getId());
		BDDMockito.when(workspaceService.updateWorkspaces(BDDMockito.anyList()))
				.thenReturn(Collections.singletonList(workspaceModel));

		clientService.fetchData();
		BDDMockito.verify(workspaceService).updateWorkspaces(BDDMockito.anyList());
		BDDMockito.verify(projectService)
				.updateProjects(BDDMockito.anyList(), BDDMockito.any(VeracodeScaWorkspace.class),
						BDDMockito.anyMap());
		BDDMockito.verify(issueService, times(0))
				.updateIssues(BDDMockito.anyList(), BDDMockito.any(VeracodeScaProject.class));
	}

	@Test
	public void testFetchDataFailVeracodeScaIssues() {
		BDDMockito.when(clientRepository.findAll()).thenReturn(Collections.singletonList(client));

		Workspace workspace = new Workspace();
		workspace.setId(UUID.randomUUID());
		workspace.setName("Workspace1");

		Project project = new Project();
		project.setId(UUID.randomUUID());
		project.setName("Project1");

		IssueSummary issue = new IssueSummary();
		issue.setId(UUID.randomUUID());
		issue.setProjectBranch("Branch1");

		PagedResourcesWorkspace pagedWorkspaces = new PagedResourcesWorkspace();
		Workspaces workspacesList = new Workspaces();
		workspacesList.setWorkspaces(Collections.singletonList(workspace));
		pagedWorkspaces.setEmbedded(workspacesList);
		pagedWorkspaces.setPage(page);

		PagedResourcesProject pagedProjects = new PagedResourcesProject();
		Projects projectsList = new Projects();
		projectsList.setProjects(Collections.singletonList(project));
		pagedProjects.setEmbedded(projectsList);
		pagedProjects.setPage(page);

		PagedResourcesIssueSummary pagedIssues = new PagedResourcesIssueSummary();
		IssueSummaries issueSummaries = new IssueSummaries();
		issueSummaries.setIssues(Collections.singletonList(issue));
		pagedIssues.setEmbedded(issueSummaries);
		pagedIssues.setPage(page);

		WireMock.stubFor(
				WireMock.get(urlPathEqualTo("/v3/workspaces")).withQueryParams(queryParams)
						.willReturn(WireMock.okJson(GSON.toJson(pagedWorkspaces))));

		WireMock.stubFor(WireMock
				.get(urlPathEqualTo(String.format("/v3/workspaces/%s/projects", workspace.getId())))
				.withQueryParams(queryParams)
				.willReturn(WireMock.okJson(GSON.toJson(pagedProjects))));

		Map<String, StringValuePattern> issuesQueryParams = new HashMap<>();
		issuesQueryParams.put("page", equalTo("0"));
		issuesQueryParams.put("size", equalTo("1"));
		issuesQueryParams.put("status", equalTo("open,fixed"));
		issuesQueryParams.put("project_id", equalTo(project.getId().toString()));

		WireMock.stubFor(WireMock
				.get(urlPathEqualTo(String.format("/v3/workspaces/%s/issues", workspace.getId())))
				.withQueryParams(issuesQueryParams)
				.willReturn(WireMock.aResponse().withBody(GSON.toJson(pagedIssues))));

		VeracodeScaWorkspace workspaceModel = new VeracodeScaWorkspace();
		workspaceModel.setId(workspace.getId());
		BDDMockito.when(workspaceService.updateWorkspaces(BDDMockito.anyList()))
				.thenReturn(Collections.singletonList(workspaceModel));

		clientService.fetchData();
		BDDMockito.verify(workspaceService, times(1))
				.updateWorkspaces(BDDMockito.anyList());
		BDDMockito.verify(projectService, times(1))
				.updateProjects(BDDMockito.anyList(), BDDMockito.any(VeracodeScaWorkspace.class),
						BDDMockito.anyMap());
		BDDMockito.verify(issueService, times(0))
				.updateIssues(BDDMockito.anyList(), BDDMockito.any(VeracodeScaProject.class));
	}

	@Test
	public void testFetchData() {
		Workspace workspace = new Workspace();
		workspace.setId(UUID.randomUUID());
		workspace.setName("Workspace1");

		Project project = new Project();
		project.setId(UUID.randomUUID());
		project.setName("Project1");

		IssueSummary issue = new IssueSummary();
		issue.setId(UUID.randomUUID());
		issue.setProjectBranch("Branch1");

		setupFetchData(workspace, project, issue);

		@SuppressWarnings("unchecked")
		ArgumentCaptor<List<Project>> projectsCaptor = ArgumentCaptor.forClass(List.class);
		ArgumentCaptor<VeracodeScaWorkspace> workspaceCaptor = ArgumentCaptor
				.forClass(VeracodeScaWorkspace.class);
		@SuppressWarnings("unchecked")
		ArgumentCaptor<Map<UUID, String>> branchesCaptor = ArgumentCaptor.forClass(Map.class);

		@SuppressWarnings("unchecked")
		ArgumentCaptor<List<IssueSummary>> issuesCaptor = ArgumentCaptor.forClass(List.class);
		ArgumentCaptor<VeracodeScaProject> projectCaptor = ArgumentCaptor
				.forClass(VeracodeScaProject.class);

		VeracodeScaWorkspace workspaceModel = new VeracodeScaWorkspace();
		workspaceModel.setId(workspace.getId());
		BDDMockito.when(workspaceService.updateWorkspaces(BDDMockito.anyList()))
				.thenReturn(Collections.singletonList(workspaceModel));

		VeracodeScaProject projectModel = new VeracodeScaProject();
		projectModel.setId(project.getId());
		projectModel.setVisibleBranch(issue.getProjectBranch());
		BDDMockito.when(projectService
				.updateProjects(BDDMockito.anyList(), BDDMockito.any(VeracodeScaWorkspace.class),
						BDDMockito.anyMap())).thenReturn(Collections.singletonList(projectModel));

		clientService.fetchData();
		BDDMockito.verify(projectService, times(1))
				.updateProjects(projectsCaptor.capture(), workspaceCaptor.capture(),
						branchesCaptor.capture());
		BDDMockito.verify(issueService, times(1))
				.updateIssues(issuesCaptor.capture(), projectCaptor.capture());

		Assert.assertEquals(project.getId(), projectsCaptor.getValue().get(0).getId());
		Assert.assertEquals(workspaceModel, workspaceCaptor.getValue());
		Assert.assertEquals(issue.getProjectBranch(),
				branchesCaptor.getValue().get(project.getId()));
		Assert.assertEquals(issue.getId(), issuesCaptor.getValue().get(0).getId());
		Assert.assertEquals(projectModel, projectCaptor.getValue());
	}

	@Test
	public void testFetchDataNoIssues() {
		Workspace workspace = new Workspace();
		workspace.setId(UUID.randomUUID());
		workspace.setName("Workspace1");

		Project project = new Project();
		project.setId(UUID.randomUUID());
		project.setName("Project1");

		setupFetchData(workspace, project, null);

		@SuppressWarnings("unchecked")
		ArgumentCaptor<List<Project>> projectsCaptor = ArgumentCaptor.forClass(List.class);
		ArgumentCaptor<VeracodeScaWorkspace> workspaceCaptor = ArgumentCaptor
				.forClass(VeracodeScaWorkspace.class);
		@SuppressWarnings("unchecked")
		ArgumentCaptor<Map<UUID, String>> branchesCaptor = ArgumentCaptor.forClass(Map.class);

		@SuppressWarnings("unchecked")
		ArgumentCaptor<List<IssueSummary>> issuesCaptor = ArgumentCaptor.forClass(List.class);
		ArgumentCaptor<VeracodeScaProject> projectCaptor = ArgumentCaptor
				.forClass(VeracodeScaProject.class);

		VeracodeScaWorkspace workspaceModel = new VeracodeScaWorkspace();
		workspaceModel.setId(workspace.getId());
		BDDMockito.when(workspaceService.updateWorkspaces(BDDMockito.anyList()))
				.thenReturn(Collections.singletonList(workspaceModel));

		VeracodeScaProject projectModel = new VeracodeScaProject();
		projectModel.setId(project.getId());
		BDDMockito.when(projectService
				.updateProjects(BDDMockito.anyList(), BDDMockito.any(VeracodeScaWorkspace.class),
						BDDMockito.anyMap())).thenReturn(Collections.singletonList(projectModel));

		clientService.fetchData();
		BDDMockito.verify(projectService, times(1))
				.updateProjects(projectsCaptor.capture(), workspaceCaptor.capture(),
						branchesCaptor.capture());
		BDDMockito.verify(issueService, times(1))
				.updateIssues(issuesCaptor.capture(), projectCaptor.capture());

		Assert.assertEquals(project.getId(), projectsCaptor.getValue().get(0).getId());
		Assert.assertEquals(workspaceModel, workspaceCaptor.getValue());
		Assert.assertTrue(branchesCaptor.getValue().isEmpty());
		Assert.assertTrue(issuesCaptor.getValue().isEmpty());
		Assert.assertEquals(projectModel, projectCaptor.getValue());
	}

	@Test
	public void testSetClientNull() {
		Assert.assertFalse(clientService.setClient(null, "bar"));
	}

	@Test
	public void testSetClient() {
		// Case where no client is set
		BDDMockito.when(clientRepository.findAll()).thenReturn(Collections.emptyList());
		Assert.assertTrue(clientService.setClient("id", "secretKey"));
		BDDMockito.verify(clientRepository, times(1))
				.saveAndFlush(BDDMockito.any(VeracodeScaClient.class));

		// Case where no client is set
		BDDMockito.when(clientRepository.findAll()).thenReturn(Collections.singletonList(client));
		Assert.assertTrue(clientService.setClient("id", "secretKey"));
		BDDMockito.verify(clientRepository, times(1)).saveAndFlush(client);
		Assert.assertEquals(wireMockRule.baseUrl(), client.getApiUrl());
		Assert.assertEquals("id", client.getApiId());
		Assert.assertEquals("secretKey", client.getApiSecretKey());
	}

	private void setupFetchData(Workspace workspace, Project project, IssueSummary issue) {
		BDDMockito.when(clientRepository.findAll()).thenReturn(Collections.singletonList(client));

		PagedResourcesWorkspace pagedWorkspaces = new PagedResourcesWorkspace();
		Workspaces workspacesList = new Workspaces();
		workspacesList.setWorkspaces(Collections.singletonList(workspace));
		pagedWorkspaces.setEmbedded(workspacesList);
		pagedWorkspaces.setPage(page);

		PagedResourcesProject pagedProjects = new PagedResourcesProject();
		Projects projectsList = new Projects();
		projectsList.setProjects(Collections.singletonList(project));
		pagedProjects.setEmbedded(projectsList);
		pagedProjects.setPage(page);

		PagedResourcesIssueSummary pagedIssues = new PagedResourcesIssueSummary();
		IssueSummaries issueSummaries = new IssueSummaries();
		if (issue == null) {
			issueSummaries.setIssues(Collections.emptyList());
		} else {
			issueSummaries.setIssues(Collections.singletonList(issue));
		}
		pagedIssues.setEmbedded(issueSummaries);
		pagedIssues.setPage(page);

		WireMock.stubFor(
				WireMock.get(urlPathEqualTo("/v3/workspaces")).withQueryParams(queryParams)
						.willReturn(WireMock.okJson(GSON.toJson(pagedWorkspaces))));

		WireMock.stubFor(WireMock
				.get(urlPathEqualTo(String.format("/v3/workspaces/%s/projects", workspace.getId())))
				.withQueryParams(queryParams)
				.willReturn(WireMock.okJson(GSON.toJson(pagedProjects))));

		Map<String, StringValuePattern> detectBranchQueryParams = new HashMap<>();
		detectBranchQueryParams.put("page", equalTo("0"));
		detectBranchQueryParams.put("size", equalTo("1"));
		detectBranchQueryParams.put("status", equalTo("open,fixed"));
		detectBranchQueryParams.put("project_id", equalTo(project.getId().toString()));

		WireMock.stubFor(WireMock
				.get(urlPathEqualTo(String.format("/v3/workspaces/%s/issues", workspace.getId())))
				.withQueryParams(detectBranchQueryParams)
				.willReturn(WireMock.aResponse().withBody(GSON.toJson(pagedIssues))));

		Map<String, StringValuePattern> issuesQueryParams = new HashMap<>(detectBranchQueryParams);
		issuesQueryParams.put("size", equalTo("50"));

		WireMock.stubFor(WireMock
				.get(urlPathEqualTo(String.format("/v3/workspaces/%s/issues", workspace.getId())))
				.withQueryParams(issuesQueryParams)
				.willReturn(WireMock.okJson(GSON.toJson(pagedIssues))));
	}

	@Test
	public void testFetchData4xx() {
		BDDMockito.when(clientRepository.findAll()).thenReturn(Collections.singletonList(client));

		WireMock.stubFor(
				WireMock.get(urlPathEqualTo("/v3/workspaces")).withQueryParams(queryParams)
						.willReturn(WireMock.badRequest()));
		clientService.fetchData();
		Assert.assertTrue(loggerRule.getMessages().get(1)
				.contains("An error occurred while fetching Veracode SCA data: "));
	}
}
