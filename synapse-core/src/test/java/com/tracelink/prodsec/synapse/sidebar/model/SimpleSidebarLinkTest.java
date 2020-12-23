package com.tracelink.prodsec.synapse.sidebar.model;

import java.util.Arrays;
import org.junit.Assert;
import org.junit.Test;

public class SimpleSidebarLinkTest {

	@Test
	public void testDAO() {
		String displayName = "displayName";
		String pageLink = "pageLink";
		String materialIcon = "materialIcon";
		String priv1 = "priv1";
		String priv2 = "priv2";

		SimpleSidebarLink link = new SimpleSidebarLink(displayName).withPageLink(pageLink)
				.withMaterialIcon(materialIcon).withPrivileges((String[]) null);

		Assert.assertEquals(displayName, link.getDisplayName());
		Assert.assertEquals(pageLink, link.getPageLink());
		Assert.assertEquals(materialIcon, link.getMaterialIcon());
		Assert.assertTrue(link.getAuthorizeExpression().contains("isAuthenticated"));
		Assert.assertTrue(link.getAuthorizePrivileges().isEmpty());

		link.withPrivileges(priv1);
		Assert.assertTrue(link.getAuthorizeExpression().contains("hasAnyAuthority"));
		Assert.assertTrue(link.getAuthorizeExpression().contains(priv1));
		Assert.assertTrue(link.getAuthorizePrivileges().contains(priv1));

		link.withPrivileges(priv2);
		Assert.assertTrue(link.getAuthorizeExpression().contains("hasAnyAuthority"));
		Assert.assertTrue(link.getAuthorizeExpression().contains(priv1));
		Assert.assertTrue(link.getAuthorizeExpression().contains(priv2));
		Assert.assertTrue(link.getAuthorizePrivileges().containsAll(Arrays.asList(priv1, priv2)));
	}
}
