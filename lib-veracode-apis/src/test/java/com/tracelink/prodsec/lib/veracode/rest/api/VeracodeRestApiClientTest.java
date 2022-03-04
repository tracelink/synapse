package com.tracelink.prodsec.lib.veracode.rest.api;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.google.gson.Gson;
import com.tracelink.prodsec.lib.veracode.api.rest.VeracodeRestApiClient;
import com.tracelink.prodsec.lib.veracode.api.rest.VeracodeRestApiException;
import com.tracelink.prodsec.lib.veracode.api.rest.model.IssueSummaries;
import com.tracelink.prodsec.lib.veracode.api.rest.model.IssueSummary;
import com.tracelink.prodsec.lib.veracode.api.rest.model.PageMetadata;
import com.tracelink.prodsec.lib.veracode.api.rest.model.PagedResourcesIssueSummary;
import com.tracelink.prodsec.lib.veracode.api.rest.model.PagedResourcesProject;
import com.tracelink.prodsec.lib.veracode.api.rest.model.PagedResourcesWorkspace;
import com.tracelink.prodsec.lib.veracode.api.rest.model.Project;
import com.tracelink.prodsec.lib.veracode.api.rest.model.Projects;
import com.tracelink.prodsec.lib.veracode.api.rest.model.Workspace;
import com.tracelink.prodsec.lib.veracode.api.rest.model.Workspaces;

public class VeracodeRestApiClientTest {

	@Rule
	public final WireMockRule wireMockRule = new WireMockRule(WireMockConfiguration.wireMockConfig().dynamicPort());

	private static final Gson GSON = new Gson();
	private static final PageMetadata page1 = new PageMetadata();
	private static final PageMetadata page2 = new PageMetadata();
	private static final Map<String, StringValuePattern> queryParams1 = new HashMap<>();
	private static final Map<String, StringValuePattern> queryParams2 = new HashMap<>();
	private static VeracodeRestApiClient apiWrapper;

	@Before
	public void setup() {
		apiWrapper = new VeracodeRestApiClient(wireMockRule.baseUrl(), "abcdef123456", "abcdef1234567890");

		page1.setNumber(0L);
		page1.setSize(1L);
		page1.setTotalElements(2L);
		page1.setTotalPages(2L);

		page2.setNumber(1L);
		page2.setSize(1L);
		page2.setTotalElements(2L);
		page2.setTotalPages(2L);

		queryParams1.put("page", equalTo("0"));
		queryParams1.put("size", equalTo("50"));

		queryParams2.put("page", equalTo("1"));
		queryParams2.put("size", equalTo("50"));
	}

	@Test
	public void testGetRequestError() {
		WireMock.stubFor(WireMock.get(urlPathEqualTo("/srcclr/v3/workspaces")).withQueryParams(queryParams1)
				.willReturn(WireMock.badRequest()));

		try {
			apiWrapper.getWorkspaces(0);
			Assert.fail("Exception should have been thrown");
		} catch (VeracodeRestApiException e) {
			Assert.assertTrue(e.getMessage(),
					e.getMessage().contains("Received status code 400 from Veracode Rest API endpoint"));
		}
	}

//	@Test
//	public void testGetRequestNullResponse() {
//		WireMock.stubFor(WireMock.get(urlPathEqualTo("/srcclr/v3/workspaces")).withQueryParams(queryParams1).willReturn(null));
//		try {
//			apiWrapper.getWorkspaces(0);
//			Assert.fail("Exception should have been thrown");
//		} catch (VeracodeScaApiException e) {
//			Assert.assertEquals("Could not obtain response from Veracode Rest API", e.getMessage());
//		}
//	}

	@Test
	public void testGetRequest4xxResponse() {
		WireMock.stubFor(WireMock.get(urlPathEqualTo("/srcclr/v3/workspaces")).withQueryParams(queryParams1)
				.willReturn(WireMock.badRequest()));

		try {
			apiWrapper.getWorkspaces(0);
			Assert.fail("Exception should have been thrown");
		} catch (VeracodeRestApiException e) {
			Assert.assertTrue(e.getMessage(),
					e.getMessage().contains("Received status code 400 from Veracode Rest API endpoint "));
		}
	}

	@Test
	public void testGetRequest5xxResponse() {
		WireMock.stubFor(WireMock.get(urlPathEqualTo("/srcclr/v3/workspaces")).withQueryParams(queryParams1)
				.willReturn(WireMock.aResponse().withStatus(504).withBody("{}")));

		try {
			apiWrapper.getWorkspaces(0);
			Assert.fail("Exception should have been thrown");
		} catch (VeracodeRestApiException e) {
			Assert.assertTrue(e.getMessage(),
					e.getMessage().contains("Received status code 504 from Veracode Rest API endpoint "));
		}
		WireMock.verify(5, WireMock.getRequestedFor(urlPathEqualTo("/srcclr/v3/workspaces")));
	}

	@Test
	public void testGetWorkspacesMultiplePages() throws VeracodeRestApiException {
		Workspace workspace1 = new Workspace();
		workspace1.setId(UUID.randomUUID());

		PagedResourcesWorkspace pagedWorkspaces1 = new PagedResourcesWorkspace();
		Workspaces workspacesList1 = new Workspaces();
		workspacesList1.setWorkspaces(Collections.singletonList(workspace1));
		pagedWorkspaces1.setEmbedded(workspacesList1);
		pagedWorkspaces1.setPage(page1);

		Workspace workspace2 = new Workspace();
		workspace2.setId(UUID.randomUUID());

		PagedResourcesWorkspace pagedWorkspaces2 = new PagedResourcesWorkspace();
		Workspaces workspacesList2 = new Workspaces();
		workspacesList2.setWorkspaces(Collections.singletonList(workspace2));
		pagedWorkspaces2.setEmbedded(workspacesList2);
		pagedWorkspaces2.setPage(page2);

		WireMock.stubFor(WireMock.get(urlPathEqualTo("/srcclr/v3/workspaces")).withQueryParams(queryParams1)
				.willReturn(WireMock.okJson(GSON.toJson(pagedWorkspaces1))));
		WireMock.stubFor(WireMock.get(urlPathEqualTo("/srcclr/v3/workspaces")).withQueryParams(queryParams2)
				.willReturn(WireMock.okJson(GSON.toJson(pagedWorkspaces2))));

		List<Workspace> workspaces = apiWrapper.getWorkspaces(0).getEmbedded().getWorkspaces();
		Assert.assertEquals(1, workspaces.size());
		Assert.assertEquals(workspace1.getId(), workspaces.get(0).getId());
		workspaces = apiWrapper.getWorkspaces(1).getEmbedded().getWorkspaces();
		Assert.assertEquals(1, workspaces.size());
		Assert.assertEquals(workspace2.getId(), workspaces.get(0).getId());
	}

	@Test
	public void testGetProjectsMultiplePages() throws VeracodeRestApiException {
		UUID workspaceId = UUID.randomUUID();

		Project project1 = new Project();
		project1.setId(UUID.randomUUID());

		PagedResourcesProject pagedProjects1 = new PagedResourcesProject();
		Projects projectsList1 = new Projects();
		projectsList1.setProjects(Collections.singletonList(project1));
		pagedProjects1.setEmbedded(projectsList1);
		pagedProjects1.setPage(page1);

		Project project2 = new Project();
		project2.setId(UUID.randomUUID());

		PagedResourcesProject pagedProjects2 = new PagedResourcesProject();
		Projects projectsList2 = new Projects();
		projectsList2.setProjects(Collections.singletonList(project2));
		pagedProjects2.setEmbedded(projectsList2);
		pagedProjects2.setPage(page2);

		WireMock.stubFor(WireMock.get(urlPathEqualTo(String.format("/srcclr/v3/workspaces/%s/projects", workspaceId)))
				.withQueryParams(queryParams1).willReturn(WireMock.okJson(GSON.toJson(pagedProjects1))));
		WireMock.stubFor(WireMock.get(urlPathEqualTo(String.format("/srcclr/v3/workspaces/%s/projects", workspaceId)))
				.withQueryParams(queryParams2).willReturn(WireMock.okJson(GSON.toJson(pagedProjects2))));

		List<Project> projects = apiWrapper.getProjects(workspaceId, 0).getEmbedded().getProjects();
		Assert.assertEquals(1, projects.size());
		Assert.assertEquals(project1.getId(), projects.get(0).getId());
		projects = apiWrapper.getProjects(workspaceId, 1).getEmbedded().getProjects();
		Assert.assertEquals(1, projects.size());
		Assert.assertEquals(project2.getId(), projects.get(0).getId());
	}

	@Test
	public void testGetIssuesMultiplePages() throws VeracodeRestApiException {
		UUID workspaceId = UUID.randomUUID();
		UUID projectId = UUID.randomUUID();

		IssueSummary issue1 = new IssueSummary();
		issue1.setId(UUID.randomUUID());

		PagedResourcesIssueSummary pagedIssues1 = new PagedResourcesIssueSummary();
		IssueSummaries issuesList1 = new IssueSummaries();
		issuesList1.setIssues(Collections.singletonList(issue1));
		pagedIssues1.setEmbedded(issuesList1);
		pagedIssues1.setPage(page1);

		IssueSummary issue2 = new IssueSummary();
		issue2.setId(UUID.randomUUID());

		PagedResourcesIssueSummary pagedIssues2 = new PagedResourcesIssueSummary();
		IssueSummaries issuesList2 = new IssueSummaries();
		issuesList2.setIssues(Collections.singletonList(issue2));
		pagedIssues2.setEmbedded(issuesList2);
		pagedIssues2.setPage(page2);

		WireMock.stubFor(WireMock.get(urlPathEqualTo(String.format("/srcclr/v3/workspaces/%s/issues", workspaceId)))
				.withQueryParams(queryParams1).willReturn(WireMock.okJson(GSON.toJson(pagedIssues1))));
		WireMock.stubFor(WireMock.get(urlPathEqualTo(String.format("/srcclr/v3/workspaces/%s/issues", workspaceId)))
				.withQueryParams(queryParams2).willReturn(WireMock.okJson(GSON.toJson(pagedIssues2))));

		List<IssueSummary> issues = apiWrapper.getIssues(workspaceId, projectId, null, 0).getEmbedded().getIssues();
		Assert.assertEquals(1, issues.size());
		Assert.assertEquals(issue1.getId(), issues.get(0).getId());
		issues = apiWrapper.getIssues(workspaceId, projectId, null, 1).getEmbedded().getIssues();
		Assert.assertEquals(1, issues.size());
		Assert.assertEquals(issue2.getId(), issues.get(0).getId());
	}

	@Test
	public void testGetIssuesMalformed() throws VeracodeRestApiException {
		UUID workspaceId = UUID.randomUUID();
		UUID projectId = UUID.randomUUID();
		String branch = "mainline";
		Map<String, StringValuePattern> queryParams = new HashMap<>(queryParams1);
		queryParams.put("project_id", equalTo(projectId.toString()));
		queryParams.put("branch", equalTo(branch));
		WireMock.stubFor(WireMock.get(urlPathEqualTo(String.format("/srcclr/v3/workspaces/%s/issues", workspaceId)))
				.withQueryParams(queryParams).willReturn(WireMock.okJson("invalid")));

		PagedResourcesIssueSummary issues = apiWrapper.getIssues(workspaceId, projectId, branch, 0);
		Assert.assertTrue(issues.getEmbedded().getIssues().isEmpty());
	}
}
