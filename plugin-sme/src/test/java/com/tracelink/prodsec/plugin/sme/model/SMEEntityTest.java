package com.tracelink.prodsec.plugin.sme.model;

import com.tracelink.prodsec.synapse.products.model.ProjectModel;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class SMEEntityTest {

	@Test
	public void testDAO() {
		SMEEntity entity = new SMEEntity();
		String name = "Name";
		List<ProjectModel> projects = new ArrayList<>();
		entity.setName(name);
		entity.setProjects(projects);
		Assert.assertEquals(name, entity.getName());
		Assert.assertEquals(projects, entity.getProjects());
	}
}
