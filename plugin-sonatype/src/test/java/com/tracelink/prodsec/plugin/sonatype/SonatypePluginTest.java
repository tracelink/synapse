package com.tracelink.prodsec.plugin.sonatype;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import com.tracelink.prodsec.plugin.sonatype.service.SonatypeAppService;
import com.tracelink.prodsec.plugin.sonatype.service.SonatypeClientService;
import com.tracelink.prodsec.plugin.sonatype.service.SonatypeThresholdsService;
import com.tracelink.prodsec.synapse.auth.SynapseAdminAuthDictionary;
import com.tracelink.prodsec.synapse.scheduler.job.SchedulerJob;
import com.tracelink.prodsec.synapse.scorecard.model.ScorecardColumn;
import com.tracelink.prodsec.synapse.sidebar.model.SidebarLink;
import com.tracelink.prodsec.synapse.spi.PluginDisplayGroup;

@RunWith(SpringRunner.class)
public class SonatypePluginTest {
	@MockBean
	private SonatypeAppService appService;

	@MockBean
	private SonatypeClientService clientService;

	@MockBean
	private SonatypeThresholdsService thresholdsService;

	private SonatypePlugin sonatypePlugin;

	@Before
	public void setup() {
		sonatypePlugin = new SonatypePlugin(appService, clientService, thresholdsService);
	}

	@Test
	public void testGetSchemaName() {
		Assert.assertEquals("sonatype_schema", sonatypePlugin.getSchemaName());
	}

	@Test
	public void testGetMigrationsLocation() {
		Assert.assertEquals("db/sonatype", sonatypePlugin.getMigrationsLocation());
	}

	@Test
	public void testGetPluginDisplayGroup() {
		PluginDisplayGroup displayGroup = sonatypePlugin.getPluginDisplayGroup();
		Assert.assertEquals("Sonatype Nexus IQ", displayGroup.getDisplayName());
		Assert.assertEquals("layers", displayGroup.getMaterialIcon());
	}

	@Test
	public void testGetJobsForScheduler() {
		List<SchedulerJob> jobs = sonatypePlugin.getJobsForScheduler();
		Assert.assertEquals(1, jobs.size());
		Assert.assertEquals("Fetch Sonatype Data", jobs.get(0).getJobName());
	}

	@Test
	public void testGetColumnsForScorecard() {
		List<ScorecardColumn> columns = sonatypePlugin.getColumnsForScorecard();
		Assert.assertEquals(1, columns.size());
		ScorecardColumn column = columns.get(0);
		Assert.assertEquals("Sonatype Violations", column.getColumnName());
		Assert.assertEquals(SonatypePlugin.DASHBOARD_PAGE, column.getPageLink());
		Assert.assertTrue(column.hasProductLineCallback());
		Assert.assertTrue(column.hasProjectCallback());
	}

	@Test
	public void testGetLinksForSidebar() {
		List<SidebarLink> links = sonatypePlugin.getLinksForSidebar();
		Assert.assertEquals(3, links.size());

		SidebarLink dashboard = links.get(0);
		Assert.assertEquals("Dashboard", dashboard.getDisplayName());
		Assert.assertEquals("dashboard", dashboard.getMaterialIcon());
		Assert.assertEquals(SonatypePlugin.DASHBOARD_PAGE, dashboard.getPageLink());

		SidebarLink configurations = links.get(1);
		Assert.assertEquals("Configurations", configurations.getDisplayName());
		Assert.assertEquals("settings_applications", configurations.getMaterialIcon());
		Assert.assertEquals(SonatypePlugin.CONFIGURATIONS_PAGE, configurations.getPageLink());
		Assert.assertEquals(SynapseAdminAuthDictionary.ADMIN_PRIV,
				configurations.getAuthorizePrivileges().toArray()[0]);

		SidebarLink mappings = links.get(2);
		Assert.assertEquals("Mappings", mappings.getDisplayName());
		Assert.assertEquals("swap_horiz", mappings.getMaterialIcon());
		Assert.assertEquals(SonatypePlugin.MAPPINGS_PAGE, mappings.getPageLink());
		Assert.assertEquals(SynapseAdminAuthDictionary.ADMIN_PRIV, mappings.getAuthorizePrivileges().toArray()[0]);
	}

	@Test
	public void testGetPrivileges() {
		Assert.assertTrue(sonatypePlugin.getPrivileges().isEmpty());
	}
}
