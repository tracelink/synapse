package com.tracelink.prodsec.plugin.veracode.sca.controller;

import com.tracelink.prodsec.plugin.veracode.sca.mock.VeracodeScaMocks;
import com.tracelink.prodsec.plugin.veracode.sca.model.VeracodeScaIssue;
import com.tracelink.prodsec.plugin.veracode.sca.model.VeracodeScaProject;
import com.tracelink.prodsec.plugin.veracode.sca.model.issue.IssueStatus;
import com.tracelink.prodsec.plugin.veracode.sca.model.issue.IssueType;
import com.tracelink.prodsec.plugin.veracode.sca.service.VeracodeScaIssueService;
import com.tracelink.prodsec.plugin.veracode.sca.service.VeracodeScaProjectService;
import com.tracelink.prodsec.synapse.products.model.ProductLineModel;
import com.tracelink.prodsec.synapse.products.model.ProjectFilterModel;
import com.tracelink.prodsec.synapse.products.model.ProjectModel;
import com.tracelink.prodsec.synapse.test.TestSynapseBootApplication;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestSynapseBootApplication.class)
@AutoConfigureMockMvc
public class VeracodeScaRestControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private VeracodeScaProjectService projectService;

	@MockBean
	private VeracodeScaIssueService issueService;

	private VeracodeScaProject project;
	private static final String ISSUES_REST_ENDPOINT = "/veracode/sca/rest/issues";
	private static final String DEVELOP = "develop";

	@Before
	public void setup() {
		project = VeracodeScaMocks.mockProject();
	}

	@Test
	@WithMockUser
	public void testGetIssuesForProductLineBySeverity() throws Exception {
		ProductLineModel productLine = new ProductLineModel();
		productLine.setName("Product Line");
		ProjectModel synapseProject = new ProjectModel();
		synapseProject.setName("Project");
		synapseProject.setOwningProductLine(productLine);
		project.setSynapseProject(synapseProject);

		VeracodeScaIssue issue = new VeracodeScaIssue();
		issue.setSeverity(7.5f);
		issue.setProjectBranch(DEVELOP);
		issue.setIssueStatus(IssueStatus.OPEN);
		issue.setCreatedDate(LocalDateTime.now());
		issue.setLastUpdatedDate(LocalDateTime.now());
		issue.setProject(project);
		project.setIssues(Collections.singletonList(issue));

		ProjectModel synapseProject2 = new ProjectModel();
		synapseProject2.setName("Project2");
		synapseProject2.setOwningProductLine(productLine);

		VeracodeScaIssue issue2 = new VeracodeScaIssue();
		issue2.setSeverity(8.5f);
		issue2.setProjectBranch(DEVELOP);
		issue2.setIssueStatus(IssueStatus.OPEN);
		issue2.setCreatedDate(LocalDateTime.now());
		issue2.setLastUpdatedDate(LocalDateTime.now());
		issue2.setVulnerableMethod(true);

		VeracodeScaProject project2 = new VeracodeScaProject();
		project2.setName("Project2");
		project2.setSynapseProject(synapseProject2);
		project2.setDefaultBranch(DEVELOP);
		project2.setIssues(Collections.singletonList(issue2));
		issue2.setProject(project2);

		productLine.setProjects(Arrays.asList(synapseProject, synapseProject2));

		BDDMockito.when(projectService.getMappedProjects())
				.thenReturn(Arrays.asList(project, project2));
		BDDMockito.when(issueService.getEarliestIssueDate()).thenReturn(LocalDateTime.now());
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM");

		mockMvc.perform(MockMvcRequestBuilders
				.get(ISSUES_REST_ENDPOINT).param("productLine", productLine.getName())
				.param("period", "all-time")
				.param("category", "severity"))
				.andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
				.andExpect(MockMvcResultMatchers.content().string(Matchers
						.containsString("\"labels\":[\"" + LocalDateTime.now().format(formatter))))
				.andExpect(MockMvcResultMatchers.content().string(Matchers
						.stringContainsInOrder("\"High\":[1]", "\"High with VM\":[1]")));
	}

	@Test
	@WithMockUser
	public void testGetIssuesForProjectFilterByVulnerability() throws Exception {
		ProjectFilterModel projectFilter = new ProjectFilterModel();
		projectFilter.setName("Project Filter");
		ProjectModel synapseProject = new ProjectModel();
		synapseProject.setName("Project");
		synapseProject.setFilters(Collections.singletonList(projectFilter));
		projectFilter.setProjects(Collections.singletonList(synapseProject));
		project.setSynapseProject(synapseProject);

		VeracodeScaIssue issue = new VeracodeScaIssue();
		issue.setVulnerability("XXE");
		issue.setProjectBranch(DEVELOP);
		issue.setCreatedDate(LocalDateTime.now().minusDays(10));
		issue.setLastUpdatedDate(LocalDateTime.now().minusDays(3));
		issue.setIssueStatus(IssueStatus.OPEN);
		issue.setIssueType(IssueType.VULNERABILITY);
		issue.setProject(project);

		// Second issue is same but of type library
		VeracodeScaIssue issue2 = new VeracodeScaIssue();
		issue2.setProjectBranch(DEVELOP);
		issue2.setCreatedDate(LocalDateTime.now().minusDays(10));
		issue2.setLastUpdatedDate(LocalDateTime.now().minusDays(3));
		issue2.setIssueStatus(IssueStatus.OPEN);
		issue2.setIssueType(IssueType.LIBRARY);
		issue2.setProject(project);

		project.setIssues(Arrays.asList(issue, issue2));

		BDDMockito.when(projectService.getMappedProjects())
				.thenReturn(Collections.singletonList(project));
		BDDMockito.when(issueService.getEarliestIssueDate()).thenReturn(LocalDateTime.now());

		List<String> days = new ArrayList<>();
		days.add(
				LocalDateTime.now().minusDays(6).getDayOfWeek()
						.getDisplayName(TextStyle.SHORT, Locale.US));
		days.add(
				LocalDateTime.now().minusDays(5).getDayOfWeek()
						.getDisplayName(TextStyle.SHORT, Locale.US));
		days.add(
				LocalDateTime.now().minusDays(4).getDayOfWeek()
						.getDisplayName(TextStyle.SHORT, Locale.US));
		days.add(
				LocalDateTime.now().minusDays(3).getDayOfWeek()
						.getDisplayName(TextStyle.SHORT, Locale.US));
		days.add(
				LocalDateTime.now().minusDays(2).getDayOfWeek()
						.getDisplayName(TextStyle.SHORT, Locale.US));
		days.add(
				LocalDateTime.now().minusDays(1).getDayOfWeek()
						.getDisplayName(TextStyle.SHORT, Locale.US));
		days.add(LocalDateTime.now().getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.US));

		mockMvc.perform(MockMvcRequestBuilders.get(ISSUES_REST_ENDPOINT)
				.param("projectFilter", projectFilter.getName())
				.param("period", "last-week")
				.param("category", "vulnerability"))
				.andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
				.andExpect(
						MockMvcResultMatchers.content().string(Matchers.containsString("labels")))
				.andExpect(MockMvcResultMatchers.content()
						.string(Matchers.stringContainsInOrder(days)))
				.andExpect(MockMvcResultMatchers.content()
						.string(Matchers.containsString("\"XXE\":[1,1,1,1,0,0,0]")));
	}

	@Test
	@WithMockUser
	public void testGetIssuesForProjectByIssueType() throws Exception {
		ProjectModel synapseProject = new ProjectModel();
		synapseProject.setName("Project");
		project.setSynapseProject(synapseProject);

		VeracodeScaIssue issue = new VeracodeScaIssue();
		issue.setSeverity(7.5f);
		issue.setIssueType(IssueType.LICENSE);
		issue.setIssueStatus(IssueStatus.FIXED);
		issue.setFixedDate(LocalDateTime.now().minusMonths(1));
		issue.setCreatedDate(LocalDateTime.now().minusMonths(3));
		issue.setLastUpdatedDate(LocalDateTime.now());
		issue.setProjectBranch(DEVELOP);
		issue.setProject(project);
		project.setIssues(Collections.singletonList(issue));

		BDDMockito.when(projectService.getMappedProjects())
				.thenReturn(Collections.singletonList(project));
		BDDMockito.when(issueService.getEarliestIssueDate()).thenReturn(LocalDateTime.now());

		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MMM");
		List<String> months = new ArrayList<>();
		months.add(LocalDateTime.now().minusMonths(5).format(dtf));
		months.add(LocalDateTime.now().minusMonths(4).format(dtf));
		months.add(LocalDateTime.now().minusMonths(3).format(dtf));
		months.add(LocalDateTime.now().minusMonths(2).format(dtf));
		months.add(LocalDateTime.now().minusMonths(1).format(dtf));
		months.add(LocalDateTime.now().format(dtf));

		mockMvc.perform(
				MockMvcRequestBuilders.get(ISSUES_REST_ENDPOINT)
						.param("project", synapseProject.getName())
						.param("period", "last-six-months")
						.param("category", "type"))
				.andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
				.andExpect(
						MockMvcResultMatchers.content().string(Matchers.containsString("labels")))
				.andExpect(
						MockMvcResultMatchers.content()
								.string(Matchers.stringContainsInOrder(months)))
				.andExpect(MockMvcResultMatchers.content()
						.string(Matchers.containsString("\"License\":[0,0,1,1,0,0]")));
	}

	@Test
	@WithMockUser
	public void testGetIssuesFourWeeks() throws Exception {
		VeracodeScaIssue issue = new VeracodeScaIssue();
		issue.setSeverity(4.5f);
		issue.setCreatedDate(LocalDateTime.now().minusWeeks(2));
		issue.setLastUpdatedDate(LocalDateTime.now().minusDays(1));
		issue.setIssueStatus(IssueStatus.OPEN);
		issue.setIgnored(true);
		issue.setIgnoredDate(LocalDateTime.now().minusWeeks(1));
		issue.setProjectBranch(DEVELOP);
		issue.setProject(project);
		project.setIssues(Collections.singletonList(issue));

		BDDMockito.when(projectService.getProjects())
				.thenReturn(Collections.singletonList(project));
		BDDMockito.when(issueService.getEarliestIssueDate()).thenReturn(LocalDateTime.now());

		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd");
		List<String> weeks = new ArrayList<>();
		weeks.add(LocalDateTime.now().minusWeeks(3).format(dtf));
		weeks.add(LocalDateTime.now().minusWeeks(2).format(dtf));
		weeks.add(LocalDateTime.now().minusWeeks(1).format(dtf));
		weeks.add(LocalDateTime.now().format(dtf));

		mockMvc.perform(
				MockMvcRequestBuilders.get(ISSUES_REST_ENDPOINT).param("period", "last-four-weeks")
						.param("category", "severity"))
				.andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
				.andExpect(
						MockMvcResultMatchers.content().string(Matchers.containsString("labels")))
				.andExpect(
						MockMvcResultMatchers.content()
								.string(Matchers.stringContainsInOrder(weeks)))
				.andExpect(MockMvcResultMatchers.content()
						.string(Matchers.containsString("\"Medium\":[0,1,0,0]")));
	}

	@Test
	@WithMockUser
	public void testGetIssuesUnknownPeriod() throws Exception {
		BDDMockito.when(projectService.getMappedProjects())
				.thenReturn(Collections.singletonList(project));
		BDDMockito.when(issueService.getEarliestIssueDate()).thenReturn(LocalDateTime.now());

		mockMvc.perform(
				MockMvcRequestBuilders.get(ISSUES_REST_ENDPOINT).param("period", "foo")
						.param("category", "severity"))
				.andExpect(MockMvcResultMatchers.status().isBadRequest())
				.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("error")))
				.andExpect(MockMvcResultMatchers.content()
						.string(Matchers.containsString("Unknown time period.")));
	}

	@Test
	@WithMockUser
	public void testGetIssuesUnknownCategory() throws Exception {
		BDDMockito.when(projectService.getMappedProjects())
				.thenReturn(Collections.singletonList(project));
		BDDMockito.when(issueService.getEarliestIssueDate()).thenReturn(LocalDateTime.now());

		mockMvc.perform(
				MockMvcRequestBuilders.get(ISSUES_REST_ENDPOINT).param("period", "last-four-weeks")
						.param("category", "foo"))
				.andExpect(MockMvcResultMatchers.status().isBadRequest())
				.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("error")))
				.andExpect(MockMvcResultMatchers.content()
						.string(Matchers.containsString("Unknown categorization.")));
	}
}
