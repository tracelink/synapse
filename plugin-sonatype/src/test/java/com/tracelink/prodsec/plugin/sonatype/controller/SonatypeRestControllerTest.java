package com.tracelink.prodsec.plugin.sonatype.controller;

import com.tracelink.prodsec.plugin.sonatype.model.SonatypeApp;
import com.tracelink.prodsec.plugin.sonatype.model.SonatypeMetrics;
import com.tracelink.prodsec.plugin.sonatype.service.SonatypeAppService;
import com.tracelink.prodsec.plugin.sonatype.service.SonatypeMetricsService;
import com.tracelink.prodsec.synapse.products.model.ProductLineModel;
import com.tracelink.prodsec.synapse.products.model.ProjectFilterModel;
import com.tracelink.prodsec.synapse.products.model.ProjectModel;
import com.tracelink.prodsec.synapse.test.TestSynapseBootApplication;

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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestSynapseBootApplication.class)
@AutoConfigureMockMvc
public class SonatypeRestControllerTest {
	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private SonatypeAppService appService;

	@MockBean
	private SonatypeMetricsService metricsService;

	private SonatypeApp app;
	private static final String violationsEndpoint = "/sonatype/rest/violations";

	@Before
	public void setup() {
		app = new SonatypeApp();
		app.setId("123");
		app.setName("App");
	}

	@Test
	@WithMockUser
	public void testGetViolationsForProductLine() throws Exception {
		ProductLineModel productLine = new ProductLineModel();
		productLine.setName("Product Line");
		ProjectModel project = new ProjectModel();
		project.setName("Project");
		project.setOwningProductLine(productLine);
		app.setSynapseProject(project);

		SonatypeMetrics metrics = new SonatypeMetrics();
		metrics.setHighVios(4);
		metrics.setRecordedDate(LocalDate.now());
		metrics.setApp(app);
		app.setMetrics(Collections.singletonList(metrics));

		ProjectModel project2 = new ProjectModel();
		project2.setName("Project2");
		project2.setOwningProductLine(productLine);

		SonatypeMetrics metrics2 = new SonatypeMetrics();
		metrics2.setHighVios(8);
		metrics2.setRecordedDate(LocalDate.now());

		SonatypeApp app2 = new SonatypeApp();
		app2.setName("App2");
		app2.setSynapseProject(project2);
		app2.setMetrics(Collections.singletonList(metrics2));
		metrics2.setApp(app2);

		productLine.setProjects(Arrays.asList(project, project2));

		BDDMockito.when(appService.getMappedApps()).thenReturn(Arrays.asList(app, app2));
		BDDMockito.when(metricsService.getEarliestMetricsDate()).thenReturn(LocalDate.now());
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM");

		mockMvc.perform(MockMvcRequestBuilders
				.get(violationsEndpoint).param("productLine", productLine.getName()).param("period", "all-time"))
				.andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
				.andExpect(MockMvcResultMatchers.content()
						.string(Matchers.containsString("\"labels\":[\"" + LocalDate.now().format(formatter))))
				.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("\"High\":[12]")));
	}

	@Test
	@WithMockUser
	public void testGetViolationsForProjectFilter() throws Exception {
		ProjectFilterModel projectFilter = new ProjectFilterModel();
		projectFilter.setName("Project Filter");
		ProjectModel project = new ProjectModel();
		project.setName("Project");
		project.setFilters(Collections.singletonList(projectFilter));
		projectFilter.setProjects(Collections.singletonList(project));
		app.setSynapseProject(project);

		SonatypeMetrics metrics = new SonatypeMetrics();
		metrics.setHighVios(4);
		metrics.setRecordedDate(LocalDate.now().minusDays(3));
		metrics.setApp(app);
		app.setMetrics(Collections.singletonList(metrics));

		BDDMockito.when(appService.getMappedApps()).thenReturn(Collections.singletonList(app));
		BDDMockito.when(metricsService.getEarliestMetricsDate()).thenReturn(LocalDate.now());

		List<String> days = new ArrayList<>();
		days.add(LocalDate.now().minusDays(6).getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.US));
		days.add(LocalDate.now().minusDays(5).getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.US));
		days.add(LocalDate.now().minusDays(4).getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.US));
		days.add(LocalDate.now().minusDays(3).getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.US));
		days.add(LocalDate.now().minusDays(2).getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.US));
		days.add(LocalDate.now().minusDays(1).getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.US));
		days.add(LocalDate.now().getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.US));

		mockMvc.perform(MockMvcRequestBuilders.get(violationsEndpoint).param("projectFilter", projectFilter.getName())
				.param("period", "last-week")).andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
				.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("labels")))
				.andExpect(MockMvcResultMatchers.content().string(Matchers.stringContainsInOrder(days)))
				.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("\"High\":[0,0,0,4,0,0,0]")));
	}

	@Test
	@WithMockUser
	public void testGetViolationsForProject() throws Exception {
		ProjectModel project = new ProjectModel();
		project.setName("Project ");
		app.setSynapseProject(project);

		SonatypeMetrics metrics = new SonatypeMetrics();
		metrics.setHighVios(4);
		metrics.setRecordedDate(LocalDate.now().minusMonths(2));
		metrics.setApp(app);
		app.setMetrics(Collections.singletonList(metrics));

		BDDMockito.when(appService.getMappedApps()).thenReturn(Collections.singletonList(app));
		BDDMockito.when(metricsService.getEarliestMetricsDate()).thenReturn(LocalDate.now());

		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MMM");
		List<String> months = new ArrayList<>();
		months.add(LocalDate.now().minusMonths(5).format(dtf));
		months.add(LocalDate.now().minusMonths(4).format(dtf));
		months.add(LocalDate.now().minusMonths(3).format(dtf));
		months.add(LocalDate.now().minusMonths(2).format(dtf));
		months.add(LocalDate.now().minusMonths(1).format(dtf));
		months.add(LocalDate.now().format(dtf));

		mockMvc.perform(MockMvcRequestBuilders.get(violationsEndpoint).param("project", project.getName())
				.param("period", "last-six-months")).andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
				.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("labels")))
				.andExpect(MockMvcResultMatchers.content().string(Matchers.stringContainsInOrder(months)))
				.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("\"High\":[0,0,0,4,0,0]")));
	}

	@Test
	@WithMockUser
	public void testGetViolationsForProjectFourWeeks() throws Exception {
		ProjectModel project = new ProjectModel();
		project.setName("Project ");
		app.setSynapseProject(project);

		SonatypeMetrics metrics = new SonatypeMetrics();
		metrics.setHighVios(4);
		metrics.setRecordedDate(LocalDate.now().minusWeeks(1));
		metrics.setApp(app);
		app.setMetrics(Collections.singletonList(metrics));

		BDDMockito.when(appService.getMappedApps()).thenReturn(Collections.singletonList(app));
		BDDMockito.when(metricsService.getEarliestMetricsDate()).thenReturn(LocalDate.now());

		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd");
		List<String> weeks = new ArrayList<>();
		weeks.add(LocalDate.now().minusWeeks(3).format(dtf));
		weeks.add(LocalDate.now().minusWeeks(2).format(dtf));
		weeks.add(LocalDate.now().minusWeeks(1).format(dtf));
		weeks.add(LocalDate.now().format(dtf));

		mockMvc.perform(MockMvcRequestBuilders.get(violationsEndpoint).param("project", project.getName())
				.param("period", "last-four-weeks")).andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
				.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("labels")))
				.andExpect(MockMvcResultMatchers.content().string(Matchers.stringContainsInOrder(weeks)))
				.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("\"High\":[0,0,4,0]")));
	}

	@Test
	@WithMockUser
	public void testGetViolationsForProjectUnknownPeriod() throws Exception {
		ProjectModel project = new ProjectModel();
		project.setName("Project ");
		app.setSynapseProject(project);

		BDDMockito.when(appService.getMappedApps()).thenReturn(Collections.singletonList(app));
		BDDMockito.when(metricsService.getEarliestMetricsDate()).thenReturn(LocalDate.now());

		mockMvc.perform(MockMvcRequestBuilders.get(violationsEndpoint).param("project", project.getName())
				.param("period", "foo")).andExpect(MockMvcResultMatchers.status().isBadRequest())
				.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("error")))
				.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("Unknown time period.")));
	}
}
