package com.tracelink.prodsec.synapse.products.model;

import java.util.Arrays;
import org.junit.Assert;
import org.junit.Test;

public class ProjectModelTest {

	@Test
	public void testDAO() {
		String name = "foo";
		ProductLineModel plm = new ProductLineModel();
		ProjectFilterModel pfm = new ProjectFilterModel();
		ProjectModel pm = new ProjectModel();
		pm.setName(name);
		pm.setFilters(Arrays.asList(pfm));
		pm.setOwningProductLine(plm);

		Assert.assertEquals(name, pm.getName());
		Assert.assertEquals(plm, pm.getOwningProductLine());
		Assert.assertEquals(1, pm.getFilters().size());
		Assert.assertEquals(pfm, pm.getFilters().get(0));
	}

	@Test
	public void testObjectOverridesEquals() {
		ProjectModel pm = new ProjectModel();
		Assert.assertNotNull(pm);

		Assert.assertEquals(pm, pm);

		ProjectModel pm2 = new ProjectModel();
		String name = "foo";
		pm.setName(name);
		pm2.setName(name);
		Assert.assertEquals(pm, pm2);

		Assert.assertNotEquals(pm, new Object());
	}

	@Test
	public void testObjectOverridesHash() {
		ProjectModel pm = new ProjectModel();
		String name = "foo";
		pm.setName(name);

		Assert.assertEquals(name.hashCode(), pm.hashCode());
	}
}
