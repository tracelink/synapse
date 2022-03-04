package com.tracelink.prodsec.plugin.veracode.sca.controller;

import java.util.Collections;
import java.util.UUID;

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

import com.tracelink.prodsec.plugin.veracode.sca.VeracodeScaPlugin;
import com.tracelink.prodsec.plugin.veracode.sca.service.VeracodeScaProjectService;
import com.tracelink.prodsec.plugin.veracode.sca.service.VeracodeScaWorkspaceService;
import com.tracelink.prodsec.synapse.auth.SynapseAdminAuthDictionary;
import com.tracelink.prodsec.synapse.mvc.SynapseModelAndView;
import com.tracelink.prodsec.synapse.test.TestSynapseBootApplication;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestSynapseBootApplication.class)
@AutoConfigureMockMvc
public class VeracodeScaDataMgmtControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private VeracodeScaWorkspaceService workspaceService;

	@MockBean
	private VeracodeScaProjectService projectService;

	private UUID uuid;

	@Before
	public void setup() {
		uuid = UUID.randomUUID();
	}

	@Test
	@WithMockUser(authorities = {SynapseAdminAuthDictionary.ADMIN_PRIV})
	public void testGetDataMgmt() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get(VeracodeScaPlugin.DATA_MGMT_PAGE))
				.andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
				.andExpect(MockMvcResultMatchers.model()
						.attribute("contentViewName", "veracode-sca-data-mgmt"))
				.andExpect(MockMvcResultMatchers.model()
						.attributeExists("workspaces", "projects", "help"));
		BDDMockito.verify(workspaceService).getWorkspaces();
		BDDMockito.verify(projectService).getProjects();
	}

	@Test
	@WithMockUser(authorities = {SynapseAdminAuthDictionary.ADMIN_PRIV})
	public void testSetIncludedWorkspacesIllegalArgumentException() throws Exception {
		BDDMockito.doThrow(IllegalArgumentException.class).when(workspaceService)
				.setIncluded(Collections.singletonList(uuid));
		mockMvc.perform(
				MockMvcRequestBuilders
						.post(VeracodeScaPlugin.DATA_MGMT_PAGE + "/workspaces/include")
						.param("workspaceIds", uuid.toString())
						.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection())
				.andExpect(MockMvcResultMatchers.flash()
						.attribute(SynapseModelAndView.FAILURE_FLASH,
								Matchers.containsString(
										"Cannot update included workspaces.")));
	}

	@Test
	@WithMockUser(authorities = {SynapseAdminAuthDictionary.ADMIN_PRIV})
	public void testSetIncludedWorkspaces() throws Exception {
		mockMvc.perform(
				MockMvcRequestBuilders
						.post(VeracodeScaPlugin.DATA_MGMT_PAGE + "/workspaces/include")
						.param("workspaceIds", uuid.toString())
						.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection())
				.andExpect(MockMvcResultMatchers.flash()
						.attribute(SynapseModelAndView.SUCCESS_FLASH,
								"Successfully updated included workspaces"));
	}

	@Test
	@WithMockUser(authorities = {SynapseAdminAuthDictionary.ADMIN_PRIV})
	public void testDeleteWorkspaceIllegalArgumentException() throws Exception {
		BDDMockito.doThrow(IllegalArgumentException.class).when(workspaceService)
				.deleteWorkspace(uuid);
		mockMvc.perform(
				MockMvcRequestBuilders
						.post(VeracodeScaPlugin.DATA_MGMT_PAGE + "/workspace/delete")
						.param("workspaceId", uuid.toString())
						.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection())
				.andExpect(MockMvcResultMatchers.flash()
						.attribute(SynapseModelAndView.FAILURE_FLASH,
								Matchers.containsString("Cannot delete workspace.")));
	}

	@Test
	@WithMockUser(authorities = {SynapseAdminAuthDictionary.ADMIN_PRIV})
	public void testDeleteWorkspace() throws Exception {
		mockMvc.perform(
				MockMvcRequestBuilders
						.post(VeracodeScaPlugin.DATA_MGMT_PAGE + "/workspace/delete")
						.param("workspaceId", uuid.toString())
						.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection())
				.andExpect(MockMvcResultMatchers.flash()
						.attribute(SynapseModelAndView.SUCCESS_FLASH,
								"Successfully deleted workspace"));
	}

	@Test
	@WithMockUser(authorities = {SynapseAdminAuthDictionary.ADMIN_PRIV})
	public void testSetIncludedProjectsIllegalArgumentException() throws Exception {
		BDDMockito.doThrow(IllegalArgumentException.class).when(projectService)
				.setIncluded(Collections.singletonList(uuid));
		mockMvc.perform(
				MockMvcRequestBuilders.post(VeracodeScaPlugin.DATA_MGMT_PAGE + "/projects/include")
						.param("projectIds", uuid.toString())
						.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection())
				.andExpect(MockMvcResultMatchers.flash()
						.attribute(SynapseModelAndView.FAILURE_FLASH,
								Matchers.containsString(
										"Cannot update included projects.")));
	}

	@Test
	@WithMockUser(authorities = {SynapseAdminAuthDictionary.ADMIN_PRIV})
	public void testSetIncludedProjects() throws Exception {
		mockMvc.perform(
				MockMvcRequestBuilders.post(VeracodeScaPlugin.DATA_MGMT_PAGE + "/projects/include")
						.param("projectIds", uuid.toString())
						.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection())
				.andExpect(MockMvcResultMatchers.flash()
						.attribute(SynapseModelAndView.SUCCESS_FLASH,
								"Successfully updated included projects"));
	}

	@Test
	@WithMockUser(authorities = {SynapseAdminAuthDictionary.ADMIN_PRIV})
	public void testDeleteProjectIllegalArgumentException() throws Exception {
		BDDMockito.doThrow(IllegalArgumentException.class).when(projectService)
				.deleteProject(uuid);
		mockMvc.perform(
				MockMvcRequestBuilders
						.post(VeracodeScaPlugin.DATA_MGMT_PAGE + "/project/delete")
						.param("projectId", uuid.toString())
						.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection())
				.andExpect(MockMvcResultMatchers.flash()
						.attribute(SynapseModelAndView.FAILURE_FLASH,
								Matchers.containsString("Cannot delete project.")));
	}

	@Test
	@WithMockUser(authorities = {SynapseAdminAuthDictionary.ADMIN_PRIV})
	public void testDeleteProject() throws Exception {
		mockMvc.perform(
				MockMvcRequestBuilders
						.post(VeracodeScaPlugin.DATA_MGMT_PAGE + "/project/delete")
						.param("projectId", uuid.toString())
						.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection())
				.andExpect(MockMvcResultMatchers.flash()
						.attribute(SynapseModelAndView.SUCCESS_FLASH,
								"Successfully deleted project"));
	}
}
