package com.tracelink.prodsec.plugin.jira;

import com.tracelink.prodsec.plugin.jira.model.JiraThresholds;
import com.tracelink.prodsec.plugin.jira.model.JiraVuln;
import com.tracelink.prodsec.plugin.jira.service.JiraThresholdsService;
import com.tracelink.prodsec.plugin.jira.service.JiraUpdateService;
import com.tracelink.prodsec.plugin.jira.service.JiraVulnMetricsService;
import com.tracelink.prodsec.synapse.auth.SynapseAdminAuthDictionary;
import com.tracelink.prodsec.synapse.products.model.ProductLineModel;
import com.tracelink.prodsec.synapse.scheduler.job.SchedulerJob;
import com.tracelink.prodsec.synapse.scorecard.model.ScorecardColumn;
import com.tracelink.prodsec.synapse.scorecard.model.ScorecardValue;
import com.tracelink.prodsec.synapse.sidebar.model.SidebarLink;
import com.tracelink.prodsec.synapse.spi.PluginDisplayGroup;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class JiraPluginTest {

	@MockBean
	private JiraUpdateService mockUpdateService;

	@MockBean
	private JiraThresholdsService mockThresholdsService;

	@MockBean
	private JiraVulnMetricsService mockVulnService;

	@MockBean
	private JiraPlugin jiraPlugin;

	@Before
	public void setup() {
		jiraPlugin = new JiraPlugin(mockUpdateService, mockVulnService, mockThresholdsService);
	}

	@Test
	public void testGetSchemaName() {
		Assert.assertEquals(JiraPlugin.SCHEMA, jiraPlugin.getSchemaName());
	}

	@Test
	public void testGetMigrationsLocation() {
		Assert.assertEquals("db/jira/", jiraPlugin.getMigrationsLocation());
	}

	@Test
	public void testGetPluginDisplayGroup() {
		PluginDisplayGroup displayGroup = jiraPlugin.getPluginDisplayGroup();
		Assert.assertEquals("Jira Plugin", displayGroup.getDisplayName());
		Assert.assertEquals("stars", displayGroup.getMaterialIcon());
	}

	@Test
	public void testGetJobsForScheduler() {
		List<SchedulerJob> jobs = jiraPlugin.getJobsForScheduler();
		Assert.assertEquals(1, jobs.size());
		Assert.assertEquals("Fetch Jira Data", jobs.get(0).getJobName());
	}

	@Test
	public void testGetColumnsForScorecard() {
		List<ScorecardColumn> columns = jiraPlugin.getColumnsForScorecard();
		Assert.assertEquals(1, columns.size());
		ScorecardColumn column = columns.get(0);
		Assert.assertEquals("Jira Vulns", column.getColumnName());
		Assert.assertEquals(JiraPlugin.VULN_PAGE, column.getPageLink());
		Assert.assertTrue(column.hasProductLineCallback());
	}

	@Test
	public void testGetLinksForSidebar() {
		List<SidebarLink> links = jiraPlugin.getLinksForSidebar();
		Assert.assertEquals(4, links.size());

		SidebarLink dashboard = links.get(0);
		Assert.assertEquals("Vulnerabilities", dashboard.getDisplayName());
		Assert.assertEquals("dashboard", dashboard.getMaterialIcon());
		Assert.assertEquals(JiraPlugin.VULN_PAGE, dashboard.getPageLink());

		SidebarLink reports = links.get(1);
		Assert.assertEquals("Scrum Metrics", reports.getDisplayName());
		Assert.assertEquals("timeline", reports.getMaterialIcon());
		Assert.assertEquals(JiraPlugin.SCRUM_PAGE, reports.getPageLink());

		SidebarLink configurations = links.get(2);
		Assert.assertEquals("Configurations", configurations.getDisplayName());
		Assert.assertEquals("settings_applications", configurations.getMaterialIcon());
		Assert.assertEquals(JiraPlugin.CONFIGURATIONS_PAGE, configurations.getPageLink());
		Assert.assertEquals(SynapseAdminAuthDictionary.ADMIN_PRIV,
				configurations.getAuthorizePrivileges().toArray()[0]);

		SidebarLink mappings = links.get(3);
		Assert.assertEquals("Mappings", mappings.getDisplayName());
		Assert.assertEquals("swap_horiz", mappings.getMaterialIcon());
		Assert.assertEquals(JiraPlugin.MAPPINGS_PAGE, mappings.getPageLink());
		Assert.assertEquals(SynapseAdminAuthDictionary.ADMIN_PRIV,
				mappings.getAuthorizePrivileges().toArray()[0]);
	}

	@Test
	public void testGetPrivileges() {
		Assert.assertTrue(jiraPlugin.getPrivileges().isEmpty());
	}

	private ScorecardValue setupProductLineModelCallback(long score) {
		ProductLineModel plm = new ProductLineModel();
		JiraVuln vuln = BDDMockito.mock(JiraVuln.class);

		//List of score number of vulnerabilities
		List<JiraVuln> issues = new ArrayList<>();
		for (long i = 0; i < score; i++) {
			issues.add(vuln);
		}
		BDDMockito.when(mockVulnService.getUnresolvedVulnsForProductLine(BDDMockito.any()))
				.thenReturn(issues);

		List<ScorecardColumn> cols = jiraPlugin.getColumnsForScorecard();

		Assert.assertTrue(cols.size() > 0);
		return cols.get(0).getProductLineCallbackFunction().apply(plm);
	}

	private void setupThresholds(int greenYellow, int yellowRed) {
		JiraThresholds threshold = new JiraThresholds();
		threshold.setGreenYellow(greenYellow);
		threshold.setYellowRed(yellowRed);
		BDDMockito.when(mockThresholdsService.getThresholds()).thenReturn(threshold);
	}

	@Test
	public void testGetScorecardForProductLineGreen() {
		int score = 5;
		setupThresholds(10, 20);
		ScorecardValue scorecard = setupProductLineModelCallback(score);
		Assert.assertEquals(ScorecardValue.TrafficLight.GREEN, scorecard.getColor());
		Assert.assertEquals(Integer.toString(score), scorecard.getValue());
	}

	@Test
	public void testGetScorecardForProductLineYellow() {
		int score = 10;
		setupThresholds(10, 20);
		ScorecardValue scorecard = setupProductLineModelCallback(score);
		Assert.assertEquals(ScorecardValue.TrafficLight.YELLOW, scorecard.getColor());
		Assert.assertEquals(Integer.toString(score), scorecard.getValue());
	}

	@Test
	public void testGetScorecardForProductLineRed() {
		int score = 20;
		setupThresholds(10, 20);
		ScorecardValue scorecard = setupProductLineModelCallback(score);
		Assert.assertEquals(ScorecardValue.TrafficLight.RED, scorecard.getColor());
		Assert.assertEquals(Integer.toString(score), scorecard.getValue());
	}

	public void testGetScorecardForProductLineNone() {
		int score = 50;
		ScorecardValue scorecard = setupProductLineModelCallback(score);
		Assert.assertEquals(ScorecardValue.TrafficLight.NONE, scorecard.getColor());
		Assert.assertEquals(Integer.toString(score), scorecard.getValue());
	}

	@Test
	public void testGetScorecardForProductLineNoData() {
		ProductLineModel plm = new ProductLineModel();

		List<ScorecardColumn> cols = jiraPlugin.getColumnsForScorecard();
		Assert.assertTrue(cols.size() > 0);

		ScorecardValue scorecard = cols.get(0).getProductLineCallbackFunction().apply(plm);
		Assert.assertEquals(ScorecardValue.TrafficLight.NONE, scorecard.getColor());
		Assert.assertEquals("No Data", scorecard.getValue());
	}
}
