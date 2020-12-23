package com.tracelink.prodsec.plugin.veracode.sast.controller;

import com.tracelink.prodsec.plugin.veracode.sast.model.VeracodeSastAppModel;
import com.tracelink.prodsec.plugin.veracode.sast.model.VeracodeSastFlawModel;
import com.tracelink.prodsec.plugin.veracode.sast.model.VeracodeSastReportModel;
import com.tracelink.prodsec.plugin.veracode.sast.service.VeracodeSastAppService;
import com.tracelink.prodsec.synapse.products.model.ProductLineModel;
import com.tracelink.prodsec.synapse.products.model.ProjectFilterModel;
import com.tracelink.prodsec.synapse.products.model.ProjectModel;
import com.tracelink.prodsec.synapse.test.TestSynapseBootApplication;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import org.hamcrest.Matchers;
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
public class VeracodeSastDashboardRestControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private VeracodeSastAppService mockAppService;

	@Test
	@WithMockUser
	public void testGetAllFlawsBySeverity() throws Exception {
		String period = "last-week";
		String category = "severity";

		LocalDateTime fixedDate = LocalDateTime.now().minusDays(2);

		VeracodeSastAppModel app = new VeracodeSastAppModel();
		VeracodeSastReportModel report = new VeracodeSastReportModel();
		report.setReportDate(fixedDate);
		report.setVeryHighVios(1);

		app.setReports(Arrays.asList(report));
		BDDMockito.when(mockAppService.getAllApps()).thenReturn(Arrays.asList(app));

		mockMvc.perform(
				MockMvcRequestBuilders.get("/veracodesast/rest/flaws").param("period", period)
						.param("category",
								category))
				.andExpect(MockMvcResultMatchers.jsonPath("$.['Very High']", Matchers.hasItem(1)));
	}

	@Test
	@WithMockUser
	public void testGetAllFlawsBadCategory() throws Exception {
		String period = "last-week";
		String category = "foobar";

		VeracodeSastAppModel app = new VeracodeSastAppModel();

		BDDMockito.when(mockAppService.getAllApps()).thenReturn(Arrays.asList(app));

		mockMvc.perform(
				MockMvcRequestBuilders.get("/veracodesast/rest/flaws").param("period", period)
						.param("category",
								category))
				.andExpect(MockMvcResultMatchers
						.jsonPath("$.['error']", Matchers.contains("Unknown categorization")));
	}

	@Test
	@WithMockUser
	public void testGetAllFlawsBySeverityAllTime() throws Exception {
		String period = "all-time";
		String category = "severity";

		VeracodeSastAppModel app = new VeracodeSastAppModel();
		VeracodeSastReportModel report = new VeracodeSastReportModel();
		report.setReportDate(LocalDateTime.now());
		report.setVeryHighVios(1);
		VeracodeSastReportModel report2 = new VeracodeSastReportModel();
		report2.setReportDate(LocalDateTime.of(2020, 2, 1, 1, 1));
		report2.setVeryHighVios(1);

		app.setReports(Arrays.asList(report, report2));
		BDDMockito.when(mockAppService.getAllApps()).thenReturn(Arrays.asList(app));

		mockMvc.perform(
				MockMvcRequestBuilders.get("/veracodesast/rest/flaws").param("period", period)
						.param("category",
								category))
				.andExpect(MockMvcResultMatchers.jsonPath("$.['labels']", Matchers.hasItem("Feb")));
	}

	@Test
	@WithMockUser
	public void testGetAllFlawsBySeverityAllTimeNoFindings() throws Exception {
		String period = "all-time";
		String category = "severity";

		VeracodeSastAppModel app = new VeracodeSastAppModel();

		BDDMockito.when(mockAppService.getAllApps()).thenReturn(Arrays.asList(app));
		String nowMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("MMM"));
		if (LocalDate.now().getMonth().equals(Month.DECEMBER) || LocalDate.now().getMonth()
				.equals(Month.JANUARY)) {
			nowMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("MMM yyyy"));
		}

		mockMvc.perform(
				MockMvcRequestBuilders.get("/veracodesast/rest/flaws").param("period", period)
						.param("category",
								category)).andExpect(
				MockMvcResultMatchers.jsonPath("$.['labels']", Matchers.contains(nowMonth)));
	}

	@Test
	@WithMockUser
	public void testGetAllFlawsBySeverityMultiple() throws Exception {
		String period = "last-week";
		String category = "severity";

		LocalDateTime fixedDate = LocalDateTime.now().minusDays(2);

		VeracodeSastAppModel app = new VeracodeSastAppModel();
		VeracodeSastReportModel report = new VeracodeSastReportModel();
		report.setReportDate(fixedDate);
		report.setVeryHighVios(1);
		VeracodeSastReportModel report2 = new VeracodeSastReportModel();
		report2.setReportDate(fixedDate);
		report2.setVeryHighVios(1);

		app.setReports(Arrays.asList(report, report2));
		BDDMockito.when(mockAppService.getAllApps()).thenReturn(Arrays.asList(app));

		mockMvc.perform(
				MockMvcRequestBuilders.get("/veracodesast/rest/flaws").param("period", period)
						.param("category",
								category))
				.andExpect(MockMvcResultMatchers.jsonPath("$.['Very High']", Matchers.hasItem(2)));
	}

	@Test
	@WithMockUser
	public void testGetAllFlawsByCweMultiple() throws Exception {
		String period = "last-week";
		String category = "cwe";

		LocalDateTime fixedDate = LocalDateTime.now().minusDays(2);
		VeracodeSastAppModel app = new VeracodeSastAppModel();
		VeracodeSastReportModel report = new VeracodeSastReportModel();
		report.setReportDate(fixedDate);
		report.setVeryHighVios(1);

		String cweName = "testCWE";
		VeracodeSastFlawModel flaw1 = new VeracodeSastFlawModel();
		flaw1.setCategoryName(cweName);
		flaw1.setCount(1);
		VeracodeSastFlawModel flaw2 = new VeracodeSastFlawModel();
		flaw2.setCategoryName(cweName);
		flaw2.setCount(1);

		report.setFlaws(Arrays.asList(flaw1, flaw2));
		app.setReports(Arrays.asList(report));
		BDDMockito.when(mockAppService.getAllApps()).thenReturn(Arrays.asList(app));

		mockMvc.perform(
				MockMvcRequestBuilders.get("/veracodesast/rest/flaws").param("period", period)
						.param("category",
								category)).andExpect(
				MockMvcResultMatchers.jsonPath("$.['" + cweName + "']", Matchers.hasItem(2)));
	}

	@Test
	@WithMockUser
	public void testGetFlawsForProductLine() throws Exception {
		String synapseProductLine = "testLine";
		String period = "last-week";
		String category = "severity";

		ProductLineModel plm = new ProductLineModel();
		plm.setName(synapseProductLine);

		ProjectModel proj = new ProjectModel();
		proj.setOwningProductLine(plm);

		LocalDateTime fixedDate = LocalDateTime.now().minusDays(2);

		VeracodeSastAppModel app = new VeracodeSastAppModel();
		app.setSynapseProject(proj);
		VeracodeSastReportModel report = new VeracodeSastReportModel();
		report.setReportDate(fixedDate);
		report.setVeryHighVios(1);

		app.setReports(Arrays.asList(report));
		BDDMockito.when(mockAppService.getMappedApps()).thenReturn(Arrays.asList(app));

		mockMvc.perform(MockMvcRequestBuilders.get("/veracodesast/rest/flaws")
				.param("productLine", synapseProductLine)
				.param("period", period).param("category", category))
				.andExpect(MockMvcResultMatchers.jsonPath("$.['Very High']", Matchers.hasItem(1)));
	}

	@Test
	@WithMockUser
	public void testGetFlawsForFilter() throws Exception {
		String synapseFilter = "testFilter";
		String period = "last-week";
		String category = "severity";

		ProjectFilterModel pfm = new ProjectFilterModel();
		pfm.setName(synapseFilter);

		ProjectModel proj = new ProjectModel();
		proj.setFilters(Arrays.asList(pfm));

		LocalDateTime fixedDate = LocalDateTime.now().minusDays(2);

		VeracodeSastAppModel app = new VeracodeSastAppModel();
		app.setSynapseProject(proj);
		VeracodeSastReportModel report = new VeracodeSastReportModel();
		report.setReportDate(fixedDate);
		report.setVeryHighVios(1);

		app.setReports(Arrays.asList(report));
		BDDMockito.when(mockAppService.getMappedApps()).thenReturn(Arrays.asList(app));

		mockMvc.perform(MockMvcRequestBuilders.get("/veracodesast/rest/flaws")
				.param("projectFilter", synapseFilter)
				.param("period", period).param("category", category))
				.andExpect(MockMvcResultMatchers.jsonPath("$.['Very High']", Matchers.hasItem(1)));
	}

	@Test
	@WithMockUser
	public void testGetFlawsForProject() throws Exception {
		String synapseProject = "testProject";
		String period = "last-week";
		String category = "severity";

		ProjectModel proj = new ProjectModel();
		proj.setName(synapseProject);

		LocalDateTime fixedDate = LocalDateTime.now().minusDays(2);

		VeracodeSastAppModel app = new VeracodeSastAppModel();
		app.setSynapseProject(proj);
		VeracodeSastReportModel report = new VeracodeSastReportModel();
		report.setReportDate(fixedDate);
		report.setVeryHighVios(1);

		app.setReports(Arrays.asList(report));
		BDDMockito.when(mockAppService.getMappedApps()).thenReturn(Arrays.asList(app));

		mockMvc.perform(MockMvcRequestBuilders.get("/veracodesast/rest/flaws")
				.param("project", synapseProject)
				.param("period", period).param("category", category))
				.andExpect(MockMvcResultMatchers.jsonPath("$.['Very High']", Matchers.hasItem(1)));
	}
}
