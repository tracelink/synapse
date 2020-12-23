package com.tracelink.prodsec.synapse.auth.controller;

import com.tracelink.prodsec.synapse.auth.SynapseAdminAuthDictionary;
import com.tracelink.prodsec.synapse.auth.model.RoleModel;
import com.tracelink.prodsec.synapse.auth.model.UserModel;
import com.tracelink.prodsec.synapse.auth.service.AuthService;
import com.tracelink.prodsec.synapse.mvc.SynapseModelAndView;
import com.tracelink.prodsec.synapse.test.TestSynapseBootApplicationCore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
public class UserMgmtControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private AuthService mockAuthService;

	private static final String mockUserName = "user@foo.com";

	@Test
	@WithMockUser(authorities = SynapseAdminAuthDictionary.ADMIN_PRIV)
	public void testUserMgmt() throws Exception {
		List<UserModel> users = new ArrayList<>();
		BDDMockito.when(mockAuthService.findAllUsers()).thenReturn(users);
		mockMvc.perform(MockMvcRequestBuilders.get("/usermgmt"))
				.andExpect(MockMvcResultMatchers.model().attribute("users", users));
	}

	@Test
	@WithMockUser(username = mockUserName, authorities = SynapseAdminAuthDictionary.ADMIN_PRIV)
	public void testEditUser() throws Exception {
		String email = "foo@bar.com";

		UserModel user = new UserModel();
		user.setUsername(email);

		BDDMockito.when(mockAuthService.findById(BDDMockito.anyLong())).thenReturn(user);

		mockMvc.perform(MockMvcRequestBuilders.get("/usermgmt/edit/1"))
				.andExpect(MockMvcResultMatchers.model().attribute("user", user));
	}

	@Test
	@WithMockUser(username = mockUserName, authorities = SynapseAdminAuthDictionary.ADMIN_PRIV)
	public void testEditUserFailNull() throws Exception {
		BDDMockito.when(mockAuthService.findById(BDDMockito.anyLong())).thenReturn(null);

		mockMvc.perform(MockMvcRequestBuilders.get("/usermgmt/edit/1"))
				.andExpect(MockMvcResultMatchers.flash()
						.attribute(SynapseModelAndView.FAILURE_FLASH,
								Matchers.containsString("Cannot find user")));
	}

	@Test
	@WithMockUser(username = mockUserName, authorities = SynapseAdminAuthDictionary.ADMIN_PRIV)
	public void testEditUserFailCurrentUser() throws Exception {
		UserModel user = new UserModel();
		user.setUsername(mockUserName);

		BDDMockito.when(mockAuthService.findById(BDDMockito.anyLong())).thenReturn(user);
		BDDMockito.when(mockAuthService.isCurrentUser(BDDMockito.any(), BDDMockito.any()))
				.thenReturn(true);

		mockMvc.perform(MockMvcRequestBuilders.get("/usermgmt/edit/1"))
				.andExpect(
						MockMvcResultMatchers.flash().attribute(SynapseModelAndView.FAILURE_FLASH,
								Matchers.containsString("You cannot edit your own information")));
	}

	@Test
	@WithMockUser(authorities = SynapseAdminAuthDictionary.ADMIN_PRIV)
	public void testSaveUserSuccess() throws Exception {
		String userName = "foo@bar.com";
		String roleName = "myRole";

		UserModel user = new UserModel();
		user.setUsername(userName);

		RoleModel role = new RoleModel();
		role.setRoleName(roleName);

		// add role
		String[] truths = new String[]{"on", "true", "yes", "1"};
		for (String truth : truths) {
			user.setRoles(new ArrayList<>());
			testSaveUser(user, role, truth)
					.andExpect(MockMvcResultMatchers.redirectedUrl("/usermgmt"))
					.andExpect(MockMvcResultMatchers.flash()
							.attribute(SynapseModelAndView.SUCCESS_FLASH,
									Matchers.containsString("successfully")));
			Assert.assertTrue(user.getRoles().contains(role));
		}

		// remove role
		String[] falses = new String[]{"off", "0"};
		for (String falsehood : falses) {
			user.setRoles(new ArrayList<>(Arrays.asList(role)));
			testSaveUser(user, role, falsehood)
					.andExpect(MockMvcResultMatchers.redirectedUrl("/usermgmt"))
					.andExpect(MockMvcResultMatchers.flash()
							.attribute(SynapseModelAndView.SUCCESS_FLASH,
									Matchers.containsString("successfully")));
			Assert.assertFalse(user.getRoles().contains(role));
		}

	}

	private ResultActions testSaveUser(UserModel user, RoleModel role, String paramTruth)
			throws Exception {
		BDDMockito.when(mockAuthService.findById(BDDMockito.anyLong())).thenReturn(user);
		BDDMockito.when(mockAuthService.findRoleByName(BDDMockito.eq(role.getRoleName())))
				.thenReturn(role);

		return mockMvc.perform(MockMvcRequestBuilders.post("/usermgmt/edit").param("id", "1")
				.param("enabled", "true")
				.param(role.getRoleName(), paramTruth)
				.with(SecurityMockMvcRequestPostProcessors.csrf()));
	}

	@Test
	@WithMockUser(authorities = SynapseAdminAuthDictionary.ADMIN_PRIV)
	public void testSaveUserFailNull() throws Exception {
		BDDMockito.when(mockAuthService.findById(BDDMockito.anyLong())).thenReturn(null);

		mockMvc.perform(MockMvcRequestBuilders.post("/usermgmt/edit").param("id", "1")
				.param("enabled", "true")
				.param("roleName", "yes").with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(
						MockMvcResultMatchers.flash().attribute(SynapseModelAndView.FAILURE_FLASH,
								Matchers.containsString("Cannot find user")));
	}

	@Test
	@WithMockUser(username = mockUserName, authorities = SynapseAdminAuthDictionary.ADMIN_PRIV)
	public void testSaveUserFailCurrentUser() throws Exception {
		UserModel user = new UserModel();
		user.setUsername(mockUserName);

		BDDMockito.when(mockAuthService.findById(BDDMockito.anyLong())).thenReturn(user);
		BDDMockito.when(mockAuthService.isCurrentUser(BDDMockito.any(), BDDMockito.any()))
				.thenReturn(true);
		mockMvc.perform(MockMvcRequestBuilders.post("/usermgmt/edit").param("id", "1")
				.param("enabled", "true")
				.param("roleName", "yes").with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(
						MockMvcResultMatchers.flash().attribute(SynapseModelAndView.FAILURE_FLASH,
								Matchers.containsString("Cannot edit own information")));
	}

	@Test
	@WithMockUser(authorities = SynapseAdminAuthDictionary.ADMIN_PRIV)
	public void testDeleteUserSuccess() throws Exception {
		String email = "foo@bar.com";

		UserModel user = new UserModel();
		user.setUsername(email);
		user.setRoles(new ArrayList<>(Arrays.asList(new RoleModel())));

		BDDMockito.when(mockAuthService.findById(BDDMockito.anyLong())).thenReturn(user);

		mockMvc.perform(MockMvcRequestBuilders.post("/usermgmt/delete").param("id", "1")
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(
						MockMvcResultMatchers.flash().attribute(SynapseModelAndView.SUCCESS_FLASH,
								Matchers.containsString("deleted successfully")));

		Assert.assertTrue(user.getRoles().isEmpty());
		ArgumentCaptor<UserModel> userCaptor = ArgumentCaptor.forClass(UserModel.class);
		BDDMockito.verify(mockAuthService).deleteUser(userCaptor.capture());
		Assert.assertEquals(user, userCaptor.getValue());
	}

	@Test
	@WithMockUser(authorities = SynapseAdminAuthDictionary.ADMIN_PRIV)
	public void testDeleteUserFailNull() throws Exception {
		BDDMockito.when(mockAuthService.findById(BDDMockito.anyLong())).thenReturn(null);

		mockMvc.perform(MockMvcRequestBuilders.post("/usermgmt/delete").param("id", "1")
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(
						MockMvcResultMatchers.flash().attribute(SynapseModelAndView.FAILURE_FLASH,
								Matchers.containsString("Cannot find user")));
	}

	@Test
	@WithMockUser(authorities = SynapseAdminAuthDictionary.ADMIN_PRIV)
	public void testDeleteUserCurrentUser() throws Exception {
		BDDMockito.when(mockAuthService.findById(BDDMockito.anyLong())).thenReturn(new UserModel());
		BDDMockito.when(mockAuthService.isCurrentUser(BDDMockito.any(), BDDMockito.any()))
				.thenReturn(true);

		mockMvc.perform(MockMvcRequestBuilders.post("/usermgmt/delete").param("id", "1")
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(
						MockMvcResultMatchers.flash().attribute(SynapseModelAndView.FAILURE_FLASH,
								Matchers.containsString("Cannot delete own account")));
	}
}
