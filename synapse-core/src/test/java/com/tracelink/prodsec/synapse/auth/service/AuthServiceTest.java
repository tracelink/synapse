package com.tracelink.prodsec.synapse.auth.service;

import com.tracelink.prodsec.synapse.auth.SynapseAdminAuthDictionary;
import com.tracelink.prodsec.synapse.auth.model.PrivilegeModel;
import com.tracelink.prodsec.synapse.auth.model.RoleModel;
import com.tracelink.prodsec.synapse.auth.model.UserModel;
import com.tracelink.prodsec.synapse.auth.repository.PrivilegeRepository;
import com.tracelink.prodsec.synapse.auth.repository.RoleRepository;
import com.tracelink.prodsec.synapse.auth.repository.UserRepository;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import org.apache.http.auth.BasicUserPrincipal;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class AuthServiceTest {

	@MockBean
	private UserRepository userRepo;

	@MockBean
	private RoleRepository roleRepo;

	@MockBean
	private PrivilegeRepository privRepo;

	// garbage no-op password encoder for testing
	private final PasswordEncoder passwordEncoder = new PassthroughPasswordEncoder();

	private AuthService createServiceSkipSetup() {
		BDDMockito.when(privRepo.findByName(SynapseAdminAuthDictionary.ADMIN_PRIV))
				.thenReturn(new PrivilegeModel());
		BDDMockito.when(roleRepo.findByRoleName(SynapseAdminAuthDictionary.ADMIN_ROLE))
				.thenReturn(new RoleModel());
		BDDMockito.when(userRepo.findByUsername(SynapseAdminAuthDictionary.DEFAULT_ADMIN_USERNAME))
				.thenReturn(new UserModel());

		AuthService auth = new AuthService(passwordEncoder, userRepo, roleRepo, privRepo);

		// reset counters on invocations for easier test counting
		BDDMockito.clearInvocations(privRepo);
		BDDMockito.clearInvocations(roleRepo);
		BDDMockito.clearInvocations(userRepo);
		return auth;
	}

	@Test
	public void defaultAuthExists() {
		BDDMockito.when(privRepo.findByName(SynapseAdminAuthDictionary.ADMIN_PRIV))
				.thenReturn(new PrivilegeModel());
		RoleModel role = new RoleModel();
		BDDMockito.when(roleRepo.findByRoleName(SynapseAdminAuthDictionary.ADMIN_ROLE))
				.thenReturn(role);
		UserModel user = new UserModel();
		user.setRoles(Arrays.asList(role));
		BDDMockito.when(userRepo.findByUsername(SynapseAdminAuthDictionary.DEFAULT_ADMIN_USERNAME))
				.thenReturn(user);

		BDDMockito.when(roleRepo.saveAndFlush(BDDMockito.any()))
				.thenAnswer(invocation -> invocation.getArguments()[0]);

		new AuthService(passwordEncoder, userRepo, roleRepo, privRepo);

		BDDMockito.verify(privRepo, BDDMockito.never()).saveAndFlush(BDDMockito.any());
		BDDMockito.verify(roleRepo, BDDMockito.times(1)).saveAndFlush(BDDMockito.any());
		BDDMockito.verify(userRepo, BDDMockito.never()).saveAndFlush(BDDMockito.any());
	}

	@Test
	public void defaultAuthNothingExists() {
		// make sure finds all return null
		BDDMockito.when(privRepo.findByName(SynapseAdminAuthDictionary.ADMIN_PRIV))
				.thenReturn(null);
		BDDMockito.when(roleRepo.findByRoleName(SynapseAdminAuthDictionary.ADMIN_ROLE))
				.thenReturn(null);
		BDDMockito.when(userRepo.findByUsername(SynapseAdminAuthDictionary.DEFAULT_ADMIN_USERNAME))
				.thenReturn(null);

		// pass back the object passed in to avoid NPEs
		BDDMockito.when(privRepo.saveAndFlush(BDDMockito.any()))
				.thenAnswer(invocation -> invocation.getArguments()[0]);
		BDDMockito.when(roleRepo.saveAndFlush(BDDMockito.any()))
				.thenAnswer(invocation -> invocation.getArguments()[0]);
		BDDMockito.when(userRepo.saveAndFlush(BDDMockito.any()))
				.thenAnswer(invocation -> invocation.getArguments()[0]);

		new AuthService(passwordEncoder, userRepo, roleRepo, privRepo);

		// prepare arg captures
		ArgumentCaptor<RoleModel> roleCaptor = ArgumentCaptor.forClass(RoleModel.class);
		ArgumentCaptor<UserModel> userCaptor = ArgumentCaptor.forClass(UserModel.class);

		// check that repos are saved the right number of times
		BDDMockito.verify(roleRepo, BDDMockito.times(2)).saveAndFlush(roleCaptor.capture());
		BDDMockito.verify(userRepo, BDDMockito.times(2)).saveAndFlush(userCaptor.capture());

		// pull out objects from captures
		RoleModel adminRole = roleCaptor.getValue();
		UserModel adminUser = userCaptor.getValue();

		Assert.assertTrue(adminUser.getRoles().contains(adminRole));
	}

	@Test
	public void findAllUsersTest() {
		AuthService authService = createServiceSkipSetup();
		authService.findAllUsers();
		BDDMockito.verify(userRepo).findAll();
	}

	@Test
	public void findByUsernameTest() {
		AuthService authService = createServiceSkipSetup();
		authService.findByUsername("");
		BDDMockito.verify(userRepo).findByUsername(BDDMockito.anyString());
	}

	@Test
	public void loadUserByUsernameSuccess() {
		AuthService authService = createServiceSkipSetup();
		String email = "my.email@example.com";
		String password = "myAwfulPw";
		boolean enabled = true;
		String privName = "myPriv";

		PrivilegeModel priv = new PrivilegeModel();
		priv.setName(privName);

		RoleModel role = new RoleModel();
		role.setPrivileges(Collections.singleton(priv));

		UserModel user = new UserModel();
		user.setUsername(email);
		user.setPassword(password);
		user.setEnabled(enabled);
		user.setRoles(Collections.singleton(role));

		BDDMockito.when(userRepo.findByUsername(BDDMockito.anyString())).thenReturn(user);
		UserDetails userDetails = authService.loadUserByUsername("");

		BDDMockito.verify(userRepo).findByUsername(BDDMockito.anyString());
		Assert.assertEquals(email, userDetails.getUsername());
		Assert.assertEquals(password, userDetails.getPassword());
		Assert.assertEquals(enabled, userDetails.isEnabled());
		Assert.assertTrue(userDetails.getAuthorities().stream()
				.anyMatch(auth -> auth.getAuthority().equals(privName)));
	}

	@Test(expected = UsernameNotFoundException.class)
	public void findByUsernameFailUser() {
		AuthService authService = createServiceSkipSetup();
		authService.loadUserByUsername("");
	}

	@Test
	public void findByIdTest() {
		AuthService authService = createServiceSkipSetup();
		BDDMockito.when(userRepo.findById(BDDMockito.anyLong()))
				.thenReturn(Optional.of(new UserModel()));
		authService.findById(1L);
		BDDMockito.verify(userRepo).findById(BDDMockito.anyLong());
	}

	@Test
	public void isAdminTest() {
		AuthService authService = createServiceSkipSetup();

		UserModel user = new UserModel();
		user.setRoles(new ArrayList<>());

		Assert.assertFalse(authService.isAdmin(user));

		RoleModel role = new RoleModel();
		role.setRoleName(SynapseAdminAuthDictionary.ADMIN_ROLE);
		user.setRoles(Collections.singleton(role));

		Assert.assertTrue(authService.isAdmin(user));
	}

	@Test
	public void updateUserTest() {
		AuthService authService = createServiceSkipSetup();
		authService.saveUser(null);
		BDDMockito.verify(userRepo).saveAndFlush(BDDMockito.any());
	}

	@Test
	public void deleteUserTest() {
		AuthService authService = createServiceSkipSetup();
		authService.deleteUser(null);
		BDDMockito.verify(userRepo).delete(BDDMockito.any());
	}

	@Test
	public void passwordMatchesTest() {
		AuthService authService = createServiceSkipSetup();
		String password = "myPassword";
		UserModel user = new UserModel();
		user.setPassword(password);
		Assert.assertTrue(authService.passwordMatches(user, password));
		Assert.assertFalse(authService.passwordMatches(user, "notMyPassword"));
	}

	@Test
	public void isCurrentUserTestSuccess() {
		AuthService authService = createServiceSkipSetup();
		String email = "foo@bar.com";
		UserModel user = new UserModel();
		user.setUsername(email);
		Principal principal = new BasicUserPrincipal(email);
		Assert.assertTrue(authService.isCurrentUser(user, principal));
	}

	@Test
	public void isCurrentUserTestFail() {
		AuthService authService = createServiceSkipSetup();
		String email = "foo@bar.com";
		UserModel user = new UserModel();
		user.setUsername(email);
		Principal principal = new BasicUserPrincipal("other@email.com");
		Assert.assertFalse(authService.isCurrentUser(user, principal));
	}

	@Test
	public void findAllRolesTest() {
		AuthService authService = createServiceSkipSetup();
		authService.findAllRoles();
		BDDMockito.verify(roleRepo).findAll();
	}

	@Test
	public void findRoleByIdTest() {
		AuthService authService = createServiceSkipSetup();
		BDDMockito.when(roleRepo.findById(BDDMockito.anyLong()))
				.thenReturn(Optional.of(new RoleModel()));
		authService.findRoleById(1L);
		BDDMockito.verify(roleRepo).findById(BDDMockito.anyLong());
	}

	@Test
	public void findRoleByNameTest() {
		AuthService authService = createServiceSkipSetup();
		authService.findRoleByName("");
		BDDMockito.verify(roleRepo).findByRoleName(BDDMockito.anyString());
	}

	@Test
	public void updateRoleTest() {
		AuthService authService = createServiceSkipSetup();
		authService.updateRole(null);
		BDDMockito.verify(roleRepo).saveAndFlush(BDDMockito.any());
	}

	@Test
	public void deleteRoleTest() {
		AuthService authService = createServiceSkipSetup();
		authService.deleteRole(null);
		BDDMockito.verify(roleRepo).delete(BDDMockito.any());
	}

	@Test
	public void isBuiltInRoleTestSuccess() {
		AuthService authService = createServiceSkipSetup();
		String roleName = SynapseAdminAuthDictionary.ADMIN_ROLE;
		RoleModel role = new RoleModel();
		role.setRoleName(roleName);
		Assert.assertTrue(authService.isBuiltInRole(role));
	}

	@Test
	public void isBuiltInRoleTestFail() {
		AuthService authService = createServiceSkipSetup();
		String roleName = "notadmin";
		RoleModel role = new RoleModel();
		role.setRoleName(roleName);
		Assert.assertFalse(authService.isBuiltInRole(role));
	}

	@Test
	public void findAllPrivilegesTest() {
		AuthService authService = createServiceSkipSetup();
		authService.findAllPrivileges();
		BDDMockito.verify(privRepo).findAll();
	}

	@Test
	public void createOrGetPrivilegeCreate() {
		AuthService authService = createServiceSkipSetup();
		BDDMockito.when(privRepo.findByName(BDDMockito.anyString())).thenReturn(null);
		BDDMockito.when(privRepo.save(BDDMockito.any()))
				.thenAnswer(invocation -> invocation.getArguments()[0]);

		RoleModel role = new RoleModel();
		role.setPrivileges(new ArrayList<>());

		BDDMockito.when(roleRepo.findByRoleName(BDDMockito.anyString())).thenReturn(role);

		String privName = "myPriv";
		PrivilegeModel priv = authService.createOrGetPrivilege(privName);

		Assert.assertEquals(privName, priv.getName());

		ArgumentCaptor<RoleModel> roleCaptor = ArgumentCaptor.forClass(RoleModel.class);
		BDDMockito.verify(roleRepo).saveAndFlush(roleCaptor.capture());
	}

	@Test
	public void createOrGetPrivilegeUpdate() {
		AuthService authService = createServiceSkipSetup();
		String privName = "myPriv";
		PrivilegeModel priv = new PrivilegeModel();
		priv.setName(privName);
		BDDMockito.when(privRepo.findByName(privName)).thenReturn(priv);

		PrivilegeModel priv2 = authService.createOrGetPrivilege(privName);

		Assert.assertEquals(privName, priv2.getName());
		BDDMockito.verify(privRepo, BDDMockito.never()).save(BDDMockito.any());
	}

	@Test
	public void findPrivilegeByNameTest() {
		AuthService authService = createServiceSkipSetup();
		authService.findPrivilegeByName(null);
		BDDMockito.verify(privRepo).findByName(BDDMockito.any());
	}
}
