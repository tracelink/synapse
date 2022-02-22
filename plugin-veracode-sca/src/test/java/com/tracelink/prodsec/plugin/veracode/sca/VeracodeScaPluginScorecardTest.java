package com.tracelink.prodsec.plugin.veracode.sca;

import java.util.Arrays;
import java.util.Collections;

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

import com.tracelink.prodsec.plugin.veracode.sca.exception.VeracodeScaThresholdsException;
import com.tracelink.prodsec.plugin.veracode.sca.mock.VeracodeScaMocks;
import com.tracelink.prodsec.plugin.veracode.sca.model.VeracodeScaIssue;
import com.tracelink.prodsec.plugin.veracode.sca.model.VeracodeScaProject;
import com.tracelink.prodsec.plugin.veracode.sca.model.VeracodeScaThresholds;
import com.tracelink.prodsec.plugin.veracode.sca.service.VeracodeScaProjectService;
import com.tracelink.prodsec.plugin.veracode.sca.service.VeracodeScaThresholdsService;
import com.tracelink.prodsec.synapse.products.model.ProductLineModel;
import com.tracelink.prodsec.synapse.products.model.ProjectModel;
import com.tracelink.prodsec.synapse.products.service.ProductsService;
import com.tracelink.prodsec.synapse.test.TestSynapseBootApplication;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestSynapseBootApplication.class)
@AutoConfigureMockMvc
public class VeracodeScaPluginScorecardTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private VeracodeScaProjectService projectService;

	@MockBean
	private VeracodeScaThresholdsService thresholdsService;

	@MockBean
	private ProductsService productsService;

	private ProductLineModel productLine;
	private ProjectModel synapseProject;
	private VeracodeScaProject project;
	private VeracodeScaThresholds thresholds;

	@Before
	public void setup() {
		synapseProject = new ProjectModel();
		synapseProject.setName("Project 2");

		productLine = new ProductLineModel();
		productLine.setName("Product Line 1");
		productLine.setProjects(Collections.singletonList(synapseProject));

		project = new VeracodeScaProject();
		project.setSynapseProject(synapseProject);

		thresholds = new VeracodeScaThresholds();
		thresholds.setGreenYellow(15);
		thresholds.setYellowRed(30);
	}

	@Test
	@WithMockUser
	public void testGetScorecardValueForProductLineTrafficGreen() throws Exception {
		VeracodeScaIssue issue1 = VeracodeScaMocks.mockVulnerabilityIssue();
		issue1.setSeverity(8.0f);
		VeracodeScaIssue issue2 = VeracodeScaMocks.mockVulnerabilityIssue();
		issue2.setSeverity(6.0f);
		VeracodeScaIssue issue3 = VeracodeScaMocks.mockVulnerabilityIssue();
		issue3.setSeverity(5.0f);
		VeracodeScaIssue issue4 = VeracodeScaMocks.mockVulnerabilityIssue();
		issue4.setSeverity(2.5f);
		VeracodeScaIssue issue5 = VeracodeScaMocks.mockVulnerabilityIssue();
		issue5.setSeverity(2.0f);
		VeracodeScaIssue issue6 = VeracodeScaMocks.mockVulnerabilityIssue();
		issue6.setSeverity(1.0f);

		BDDMockito.when(productsService.getAllProductLines())
			.thenReturn(Collections.singletonList(productLine));
		BDDMockito.when(projectService.getMappedProjects()).thenReturn(Collections.singletonList(
			project));
		BDDMockito.when(projectService.getUnresolvedIssuesForProductLine(productLine))
			.thenReturn(Arrays.asList(issue1, issue2, issue3, issue4, issue5, issue6));
		BDDMockito.when(thresholdsService.getThresholds()).thenReturn(thresholds);

		mockMvc.perform(MockMvcRequestBuilders.get("/"))
			.andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
			.andExpect(MockMvcResultMatchers.content().string(Matchers.stringContainsInOrder(
				Arrays
					.asList("Product Line 1", "traffic-light-green", "High: 1, Med: 2, Low: 3"))));
	}

	@Test
	@WithMockUser
	public void testGetScorecardValueForProductLineTrafficYellow() throws Exception {
		VeracodeScaIssue issue1 = VeracodeScaMocks.mockVulnerabilityIssue();
		issue1.setSeverity(9.0f);
		VeracodeScaIssue issue2 = VeracodeScaMocks.mockVulnerabilityIssue();
		issue2.setSeverity(8.0f);
		VeracodeScaIssue issue3 = VeracodeScaMocks.mockVulnerabilityIssue();
		issue3.setSeverity(7.5f);
		VeracodeScaIssue issue4 = VeracodeScaMocks.mockVulnerabilityIssue();
		issue4.setSeverity(5.0f);
		VeracodeScaIssue issue5 = VeracodeScaMocks.mockVulnerabilityIssue();
		issue5.setSeverity(2.0f);
		VeracodeScaIssue issue6 = VeracodeScaMocks.mockVulnerabilityIssue();
		issue6.setSeverity(1.0f);

		BDDMockito.when(productsService.getAllProductLines())
			.thenReturn(Collections.singletonList(productLine));
		BDDMockito.when(projectService.getMappedProjects())
			.thenReturn(Collections.singletonList(project));
		BDDMockito.when(projectService.getUnresolvedIssuesForProductLine(productLine))
			.thenReturn(Arrays.asList(issue1, issue2, issue3, issue4, issue5, issue6));
		BDDMockito.when(thresholdsService.getThresholds()).thenReturn(thresholds);

		mockMvc.perform(MockMvcRequestBuilders.get("/"))
			.andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
			.andExpect(MockMvcResultMatchers.content().string(Matchers.stringContainsInOrder(
				Arrays
					.asList("Product Line 1", "traffic-light-yellow", "High: 3, Med: 1, Low: 2"))));
	}

	@Test
	@WithMockUser
	public void testGetScorecardValueForProjectTrafficRed() throws Exception {
		VeracodeScaIssue issue1 = VeracodeScaMocks.mockVulnerabilityIssue();
		issue1.setSeverity(9.0f);
		VeracodeScaIssue issue2 = VeracodeScaMocks.mockVulnerabilityIssue();
		issue2.setSeverity(8.0f);
		VeracodeScaIssue issue3 = VeracodeScaMocks.mockVulnerabilityIssue();
		issue3.setSeverity(7.5f);
		VeracodeScaIssue issue4 = VeracodeScaMocks.mockVulnerabilityIssue();
		issue4.setSeverity(7.2f);
		VeracodeScaIssue issue5 = VeracodeScaMocks.mockVulnerabilityIssue();
		issue5.setSeverity(7.1f);
		VeracodeScaIssue issue6 = VeracodeScaMocks.mockVulnerabilityIssue();
		issue6.setSeverity(1.0f);

		BDDMockito.when(productsService.getProductLine(productLine.getName()))
			.thenReturn(productLine);
		BDDMockito.when(projectService.getUnresolvedIssuesForProject(synapseProject))
			.thenReturn(Arrays.asList(issue1, issue2, issue3, issue4, issue5, issue6));
		BDDMockito.when(thresholdsService.getThresholds()).thenReturn(thresholds);

		mockMvc.perform(
			MockMvcRequestBuilders.get("/").param("filterType", "productLine")
				.param("name", "Product Line 1"))
			.andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
			.andExpect(MockMvcResultMatchers.content().string(Matchers.stringContainsInOrder(
				Arrays.asList("Project 2", "traffic-light-red", "High: 5, Med: 0, Low: 1"))));
	}

	@Test
	@WithMockUser
	public void testGetScorecardValueForProjectNoThresholds() throws Exception {
		VeracodeScaIssue issue1 = VeracodeScaMocks.mockVulnerabilityIssue();
		issue1.setSeverity(9.0f);
		VeracodeScaIssue issue2 = VeracodeScaMocks.mockVulnerabilityIssue();
		issue2.setSeverity(8.0f);
		VeracodeScaIssue issue3 = VeracodeScaMocks.mockVulnerabilityIssue();
		issue3.setSeverity(5.0f);
		VeracodeScaIssue issue4 = VeracodeScaMocks.mockVulnerabilityIssue();
		issue4.setSeverity(2.5f);
		VeracodeScaIssue issue5 = VeracodeScaMocks.mockVulnerabilityIssue();
		issue5.setSeverity(2.0f);
		VeracodeScaIssue issue6 = VeracodeScaMocks.mockVulnerabilityIssue();
		issue6.setSeverity(1.0f);

		BDDMockito.when(productsService.getProductLine(productLine.getName()))
			.thenReturn(productLine);
		BDDMockito.when(projectService.getUnresolvedIssuesForProject(synapseProject))
			.thenReturn(Arrays.asList(issue1, issue2, issue3, issue4, issue5, issue6));
		BDDMockito.when(thresholdsService.getThresholds())
			.thenThrow(VeracodeScaThresholdsException.class);

		mockMvc.perform(
			MockMvcRequestBuilders.get("/").param("filterType", "productLine")
				.param("name", "Product Line 1"))
			.andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
			.andExpect(MockMvcResultMatchers.content().string(Matchers.stringContainsInOrder(
				Arrays.asList("Project 2", "traffic-light-none", "High: 2, Med: 1, Low: 3"))));
	}

	@Test
	@WithMockUser
	public void testGetScorecardValueNoData() throws Exception {
		BDDMockito.when(productsService.getProductLine(productLine.getName()))
			.thenReturn(productLine);
		BDDMockito.when(projectService.getUnresolvedIssuesForProject(synapseProject))
			.thenReturn(null);

		mockMvc.perform(
			MockMvcRequestBuilders.get("/").param("filterType", "productLine")
				.param("name", "Product Line 1"))
			.andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
			.andExpect(MockMvcResultMatchers.content().string(
				Matchers.stringContainsInOrder(
					Arrays.asList("Project 2", "traffic-light-none", "No data"))));
	}
}
