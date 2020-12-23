package com.tracelink.prodsec.plugin.demo.model;

import org.junit.Assert;
import org.junit.Test;

import com.tracelink.prodsec.synapse.products.model.ProjectModel;

public class DemoProjectEntityTest {

	@Test
	public void testDAO() {
		int vuln = 2;
		ProjectModel pm = new ProjectModel();

		DemoProjectEntity dpm = new DemoProjectEntity();
		dpm.setProjectModel(pm);
		dpm.setVuln(vuln);

		Assert.assertEquals(vuln, dpm.getVuln());
		Assert.assertEquals(pm, dpm.getProjectModel());
	}
}
