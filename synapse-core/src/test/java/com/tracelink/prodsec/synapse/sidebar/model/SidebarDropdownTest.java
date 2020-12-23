package com.tracelink.prodsec.synapse.sidebar.model;

import com.tracelink.prodsec.synapse.spi.PluginDisplayGroup;
import java.util.Arrays;
import org.junit.Assert;
import org.junit.Test;

public class SidebarDropdownTest {

	@Test
	public void testDAO() {
		String displayName = "displayName";
		String materialIcon = "materialIcon";

		PluginDisplayGroup pdg = new PluginDisplayGroup(displayName, materialIcon);
		SidebarDropdown drop = new SidebarDropdown(pdg);
		drop.addAuthorization(null);
		Assert.assertEquals(displayName, drop.getDisplayName());
		Assert.assertEquals(materialIcon, drop.getMaterialIcon());
		Assert.assertTrue(drop.getAuthorizeExpression().contains("isAuthenticated"));

		String authorization = "somePriv";
		drop.addAuthorization(Arrays.asList(authorization));
		Assert.assertTrue(drop.getAuthorizeExpression().contains("hasAnyAuthority"));
		Assert.assertTrue(drop.getAuthorizeExpression().contains(authorization));
	}
}
