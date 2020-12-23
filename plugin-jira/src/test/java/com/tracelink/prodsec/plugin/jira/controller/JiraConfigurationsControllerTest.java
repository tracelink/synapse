package com.tracelink.prodsec.plugin.jira.controller;

import com.tracelink.prodsec.plugin.jira.model.JiraPhraseDataFormat;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.tracelink.prodsec.plugin.jira.model.JiraPhrases;
import com.tracelink.prodsec.plugin.jira.service.JiraPhrasesService;
import com.tracelink.prodsec.plugin.jira.service.JiraUpdateService;
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

import com.tracelink.prodsec.plugin.jira.JiraPlugin;
import com.tracelink.prodsec.plugin.jira.exception.JiraClientException;
import com.tracelink.prodsec.plugin.jira.exception.JiraThresholdsException;
import com.tracelink.prodsec.plugin.jira.model.JiraClient;
import com.tracelink.prodsec.plugin.jira.model.JiraThresholds;
import com.tracelink.prodsec.plugin.jira.service.JiraClientConfigService;
import com.tracelink.prodsec.plugin.jira.service.JiraThresholdsService;
import com.tracelink.prodsec.synapse.auth.SynapseAdminAuthDictionary;
import com.tracelink.prodsec.synapse.mvc.SynapseModelAndView;
import com.tracelink.prodsec.synapse.test.TestSynapseBootApplication;

import static org.mockito.Mockito.mock;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestSynapseBootApplication.class)
@AutoConfigureMockMvc
public class JiraConfigurationsControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private JiraClientConfigService clientService;

	@MockBean
	private JiraUpdateService updateService;

	@MockBean
	private JiraThresholdsService thresholdsService;

	@MockBean
	private JiraPhrasesService searchJqlService;

	private JiraClient client;
	private JiraThresholds thresholds;

	@Before
	public void setup() throws MalformedURLException {
		client = new JiraClient();
		client.setApiUrl(new URL("https://example.com"));
		client.setUser("jdoe");
		client.setAuth("foo");

		thresholds = new JiraThresholds();
		thresholds.setGreenYellow(50);
		thresholds.setYellowRed(100);
	}

	@Test
	@WithMockUser(authorities = {SynapseAdminAuthDictionary.ADMIN_PRIV})
	public void testGetConfigurations() throws Exception {
		BDDMockito.when(clientService.getClient()).thenReturn(client);
		BDDMockito.when(thresholdsService.getThresholds()).thenReturn(thresholds);

		mockMvc.perform(MockMvcRequestBuilders.get(JiraPlugin.CONFIGURATIONS_PAGE))
				.andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
				.andExpect(MockMvcResultMatchers.content()
						.string(Matchers.containsString("https://example.com")))
				.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("jdoe")))
				.andExpect(MockMvcResultMatchers.content().string(
						Matchers.stringContainsInOrder(
								Arrays.asList("Green/Yellow", "Yellow/Red", "50", "100"))));
	}

	@Test
	@WithMockUser(authorities = {SynapseAdminAuthDictionary.ADMIN_PRIV})
	public void testGetConfigurationsException() throws Exception {
		BDDMockito.when(clientService.getClient()).thenThrow(JiraClientException.class);
		BDDMockito.when(thresholdsService.getThresholds()).thenThrow(JiraThresholdsException.class);

		mockMvc.perform(MockMvcRequestBuilders.get(JiraPlugin.CONFIGURATIONS_PAGE))
				.andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
				.andExpect(MockMvcResultMatchers.content()
						.string(Matchers.containsString("No client configured.")))
				.andExpect(
						MockMvcResultMatchers.content()
								.string(Matchers.containsString("No thresholds configured.")));
	}

	@Test
	@WithMockUser(authorities = {SynapseAdminAuthDictionary.ADMIN_PRIV})
	public void testSetApiClientNullEmpty() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.post(JiraPlugin.CONFIGURATIONS_PAGE + "/client")
				.param("apiUrl", "")
				.param("user", "foo").param("auth", "bar")
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection())
				.andExpect(MockMvcResultMatchers.flash()
						.attribute(SynapseModelAndView.FAILURE_FLASH,
								"Please provide all inputs."));

		mockMvc.perform(
				MockMvcRequestBuilders.post(JiraPlugin.CONFIGURATIONS_PAGE + "/client")
						.param("apiUrl", "http://foo.com")
						.param("user", "").param("auth", "bar")
						.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection())
				.andExpect(MockMvcResultMatchers.flash()
						.attribute(SynapseModelAndView.FAILURE_FLASH,
								"Please provide all inputs."));

		mockMvc.perform(
				MockMvcRequestBuilders.post(JiraPlugin.CONFIGURATIONS_PAGE + "/client")
						.param("apiUrl", "http://foo.com")
						.param("user", "bar").param("auth", "")
						.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection())
				.andExpect(MockMvcResultMatchers.flash()
						.attribute(SynapseModelAndView.FAILURE_FLASH,
								"Please provide all inputs."));
	}

	@Test
	@WithMockUser(authorities = {SynapseAdminAuthDictionary.ADMIN_PRIV})
	public void testSetApiClientTrue() throws Exception {
		BDDMockito.when(clientService.setClient(new URL("https://example.com"), "foo", "bar"))
				.thenReturn(new JiraClient());
		mockMvc.perform(MockMvcRequestBuilders.post(JiraPlugin.CONFIGURATIONS_PAGE + "/client")
				.param("apiUrl", "https://example.com").param("user", "foo").param("auth", "bar")
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection())
				.andExpect(MockMvcResultMatchers.flash()
						.attribute(SynapseModelAndView.SUCCESS_FLASH, "Configured API client."));
	}

	@Test
	@WithMockUser(authorities = {SynapseAdminAuthDictionary.ADMIN_PRIV})
	public void testSetApiClientFalse() throws Exception {
		BDDMockito.when(clientService.setClient(new URL("https://example.com"), "foo", "bar"))
				.thenReturn(null);
		mockMvc.perform(MockMvcRequestBuilders.post(JiraPlugin.CONFIGURATIONS_PAGE + "/client")
				.param("apiUrl", "https://example.com").param("user", "foo").param("auth", "bar")
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection())
				.andExpect(MockMvcResultMatchers.flash()
						.attribute(SynapseModelAndView.FAILURE_FLASH, "Invalid API client URL."));
	}

	@Test
	@WithMockUser(authorities = {SynapseAdminAuthDictionary.ADMIN_PRIV})
	public void testTestConnectionTrue() throws Exception {
		BDDMockito.when(clientService.getClient()).thenReturn(new JiraClient());
		BDDMockito.when(updateService.testConnection()).thenReturn(true);
		BDDMockito.when(clientService.createRestClient()).thenReturn(mock(JiraRestClient.class));

		mockMvc.perform(MockMvcRequestBuilders.get(JiraPlugin.CONFIGURATIONS_PAGE + "/test"))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection())
				.andExpect(MockMvcResultMatchers.flash()
						.attribute(SynapseModelAndView.SUCCESS_FLASH, "Connection successful."));
	}

	@Test
	@WithMockUser(authorities = {SynapseAdminAuthDictionary.ADMIN_PRIV})
	public void testTestConnectionFalse() throws Exception {
		BDDMockito.when(clientService.getClient()).thenReturn(new JiraClient());
		BDDMockito.when(updateService.testConnection()).thenReturn(false);
		BDDMockito.when(clientService.createRestClient()).thenReturn(mock(JiraRestClient.class));

		mockMvc.perform(MockMvcRequestBuilders.get(JiraPlugin.CONFIGURATIONS_PAGE + "/test"))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection())
				.andExpect(MockMvcResultMatchers.flash()
						.attribute(SynapseModelAndView.FAILURE_FLASH, "Connection failed."));
	}

	@Test
	@WithMockUser(authorities = {SynapseAdminAuthDictionary.ADMIN_PRIV})
	public void testTestConnectionNoConfig() throws Exception {
		BDDMockito.when(clientService.getClient()).thenReturn(null);

		mockMvc.perform(MockMvcRequestBuilders.get(JiraPlugin.CONFIGURATIONS_PAGE + "/test"))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection())
				.andExpect(MockMvcResultMatchers.flash()
						.attribute(SynapseModelAndView.FAILURE_FLASH,
								"Client has not been configured"));
	}

	@Test
	@WithMockUser(authorities = {SynapseAdminAuthDictionary.ADMIN_PRIV})
	public void testFetch() throws Exception {
		BDDMockito.when(clientService.getClient()).thenReturn(new JiraClient());

		mockMvc.perform(MockMvcRequestBuilders.post(JiraPlugin.CONFIGURATIONS_PAGE + "/fetch")
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection())
				.andExpect(MockMvcResultMatchers.flash()
						.attribute(SynapseModelAndView.SUCCESS_FLASH,
								"Jira data fetch in progress."));
	}

	@Test
	@WithMockUser(authorities = {SynapseAdminAuthDictionary.ADMIN_PRIV})
	public void testFetchNoConfig() throws Exception {
		BDDMockito.when(clientService.getClient()).thenReturn(null);

		mockMvc.perform(MockMvcRequestBuilders.post(JiraPlugin.CONFIGURATIONS_PAGE + "/fetch")
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection())
				.andExpect(MockMvcResultMatchers.flash()
						.attribute(SynapseModelAndView.FAILURE_FLASH,
								"Client has not been configured"));
	}

	@Test
	@WithMockUser(authorities = {SynapseAdminAuthDictionary.ADMIN_PRIV})
	public void testSetThresholdsInvalid() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.post(JiraPlugin.CONFIGURATIONS_PAGE + "/thresholds")
				.param("greenYellow", "-10").param("yellowRed", "100")
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection())
				.andExpect(
						MockMvcResultMatchers.flash().attribute(SynapseModelAndView.FAILURE_FLASH,
								"Please provide risk thresholds greater than zero, where Green/Yellow is less than Yellow/Red."));

		mockMvc.perform(MockMvcRequestBuilders.post(JiraPlugin.CONFIGURATIONS_PAGE + "/thresholds")
				.param("greenYellow", "60").param("yellowRed", "50")
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection())
				.andExpect(
						MockMvcResultMatchers.flash().attribute(SynapseModelAndView.FAILURE_FLASH,
								"Please provide risk thresholds greater than zero, where Green/Yellow is less than Yellow/Red."));
	}

	@Test
	@WithMockUser(authorities = {SynapseAdminAuthDictionary.ADMIN_PRIV})
	public void testSetThresholds() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.post(JiraPlugin.CONFIGURATIONS_PAGE + "/thresholds")
				.param("greenYellow", "10").param("yellowRed", "100")
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection())
				.andExpect(MockMvcResultMatchers.flash()
						.attribute(SynapseModelAndView.SUCCESS_FLASH,
								"Configured risk score thresholds."));
	}

	@Test
	@WithMockUser(authorities = {SynapseAdminAuthDictionary.ADMIN_PRIV})
	public void testSetJQLSearchforDataFormat() throws Exception {
		String dataFormat = "Scrum";
		String jqlString = "type = Story";
		JiraPhrases phrase = new JiraPhrases();
		phrase.setJQL(jqlString);
		phrase.setDataFormat(JiraPhraseDataFormat.SCRUM);
		BDDMockito.when(searchJqlService.setPhraseForDataFormat(jqlString, JiraPhraseDataFormat.SCRUM))
				.thenReturn(phrase);

		mockMvc.perform(MockMvcRequestBuilders.post(JiraPlugin.CONFIGURATIONS_PAGE + "/jqlPhrase")
				.param("dataFormat", dataFormat).param("jqlString", jqlString)
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection())
				.andExpect(MockMvcResultMatchers.flash()
						.attribute(SynapseModelAndView.SUCCESS_FLASH,
								"Configured JQL String for " + dataFormat + " data."));
	}

	@Test
	@WithMockUser(authorities = {SynapseAdminAuthDictionary.ADMIN_PRIV})
	public void testSetJQLSearchforDataFormatNoMatchingFormat() throws Exception {
		String dataFormat = "Foo";
		String jqlString = "type = Story";

		mockMvc.perform(MockMvcRequestBuilders.post(JiraPlugin.CONFIGURATIONS_PAGE + "/jqlPhrase")
				.param("dataFormat", dataFormat).param("jqlString", jqlString)
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection())
				.andExpect(MockMvcResultMatchers.flash()
						.attribute(SynapseModelAndView.FAILURE_FLASH,
								"Data format is unknown"));
	}

	@Test
	@WithMockUser(authorities = {SynapseAdminAuthDictionary.ADMIN_PRIV})
	public void testSetJQLSearchforDataFormatFail() throws Exception {
		String dataFormat = "Scrum";
		String jqlString = "type = Story";
		BDDMockito.when(searchJqlService.setPhraseForDataFormat(jqlString, JiraPhraseDataFormat.SCRUM))
				.thenReturn(null);

		mockMvc.perform(MockMvcRequestBuilders.post(JiraPlugin.CONFIGURATIONS_PAGE + "/jqlPhrase")
				.param("dataFormat", dataFormat).param("jqlString", jqlString)
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection())
				.andExpect(MockMvcResultMatchers.flash()
						.attribute(SynapseModelAndView.FAILURE_FLASH,
								"Error configuring JQL String for data format"));
	}

	@Test
	@WithMockUser(authorities = {SynapseAdminAuthDictionary.ADMIN_PRIV})
	public void testSetJQLNoParam() throws Exception {
		String dataFormat = "";
		String jqlString = "type = Story";
		mockMvc.perform(MockMvcRequestBuilders.post(JiraPlugin.CONFIGURATIONS_PAGE + "/jqlPhrase")
				.param("dataFormat", dataFormat).param("jqlString", jqlString)
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection())
				.andExpect(MockMvcResultMatchers.flash()
						.attribute(SynapseModelAndView.FAILURE_FLASH,
								"Please provide all inputs."));

		jqlString = "";
		mockMvc.perform(MockMvcRequestBuilders.post(JiraPlugin.CONFIGURATIONS_PAGE + "/jqlPhrase")
				.param("dataFormat", dataFormat).param("jqlString", jqlString)
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection())
				.andExpect(MockMvcResultMatchers.flash()
						.attribute(SynapseModelAndView.FAILURE_FLASH,
								"Please provide all inputs."));
	}

	@Test
	@WithMockUser(authorities = {SynapseAdminAuthDictionary.ADMIN_PRIV})
	public void testUpdateAllowedDays() throws Exception {
		Integer allowedSlaInput = 10;
		String severity = "High";
		mockMvc.perform(
				MockMvcRequestBuilders.post(JiraPlugin.CONFIGURATIONS_PAGE + "/updateAllowedDays")
						.param("severity", severity)
						.param("allowedSlaInput", String.valueOf(allowedSlaInput))
						.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection())
				.andExpect(MockMvcResultMatchers.flash()
						.attribute(SynapseModelAndView.SUCCESS_FLASH,
								"Allowed days in SLA updated successfully for severity "
										+ severity));
	}

	@Test
	@WithMockUser(authorities = {SynapseAdminAuthDictionary.ADMIN_PRIV})
	public void testUpdateAllowedDaysEmptySeverity() throws Exception {
		Integer allowedSlaInput = 10;
		String severity = "";
		mockMvc.perform(
				MockMvcRequestBuilders.post(JiraPlugin.CONFIGURATIONS_PAGE + "/updateAllowedDays")
						.param("severity", severity)
						.param("allowedSlaInput", String.valueOf(allowedSlaInput))
						.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection())
				.andExpect(MockMvcResultMatchers.flash()
						.attribute(SynapseModelAndView.FAILURE_FLASH,
								"Please provide severity value."));
	}

	@Test
	@WithMockUser(authorities = {SynapseAdminAuthDictionary.ADMIN_PRIV})
	public void testUpdateAllowedDaysNegativeDays() throws Exception {
		Integer allowedSlaInput = -1;
		String severity = "High";
		mockMvc.perform(
				MockMvcRequestBuilders.post(JiraPlugin.CONFIGURATIONS_PAGE + "/updateAllowedDays")
						.param("severity", severity)
						.param("allowedSlaInput", String.valueOf(allowedSlaInput))
						.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection())
				.andExpect(MockMvcResultMatchers.flash()
						.attribute(SynapseModelAndView.FAILURE_FLASH,
								"Please provide a number of days that is greater than zero"));
	}

	@Test
	@WithMockUser(authorities = {SynapseAdminAuthDictionary.ADMIN_PRIV})
	public void testUpdateAllowedDaysBadSeverity() throws Exception {
		Integer allowedSlaInput = 10;
		String severity = "Severe";
		mockMvc.perform(
				MockMvcRequestBuilders.post(JiraPlugin.CONFIGURATIONS_PAGE + "/updateAllowedDays")
						.param("severity", severity)
						.param("allowedSlaInput", String.valueOf(allowedSlaInput))
						.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection())
				.andExpect(MockMvcResultMatchers.flash()
						.attribute(SynapseModelAndView.FAILURE_FLASH,
								"Please provide a valid string representing vulnerability severity"));
	}
}
