package com.tracelink.prodsec.plugin.sonatype.controller;

import java.util.Arrays;

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
import com.tracelink.prodsec.plugin.sonatype.exception.SonatypeClientException;
import com.tracelink.prodsec.plugin.sonatype.exception.SonatypeThresholdsException;
import com.tracelink.prodsec.plugin.sonatype.model.SonatypeClient;
import com.tracelink.prodsec.plugin.sonatype.model.SonatypeThresholds;
import com.tracelink.prodsec.plugin.sonatype.service.SonatypeClientService;
import com.tracelink.prodsec.plugin.sonatype.service.SonatypeThresholdsService;
import com.tracelink.prodsec.synapse.auth.SynapseAdminAuthDictionary;
import com.tracelink.prodsec.synapse.mvc.SynapseModelAndView;
import com.tracelink.prodsec.synapse.test.TestSynapseBootApplication;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestSynapseBootApplication.class)
@AutoConfigureMockMvc
public class SonatypeConfigurationsControllerTest {
	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private SonatypeClientService clientService;

	@MockBean
	private SonatypeThresholdsService thresholdsService;

	private SonatypeClient client;
	private SonatypeThresholds thresholds;

	@Before
	public void setup() {
		client = new SonatypeClient();
		client.setApiUrl("https://example.com");
		client.setUser("jdoe");
		client.setAuth("foo");

		thresholds = new SonatypeThresholds();
		thresholds.setGreenYellow(50);
		thresholds.setYellowRed(100);
	}

	@Test
	@WithMockUser(authorities = { SynapseAdminAuthDictionary.ADMIN_PRIV })
	public void testGetConfigurations() throws Exception {
		BDDMockito.when(clientService.getClient()).thenReturn(client);
		BDDMockito.when(thresholdsService.getThresholds()).thenReturn(thresholds);

		mockMvc.perform(MockMvcRequestBuilders.get(SonatypePlugin.CONFIGURATIONS_PAGE))
				.andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
				.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("https://example.com")))
				.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("jdoe")))
				.andExpect(MockMvcResultMatchers.content().string(
						Matchers.stringContainsInOrder(Arrays.asList("Green/Yellow", "Yellow/Red", "50", "100"))));
	}

	@Test
	@WithMockUser(authorities = { SynapseAdminAuthDictionary.ADMIN_PRIV })
	public void testGetConfigurationsException() throws Exception {
		BDDMockito.when(clientService.getClient()).thenThrow(SonatypeClientException.class);
		BDDMockito.when(thresholdsService.getThresholds()).thenThrow(SonatypeThresholdsException.class);

		mockMvc.perform(MockMvcRequestBuilders.get(SonatypePlugin.CONFIGURATIONS_PAGE))
				.andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
				.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("No client configured.")))
				.andExpect(
						MockMvcResultMatchers.content().string(Matchers.containsString("No thresholds configured.")));
	}

	@Test
	@WithMockUser(authorities = { SynapseAdminAuthDictionary.ADMIN_PRIV })
	public void testSetApiClientNullEmpty() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.post(SonatypePlugin.CONFIGURATIONS_PAGE + "/client").param("apiUrl", "")
				.param("user", "foo").param("auth", "bar").with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection()).andExpect(MockMvcResultMatchers.flash()
						.attribute(SynapseModelAndView.FAILURE_FLASH, "Please provide all inputs."));

		mockMvc.perform(
				MockMvcRequestBuilders.post(SonatypePlugin.CONFIGURATIONS_PAGE + "/client").param("apiUrl", "foo")
						.param("user", "").param("auth", "bar").with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection()).andExpect(MockMvcResultMatchers.flash()
						.attribute(SynapseModelAndView.FAILURE_FLASH, "Please provide all inputs."));

		mockMvc.perform(
				MockMvcRequestBuilders.post(SonatypePlugin.CONFIGURATIONS_PAGE + "/client").param("apiUrl", "foo")
						.param("user", "bar").param("auth", "").with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection()).andExpect(MockMvcResultMatchers.flash()
						.attribute(SynapseModelAndView.FAILURE_FLASH, "Please provide all inputs."));
	}

	@Test
	@WithMockUser(authorities = { SynapseAdminAuthDictionary.ADMIN_PRIV })
	public void testSetApiClientTrue() throws Exception {
		BDDMockito.when(clientService.setClient(BDDMockito.anyString(), BDDMockito.anyString(), BDDMockito.anyString()))
				.thenReturn(true);
		mockMvc.perform(MockMvcRequestBuilders.post(SonatypePlugin.CONFIGURATIONS_PAGE + "/client")
				.param("apiUrl", "https://example.com").param("user", "foo").param("auth", "bar")
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection()).andExpect(MockMvcResultMatchers.flash()
						.attribute(SynapseModelAndView.SUCCESS_FLASH, "Configured API client."));
	}

	@Test
	@WithMockUser(authorities = { SynapseAdminAuthDictionary.ADMIN_PRIV })
	public void testSetApiClientFalse() throws Exception {
		BDDMockito.when(clientService.setClient(BDDMockito.anyString(), BDDMockito.anyString(), BDDMockito.anyString()))
				.thenReturn(false);
		mockMvc.perform(MockMvcRequestBuilders.post(SonatypePlugin.CONFIGURATIONS_PAGE + "/client")
				.param("apiUrl", "https://example.com").param("user", "foo").param("auth", "bar")
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection()).andExpect(MockMvcResultMatchers.flash()
						.attribute(SynapseModelAndView.FAILURE_FLASH, "Invalid API client URL."));
	}

	@Test
	@WithMockUser(authorities = { SynapseAdminAuthDictionary.ADMIN_PRIV })
	public void testTestConnectionTrue() throws Exception {
		BDDMockito.when(clientService.testConnection()).thenReturn(true);
		mockMvc.perform(MockMvcRequestBuilders.get(SonatypePlugin.CONFIGURATIONS_PAGE + "/test"))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection()).andExpect(MockMvcResultMatchers.flash()
						.attribute(SynapseModelAndView.SUCCESS_FLASH, "Connection successful."));
	}

	@Test
	@WithMockUser(authorities = { SynapseAdminAuthDictionary.ADMIN_PRIV })
	public void testTestConnectionFalse() throws Exception {
		BDDMockito.when(clientService.testConnection()).thenReturn(false);
		mockMvc.perform(MockMvcRequestBuilders.get(SonatypePlugin.CONFIGURATIONS_PAGE + "/test"))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection()).andExpect(MockMvcResultMatchers.flash()
						.attribute(SynapseModelAndView.FAILURE_FLASH, "Connection failed."));
	}

	@Test
	@WithMockUser(authorities = { SynapseAdminAuthDictionary.ADMIN_PRIV })
	public void testFetch() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.post(SonatypePlugin.CONFIGURATIONS_PAGE + "/fetch")
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection()).andExpect(MockMvcResultMatchers.flash()
						.attribute(SynapseModelAndView.SUCCESS_FLASH, "Sonatype data fetch in progress."));
	}

	@Test
	@WithMockUser(authorities = { SynapseAdminAuthDictionary.ADMIN_PRIV })
	public void testSetThresholdsInvalid() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.post(SonatypePlugin.CONFIGURATIONS_PAGE + "/thresholds")
				.param("greenYellow", "-10").param("yellowRed", "100")
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection())
				.andExpect(MockMvcResultMatchers.flash().attribute(SynapseModelAndView.FAILURE_FLASH,
						"Please provide risk thresholds greater than zero, where Green/Yellow is less than Yellow/Red."));

		mockMvc.perform(MockMvcRequestBuilders.post(SonatypePlugin.CONFIGURATIONS_PAGE + "/thresholds")
				.param("greenYellow", "60").param("yellowRed", "50").with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection())
				.andExpect(MockMvcResultMatchers.flash().attribute(SynapseModelAndView.FAILURE_FLASH,
						"Please provide risk thresholds greater than zero, where Green/Yellow is less than Yellow/Red."));
	}

	@Test
	@WithMockUser(authorities = { SynapseAdminAuthDictionary.ADMIN_PRIV })
	public void testSetThresholds() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.post(SonatypePlugin.CONFIGURATIONS_PAGE + "/thresholds")
				.param("greenYellow", "10").param("yellowRed", "100").with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection()).andExpect(MockMvcResultMatchers.flash()
						.attribute(SynapseModelAndView.SUCCESS_FLASH, "Configured risk score thresholds."));
	}
}
