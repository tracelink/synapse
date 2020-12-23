package com.tracelink.prodsec.plugin.sonatype.controller;

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

import com.tracelink.prodsec.plugin.sonatype.SonatypePlugin;
import com.tracelink.prodsec.plugin.sonatype.model.SonatypeApp;
import com.tracelink.prodsec.plugin.sonatype.service.SonatypeAppService;
import com.tracelink.prodsec.synapse.auth.SynapseAdminAuthDictionary;
import com.tracelink.prodsec.synapse.mvc.SynapseModelAndView;
import com.tracelink.prodsec.synapse.products.model.ProjectModel;
import com.tracelink.prodsec.synapse.products.service.ProductsService;
import com.tracelink.prodsec.synapse.test.TestSynapseBootApplication;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestSynapseBootApplication.class)
@AutoConfigureMockMvc
public class SonatypeMappingsControllerTest {
	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private ProductsService productsService;

	@MockBean
	private SonatypeAppService appService;

	private SonatypeApp app;
	private SonatypeApp app2;
	private ProjectModel project;
	private ProjectModel project2;

	@Before
	public void setup() {
		app = new SonatypeApp();
		app.setName("App1");
		app2 = new SonatypeApp();
		app2.setName("App2");
		project = new ProjectModel();
		project.setName("Project1");
		app.setSynapseProject(project);
		project2 = new ProjectModel();
		project2.setName("Project2");
	}

	@Test
	@WithMockUser(authorities = { SynapseAdminAuthDictionary.ADMIN_PRIV })
	public void testGetMappings() throws Exception {
		BDDMockito.when(appService.getMappedApps()).thenReturn(Collections.singletonList(app));
		BDDMockito.when(appService.getUnmappedApps()).thenReturn(Collections.singletonList(app2));
		BDDMockito.when(productsService.getAllProjects()).thenReturn(Arrays.asList(project, project2));

		mockMvc.perform(MockMvcRequestBuilders.get(SonatypePlugin.MAPPINGS_PAGE))
				.andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
				.andExpect(MockMvcResultMatchers.model().attribute("mappedApps", Matchers.contains(app)))
				.andExpect(MockMvcResultMatchers.model().attribute("sonatypeApps", Matchers.contains(app2)))
				.andExpect(MockMvcResultMatchers.model().attribute("synapseProjects", Matchers.contains(project2)));
	}

	@Test
	@WithMockUser(authorities = { SynapseAdminAuthDictionary.ADMIN_PRIV })
	public void testCreateMapping() throws Exception {

		BDDMockito.when(productsService.getProject(BDDMockito.anyString())).thenReturn(project2);
		mockMvc.perform(MockMvcRequestBuilders.post(SonatypePlugin.MAPPINGS_PAGE + "/create")
				.param("project", "Project2").param("app", "App2").with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection()).andExpect(MockMvcResultMatchers.flash()
						.attribute(SynapseModelAndView.SUCCESS_FLASH, "Created new mapping."));
	}

	@Test
	@WithMockUser(authorities = { SynapseAdminAuthDictionary.ADMIN_PRIV })
	public void testDeleteMapping() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.post(SonatypePlugin.MAPPINGS_PAGE + "/delete").param("app", "App2")
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection()).andExpect(
						MockMvcResultMatchers.flash().attribute(SynapseModelAndView.SUCCESS_FLASH, "Removed mapping."));
	}
}
