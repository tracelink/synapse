package com.tracelink.prodsec.plugin.veracode.dast.controller;

import com.tracelink.prodsec.plugin.veracode.dast.model.VeracodeDastAppModel;
import com.tracelink.prodsec.plugin.veracode.dast.model.VeracodeDastFlawModel;
import com.tracelink.prodsec.plugin.veracode.dast.model.VeracodeDastReportModel;
import com.tracelink.prodsec.plugin.veracode.dast.service.VeracodeDastAppService;
import com.tracelink.prodsec.synapse.products.model.ProductLineModel;
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
public class VeracodeDastDashboardRestControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private VeracodeDastAppService mockAppService;

	@Test
	@WithMockUser
	public void testGetAllFlawsBySeverity() throws Exception {
		String period = "last-week";
		String category = "severity";

		LocalDateTime fixedDate = LocalDateTime.now().minusDays(2);

		VeracodeDastAppModel app = new VeracodeDastAppModel();
		VeracodeDastReportModel report = new VeracodeDastReportModel();
		report.setReportDate(fixedDate);
		report.setVeryHighVios(1);

		app.setReports(Arrays.asList(report));
		BDDMockito.when(mockAppService.getAllApps()).thenReturn(Arrays.asList(app));

		mockMvc.perform(
				MockMvcRequestBuilders.get("/veracodedast/rest/flaws").param("period", period)
						.param("category",
								category))
				.andExpect(MockMvcResultMatchers.jsonPath("$.['Very High']", Matchers.hasItem(1)));
	}

	@Test
	@WithMockUser
	public void testGetAllFlawsBadCategory() throws Exception {
		String period = "last-week";
		String category = "foobar";

		VeracodeDastAppModel app = new VeracodeDastAppModel();

		BDDMockito.when(mockAppService.getAllApps()).thenReturn(Arrays.asList(app));

		mockMvc.perform(
				MockMvcRequestBuilders.get("/veracodedast/rest/flaws").param("period", period)
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

		VeracodeDastAppModel app = new VeracodeDastAppModel();
		VeracodeDastReportModel report = new VeracodeDastReportModel();
		report.setReportDate(LocalDateTime.now());
		report.setVeryHighVios(1);
		VeracodeDastReportModel report2 = new VeracodeDastReportModel();
		report2.setReportDate(LocalDateTime.of(2020, 2, 1, 1, 1));
		report2.setVeryHighVios(1);

		app.setReports(Arrays.asList(report, report2));
		BDDMockito.when(mockAppService.getAllApps()).thenReturn(Arrays.asList(app));

		mockMvc.perform(
				MockMvcRequestBuilders.get("/veracodedast/rest/flaws").param("period", period)
						.param("category",
								category))
				.andExpect(MockMvcResultMatchers.jsonPath("$.['labels']", Matchers.hasItem("Feb")));
	}

	@Test
	@WithMockUser
	public void testGetAllFlawsBySeverityAllTimeNoFindings() throws Exception {
		String period = "all-time";
		String category = "severity";

		VeracodeDastAppModel app = new VeracodeDastAppModel();

		BDDMockito.when(mockAppService.getAllApps()).thenReturn(Arrays.asList(app));
		String nowMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("MMM"));
		if (LocalDate.now().getMonth().equals(Month.DECEMBER) || LocalDate.now().getMonth()
				.equals(Month.JANUARY)) {
			nowMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("MMM yyyy"));
		}

		mockMvc.perform(
				MockMvcRequestBuilders.get("/veracodedast/rest/flaws").param("period", period)
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

		VeracodeDastAppModel app = new VeracodeDastAppModel();
		VeracodeDastReportModel report = new VeracodeDastReportModel();
		report.setReportDate(fixedDate);
		report.setVeryHighVios(1);
		VeracodeDastReportModel report2 = new VeracodeDastReportModel();
		report2.setReportDate(fixedDate);
		report2.setVeryHighVios(1);

		app.setReports(Arrays.asList(report, report2));
		BDDMockito.when(mockAppService.getAllApps()).thenReturn(Arrays.asList(app));

		mockMvc.perform(
				MockMvcRequestBuilders.get("/veracodedast/rest/flaws").param("period", period)
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
		VeracodeDastAppModel app = new VeracodeDastAppModel();
		VeracodeDastReportModel report = new VeracodeDastReportModel();
		report.setReportDate(fixedDate);
		report.setVeryHighVios(1);

		String cweName = "testCWE";
		VeracodeDastFlawModel flaw1 = new VeracodeDastFlawModel();
		flaw1.setCategoryName(cweName);
		flaw1.setCount(1);
		VeracodeDastFlawModel flaw2 = new VeracodeDastFlawModel();
		flaw2.setCategoryName(cweName);
		flaw2.setCount(1);

		report.setFlaws(Arrays.asList(flaw1, flaw2));
		app.setReports(Arrays.asList(report));
		BDDMockito.when(mockAppService.getAllApps()).thenReturn(Arrays.asList(app));

		mockMvc.perform(
				MockMvcRequestBuilders.get("/veracodedast/rest/flaws").param("period", period)
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

		VeracodeDastAppModel app = new VeracodeDastAppModel();
		app.setSynapseProductLine(plm);
		VeracodeDastReportModel report = new VeracodeDastReportModel();
		report.setReportDate(fixedDate);
		report.setVeryHighVios(1);

		app.setReports(Arrays.asList(report));
		BDDMockito.when(mockAppService.getMappedApps()).thenReturn(Arrays.asList(app));

		mockMvc.perform(MockMvcRequestBuilders.get("/veracodedast/rest/flaws")
				.param("productLine", synapseProductLine)
				.param("period", period).param("category", category))
				.andExpect(MockMvcResultMatchers.jsonPath("$.['Very High']", Matchers.hasItem(1)));
	}
}
