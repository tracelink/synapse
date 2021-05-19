package com.tracelink.prodsec.plugin.veracode.sast.controller;

import com.tracelink.prodsec.plugin.veracode.sast.VeracodeSastPlugin;
import com.tracelink.prodsec.plugin.veracode.sast.service.VeracodeSastAppService;
import com.tracelink.prodsec.synapse.auth.SynapseAdminAuthDictionary;
import com.tracelink.prodsec.synapse.mvc.SynapseModelAndView;
import com.tracelink.prodsec.synapse.test.TestSynapseBootApplication;
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
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestSynapseBootApplication.class)
@AutoConfigureMockMvc
public class VeracodeSastDataMgmtControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private VeracodeSastAppService appService;

	@Test
	@WithMockUser(authorities = {SynapseAdminAuthDictionary.ADMIN_PRIV})
	public void testGetDataMgmt() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get(VeracodeSastPlugin.DATA_MGMT_PAGE))
				.andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
				.andExpect(MockMvcResultMatchers.model()
						.attribute("contentViewName", "veracode-sast-data-mgmt"))
				.andExpect(MockMvcResultMatchers.model()
						.attributeExists("apps", "help"));
		BDDMockito.verify(appService).getAllApps();
	}

	@Test
	@WithMockUser(authorities = {SynapseAdminAuthDictionary.ADMIN_PRIV})
	public void testSetIncludedAppsIllegalArgumentException() throws Exception {
		BDDMockito.doThrow(IllegalArgumentException.class).when(appService)
				.setIncluded(Collections.singletonList(1L));
		mockMvc.perform(
				MockMvcRequestBuilders
						.post(VeracodeSastPlugin.DATA_MGMT_PAGE + "/apps/include")
						.param("appIds", "1")
						.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection())
				.andExpect(MockMvcResultMatchers.flash()
						.attribute(SynapseModelAndView.FAILURE_FLASH,
								Matchers.containsString(
										"Cannot update included apps.")));
	}

	@Test
	@WithMockUser(authorities = {SynapseAdminAuthDictionary.ADMIN_PRIV})
	public void testSetIncludedApps() throws Exception {
		mockMvc.perform(
				MockMvcRequestBuilders
						.post(VeracodeSastPlugin.DATA_MGMT_PAGE + "/apps/include")
						.param("appIds", "1")
						.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection())
				.andExpect(MockMvcResultMatchers.flash()
						.attribute(SynapseModelAndView.SUCCESS_FLASH,
								"Successfully updated included apps"));
	}

	@Test
	@WithMockUser(authorities = {SynapseAdminAuthDictionary.ADMIN_PRIV})
	public void testDeleteAppIllegalArgumentException() throws Exception {
		BDDMockito.doThrow(IllegalArgumentException.class).when(appService)
				.deleteApp(1L);
		mockMvc.perform(
				MockMvcRequestBuilders
						.post(VeracodeSastPlugin.DATA_MGMT_PAGE + "/app/delete")
						.param("appId", "1")
						.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection())
				.andExpect(MockMvcResultMatchers.flash()
						.attribute(SynapseModelAndView.FAILURE_FLASH,
								Matchers.containsString("Cannot delete app.")));
	}

	@Test
	@WithMockUser(authorities = {SynapseAdminAuthDictionary.ADMIN_PRIV})
	public void testDeleteApp() throws Exception {
		mockMvc.perform(
				MockMvcRequestBuilders
						.post(VeracodeSastPlugin.DATA_MGMT_PAGE + "/app/delete")
						.param("appId", "1")
						.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection())
				.andExpect(MockMvcResultMatchers.flash()
						.attribute(SynapseModelAndView.SUCCESS_FLASH,
								"Successfully deleted app"));
	}
}
