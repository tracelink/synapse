package com.tracelink.prodsec.synapse.auth.model;

import org.junit.Assert;
import org.junit.Test;

public class PrivilegeModelTest {

	@Test
	public void testDAO() {
		String name = "privilege";
		PrivilegeModel priv = new PrivilegeModel();
		priv.setName(name);
		Assert.assertEquals(name, priv.getName());
	}
}
