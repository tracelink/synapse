package com.tracelink.prodsec.synapse.auth.controller;

import com.tracelink.prodsec.synapse.auth.model.UserModel;
import com.tracelink.prodsec.synapse.auth.service.AuthService;
import com.tracelink.prodsec.synapse.mvc.SynapseModelAndView;
import com.tracelink.prodsec.synapse.test.TestSynapseBootApplicationCore;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestSynapseBootApplicationCore.class)
@AutoConfigureMockMvc
public class ProfileControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private AuthService mockAuthService;

	@MockBean
	private PasswordEncoder mockPasswordEncoder;

	private UserModel mockUser;

	@Before
	public void setup() {
		String email = "foo@bar.com";
		String rolesString = "Role1, Role2";

		mockUser = BDDMockito.mock(UserModel.class);
		BDDMockito.when(mockUser.getUsername()).thenReturn(email);
		BDDMockito.when(mockUser.getRolesAsString()).thenReturn(rolesString);
	}


	@Test
	@WithMockUser
	public void testProfile() throws Exception {
		String email = "foo@bar.com";
		String rolesString = "Role1, Role2";

		UserModel mockUser = BDDMockito.mock(UserModel.class);
		BDDMockito.when(mockUser.getUsername()).thenReturn(email);
		BDDMockito.when(mockUser.getRolesAsString()).thenReturn(rolesString);

		BDDMockito.when(mockAuthService.findByUsername(BDDMockito.anyString()))
				.thenReturn(mockUser);

		mockMvc.perform(MockMvcRequestBuilders.get("/profile"))
				.andExpect(MockMvcResultMatchers.model().attribute("user_name", email))
				.andExpect(MockMvcResultMatchers.model().attribute("user_role", rolesString));
	}

	@Test
	@WithMockUser
	public void testChangePasswordSuccess() throws Exception {
		String currentPassword = "pass";
		String newPassword = "newpass";

		BDDMockito.when(mockAuthService.findByUsername(BDDMockito.anyString()))
				.thenReturn(mockUser);
		BDDMockito.when(mockAuthService
				.passwordMatches(BDDMockito.any(), BDDMockito.eq(currentPassword)))
				.thenReturn(true);
		BDDMockito
				.when(mockAuthService.passwordMatches(BDDMockito.any(), BDDMockito.eq(newPassword)))
				.thenReturn(false);

		mockMvc.perform(
				MockMvcRequestBuilders.post("/profile").param("currentPassword", currentPassword)
						.param("newPassword", newPassword).param("confirmPassword", newPassword)
						.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(
						MockMvcResultMatchers.flash().attribute(SynapseModelAndView.SUCCESS_FLASH,
								Matchers.containsString("successfully")));
	}

	@Test
	@WithMockUser
	public void testChangePasswordFailSsoUser() throws Exception {
		String currentPassword = "pass";
		String newPassword = "newpass";

		BDDMockito.when(mockUser.getSsoId()).thenReturn("abcdef1234567890");

		BDDMockito.when(mockAuthService.findByUsername(BDDMockito.anyString()))
				.thenReturn(mockUser);

		mockMvc.perform(
				MockMvcRequestBuilders.post("/profile").param("currentPassword", currentPassword)
						.param("newPassword", newPassword)
						.param("confirmPassword", newPassword)
						.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(
						MockMvcResultMatchers.flash().attribute(SynapseModelAndView.FAILURE_FLASH,
								Matchers.containsString(
										"You cannot update your password if you authenticate using SSO.")));
	}

	@Test
	@WithMockUser
	public void testChangePasswordFailPasswordMismatch() throws Exception {
		String currentPassword = "pass";
		String newPassword = "newpass";

		BDDMockito.when(mockAuthService.findByUsername(BDDMockito.anyString()))
				.thenReturn(mockUser);

		mockMvc.perform(
				MockMvcRequestBuilders.post("/profile").param("currentPassword", currentPassword)
						.param("newPassword", newPassword)
						.param("confirmPassword", newPassword + "FOO")
						.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(
						MockMvcResultMatchers.flash().attribute(SynapseModelAndView.FAILURE_FLASH,
								Matchers.containsString("don't match")));
	}

	@Test
	@WithMockUser
	public void testChangePasswordFailBadPassword() throws Exception {
		String currentPassword = "pass";
		String newPassword = "newpass";

		BDDMockito.when(mockAuthService.findByUsername(BDDMockito.anyString()))
				.thenReturn(mockUser);

		BDDMockito.when(mockAuthService
				.passwordMatches(BDDMockito.any(), BDDMockito.eq(currentPassword)))
				.thenReturn(false);

		mockMvc.perform(
				MockMvcRequestBuilders.post("/profile").param("currentPassword", currentPassword)
						.param("newPassword", newPassword).param("confirmPassword", newPassword)
						.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(
						MockMvcResultMatchers.flash().attribute(SynapseModelAndView.FAILURE_FLASH,
								Matchers.containsString("invalid")));
	}

	@Test
	@WithMockUser
	public void testChangePasswordFailPasswordSameAsOld() throws Exception {
		String currentPassword = "pass";
		String newPassword = "newpass";

		BDDMockito.when(mockAuthService.findByUsername(BDDMockito.anyString()))
				.thenReturn(mockUser);

		BDDMockito.when(mockAuthService
				.passwordMatches(BDDMockito.any(), BDDMockito.eq(currentPassword)))
				.thenReturn(true);
		BDDMockito
				.when(mockAuthService.passwordMatches(BDDMockito.any(), BDDMockito.eq(newPassword)))
				.thenReturn(true);

		mockMvc.perform(
				MockMvcRequestBuilders.post("/profile").param("currentPassword", currentPassword)
						.param("newPassword", newPassword).param("confirmPassword", newPassword)
						.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(
						MockMvcResultMatchers.flash().attribute(SynapseModelAndView.FAILURE_FLASH,
								Matchers.containsString("old password")));
	}

}
