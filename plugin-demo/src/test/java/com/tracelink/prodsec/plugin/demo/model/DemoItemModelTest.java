package com.tracelink.prodsec.plugin.demo.model;

import org.junit.Assert;
import org.junit.Test;

public class DemoItemModelTest {

	@Test
	public void testDAO() {
		String projectName = "projectName";
		boolean configured = true;
		int vulns = 2;

		DemoItemModel dim = new DemoItemModel(projectName, configured, vulns);
		Assert.assertEquals(projectName, dim.getProjectName());
		Assert.assertEquals(configured, dim.isConfigured());
		Assert.assertEquals(vulns, dim.getVulns());

	}
}
