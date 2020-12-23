package com.tracelink.prodsec.plugin.veracode.dast;

import com.tracelink.prodsec.plugin.veracode.dast.model.VeracodeDastAppModel;
import com.tracelink.prodsec.plugin.veracode.dast.model.VeracodeDastReportModel;
import com.tracelink.prodsec.plugin.veracode.dast.model.VeracodeDastThresholdModel;
import com.tracelink.prodsec.plugin.veracode.dast.service.VeracodeDastAppService;
import com.tracelink.prodsec.plugin.veracode.dast.service.VeracodeDastThresholdsService;
import com.tracelink.prodsec.plugin.veracode.dast.service.VeracodeDastUpdateService;
import com.tracelink.prodsec.synapse.auth.SynapseAdminAuthDictionary;
import com.tracelink.prodsec.synapse.products.model.ProductLineModel;
import com.tracelink.prodsec.synapse.products.model.ProjectModel;
import com.tracelink.prodsec.synapse.scheduler.job.SchedulerJob;
import com.tracelink.prodsec.synapse.scorecard.model.ScorecardColumn;
import com.tracelink.prodsec.synapse.scorecard.model.ScorecardValue;
import com.tracelink.prodsec.synapse.scorecard.model.ScorecardValue.TrafficLight;
import com.tracelink.prodsec.synapse.sidebar.model.SidebarLink;
import com.tracelink.prodsec.synapse.spi.PluginDisplayGroup;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class VeracodeDastPluginTest {

	@MockBean
	private VeracodeDastAppService mockAppService;

	@MockBean
	private VeracodeDastUpdateService mockUpdateService;

	@MockBean
	private VeracodeDastThresholdsService mockThresholdsService;

	private VeracodeDastPlugin plugin;

	@Before
	public void setup() {
		plugin = new VeracodeDastPlugin(mockAppService, mockUpdateService, mockThresholdsService);
	}

	@Test
	public void testGetSchemaName() {
		Assert.assertEquals(VeracodeDastPlugin.SCHEMA, plugin.getSchemaName());
	}

	@Test
	public void testGetMigrationsLocation() {
		Assert.assertEquals("db/veracodedast", plugin.getMigrationsLocation());
	}

	@Test
	public void testGetPluginDisplayGroup() {
		PluginDisplayGroup displayGroup = plugin.getPluginDisplayGroup();
		Assert.assertEquals("Veracode DAST", displayGroup.getDisplayName());
		Assert.assertEquals("zoom_in", displayGroup.getMaterialIcon());
	}

	@Test
	public void testGetJobsForScheduler() {
		List<SchedulerJob> jobs = plugin.getJobsForScheduler();
		Assert.assertEquals(1, jobs.size());
		Assert.assertEquals("Veracode DAST Updater", jobs.get(0).getJobName());
	}

	@Test
	public void testGetColumnsForScorecard() {
		List<ScorecardColumn> columns = plugin.getColumnsForScorecard();
		Assert.assertEquals(1, columns.size());
		ScorecardColumn column = columns.get(0);
		Assert.assertEquals("Veracode DAST", column.getColumnName());
		Assert.assertEquals(VeracodeDastPlugin.DASHBOARD_PAGE, column.getPageLink());
		Assert.assertTrue(column.hasProductLineCallback());
		Assert.assertFalse(column.hasProjectCallback());
	}

	@Test
	public void testGetLinksForSidebar() {
		List<SidebarLink> links = plugin.getLinksForSidebar();
		Assert.assertEquals(4, links.size());

		SidebarLink dashboard = links.get(0);
		Assert.assertEquals("Dashboard", dashboard.getDisplayName());
		Assert.assertEquals("dashboard", dashboard.getMaterialIcon());
		Assert.assertEquals(VeracodeDastPlugin.DASHBOARD_PAGE, dashboard.getPageLink());

		SidebarLink reports = links.get(1);
		Assert.assertEquals("Flaw Reports", reports.getDisplayName());
		Assert.assertEquals("report_problem", reports.getMaterialIcon());
		Assert.assertEquals(VeracodeDastPlugin.FLAWS_PAGE, reports.getPageLink());

		SidebarLink configurations = links.get(2);
		Assert.assertEquals("Configurations", configurations.getDisplayName());
		Assert.assertEquals("settings_applications", configurations.getMaterialIcon());
		Assert.assertEquals(VeracodeDastPlugin.CONFIGURATIONS_PAGE, configurations.getPageLink());
		Assert.assertEquals(SynapseAdminAuthDictionary.ADMIN_PRIV,
				configurations.getAuthorizePrivileges().toArray()[0]);

		SidebarLink mappings = links.get(3);
		Assert.assertEquals("Mappings", mappings.getDisplayName());
		Assert.assertEquals("swap_horiz", mappings.getMaterialIcon());
		Assert.assertEquals(VeracodeDastPlugin.MAPPINGS_PAGE, mappings.getPageLink());
		Assert.assertEquals(SynapseAdminAuthDictionary.ADMIN_PRIV,
				mappings.getAuthorizePrivileges().toArray()[0]);
	}

	@Test
	public void testGetPrivileges() {
		Assert.assertEquals(VeracodeDastPlugin.FLAWS_VIEWER_PRIVILEGE,
				plugin.getPrivileges().get(0));
	}

	private ScorecardValue setupProductLineModelCallback(long score) {
		ProjectModel project = new ProjectModel();
		ProductLineModel plm = new ProductLineModel();
		plm.setProjects(Arrays.asList(project));

		VeracodeDastReportModel report = BDDMockito.mock(VeracodeDastReportModel.class);
		BDDMockito.when(report.getScore()).thenReturn(score);

		VeracodeDastAppModel appModel = BDDMockito.mock(VeracodeDastAppModel.class);
		BDDMockito.when(appModel.getCurrentReport()).thenReturn(report);

		BDDMockito.when(mockAppService.getAppsBySynapseProductLine(BDDMockito.any()))
				.thenReturn(Arrays.asList(appModel));

		List<ScorecardColumn> cols = plugin.getColumnsForScorecard();

		Assert.assertTrue(cols.size() > 0);
		return cols.get(0).getProductLineCallbackFunction().apply(plm);
	}

	private void setupThresholds(int greenYellow, int yellowRed) {
		VeracodeDastThresholdModel threshold = new VeracodeDastThresholdModel();
		threshold.setGreenYellow(greenYellow);
		threshold.setYellowRed(yellowRed);
		BDDMockito.when(mockThresholdsService.getThresholds()).thenReturn(threshold);
	}

	@Test
	public void testGetScorecardForProductLineGreen() {
		int score = 100;
		setupThresholds(90, 80);
		ScorecardValue scorecard = setupProductLineModelCallback(score);
		Assert.assertEquals(TrafficLight.GREEN, scorecard.getColor());
		Assert.assertEquals(score + "/100", scorecard.getValue());
	}

	@Test
	public void testGetScorecardForProductLineYellow() {
		int score = 90;
		setupThresholds(90, 80);
		ScorecardValue scorecard = setupProductLineModelCallback(score);
		Assert.assertEquals(TrafficLight.YELLOW, scorecard.getColor());
		Assert.assertEquals(score + "/100", scorecard.getValue());
	}

	@Test
	public void testGetScorecardForProductLineRed() {
		int score = 50;
		setupThresholds(90, 80);
		ScorecardValue scorecard = setupProductLineModelCallback(score);
		Assert.assertEquals(TrafficLight.RED, scorecard.getColor());
		Assert.assertEquals(score + "/100", scorecard.getValue());
	}

	@Test
	public void testGetScorecardForProductLineNone() {
		int score = 50;
		ScorecardValue scorecard = setupProductLineModelCallback(score);
		Assert.assertEquals(TrafficLight.NONE, scorecard.getColor());
		Assert.assertEquals(score + "/100", scorecard.getValue());
	}

	@Test
	public void testGetScorecardForProductLineNoData() {
		ProductLineModel plm = new ProductLineModel();
		plm.setProjects(Arrays.asList(new ProjectModel()));

		List<ScorecardColumn> cols = plugin.getColumnsForScorecard();
		Assert.assertTrue(cols.size() > 0);

		ScorecardValue scorecard = cols.get(0).getProductLineCallbackFunction().apply(plm);
		Assert.assertEquals(TrafficLight.NONE, scorecard.getColor());
		Assert.assertEquals("No Data", scorecard.getValue());
	}
}
