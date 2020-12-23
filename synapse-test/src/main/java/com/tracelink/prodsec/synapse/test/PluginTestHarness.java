package com.tracelink.prodsec.synapse.test;

import com.tracelink.prodsec.synapse.scheduler.job.SchedulerJob;
import com.tracelink.prodsec.synapse.scorecard.model.ScorecardColumn;
import com.tracelink.prodsec.synapse.sidebar.model.SidebarLink;
import com.tracelink.prodsec.synapse.spi.Plugin;
import com.tracelink.prodsec.synapse.spi.PluginDisplayGroup;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

public abstract class PluginTestHarness {

	protected abstract Plugin buildPlugin();

	protected abstract void configurePluginTester(PluginTestBuilder<?> testPlan);

	private PluginTestBuilder<?> pluginTester;
	Plugin pluginUnderTest;

	public PluginTestBuilder<?> createTestPlanForPlugin() {
		PluginTestBuilder<?> pt = new PluginTestBuilder<>();
		pluginTester = pt;
		return pt;
	}

	@Before
	public void setupTestHarness() {
		pluginUnderTest = buildPlugin();
		pluginTester = createTestPlanForPlugin();
		configurePluginTester(pluginTester);
	}

	@Test
	public void testGetPluginDisplayGroup() {
		PluginDisplayGroup pdgActual = (PluginDisplayGroup) ReflectionTestUtils
				.invokeGetterMethod(pluginUnderTest, "getPluginDisplayGroup");
		PluginDisplayGroup pdgExpect = pluginTester.pdg;

		Assert.assertEquals(pdgExpect.getDisplayName(), pdgActual.getDisplayName());
		Assert.assertEquals(pdgExpect.getMaterialIcon(), pdgActual.getMaterialIcon());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGetJobsForScheduler() {
		List<SchedulerJob> sjActuals = (List<SchedulerJob>) ReflectionTestUtils
				.invokeGetterMethod(pluginUnderTest, "getJobsForScheduler");
		List<SchedulerJob> sjExpects = pluginTester.sjs;
		if (sjActuals == null) {
			sjActuals = new ArrayList<>();
		}
		Assert.assertEquals(sjExpects.size(), sjActuals.size());
		for (int i = 0; i < sjExpects.size(); i++) {
			SchedulerJob sjActual = sjActuals.get(i);
			SchedulerJob sjExpect = sjExpects.get(i);

			Assert.assertEquals(sjExpect.getJobName(), sjActual.getJobName());
			Assert.assertEquals(sjExpect.getJob() == null, sjActual.getJob() == null);
			Assert.assertEquals(sjExpect.getSchedule().makeTrigger(),
					sjActual.getSchedule().makeTrigger());
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGetColumnsForScorecard() {
		List<ScorecardColumn> scActuals = (List<ScorecardColumn>) ReflectionTestUtils
				.invokeGetterMethod(pluginUnderTest, "getColumnsForScorecard");
		List<ScorecardColumn> scExpects = pluginTester.scs;
		if (scActuals == null) {
			scActuals = new ArrayList<>();
		}
		Assert.assertEquals(scExpects.size(), scActuals.size());
		for (int i = 0; i < scExpects.size(); i++) {
			ScorecardColumn scActual = scActuals.get(i);
			ScorecardColumn scExpect = scExpects.get(i);

			Assert.assertEquals(scExpect.getColumnName(), scActual.getColumnName());
			Assert.assertEquals(scExpect.getPageLink(), scActual.getPageLink());
			Assert.assertEquals(scExpect.getProjectCallbackFunction() == null,
					scActual.getProjectCallbackFunction() == null);
			Assert.assertEquals(scExpect.getProductLineCallbackFunction() == null,
					scActual.getProductLineCallbackFunction() == null);
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGetLinksForSidebar() {
		List<SidebarLink> slActuals = (List<SidebarLink>) ReflectionTestUtils
				.invokeGetterMethod(pluginUnderTest, "getLinksForSidebar");
		List<SidebarLink> slExpects = pluginTester.sls;
		if (slActuals == null) {
			slActuals = new ArrayList<>();
		}
		Assert.assertEquals(slExpects.size(), slActuals.size());
		for (int i = 0; i < slExpects.size(); i++) {
			SidebarLink slActual = slActuals.get(i);
			SidebarLink slExpect = slExpects.get(i);

			Assert.assertEquals(slExpect.getDisplayName(), slActual.getDisplayName());
			Assert.assertEquals(slActual.getAuthorizePrivileges(),
					slExpect.getAuthorizePrivileges());
			Assert.assertEquals(slExpect.getPageLink(), slActual.getPageLink());
			Assert.assertEquals(slExpect.getMaterialIcon(), slActual.getMaterialIcon());
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGetPrivileges() {
		List<String> privActuals = (List<String>) ReflectionTestUtils
				.invokeGetterMethod(pluginUnderTest, "getPrivileges");
		List<String> privExpects = pluginTester.privs;
		if (privActuals == null) {
			privActuals = new ArrayList<>();
		}
		Assert.assertEquals(privExpects, privActuals);
	}

}
