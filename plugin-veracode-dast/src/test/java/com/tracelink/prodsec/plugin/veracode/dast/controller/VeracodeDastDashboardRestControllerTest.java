package com.tracelink.prodsec.plugin.veracode.dast.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.tracelink.prodsec.plugin.veracode.dast.model.VeracodeDastAppModel;
import com.tracelink.prodsec.plugin.veracode.dast.model.VeracodeDastReportModel;
import com.tracelink.prodsec.plugin.veracode.dast.service.VeracodeDastAppService;
import com.tracelink.prodsec.synapse.products.model.ProductLineModel;
import com.tracelink.prodsec.synapse.test.TestSynapseBootApplication;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestSynapseBootApplication.class)
@AutoConfigureMockMvc
public class VeracodeDastDashboardRestControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private VeracodeDastAppService mockAppService;

	private String[] periods = { "last-week", "last-four-weeks", "last-six-months", "all-time" };

	private String[] categories = { "policy", "flaws", "severity" };
	private static final long SCORE = 80L;
	private static final long NUM_FLAWS = 5L;

	@Test
	@WithMockUser
	public void testGetAllFlaws() throws Exception {
		for (String period : periods) {
			for (String category : categories) {
				VeracodeDastAppModel app = setupAppForPeriod(period);
				BDDMockito.when(mockAppService.getAllApps()).thenReturn(Arrays.asList(app));
				ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/veracodedast/rest/reports")
						.param("period", period).param("category", category));
				verify(result, app, category, period);
			}
		}
	}

	@Test
	@WithMockUser
	public void testGetFlawsForProductLine() throws Exception {
		String productLine = "foo";
		for (String period : periods) {
			for (String category : categories) {
				VeracodeDastAppModel app = setupAppForPeriod(period);
				ProductLineModel productLineModel = new ProductLineModel();
				productLineModel.setName(productLine);
				app.setSynapseProductLine(productLineModel);
				BDDMockito.when(mockAppService.getMappedApps()).thenReturn(Arrays.asList(app));
				ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/veracodedast/rest/reports")
						.param("productLine", productLine).param("period", period).param("category", category));
				verify(result, app, category, period);
			}
		}
	}

	private VeracodeDastAppModel setupAppForPeriod(String period) {
		VeracodeDastAppModel app = new VeracodeDastAppModel();
		app.setReports(new ArrayList<>());

		switch (period) {
		case "all-time":
			VeracodeDastReportModel reportAT = new VeracodeDastReportModel();
			reportAT.setReportDate(LocalDateTime.now().minusYears(2));
			reportAT.setScore(SCORE);
			reportAT.setTotalFlaws(NUM_FLAWS);
			reportAT.setvHigh(NUM_FLAWS);
			app.getReports().add(reportAT);
		case "last-six-months":
			VeracodeDastReportModel reportSM = new VeracodeDastReportModel();
			reportSM.setReportDate(LocalDateTime.now().minusMonths(2));
			reportSM.setScore(SCORE);
			reportSM.setTotalFlaws(NUM_FLAWS);
			reportSM.setvHigh(NUM_FLAWS);
			app.getReports().add(reportSM);
		case "last-four-weeks":
			VeracodeDastReportModel reportFW = new VeracodeDastReportModel();
			reportFW.setReportDate(LocalDateTime.now().minusDays(2 * 7));
			reportFW.setScore(SCORE);
			reportFW.setTotalFlaws(NUM_FLAWS);
			reportFW.setvHigh(NUM_FLAWS);
			app.getReports().add(reportFW);
		case "last-week":
			VeracodeDastReportModel reportLW = new VeracodeDastReportModel();
			reportLW.setReportDate(LocalDateTime.now().minusDays(2));
			reportLW.setScore(SCORE);
			reportLW.setTotalFlaws(NUM_FLAWS);
			reportLW.setvHigh(NUM_FLAWS);
			app.getReports().add(reportLW);
		}

		return app;
	}

	private void verify(ResultActions result, VeracodeDastAppModel app, String category, String period)
			throws Exception {

		switch (category) {
		case "policy":
			result.andExpect(MockMvcResultMatchers.jsonPath("$['Policy Score']", Matchers.hasItem((double) SCORE)));
			break;
		case "flaws":
			result.andExpect(
					MockMvcResultMatchers.jsonPath("$['Total Flaws']", Matchers.hasItem(Matchers.greaterThan(0))));
			break;
		case "severity":
			result.andExpect(
					MockMvcResultMatchers.jsonPath("$['Very High']", Matchers.hasItem(Matchers.greaterThan(0))));
			break;
		}
	}
}