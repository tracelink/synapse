package com.tracelink.prodsec.synapse.auth.model;

import java.util.Arrays;
import org.junit.Assert;
import org.junit.Test;

public class UserModelTest {

	@Test
	public void testDAO() {
		String email = "test@foo.com";
		String password = "myPass";
		boolean enabled = true;
		String role1Name = "role1";
		String role2Name = "role2";
		RoleModel role1 = new RoleModel();
		role1.setRoleName(role1Name);
		RoleModel role2 = new RoleModel();
		role2.setRoleName(role2Name);

		UserModel user = new UserModel();
		user.setUsername(email);
		user.setPassword(password);
		user.setEnabled(enabled);
		user.setRoles(Arrays.asList(role1, role2));

		Assert.assertEquals(email, user.getUsername());
		Assert.assertEquals(password, user.getPassword());
		Assert.assertEquals(enabled, user.isEnabled());
		Assert.assertTrue(user.getRoles().containsAll(Arrays.asList(role1, role2)));
		Assert.assertTrue(user.getRolesAsString().contains(role1Name));
		Assert.assertTrue(user.getRolesAsString().contains(role2Name));
		Assert.assertTrue(user.hasRole(role1Name));
	}
}
