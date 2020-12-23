package com.tracelink.prodsec.synapse.auth.controller;

import com.tracelink.prodsec.synapse.auth.SynapseAdminAuthDictionary;
import com.tracelink.prodsec.synapse.auth.model.PrivilegeModel;
import com.tracelink.prodsec.synapse.auth.model.RoleModel;
import com.tracelink.prodsec.synapse.auth.service.AuthService;
import com.tracelink.prodsec.synapse.mvc.SynapseModelAndView;
import com.tracelink.prodsec.synapse.test.TestSynapseBootApplicationCore;
import java.util.ArrayList;
import java.util.Arrays;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestSynapseBootApplicationCore.class)
@AutoConfigureMockMvc
public class RoleMgmtControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private AuthService mockAuthService;

	@Test
	@WithMockUser(authorities = SynapseAdminAuthDictionary.ADMIN_PRIV)
	public void testRoleManagement() throws Exception {
		RoleModel role = BDDMockito.mock(RoleModel.class);
		BDDMockito.when(mockAuthService.findAllRoles()).thenReturn(Arrays.asList(role));

		mockMvc.perform(MockMvcRequestBuilders.get("/rolemgmt"))
				.andExpect(
						MockMvcResultMatchers.model().attribute("roles", Matchers.contains(role)));
	}

	@Test
	@WithMockUser(authorities = SynapseAdminAuthDictionary.ADMIN_PRIV)
	public void testEditRoleSuccess() throws Exception {
		RoleModel role = BDDMockito.mock(RoleModel.class);
		BDDMockito.when(role.getRoleName()).thenReturn("notadmin");

		PrivilegeModel priv = BDDMockito.mock(PrivilegeModel.class);
		BDDMockito.when(priv.getName()).thenReturn("notadminpriv");

		PrivilegeModel priv2 = BDDMockito.mock(PrivilegeModel.class);
		BDDMockito.when(priv2.getName()).thenReturn(SynapseAdminAuthDictionary.ADMIN_PRIV);

		BDDMockito.when(mockAuthService.findRoleById(BDDMockito.anyLong())).thenReturn(role);
		BDDMockito.when(mockAuthService.findAllPrivileges()).thenReturn(Arrays.asList(priv, priv2));

		mockMvc.perform(MockMvcRequestBuilders.get("/rolemgmt/edit/1"))
				.andExpect(MockMvcResultMatchers.model().attribute("role", Matchers.is(role)))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("privileges", Matchers.contains(priv)));
	}

	@Test
	@WithMockUser(authorities = SynapseAdminAuthDictionary.ADMIN_PRIV)
	public void testEditRoleFailNoRole() throws Exception {
		BDDMockito.when(mockAuthService.findRoleById(BDDMockito.anyLong())).thenReturn(null);

		mockMvc.perform(MockMvcRequestBuilders.get("/rolemgmt/edit/1"))
				.andExpect(MockMvcResultMatchers.flash()
						.attribute(SynapseModelAndView.FAILURE_FLASH,
								Matchers.containsString("Cannot find role")));
	}

	@Test
	@WithMockUser(authorities = SynapseAdminAuthDictionary.ADMIN_PRIV)
	public void testEditRoleFailBuiltin() throws Exception {
		RoleModel role = BDDMockito.mock(RoleModel.class);
		BDDMockito.when(role.getRoleName()).thenReturn(SynapseAdminAuthDictionary.ADMIN_ROLE);

		BDDMockito.when(mockAuthService.findRoleById(BDDMockito.anyLong())).thenReturn(role);
		BDDMockito.when(mockAuthService.isBuiltInRole(BDDMockito.any())).thenReturn(true);

		mockMvc.perform(MockMvcRequestBuilders.get("/rolemgmt/edit/1"))
				.andExpect(MockMvcResultMatchers.flash()
						.attribute(SynapseModelAndView.FAILURE_FLASH,
								Matchers.containsString("Cannot edit a built in role")));
	}

	@Test
	@WithMockUser(authorities = SynapseAdminAuthDictionary.ADMIN_PRIV)
	public void testSaveRoleSuccess() throws Exception {
		String roleName = "notadmin";
		String privName = "priv";

		RoleModel role = new RoleModel();
		role.setRoleName(roleName);

		PrivilegeModel priv = new PrivilegeModel();
		priv.setName(privName);

		// add priv
		String[] truths = new String[]{"on", "true", "yes", "1"};
		for (String truth : truths) {
			role.setPrivileges(new ArrayList<>());
			testSaveRole(role, priv, truth)
					.andExpect(MockMvcResultMatchers.redirectedUrl("/rolemgmt"))
					.andExpect(MockMvcResultMatchers.flash()
							.attribute(SynapseModelAndView.SUCCESS_FLASH,
									Matchers.containsString("successfully")));
			Assert.assertTrue(role.getPrivileges().contains(priv));
		}

		// remove priv
		String[] falses = new String[]{"off", "0"};
		for (String falsehood : falses) {
			role.setPrivileges(new ArrayList<>(Arrays.asList(priv)));
			testSaveRole(role, priv, falsehood)
					.andExpect(MockMvcResultMatchers.redirectedUrl("/rolemgmt"))
					.andExpect(MockMvcResultMatchers.flash()
							.attribute(SynapseModelAndView.SUCCESS_FLASH,
									Matchers.containsString("successfully")));
			Assert.assertFalse(role.getPrivileges().contains(priv));
		}
	}

	private ResultActions testSaveRole(RoleModel role, PrivilegeModel priv, String paramTruth)
			throws Exception {
		BDDMockito.when(mockAuthService.findRoleById(BDDMockito.anyLong())).thenReturn(role);
		BDDMockito.when(mockAuthService.findPrivilegeByName(BDDMockito.eq(priv.getName())))
				.thenReturn(priv);

		return mockMvc.perform(MockMvcRequestBuilders.post("/rolemgmt/edit").param("id", "1")
				.param(priv.getName(), paramTruth)
				.with(SecurityMockMvcRequestPostProcessors.csrf()));
	}

	@Test
	@WithMockUser(authorities = SynapseAdminAuthDictionary.ADMIN_PRIV)
	public void testSaveRoleFailRoleNotExist() throws Exception {
		mockMvc.perform(
				MockMvcRequestBuilders.post("/rolemgmt/edit").param("id", "1").param("test", "on")
						.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(
						MockMvcResultMatchers.flash().attribute(SynapseModelAndView.FAILURE_FLASH,
								Matchers.containsString("Cannot find role")));
	}

	@Test
	@WithMockUser(authorities = SynapseAdminAuthDictionary.ADMIN_PRIV)
	public void testSaveRoleFailBuiltIn() throws Exception {
		RoleModel role = new RoleModel();
		role.setRoleName(SynapseAdminAuthDictionary.ADMIN_ROLE);

		BDDMockito.when(mockAuthService.findRoleById(BDDMockito.anyLong())).thenReturn(role);
		BDDMockito.when(mockAuthService.isBuiltInRole(BDDMockito.any())).thenReturn(true);

		mockMvc.perform(
				MockMvcRequestBuilders.post("/rolemgmt/edit").param("id", "1").param("test", "on")
						.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(
						MockMvcResultMatchers.flash().attribute(SynapseModelAndView.FAILURE_FLASH,
								Matchers.containsString("Cannot edit")));
	}

	@Test
	@WithMockUser(authorities = SynapseAdminAuthDictionary.ADMIN_PRIV)
	public void testDeleteRoleSuccess() throws Exception {
		RoleModel role = new RoleModel();
		role.setRoleName("notadmin");

		BDDMockito.when(mockAuthService.findRoleById(BDDMockito.anyLong())).thenReturn(role);

		mockMvc.perform(MockMvcRequestBuilders.post("/rolemgmt/delete").param("id", "1")
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(
						MockMvcResultMatchers.flash().attribute(SynapseModelAndView.SUCCESS_FLASH,
								Matchers.containsString("deleted successfully")));

		BDDMockito.verify(mockAuthService).deleteRole(BDDMockito.eq(role));
	}

	@Test
	@WithMockUser(authorities = SynapseAdminAuthDictionary.ADMIN_PRIV)
	public void testDeleteRoleNull() throws Exception {
		BDDMockito.when(mockAuthService.findRoleById(BDDMockito.anyLong())).thenReturn(null);

		mockMvc.perform(MockMvcRequestBuilders.post("/rolemgmt/delete").param("id", "1")
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(
						MockMvcResultMatchers.flash().attribute(SynapseModelAndView.FAILURE_FLASH,
								Matchers.containsString("Cannot delete role")));
	}

	@Test
	@WithMockUser(authorities = SynapseAdminAuthDictionary.ADMIN_PRIV)
	public void testDeleteRoleBuiltIn() throws Exception {
		RoleModel role = new RoleModel();
		role.setRoleName(SynapseAdminAuthDictionary.ADMIN_ROLE);

		BDDMockito.when(mockAuthService.findRoleById(BDDMockito.anyLong())).thenReturn(role);
		BDDMockito.when(mockAuthService.isBuiltInRole(BDDMockito.any())).thenReturn(true);

		mockMvc.perform(MockMvcRequestBuilders.post("/rolemgmt/delete").param("id", "1")
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(
						MockMvcResultMatchers.flash().attribute(SynapseModelAndView.FAILURE_FLASH,
								Matchers.containsString("Cannot delete a built in")));
	}

	//
	@Test
	@WithMockUser(authorities = SynapseAdminAuthDictionary.ADMIN_PRIV)
	public void testCreateRoleSuccess() throws Exception {
		String roleName = "notadmin";
		BDDMockito.when(mockAuthService.findRoleByName(BDDMockito.anyString())).thenReturn(null);

		BDDMockito.when(mockAuthService.updateRole(BDDMockito.any()))
				.thenAnswer(answer -> answer.getArgument(0));

		mockMvc.perform(MockMvcRequestBuilders.post("/rolemgmt/create").param("roleName", roleName)
				.with(SecurityMockMvcRequestPostProcessors.csrf()));

		ArgumentCaptor<RoleModel> roleCaptor = ArgumentCaptor.forClass(RoleModel.class);
		BDDMockito.verify(mockAuthService).updateRole(roleCaptor.capture());
		Assert.assertEquals(roleName, roleCaptor.getValue().getRoleName());
	}

	@Test
	@WithMockUser(authorities = SynapseAdminAuthDictionary.ADMIN_PRIV)
	public void testCreateRoleBlank() throws Exception {
		String roleName = "";

		mockMvc.perform(MockMvcRequestBuilders.post("/rolemgmt/create").param("roleName", roleName)
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(
						MockMvcResultMatchers.flash().attribute(SynapseModelAndView.FAILURE_FLASH,
								Matchers.containsString("Role must have a name")));
	}

	@Test
	@WithMockUser(authorities = SynapseAdminAuthDictionary.ADMIN_PRIV)
	public void testCreateRoleExists() throws Exception {
		String roleName = "notadmin";
		BDDMockito.when(mockAuthService.findRoleByName(BDDMockito.anyString()))
				.thenReturn(new RoleModel());

		mockMvc.perform(MockMvcRequestBuilders.post("/rolemgmt/create").param("roleName", roleName)
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(
						MockMvcResultMatchers.flash().attribute(SynapseModelAndView.FAILURE_FLASH,
								Matchers.containsString("Role already exists")));
	}
}
