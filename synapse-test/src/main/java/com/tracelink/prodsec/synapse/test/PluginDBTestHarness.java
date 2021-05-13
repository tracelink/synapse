package com.tracelink.prodsec.synapse.test;

import com.tracelink.prodsec.synapse.spi.Plugin;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Test harness to test basic functionality of a Synapse database plugin.
 */
public abstract class PluginDBTestHarness extends PluginTestHarness {

	/**
	 * Builds the plugin under test.
	 *
	 * @return the built plugin
	 */
	protected abstract Plugin buildPlugin();

	/**
	 * Configures the test builder for a database plugin.
	 *
	 * @param testPlan the test plan to configure the DB tester
	 */
	protected abstract void configurePluginDBTester(PluginDBTestBuilder<?> testPlan);

	/**
	 * Configures the test builder for a basic plugin.
	 *
	 * @param testPlan the test plan to configure the basic tester
	 */
	protected void configurePluginTester(PluginTestBuilder<?> testPlan) {
		configurePluginDBTester((PluginDBTestBuilder<?>) testPlan);
	}

	private PluginDBTestBuilder<?> pluginDBTester;

	/**
	 * Creates a test plan for database plugin features and returns the tester.
	 *
	 * @return the database plugin tester
	 */
	public PluginDBTestBuilder<?> createTestPlanForPlugin() {
		PluginDBTestBuilder<?> pt = new PluginDBTestBuilder<>();
		pluginDBTester = pt;
		return pt;
	}

	@Test
	public void testGetMigrationsLocation() {
		String migActual = (String) ReflectionTestUtils
				.invokeGetterMethod(getPluginUnderTest(), "getMigrationsLocation");
		String migExpect = pluginDBTester.getMigration();

		Assert.assertEquals(migExpect, migActual);
	}

	@Test
	public void testGetSchemaName() {
		String scActual = (String) ReflectionTestUtils
				.invokeGetterMethod(getPluginUnderTest(), "getSchemaName");
		String scExpect = pluginDBTester.getSchemaName();

		Assert.assertEquals(scExpect, scActual);
	}
}
