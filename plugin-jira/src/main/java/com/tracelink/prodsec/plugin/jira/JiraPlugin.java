package com.tracelink.prodsec.plugin.jira;

import com.tracelink.prodsec.plugin.jira.model.JiraThresholds;
import com.tracelink.prodsec.plugin.jira.model.JiraVuln;
import com.tracelink.prodsec.plugin.jira.service.JiraThresholdsService;
import com.tracelink.prodsec.plugin.jira.service.JiraUpdateService;
import com.tracelink.prodsec.plugin.jira.service.JiraVulnMetricsService;

import com.tracelink.prodsec.synapse.auth.SynapseAdminAuthDictionary;
import com.tracelink.prodsec.synapse.products.model.ProductLineModel;
import com.tracelink.prodsec.synapse.scheduler.job.SchedulerJob;
import com.tracelink.prodsec.synapse.scheduler.job.SimpleSchedulerJob;
import com.tracelink.prodsec.synapse.scheduler.service.schedule.PeriodicSchedule;
import com.tracelink.prodsec.synapse.scorecard.model.ScorecardColumn;
import com.tracelink.prodsec.synapse.scorecard.model.ScorecardValue;
import com.tracelink.prodsec.synapse.scorecard.model.SimpleScorecardColumn;
import com.tracelink.prodsec.synapse.sidebar.model.SidebarLink;
import com.tracelink.prodsec.synapse.sidebar.model.SimpleSidebarLink;
import com.tracelink.prodsec.synapse.spi.PluginDisplayGroup;
import com.tracelink.prodsec.synapse.spi.PluginWithDatabase;
import com.tracelink.prodsec.synapse.spi.annotation.SynapsePluginDatabaseEnabled;

import java.util.Collections;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * The Jira Plugin allows for gathering information from a Jira Server. Using JQL
 * search phrases to query the client, this plugin displays metrics
 * from a team's scrum progress and gathers information on Jira issues,
 * in this case, issues regarding security vulnerabilities.
 *
 * @author bhoran
 */
@SynapsePluginDatabaseEnabled
public class JiraPlugin extends PluginWithDatabase {

	/**
	 * These fields are made public to share the schema definition with the model,
	 * the privilege definition with the controller, and pagelinks with the controller
	 */
	public static final String SCHEMA = "jira";

	public static final String PAGELINK = "/jira";

	public static final String CONFIGURATIONS_PAGE = PAGELINK + "/configure";

	public static final String VULN_PAGE = PAGELINK + "/vulns";

	public static final String SCRUM_PAGE = PAGELINK + "/scrum";

	public static final String MAPPINGS_PAGE = PAGELINK + "/mappings";

	private final JiraUpdateService jiraUpdateService;

	private final JiraVulnMetricsService vulnService;

	private final JiraThresholdsService thresholdsService;

	/**
	 * Creates the Jira  plugin with three pre-configured services: the {@link
	 * JiraUpdateService}, to regularly retrieve data from the Jira server; the {@link
	 * JiraVulnMetricsService}, to parse and store data regarding vulnerable issues
	 * and the {@link JiraThresholdsService}, to store and retrieve data
	 * that defines a relative tolerance to issues.
	 *
	 * @param jiraUpdateService the Jira Update service
	 * @param vulnService       the Jira VulnMetrics Service
	 * @param thresholdsService the risk tolerance thresholds service
	 */
	public JiraPlugin(@Autowired JiraUpdateService jiraUpdateService,
			@Autowired JiraVulnMetricsService vulnService,
			@Autowired JiraThresholdsService thresholdsService) {
		this.jiraUpdateService = jiraUpdateService;
		this.vulnService = vulnService;
		this.thresholdsService = thresholdsService;
	}

	@Override
	protected String getSchemaName() {
		return SCHEMA;
	}

	@Override
	protected String getMigrationsLocation() {
		return "db/jira/";
	}

	@Override
	protected PluginDisplayGroup getPluginDisplayGroup() {
		return new PluginDisplayGroup("Jira Plugin", "stars");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected List<SchedulerJob> getJobsForScheduler() {
		return Arrays.asList(
				new SimpleSchedulerJob("Fetch Jira Data").withJob(jiraUpdateService::syncAllData)
						.onSchedule(new PeriodicSchedule(1, TimeUnit.DAYS)));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected List<ScorecardColumn> getColumnsForScorecard() {
		return Arrays.asList(
				// Create the scorecard column for reporting
				new SimpleScorecardColumn("Jira Vulns").withPageLink(VULN_PAGE)
						.withProductLineCallback(this::getScorecardForProductLine));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected List<SidebarLink> getLinksForSidebar() {
		// Vulnerabilities page
		SidebarLink vulnMetrics = new SimpleSidebarLink("Vulnerabilities")
				.withMaterialIcon("dashboard")
				.withPageLink(VULN_PAGE);

		// Scrum Metrics page
		SidebarLink scrumMetrics = new SimpleSidebarLink("Scrum Metrics")
				.withMaterialIcon("timeline")
				.withPageLink(SCRUM_PAGE);

		// Configurations page
		SidebarLink configurations = new SimpleSidebarLink("Configurations")
				.withMaterialIcon("settings_applications")
				.withPageLink(CONFIGURATIONS_PAGE)
				.withPrivileges(SynapseAdminAuthDictionary.ADMIN_PRIV);

		// Mappings page
		SidebarLink mappings = new SimpleSidebarLink("Mappings").withMaterialIcon("swap_horiz")
				.withPageLink(MAPPINGS_PAGE).withPrivileges(SynapseAdminAuthDictionary.ADMIN_PRIV);

		return Arrays.asList(vulnMetrics, scrumMetrics, configurations, mappings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected List<String> getPrivileges() {
		return Collections.emptyList();
	}

	private ScorecardValue getScorecardForProductLine(ProductLineModel productLine) {
		List<JiraVuln> issues = vulnService
				.getUnresolvedVulnsForProductLine(productLine);

		int numIssues = issues.size();

		if (numIssues == 0) {
			return new ScorecardValue("No Data", ScorecardValue.TrafficLight.NONE);
		}

		return new ScorecardValue(numIssues + "", getTrafficLight(numIssues));
	}

	private ScorecardValue.TrafficLight getTrafficLight(long score) {
		JiraThresholds thresholds = thresholdsService.getThresholds();
		if (thresholds == null) {
			return ScorecardValue.TrafficLight.NONE;
		} else if (score < thresholds.getGreenYellow()) {
			return ScorecardValue.TrafficLight.GREEN;
		} else if (score >= thresholds.getGreenYellow() && score < thresholds.getYellowRed()) {
			return ScorecardValue.TrafficLight.YELLOW;
		} else {
			return ScorecardValue.TrafficLight.RED;
		}
	}
}
