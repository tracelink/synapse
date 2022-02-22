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
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.tracelink.prodsec.plugin.veracode.dast.VeracodeDastPlugin;
import com.tracelink.prodsec.plugin.veracode.dast.model.VeracodeDastAppModel;
import com.tracelink.prodsec.plugin.veracode.dast.service.VeracodeDastAppService;
import com.tracelink.prodsec.synapse.auth.SynapseAdminAuthDictionary;
import com.tracelink.prodsec.synapse.mvc.SynapseModelAndView;
import com.tracelink.prodsec.synapse.products.model.ProductLineModel;
import com.tracelink.prodsec.synapse.products.model.ProjectModel;
import com.tracelink.prodsec.synapse.products.service.ProductsService;
import com.tracelink.prodsec.synapse.test.TestSynapseBootApplication;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestSynapseBootApplication.class)
@AutoConfigureMockMvc
public class VeracodeDastMappingControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private ProductsService mockProductsService;

	@MockBean
	private VeracodeDastAppService mockAppService;

	@Test
	@WithMockUser(authorities = {SynapseAdminAuthDictionary.ADMIN_PRIV})
	public void testGetMappings() throws Exception {
		ProductLineModel plm = new ProductLineModel();
		plm.setName("plm");
		ProjectModel proj = new ProjectModel();
		proj.setName("test");
		proj.setOwningProductLine(plm);
		plm.setProjects(Arrays.asList(proj));

		VeracodeDastAppModel app = new VeracodeDastAppModel();
		app.setName("name");
		app.setSynapseProductLine(plm);
		VeracodeDastAppModel unmapapp = new VeracodeDastAppModel();
		unmapapp.setName("foo");
		BDDMockito.when(mockAppService.getMappedApps()).thenReturn(Arrays.asList(app));
		BDDMockito.when(mockProductsService.getAllProductLines()).thenReturn(Arrays.asList(plm));
		BDDMockito.when(mockAppService.getUnmappedApps()).thenReturn(Arrays.asList(unmapapp));

		mockMvc.perform(MockMvcRequestBuilders.get(VeracodeDastPlugin.MAPPINGS_PAGE))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("mappedApps", Matchers.contains(app)))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("synapseProductLines", Matchers.contains(plm)))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("veracodeApps", Matchers.contains(unmapapp)))
				.andExpect(MockMvcResultMatchers.model().attribute("scripts",
						Matchers.contains("/scripts/veracodedast/datatable.js")));
	}

	@Test
	@WithMockUser(authorities = {SynapseAdminAuthDictionary.ADMIN_PRIV})
	public void testCreateMapping() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.post(VeracodeDastPlugin.MAPPINGS_PAGE + "/create")
				.param("productLine", "foo").param("app", "bar")
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(
						MockMvcResultMatchers.flash().attribute(SynapseModelAndView.SUCCESS_FLASH,
								Matchers.containsString("Created new mapping")));
	}

	@Test
	@WithMockUser(authorities = {SynapseAdminAuthDictionary.ADMIN_PRIV})
	public void testDeleteMapping() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.post(VeracodeDastPlugin.MAPPINGS_PAGE + "/delete")
				.param("app", "bar")
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(
						MockMvcResultMatchers.flash().attribute(SynapseModelAndView.SUCCESS_FLASH,
								Matchers.containsString("Removed mapping")));
	}
}
