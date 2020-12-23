package com.tracelink.prodsec.synapse.products.model;

import java.util.ArrayList;
import java.util.Arrays;
import org.junit.Assert;
import org.junit.Test;

public class ProjectFilterModelTest {

	@Test
	public void testDAO() {
		String name = "productLine";
		String projectName = "projectName";

		ProjectModel project = new ProjectModel();
		project.setName(projectName);

		ProjectFilterModel projectFilter = new ProjectFilterModel();
		projectFilter.setName(name);
		projectFilter.setProjects(Arrays.asList(project));

		Assert.assertEquals(name, projectFilter.getName());
		Assert.assertTrue(projectFilter.getProjects().contains(project));
		Assert.assertTrue(projectFilter.getProjectNames().contains(projectName));

		projectFilter.setProjects(new ArrayList<>());
		Assert.assertTrue(projectFilter.getProjectNames().isEmpty());
	}

	@Test
	public void testObjectOverridesEquals() {
		ProjectFilterModel pfm = new ProjectFilterModel();
		Assert.assertNotEquals(null, pfm);

		Assert.assertEquals(pfm, pfm);

		ProjectFilterModel pfm2 = new ProjectFilterModel();
		String name = "foo";
		pfm.setName(name);
		pfm2.setName(name);
		Assert.assertEquals(pfm, pfm2);

		Assert.assertNotEquals(pfm, new Object());
	}

	@Test
	public void testObjectOverridesHash() {
		ProjectFilterModel pfm = new ProjectFilterModel();
		String name = "foo";
		pfm.setName(name);

		Assert.assertEquals(name.hashCode(), pfm.hashCode());
	}
}
