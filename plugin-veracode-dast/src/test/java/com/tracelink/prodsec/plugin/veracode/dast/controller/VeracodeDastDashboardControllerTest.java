package com.tracelink.prodsec.plugin.veracode.dast.controller;

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

import com.tracelink.prodsec.plugin.veracode.dast.VeracodeDastPlugin;
import com.tracelink.prodsec.plugin.veracode.dast.model.VeracodeDastAppModel;
import com.tracelink.prodsec.plugin.veracode.dast.model.VeracodeDastReportModel;
import com.tracelink.prodsec.plugin.veracode.dast.service.VeracodeDastAppService;
import com.tracelink.prodsec.synapse.products.model.ProductLineModel;
import com.tracelink.prodsec.synapse.products.model.ProjectFilterModel;
import com.tracelink.prodsec.synapse.products.model.ProjectModel;
import com.tracelink.prodsec.synapse.products.service.ProductsService;
import com.tracelink.prodsec.synapse.test.TestSynapseBootApplication;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestSynapseBootApplication.class)
@AutoConfigureMockMvc
public class VeracodeDastDashboardControllerTest {
	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private ProductsService mockProductsService;

	@MockBean
	private VeracodeDastAppService mockAppService;

	@Test
	@WithMockUser
	public void testGetDashboardNoApps() throws Exception {
		ProductLineModel plm = new ProductLineModel();
		BDDMockito.when(mockProductsService.getAllProductLines()).thenReturn(Arrays.asList(plm));

		ProjectFilterModel pfm = new ProjectFilterModel();
		BDDMockito.when(mockProductsService.getAllProjectFilters()).thenReturn(Arrays.asList(pfm));

		ProjectModel proj = new ProjectModel();
		BDDMockito.when(mockProductsService.getAllProjects()).thenReturn(Arrays.asList(proj));

		mockMvc.perform(MockMvcRequestBuilders.get(VeracodeDastPlugin.DASHBOARD_PAGE))
				.andExpect(MockMvcResultMatchers.model().attribute("coveredApps", Matchers.is(0)))
				.andExpect(MockMvcResultMatchers.model().attribute("vulnApps", Matchers.is("N/A")))
				.andExpect(MockMvcResultMatchers.model().attribute("scoreStats", Matchers.nullValue()))
				.andExpect(MockMvcResultMatchers.model().attribute("totalVulns", Matchers.is("N/A")))
				.andExpect(MockMvcResultMatchers.model().attribute("productLines", Matchers.contains(plm)))
				.andExpect(MockMvcResultMatchers.model().attribute("filters", Matchers.contains(pfm)))
				.andExpect(MockMvcResultMatchers.model().attribute("projects", Matchers.contains(proj)))
				.andExpect(MockMvcResultMatchers.model().attribute("styles",
						Matchers.contains("/styles/veracodedast/veracode-dast-dashboard.css")))
				.andExpect(MockMvcResultMatchers.model().attribute("scripts", Matchers.contains(
						"/scripts/veracodedast/util.js", "/scripts/veracodedast/veracode-dast-dashboard.js")));
	}

	@Test
	@WithMockUser
	public void testGetDashboardVulns() throws Exception {
		VeracodeDastAppModel noVulns = BDDMockito.mock(VeracodeDastAppModel.class);
		BDDMockito.when(noVulns.isVulnerable()).thenReturn(false);

		long countVulns = 3;
		long score = 70;
		VeracodeDastReportModel vulnReport = BDDMockito.mock(VeracodeDastReportModel.class);
		VeracodeDastAppModel hasVulns = BDDMockito.mock(VeracodeDastAppModel.class);
		BDDMockito.when(hasVulns.isVulnerable()).thenReturn(true);
		BDDMockito.when(hasVulns.getCurrentReport()).thenReturn(vulnReport);
		BDDMockito.when(vulnReport.getScore()).thenReturn(score);
		BDDMockito.when(vulnReport.getVulnerabilitiesCount()).thenReturn(countVulns);

		BDDMockito.when(mockAppService.getAllApps()).thenReturn(Arrays.asList(noVulns, hasVulns));

		ProductLineModel plm = new ProductLineModel();
		BDDMockito.when(mockProductsService.getAllProductLines()).thenReturn(Arrays.asList(plm));

		ProjectFilterModel pfm = new ProjectFilterModel();
		BDDMockito.when(mockProductsService.getAllProjectFilters()).thenReturn(Arrays.asList(pfm));

		ProjectModel proj = new ProjectModel();
		BDDMockito.when(mockProductsService.getAllProjects()).thenReturn(Arrays.asList(proj));

		mockMvc.perform(MockMvcRequestBuilders.get(VeracodeDastPlugin.DASHBOARD_PAGE))
				.andExpect(MockMvcResultMatchers.model().attribute("coveredApps", Matchers.is(2)))
				.andExpect(MockMvcResultMatchers.model().attribute("vulnApps", Matchers.is(1L)))
				.andExpect(MockMvcResultMatchers.model().attribute("scoreStats",
						Matchers.hasProperty("sum", Matchers.is(score))))
				.andExpect(MockMvcResultMatchers.model().attribute("totalVulns", Matchers.is(countVulns)))
				.andExpect(MockMvcResultMatchers.model().attribute("productLines", Matchers.contains(plm)))
				.andExpect(MockMvcResultMatchers.model().attribute("filters", Matchers.contains(pfm)))
				.andExpect(MockMvcResultMatchers.model().attribute("projects", Matchers.contains(proj)))
				.andExpect(MockMvcResultMatchers.model().attribute("styles",
						Matchers.contains("/styles/veracodedast/veracode-dast-dashboard.css")))
				.andExpect(MockMvcResultMatchers.model().attribute("scripts", Matchers.contains(
						"/scripts/veracodedast/util.js", "/scripts/veracodedast/veracode-dast-dashboard.js")));
	}

}
