package com.tracelink.prodsec.synapse.auth.model;

import java.util.Collections;
import org.junit.Assert;
import org.junit.Test;

public class RoleModelTest {

	@Test
	public void testDAO() {
		String roleName = "roleName";
		String privName = "privName";

		PrivilegeModel priv = new PrivilegeModel();
		priv.setName(privName);

		RoleModel role = new RoleModel();
		role.setRoleName(roleName);
		role.setPrivileges(Collections.singleton(priv));

		Assert.assertEquals(roleName, role.getRoleName());
		Assert.assertTrue(role.getPrivileges().contains(priv));
		Assert.assertTrue(role.getPrivilegesAsString().contains(privName));
		Assert.assertTrue(role.hasPrivilege(privName));
	}
}
