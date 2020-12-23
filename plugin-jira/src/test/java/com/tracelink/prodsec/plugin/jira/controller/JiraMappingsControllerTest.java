package com.tracelink.prodsec.plugin.jira.controller;

import java.util.Arrays;

import com.tracelink.prodsec.plugin.jira.JiraPlugin;
import com.tracelink.prodsec.plugin.jira.exception.JiraMappingsException;
import com.tracelink.prodsec.plugin.jira.model.JiraVuln;
import com.tracelink.prodsec.plugin.jira.service.JiraVulnMetricsService;
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

import com.tracelink.prodsec.synapse.auth.SynapseAdminAuthDictionary;
import com.tracelink.prodsec.synapse.mvc.SynapseModelAndView;
import com.tracelink.prodsec.synapse.products.model.ProductLineModel;
import com.tracelink.prodsec.synapse.products.service.ProductsService;
import com.tracelink.prodsec.synapse.test.TestSynapseBootApplication;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestSynapseBootApplication.class)
@AutoConfigureMockMvc
public class JiraMappingsControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private ProductsService mockProductsService;

	@MockBean
	private JiraVulnMetricsService mockVulnMetricsService;

	@Test
	@WithMockUser(authorities = {SynapseAdminAuthDictionary.ADMIN_PRIV})
	public void testGetMappings() throws Exception {
		ProductLineModel plm = new ProductLineModel();
		plm.setName("plm");

		JiraVuln vuln1 = new JiraVuln();
		vuln1.setId(1);
		vuln1.setProductLine(plm);
		JiraVuln vuln2 = new JiraVuln();
		vuln2.setId(2);

		BDDMockito.when(mockVulnMetricsService.getAllVulnMetrics())
				.thenReturn(Arrays.asList(vuln1, vuln2));
		BDDMockito.when(mockProductsService.getAllProductLines()).thenReturn(Arrays.asList(plm));

		mockMvc.perform(MockMvcRequestBuilders.get(JiraPlugin.MAPPINGS_PAGE))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("vulnerabilities", Matchers.contains(vuln1, vuln2)))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("synapseProducts", Matchers.contains(plm)))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("mappedVulns", Matchers.contains(vuln1)))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("unmappedVulns", Matchers.contains(vuln2)));
	}

	@Test
	@WithMockUser(authorities = {SynapseAdminAuthDictionary.ADMIN_PRIV})
	public void testCreateMapping() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.post(JiraPlugin.MAPPINGS_PAGE + "/create")
				.param("synapseProduct", "foo").param("vulnId", "1")
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(
						MockMvcResultMatchers.flash().attribute(SynapseModelAndView.SUCCESS_FLASH,
								Matchers.containsString("Created new mapping")));
	}

	@Test
	@WithMockUser(authorities = {SynapseAdminAuthDictionary.ADMIN_PRIV})
	public void testCreateMappingNoProduct() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.post(JiraPlugin.MAPPINGS_PAGE + "/create")
				.param("synapseProduct", "").param("vulnId", "1")
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(
						MockMvcResultMatchers.flash().attribute(SynapseModelAndView.FAILURE_FLASH,
								Matchers.containsString("Please provide all inputs.")));
	}

	@Test
	@WithMockUser(authorities = {SynapseAdminAuthDictionary.ADMIN_PRIV})
	public void testCreateMappingFail() throws Exception {
		JiraMappingsException e = new JiraMappingsException(
				"Error creating mapping, vulnerability selected was not found in database");
		ProductLineModel foo = new ProductLineModel();
		BDDMockito.when(mockProductsService.getProductLine("foo")).thenReturn(foo);
		BDDMockito.doThrow(e).when(mockVulnMetricsService).createMapping(foo, 1);

		mockMvc.perform(MockMvcRequestBuilders.post(JiraPlugin.MAPPINGS_PAGE + "/create")
				.param("synapseProduct", "foo").param("vulnId", "1")
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.redirectedUrl(JiraPlugin.MAPPINGS_PAGE))
				.andExpect(
						MockMvcResultMatchers.flash().attribute(SynapseModelAndView.FAILURE_FLASH,
								Matchers.containsString(
										"Error creating mapping, vulnerability selected was not found in database")));
	}

	@Test
	@WithMockUser(authorities = {SynapseAdminAuthDictionary.ADMIN_PRIV})
	public void testDeleteMapping() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.post(JiraPlugin.MAPPINGS_PAGE + "/delete")
				.param("vulnId", "2")
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(
						MockMvcResultMatchers.flash().attribute(SynapseModelAndView.SUCCESS_FLASH,
								Matchers.containsString("Deleted mapping")));
	}

	@Test
	@WithMockUser(authorities = {SynapseAdminAuthDictionary.ADMIN_PRIV})
	public void testDeleteMappingFail() throws Exception {
		int vulnId = 2;
		JiraMappingsException e = new JiraMappingsException(
				"Error deleting mapping, vulnerability selected was not found in database");
		BDDMockito.doThrow(e).when(mockVulnMetricsService).deleteMapping(vulnId);

		mockMvc.perform(MockMvcRequestBuilders.post(JiraPlugin.MAPPINGS_PAGE + "/delete")
				.param("vulnId", "2")
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.redirectedUrl(JiraPlugin.MAPPINGS_PAGE))
				.andExpect(
						MockMvcResultMatchers.flash().attribute(SynapseModelAndView.FAILURE_FLASH,
								Matchers.containsString(
										"Error deleting mapping, vulnerability selected was not found in database")));
	}
}
