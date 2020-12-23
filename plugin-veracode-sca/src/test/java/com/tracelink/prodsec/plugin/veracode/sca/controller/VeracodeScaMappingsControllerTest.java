package com.tracelink.prodsec.plugin.veracode.sca.controller;

import com.tracelink.prodsec.plugin.veracode.sca.VeracodeScaPlugin;
import com.tracelink.prodsec.plugin.veracode.sca.model.VeracodeScaProject;
import com.tracelink.prodsec.plugin.veracode.sca.model.VeracodeScaWorkspace;
import com.tracelink.prodsec.plugin.veracode.sca.service.VeracodeScaProjectService;
import com.tracelink.prodsec.synapse.auth.SynapseAdminAuthDictionary;
import com.tracelink.prodsec.synapse.mvc.SynapseModelAndView;
import com.tracelink.prodsec.synapse.products.model.ProductLineModel;
import com.tracelink.prodsec.synapse.products.model.ProjectModel;
import com.tracelink.prodsec.synapse.products.service.ProductsService;
import com.tracelink.prodsec.synapse.test.TestSynapseBootApplication;
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
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestSynapseBootApplication.class)
@AutoConfigureMockMvc
public class VeracodeScaMappingsControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private ProductsService productsService;

	@MockBean
	private VeracodeScaProjectService projectService;

	private VeracodeScaWorkspace workspace;
	private VeracodeScaProject project;
	private VeracodeScaProject project2;
	private ProjectModel synapseProject;
	private ProjectModel synapseProject2;
	private ProductLineModel synapseProductLine;

	@Before
	public void setup() {
		workspace = new VeracodeScaWorkspace();
		workspace.setName("Workspace1");
		project = new VeracodeScaProject();
		project.setName("Project1");
		project.setWorkspace(workspace);
		project2 = new VeracodeScaProject();
		project2.setName("Project2");
		project2.setWorkspace(workspace);
		synapseProject = new ProjectModel();
		synapseProject.setName("SynapseProject1");
		project.setSynapseProject(synapseProject);
		synapseProject2 = new ProjectModel();
		synapseProject2.setName("SynapseProject2");
		synapseProductLine = new ProductLineModel();
		synapseProductLine.setName("ProductLine1");
		synapseProductLine.setProjects(Arrays.asList(synapseProject, synapseProject2));
		synapseProject.setOwningProductLine(synapseProductLine);
		synapseProject2.setOwningProductLine(synapseProductLine);
	}

	@Test
	@WithMockUser(authorities = {SynapseAdminAuthDictionary.ADMIN_PRIV})
	public void testGetMappings() throws Exception {
		BDDMockito.when(projectService.getMappedProjects())
				.thenReturn(Collections.singletonList(project));
		BDDMockito.when(projectService.getUnmappedProjects())
				.thenReturn(Collections.singletonList(project2));
		BDDMockito.when(productsService.getAllProductLines())
				.thenReturn(Collections.singletonList(synapseProductLine));

		mockMvc.perform(MockMvcRequestBuilders.get(VeracodeScaPlugin.MAPPINGS_PAGE))
				.andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
				.andExpect(MockMvcResultMatchers.model()
						.attribute("mappedProjects", Matchers.contains(project)))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("unmappedProjects", Matchers.contains(project2)))
				.andExpect(MockMvcResultMatchers.model().attribute("synapseProjects",
						Matchers.contains(synapseProject, synapseProject2)))
				.andExpect(MockMvcResultMatchers.model().attribute("scripts",
						Matchers.contains("/scripts/veracode/sca/datatable.js")));
	}

	@Test
	@WithMockUser(authorities = {SynapseAdminAuthDictionary.ADMIN_PRIV})
	public void testCreateMapping() throws Exception {

		BDDMockito.when(productsService.getProject(BDDMockito.anyString())).thenReturn(
				synapseProject2);
		mockMvc.perform(MockMvcRequestBuilders.post(VeracodeScaPlugin.MAPPINGS_PAGE + "/create")
				.param("synapseProject", "SynapseProject2").param("veracodeScaProject", "Project2")
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection())
				.andExpect(MockMvcResultMatchers.flash()
						.attribute(SynapseModelAndView.SUCCESS_FLASH, "Created new mapping."));
	}

	@Test
	@WithMockUser(authorities = {SynapseAdminAuthDictionary.ADMIN_PRIV})
	public void testDeleteMapping() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.post(VeracodeScaPlugin.MAPPINGS_PAGE + "/delete")
				.param("project", "Project2").with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection())
				.andExpect(MockMvcResultMatchers.flash()
						.attribute(SynapseModelAndView.SUCCESS_FLASH, "Removed mapping."));
	}
}
