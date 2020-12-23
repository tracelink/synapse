package com.tracelink.prodsec.synapse.spi;

import org.junit.Assert;
import org.junit.Test;

public class PluginDisplayGroupTest {

	@Test
	public void testDAO() {
		String displayName = "displayName";
		String materialIcon = "materialIcon";

		PluginDisplayGroup pdg = new PluginDisplayGroup(displayName, materialIcon);
		Assert.assertEquals(displayName, pdg.getDisplayName());
		Assert.assertEquals(materialIcon, pdg.getMaterialIcon());
	}
}
