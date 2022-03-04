package com.tracelink.prodsec.plugin.veracode.sca.controller;

import java.util.Arrays;
import java.util.Collections;

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

import com.tracelink.prodsec.plugin.veracode.sca.VeracodeScaPlugin;
import com.tracelink.prodsec.plugin.veracode.sca.mock.VeracodeScaMocks;
import com.tracelink.prodsec.plugin.veracode.sca.model.VeracodeScaIssue;
import com.tracelink.prodsec.plugin.veracode.sca.model.VeracodeScaProject;
import com.tracelink.prodsec.plugin.veracode.sca.service.VeracodeScaProjectService;
import com.tracelink.prodsec.synapse.products.model.ProductLineModel;
import com.tracelink.prodsec.synapse.products.model.ProjectFilterModel;
import com.tracelink.prodsec.synapse.products.model.ProjectModel;
import com.tracelink.prodsec.synapse.products.service.ProductsService;
import com.tracelink.prodsec.synapse.test.TestSynapseBootApplication;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestSynapseBootApplication.class)
@AutoConfigureMockMvc
public class VeracodeScaDashboardControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private ProductsService productsService;

	@MockBean
	private VeracodeScaProjectService projectService;

	@Test
	@WithMockUser
	public void testGetDashboard() throws Exception {
		ProductLineModel productLine = new ProductLineModel();
		productLine.setName("Product Line");
		ProjectFilterModel projectFilter = new ProjectFilterModel();
		projectFilter.setName("Project Filter");
		ProjectModel synapseProject = new ProjectModel();
		synapseProject.setName("Project");

		VeracodeScaIssue issue1 = VeracodeScaMocks.mockVulnerabilityIssue();
		issue1.setSeverity(7.5f);
		issue1.setVulnerableMethod(true);

		VeracodeScaIssue issue2 = VeracodeScaMocks.mockVulnerabilityIssue();
		issue2.setSeverity(4.5f);

		VeracodeScaProject project = VeracodeScaMocks.mockProject();
		project.setSynapseProject(synapseProject);
		project.setIssues(Arrays.asList(issue1, issue2));

		BDDMockito.when(projectService.getIncludedProjects())
				.thenReturn(Collections.singletonList(project));
		BDDMockito.when(productsService.getAllProductLines())
				.thenReturn(Collections.singletonList(productLine));
		BDDMockito.when(productsService.getAllProjectFilters())
				.thenReturn(Collections.singletonList(projectFilter));
		BDDMockito.when(productsService.getAllProjects())
				.thenReturn(Collections.singletonList(synapseProject));

		mockMvc.perform(MockMvcRequestBuilders.get(VeracodeScaPlugin.DASHBOARD_PAGE))
				.andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
				.andExpect(
						MockMvcResultMatchers.content()
								.string(Matchers.containsString("Product Line")))
				.andExpect(
						MockMvcResultMatchers.content()
								.string(Matchers.containsString("Project Filter")))
				.andExpect(
						MockMvcResultMatchers.content().string(Matchers.containsString("Project")))
				.andExpect(MockMvcResultMatchers.model().attribute("coveredProjects", 1))
				.andExpect(MockMvcResultMatchers.model().attribute("vulnerableProjects", 1L))
				.andExpect(MockMvcResultMatchers.model().attribute("totalIssues", 2L))
				.andExpect(MockMvcResultMatchers.model().attribute("highIssues", 1L))
				.andExpect(MockMvcResultMatchers.model().attribute("vulnerableMethods", 1L));
	}
}
