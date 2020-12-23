package com.tracelink.prodsec.synapse.products.model;

import java.util.ArrayList;
import java.util.Arrays;
import org.junit.Assert;
import org.junit.Test;

public class ProductLineModelTest {

	@Test
	public void testDAO() {
		String name = "productLine";
		String projectName = "projectName";

		ProjectModel project = new ProjectModel();
		project.setName(projectName);

		ProductLineModel productLine = new ProductLineModel();
		productLine.setName(name);
		productLine.setProjects(Arrays.asList(project));

		Assert.assertEquals(name, productLine.getName());
		Assert.assertTrue(productLine.getProjects().contains(project));
		Assert.assertTrue(productLine.getProjectNames().contains(projectName));

		productLine.setProjects(new ArrayList<>());
		Assert.assertTrue(productLine.getProjectNames().isEmpty());
	}

	@Test
	public void testObjectOverridesEquals() {
		ProductLineModel plm = new ProductLineModel();
		Assert.assertNotEquals(null, plm);

		Assert.assertEquals(plm, plm);

		ProductLineModel plm2 = new ProductLineModel();
		String name = "foo";
		plm.setName(name);
		plm2.setName(name);
		Assert.assertEquals(plm, plm2);

		Assert.assertNotEquals(plm, new Object());
	}

	@Test
	public void testObjectOverridesHash() {
		ProductLineModel plm = new ProductLineModel();
		String name = "foo";
		plm.setName(name);

		Assert.assertEquals(name.hashCode(), plm.hashCode());
	}
}
