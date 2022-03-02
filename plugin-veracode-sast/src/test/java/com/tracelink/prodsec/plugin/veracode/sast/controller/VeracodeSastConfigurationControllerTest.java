package com.tracelink.prodsec.plugin.veracode.sast.controller;

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

import com.tracelink.prodsec.lib.veracode.api.VeracodeApiClient;
import com.tracelink.prodsec.lib.veracode.api.VeracodeApiException;
import com.tracelink.prodsec.lib.veracode.api.rest.model.ApplicationScan.ScanTypeEnum;
import com.tracelink.prodsec.plugin.veracode.sast.VeracodeSastPlugin;
import com.tracelink.prodsec.plugin.veracode.sast.model.VeracodeSastClientConfigModel;
import com.tracelink.prodsec.plugin.veracode.sast.model.VeracodeSastThresholdModel;
import com.tracelink.prodsec.plugin.veracode.sast.service.VeracodeSastClientConfigService;
import com.tracelink.prodsec.plugin.veracode.sast.service.VeracodeSastThresholdsService;
import com.tracelink.prodsec.plugin.veracode.sast.service.VeracodeSastUpdateService;
import com.tracelink.prodsec.synapse.auth.SynapseAdminAuthDictionary;
import com.tracelink.prodsec.synapse.mvc.SynapseModelAndView;
import com.tracelink.prodsec.synapse.test.TestSynapseBootApplication;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestSynapseBootApplication.class)
@AutoConfigureMockMvc
public class VeracodeSastConfigurationControllerTest {
	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private VeracodeSastClientConfigService mockConfigService;

	@MockBean
	private VeracodeSastUpdateService mockMetricsService;

	@MockBean
	private VeracodeSastThresholdsService mockThresholdService;

	@Test
	@WithMockUser(authorities = { SynapseAdminAuthDictionary.ADMIN_PRIV })
	public void testGetConfigurations() throws Exception {
		VeracodeSastClientConfigModel config = new VeracodeSastClientConfigModel();
		VeracodeSastThresholdModel threshold = new VeracodeSastThresholdModel();

		BDDMockito.when(mockConfigService.getClientConfig()).thenReturn(config);
		BDDMockito.when(mockThresholdService.getThresholds()).thenReturn(threshold);

		mockMvc.perform(MockMvcRequestBuilders.get(VeracodeSastPlugin.CONFIGURATIONS_PAGE))
				.andExpect(MockMvcResultMatchers.model().attribute("config", Matchers.is(config)))
				.andExpect(MockMvcResultMatchers.model().attribute("thresholds", Matchers.is(threshold)));
	}

	@Test
	@WithMockUser(authorities = { SynapseAdminAuthDictionary.ADMIN_PRIV })
	public void testUpdateConfigEmpty() throws Exception {
		// no apiId
		mockMvc.perform(MockMvcRequestBuilders.post(VeracodeSastPlugin.CONFIGURATIONS_PAGE).param("apiId", "")
				.param("apiKey", "5678").with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.redirectedUrl(VeracodeSastPlugin.CONFIGURATIONS_PAGE))
				.andExpect(MockMvcResultMatchers.flash().attribute(SynapseModelAndView.FAILURE_FLASH,
						Matchers.containsString("Must fill out")));

		BDDMockito.verify(mockConfigService, BDDMockito.never()).setClientConfig(BDDMockito.anyString(),
				BDDMockito.anyString());

		// no apiid
		mockMvc.perform(MockMvcRequestBuilders.post(VeracodeSastPlugin.CONFIGURATIONS_PAGE).param("apiId", "1234")
				.param("apiKey", "").with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.redirectedUrl(VeracodeSastPlugin.CONFIGURATIONS_PAGE))
				.andExpect(MockMvcResultMatchers.flash().attribute(SynapseModelAndView.FAILURE_FLASH,
						Matchers.containsString("Must fill out")));

		BDDMockito.verify(mockConfigService, BDDMockito.never()).setClientConfig(BDDMockito.anyString(),
				BDDMockito.anyString());
	}

	@Test
	@WithMockUser(authorities = { SynapseAdminAuthDictionary.ADMIN_PRIV })
	public void testUpdateConfigSuccess() throws Exception {
		String apiKey = "1234";
		String apiId = "5678";

		mockMvc.perform(MockMvcRequestBuilders.post(VeracodeSastPlugin.CONFIGURATIONS_PAGE).param("apiId", apiId)
				.param("apiKey", apiKey).with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.redirectedUrl(VeracodeSastPlugin.CONFIGURATIONS_PAGE))
				.andExpect(MockMvcResultMatchers.flash().attribute(SynapseModelAndView.SUCCESS_FLASH,
						Matchers.containsString("Configured API client")));

		BDDMockito.verify(mockConfigService, BDDMockito.times(1)).setClientConfig(apiId, apiKey);
	}

	@Test
	@WithMockUser(authorities = { SynapseAdminAuthDictionary.ADMIN_PRIV })
	public void testTestConfigUnconfigured() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get(VeracodeSastPlugin.CONFIGURATIONS_PAGE + "/test"))
				.andExpect(MockMvcResultMatchers.redirectedUrl(VeracodeSastPlugin.CONFIGURATIONS_PAGE))
				.andExpect(MockMvcResultMatchers.flash().attribute(SynapseModelAndView.FAILURE_FLASH,
						Matchers.containsString("not been configured")));
	}

	@WithMockUser(authorities = { SynapseAdminAuthDictionary.ADMIN_PRIV })
	public void testTestConfigNoAccess() throws Exception {
		VeracodeApiClient client = BDDMockito.mock(VeracodeApiClient.class);
		BDDMockito.when(mockConfigService.getApiClient()).thenReturn(client);
		BDDMockito.willThrow(new VeracodeApiException("No Access")).given(client).testAccess(ScanTypeEnum.STATIC);

		mockMvc.perform(MockMvcRequestBuilders.get(VeracodeSastPlugin.CONFIGURATIONS_PAGE + "/test"))
				.andExpect(MockMvcResultMatchers.redirectedUrl(VeracodeSastPlugin.CONFIGURATIONS_PAGE))
				.andExpect(MockMvcResultMatchers.flash().attribute(SynapseModelAndView.FAILURE_FLASH,
						Matchers.containsString("Client does not have access")));
	}

	@Test
	@WithMockUser(authorities = { SynapseAdminAuthDictionary.ADMIN_PRIV })
	public void testTestConfigSuccess() throws Exception {
		String apiKey = "1234";
		String apiId = "5678";

		mockMvc.perform(MockMvcRequestBuilders.post(VeracodeSastPlugin.CONFIGURATIONS_PAGE).param("apiId", apiId)
				.param("apiKey", apiKey).with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.redirectedUrl(VeracodeSastPlugin.CONFIGURATIONS_PAGE))
				.andExpect(MockMvcResultMatchers.flash().attribute(SynapseModelAndView.SUCCESS_FLASH,
						Matchers.containsString("Configured API client")));

		BDDMockito.verify(mockConfigService, BDDMockito.times(1)).setClientConfig(apiId, apiKey);
	}

	@Test
	@WithMockUser(authorities = { SynapseAdminAuthDictionary.ADMIN_PRIV })
	public void testFetchDataUnconfigured() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.post(VeracodeSastPlugin.CONFIGURATIONS_PAGE + "/fetch")
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.redirectedUrl(VeracodeSastPlugin.CONFIGURATIONS_PAGE))
				.andExpect(MockMvcResultMatchers.flash().attribute(SynapseModelAndView.FAILURE_FLASH,
						Matchers.containsString("not been configured")));
	}

	@Test
	@WithMockUser(authorities = { SynapseAdminAuthDictionary.ADMIN_PRIV })
	public void testFetchDataSubmitted() throws Exception {
		BDDMockito.when(mockConfigService.getClientConfig()).thenReturn(new VeracodeSastClientConfigModel());
		mockMvc.perform(MockMvcRequestBuilders.post(VeracodeSastPlugin.CONFIGURATIONS_PAGE + "/fetch")
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.redirectedUrl(VeracodeSastPlugin.CONFIGURATIONS_PAGE))
				.andExpect(MockMvcResultMatchers.flash().attribute(SynapseModelAndView.SUCCESS_FLASH,
						Matchers.containsString("data fetch in progress")));
	}

	@Test
	@WithMockUser(authorities = { SynapseAdminAuthDictionary.ADMIN_PRIV })
	public void testSetThresholdsLowGreen() throws Exception {
		String greenYellow = "1";
		String yellowRed = "0";
		mockMvc.perform(MockMvcRequestBuilders.post(VeracodeSastPlugin.CONFIGURATIONS_PAGE + "/thresholds")
				.param("greenYellow", greenYellow).param("yellowRed", yellowRed)
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.redirectedUrl(VeracodeSastPlugin.CONFIGURATIONS_PAGE))
				.andExpect(MockMvcResultMatchers.flash().attribute(SynapseModelAndView.FAILURE_FLASH,
						Matchers.containsString("greater than zero")));
	}

	@Test
	@WithMockUser(authorities = { SynapseAdminAuthDictionary.ADMIN_PRIV })
	public void testSetThresholdsHighRed() throws Exception {
		String greenYellow = "100";
		String yellowRed = "99";
		mockMvc.perform(MockMvcRequestBuilders.post(VeracodeSastPlugin.CONFIGURATIONS_PAGE + "/thresholds")
				.param("greenYellow", greenYellow).param("yellowRed", yellowRed)
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.redirectedUrl(VeracodeSastPlugin.CONFIGURATIONS_PAGE))
				.andExpect(MockMvcResultMatchers.flash().attribute(SynapseModelAndView.FAILURE_FLASH,
						Matchers.containsString("< 100")));
	}

	@Test
	@WithMockUser(authorities = { SynapseAdminAuthDictionary.ADMIN_PRIV })
	public void testSetThresholdsYellowInverse() throws Exception {
		String greenYellow = "80";
		String yellowRed = "81";
		mockMvc.perform(MockMvcRequestBuilders.post(VeracodeSastPlugin.CONFIGURATIONS_PAGE + "/thresholds")
				.param("greenYellow", greenYellow).param("yellowRed", yellowRed)
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.redirectedUrl(VeracodeSastPlugin.CONFIGURATIONS_PAGE))
				.andExpect(MockMvcResultMatchers.flash().attribute(SynapseModelAndView.FAILURE_FLASH,
						Matchers.containsString("Green/Yellow is greater than Yellow/Red")));
	}

	@Test
	@WithMockUser(authorities = { SynapseAdminAuthDictionary.ADMIN_PRIV })
	public void testSetThresholdsSuccess() throws Exception {
		String greenYellow = "90";
		String yellowRed = "80";
		mockMvc.perform(MockMvcRequestBuilders.post(VeracodeSastPlugin.CONFIGURATIONS_PAGE + "/thresholds")
				.param("greenYellow", greenYellow).param("yellowRed", yellowRed)
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.redirectedUrl(VeracodeSastPlugin.CONFIGURATIONS_PAGE))
				.andExpect(MockMvcResultMatchers.flash().attribute(SynapseModelAndView.SUCCESS_FLASH,
						Matchers.containsString("Thresholds updated successfully")));
	}

}
