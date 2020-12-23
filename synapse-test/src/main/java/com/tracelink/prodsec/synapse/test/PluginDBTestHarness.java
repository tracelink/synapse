package com.tracelink.prodsec.synapse.test;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tracelink.prodsec.synapse.spi.Plugin;

public abstract class PluginDBTestHarness extends PluginTestHarness {

	protected abstract Plugin buildPlugin();

	protected abstract void configurePluginDBTester(PluginDBTestBuilder<?> testPlan);

	protected void configurePluginTester(PluginTestBuilder<?> testPlan) {
		configurePluginDBTester((PluginDBTestBuilder<?>) testPlan);
	}

	private PluginDBTestBuilder<?> pluginDBTester;

	public PluginDBTestBuilder<?> createTestPlanForPlugin() {
		PluginDBTestBuilder<?> pt = new PluginDBTestBuilder<>();
		pluginDBTester = pt;
		return pt;
	}

	@Test
	public void testGetMigrationsLocation() {
		String migActual = (String) ReflectionTestUtils.invokeGetterMethod(pluginUnderTest, "getMigrationsLocation");
		String migExpect = pluginDBTester.migration;

		Assert.assertEquals(migExpect, migActual);
	}

	@Test
	public void testGetSchemaName() {
		String scActual = (String) ReflectionTestUtils.invokeGetterMethod(pluginUnderTest, "getSchemaName");
		String scExpect = pluginDBTester.schemaName;

		Assert.assertEquals(scExpect, scActual);
	}
}
