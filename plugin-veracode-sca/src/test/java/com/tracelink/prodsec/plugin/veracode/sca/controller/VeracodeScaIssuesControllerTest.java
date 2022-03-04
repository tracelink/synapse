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
import com.tracelink.prodsec.plugin.veracode.sca.model.VeracodeScaWorkspace;
import com.tracelink.prodsec.plugin.veracode.sca.service.VeracodeScaProjectService;
import com.tracelink.prodsec.synapse.products.model.ProductLineModel;
import com.tracelink.prodsec.synapse.products.model.ProjectModel;
import com.tracelink.prodsec.synapse.products.service.ProductsService;
import com.tracelink.prodsec.synapse.test.TestSynapseBootApplication;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestSynapseBootApplication.class)
@AutoConfigureMockMvc
public class VeracodeScaIssuesControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private ProductsService productsService;

	@MockBean
	private VeracodeScaProjectService projectService;

	@Test
	@WithMockUser(authorities = VeracodeScaPlugin.VIEW_ISSUES_PRIV)
	public void testGetIssues() throws Exception {
		ProductLineModel productLine = new ProductLineModel();
		productLine.setName("Product Line");
		ProjectModel synapseProject = new ProjectModel();
		synapseProject.setName("Project");
		synapseProject.setOwningProductLine(productLine);

		VeracodeScaIssue issue1 = VeracodeScaMocks.mockVulnerabilityIssue();
		issue1.setSeverity(7.5f);
		issue1.setVulnerableMethod(true);

		VeracodeScaIssue issue2 = VeracodeScaMocks.mockVulnerabilityIssue();
		issue2.setSeverity(4.5f);

		VeracodeScaWorkspace workspace = new VeracodeScaWorkspace();
		workspace.setName("Workspace");
		workspace.setSiteId("abcXYZ");

		VeracodeScaProject project = VeracodeScaMocks.mockProject();
		project.setSynapseProject(synapseProject);
		project.setIssues(Arrays.asList(issue1, issue2));
		project.setWorkspace(workspace);

		VeracodeScaProject project2 = VeracodeScaMocks.mockProject();
		project2.setIssues(Collections.emptyList());
		project2.setWorkspace(workspace);

		BDDMockito.when(projectService.getMappedProjects())
				.thenReturn(Collections.singletonList(project));
		BDDMockito.when(projectService.getUnmappedProjects())
				.thenReturn(Collections.singletonList(project2));
		BDDMockito.when(productsService.getAllProductLines())
				.thenReturn(Collections.singletonList(productLine));

		mockMvc.perform(MockMvcRequestBuilders.get(VeracodeScaPlugin.ISSUES_PAGE))
				.andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
				.andExpect(MockMvcResultMatchers.content()
						.string(Matchers.containsString("Unmapped Projects")))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("mappedProjects", Matchers.contains(project)))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("unmappedProjects", Matchers.contains(project2)))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("productLines", Matchers.contains(productLine)))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("scripts",
								Matchers.contains("/scripts/veracode/sca/issues.js")));

	}
}
