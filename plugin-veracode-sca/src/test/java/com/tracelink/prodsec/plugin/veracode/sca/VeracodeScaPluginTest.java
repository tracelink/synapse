package com.tracelink.prodsec.plugin.veracode.sca;

import com.tracelink.prodsec.plugin.veracode.sca.service.VeracodeScaClientService;
import com.tracelink.prodsec.plugin.veracode.sca.service.VeracodeScaProjectService;
import com.tracelink.prodsec.plugin.veracode.sca.service.VeracodeScaThresholdsService;
import com.tracelink.prodsec.synapse.auth.SynapseAdminAuthDictionary;
import com.tracelink.prodsec.synapse.scheduler.job.SchedulerJob;
import com.tracelink.prodsec.synapse.scorecard.model.ScorecardColumn;
import com.tracelink.prodsec.synapse.sidebar.model.SidebarLink;
import com.tracelink.prodsec.synapse.spi.PluginDisplayGroup;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class VeracodeScaPluginTest {

	@MockBean
	private VeracodeScaProjectService appService;

	@MockBean
	private VeracodeScaClientService clientService;

	@MockBean
	private VeracodeScaThresholdsService thresholdsService;

	private VeracodeScaPlugin veracodeScaPlugin;

	@Before
	public void setup() {
		veracodeScaPlugin = new VeracodeScaPlugin(appService, clientService, thresholdsService);
	}

	@Test
	public void testGetSchemaName() {
		Assert.assertEquals("veracode_sca_schema", veracodeScaPlugin.getSchemaName());
	}

	@Test
	public void testGetMigrationsLocation() {
		Assert.assertEquals("db/veracode/sca", veracodeScaPlugin.getMigrationsLocation());
	}

	@Test
	public void testGetPluginDisplayGroup() {
		PluginDisplayGroup displayGroup = veracodeScaPlugin.getPluginDisplayGroup();
		Assert.assertEquals("Veracode SCA", displayGroup.getDisplayName());
		Assert.assertEquals("layers", displayGroup.getMaterialIcon());
	}

	@Test
	public void testGetJobsForScheduler() {
		List<SchedulerJob> jobs = veracodeScaPlugin.getJobsForScheduler();
		Assert.assertEquals(1, jobs.size());
		Assert.assertEquals("Fetch Veracode SCA Data", jobs.get(0).getJobName());
	}

	@Test
	public void testGetColumnsForScorecard() {
		List<ScorecardColumn> columns = veracodeScaPlugin.getColumnsForScorecard();
		Assert.assertEquals(1, columns.size());
		ScorecardColumn column = columns.get(0);
		Assert.assertEquals("Veracode SCA", column.getColumnName());
		Assert.assertEquals(VeracodeScaPlugin.DASHBOARD_PAGE, column.getPageLink());
		Assert.assertTrue(column.hasProductLineCallback());
		Assert.assertTrue(column.hasProjectCallback());
	}

	@Test
	public void testGetLinksForSidebar() {
		List<SidebarLink> links = veracodeScaPlugin.getLinksForSidebar();
		Assert.assertEquals(5, links.size());

		SidebarLink dashboard = links.get(0);
		Assert.assertEquals("Dashboard", dashboard.getDisplayName());
		Assert.assertEquals("dashboard", dashboard.getMaterialIcon());
		Assert.assertEquals(VeracodeScaPlugin.DASHBOARD_PAGE, dashboard.getPageLink());

		SidebarLink issues = links.get(1);
		Assert.assertEquals("Issues", issues.getDisplayName());
		Assert.assertEquals("report", issues.getMaterialIcon());
		Assert.assertEquals(VeracodeScaPlugin.ISSUES_PAGE, issues.getPageLink());
		Assert.assertEquals(VeracodeScaPlugin.VIEW_ISSUES_PRIV,
				issues.getAuthorizePrivileges().toArray()[0]);

		SidebarLink configurations = links.get(2);
		Assert.assertEquals("Configurations", configurations.getDisplayName());
		Assert.assertEquals("settings_applications", configurations.getMaterialIcon());
		Assert.assertEquals(VeracodeScaPlugin.CONFIGURATIONS_PAGE, configurations.getPageLink());
		Assert.assertEquals(SynapseAdminAuthDictionary.ADMIN_PRIV,
				configurations.getAuthorizePrivileges().toArray()[0]);

		SidebarLink dataMgmt = links.get(3);
		Assert.assertEquals("Manage Data", dataMgmt.getDisplayName());
		Assert.assertEquals("visibility", dataMgmt.getMaterialIcon());
		Assert.assertEquals(VeracodeScaPlugin.DATA_MGMT_PAGE, dataMgmt.getPageLink());
		Assert.assertEquals(SynapseAdminAuthDictionary.ADMIN_PRIV,
				dataMgmt.getAuthorizePrivileges().toArray()[0]);

		SidebarLink mappings = links.get(4);
		Assert.assertEquals("Mappings", mappings.getDisplayName());
		Assert.assertEquals("swap_horiz", mappings.getMaterialIcon());
		Assert.assertEquals(VeracodeScaPlugin.MAPPINGS_PAGE, mappings.getPageLink());
		Assert.assertEquals(SynapseAdminAuthDictionary.ADMIN_PRIV,
				mappings.getAuthorizePrivileges().toArray()[0]);
	}

	@Test
	public void testGetPrivileges() {
		Assert.assertEquals(1, veracodeScaPlugin.getPrivileges().size());
		Assert.assertEquals(VeracodeScaPlugin.VIEW_ISSUES_PRIV,
				veracodeScaPlugin.getPrivileges().get(0));
	}
}
