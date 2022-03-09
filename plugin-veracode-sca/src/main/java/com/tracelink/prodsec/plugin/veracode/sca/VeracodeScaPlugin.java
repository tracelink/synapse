package com.tracelink.prodsec.plugin.veracode.sca;

import com.tracelink.prodsec.plugin.veracode.sca.exception.VeracodeScaThresholdsException;
import com.tracelink.prodsec.plugin.veracode.sca.model.VeracodeScaIssue;
import com.tracelink.prodsec.plugin.veracode.sca.model.VeracodeScaProject;
import com.tracelink.prodsec.plugin.veracode.sca.model.VeracodeScaThresholds;
import com.tracelink.prodsec.plugin.veracode.sca.service.VeracodeScaClientService;
import com.tracelink.prodsec.plugin.veracode.sca.service.VeracodeScaProjectService;
import com.tracelink.prodsec.plugin.veracode.sca.service.VeracodeScaThresholdsService;
import com.tracelink.prodsec.synapse.auth.SynapseAdminAuthDictionary;
import com.tracelink.prodsec.synapse.products.model.ProductLineModel;
import com.tracelink.prodsec.synapse.products.model.ProjectModel;
import com.tracelink.prodsec.synapse.scheduler.job.SchedulerJob;
import com.tracelink.prodsec.synapse.scheduler.job.SimpleSchedulerJob;
import com.tracelink.prodsec.synapse.scheduler.service.schedule.PeriodicSchedule;
import com.tracelink.prodsec.synapse.scorecard.model.ScorecardColumn;
import com.tracelink.prodsec.synapse.scorecard.model.ScorecardValue;
import com.tracelink.prodsec.synapse.scorecard.model.ScorecardValue.TrafficLight;
import com.tracelink.prodsec.synapse.scorecard.model.SimpleScorecardColumn;
import com.tracelink.prodsec.synapse.sidebar.model.SidebarLink;
import com.tracelink.prodsec.synapse.sidebar.model.SimpleSidebarLink;
import com.tracelink.prodsec.synapse.spi.PluginDisplayGroup;
import com.tracelink.prodsec.synapse.spi.PluginWithDatabase;
import com.tracelink.prodsec.synapse.spi.annotation.SynapsePluginDatabaseEnabled;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The Veracode SCA plugin gathers data from the Veracode SCA server on SCA issues. The plugin
 * allows mapping a {@link VeracodeScaProject} from the Veracode SCA server to a {@link
 * ProjectModel} in Synapse in a many-to-one relationship. The plugin will then display issue
 * statistics per product line, project filter, and project on the scorecard.
 *
 * @author mcool
 */
@SynapsePluginDatabaseEnabled
public class VeracodeScaPlugin extends PluginWithDatabase {

	public static final String SCHEMA = "veracode_sca_schema";

	public static final String DASHBOARD_PAGE = "/veracode/sca";

	public static final String CONFIGURATIONS_PAGE = DASHBOARD_PAGE + "/configure";

	public static final String DATA_MGMT_PAGE = DASHBOARD_PAGE + "/data";

	public static final String MAPPINGS_PAGE = DASHBOARD_PAGE + "/mappings";

	public static final String ISSUES_PAGE = DASHBOARD_PAGE + "/issues";

	public static final String VIEW_ISSUES_PRIV = "ViewVeracodeSCAIssues";

	private static final long PERIOD = 1;
	private static final TimeUnit TIME_UNIT = TimeUnit.HOURS;

	private final VeracodeScaProjectService projectService;
	private final VeracodeScaClientService clientService;
	private final VeracodeScaThresholdsService thresholdsService;

	public VeracodeScaPlugin(@Autowired VeracodeScaProjectService projectService,
			@Autowired VeracodeScaClientService clientService,
			@Autowired VeracodeScaThresholdsService thresholdsService) {
		this.projectService = projectService;
		this.clientService = clientService;
		this.thresholdsService = thresholdsService;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getSchemaName() {
		return SCHEMA;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getMigrationsLocation() {
		return "db/veracode/sca";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PluginDisplayGroup getPluginDisplayGroup() {
		return new PluginDisplayGroup("Veracode SCA", "layers");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<SchedulerJob> getJobsForScheduler() {
		SchedulerJob fetchViolationsJob = new SimpleSchedulerJob("Fetch Veracode SCA Data")
				.withJob(clientService::fetchData)
				.onSchedule(new PeriodicSchedule(PERIOD, TIME_UNIT));
		return Collections.singletonList(fetchViolationsJob);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<ScorecardColumn> getColumnsForScorecard() {
		ScorecardColumn issuesColumn = new SimpleScorecardColumn("Veracode SCA")
				.withPageLink(DASHBOARD_PAGE)
				.withProductLineCallback(this::getScorecardValueForProductLine)
				.withProjectCallback(this::getScorecardValueForProject);
		return Collections.singletonList(issuesColumn);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<SidebarLink> getLinksForSidebar() {
		// Dashboard page
		SidebarLink dashboard = new SimpleSidebarLink("Dashboard").withMaterialIcon("dashboard")
				.withPageLink(DASHBOARD_PAGE);

		// Issues page
		SidebarLink issues = new SimpleSidebarLink("Issues").withMaterialIcon("report")
				.withPageLink(ISSUES_PAGE)
				.withPrivileges(VIEW_ISSUES_PRIV);

		// Configurations page
		SidebarLink configurations = new SimpleSidebarLink("Configurations")
				.withMaterialIcon("settings_applications")
				.withPageLink(CONFIGURATIONS_PAGE)
				.withPrivileges(SynapseAdminAuthDictionary.ADMIN_PRIV);

		// Projects page
		SidebarLink dataMgmt = new SimpleSidebarLink("Manage Data")
				.withMaterialIcon("visibility")
				.withPageLink(DATA_MGMT_PAGE)
				.withPrivileges(SynapseAdminAuthDictionary.ADMIN_PRIV);

		// Mappings page
		SidebarLink mappings = new SimpleSidebarLink("Mappings").withMaterialIcon("swap_horiz")
				.withPageLink(MAPPINGS_PAGE).withPrivileges(SynapseAdminAuthDictionary.ADMIN_PRIV);

		return Arrays.asList(dashboard, issues, configurations, dataMgmt, mappings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<String> getPrivileges() {
		return Collections.singletonList(VIEW_ISSUES_PRIV);
	}

	private ScorecardValue getScorecardValueForProductLine(ProductLineModel synapseProductLine) {
		List<VeracodeScaIssue> issues = projectService
				.getUnresolvedIssuesForProductLine(synapseProductLine);
		return getScorecardValue(issues, synapseProductLine.getProjects().stream()
				.filter(p -> projectService.getMappedProjects().stream()
						.map(VeracodeScaProject::getSynapseProject)
						.collect(Collectors.toList()).contains(p))
				.count());
	}

	private ScorecardValue getScorecardValueForProject(ProjectModel synapseProject) {
		List<VeracodeScaIssue> metrics = projectService
				.getUnresolvedIssuesForProject(synapseProject);
		return getScorecardValue(metrics, 1);
	}

	private ScorecardValue getScorecardValue(List<VeracodeScaIssue> issues, long numProjects) {
		// No data configured
		if (issues == null || numProjects == 0) {
			return new ScorecardValue("No data", TrafficLight.NONE);
		}

		// Get counts of high issues and high vuln methods
		List<VeracodeScaIssue> highIssues = issues.stream()
				.filter(i -> i.getSeverityString().equals(VeracodeScaIssue.SEVERITY_HIGH))
				.collect(Collectors.toList());
		long highIssuesCount = highIssues.size();
		long highVulnMethodsCount = highIssues.stream().filter(VeracodeScaIssue::isVulnerableMethod)
				.count();
		// Get counts of medium issues and medium vuln methods
		List<VeracodeScaIssue> mediumIssues = issues.stream()
				.filter(i -> i.getSeverityString().equals(VeracodeScaIssue.SEVERITY_MEDIUM))
				.collect(Collectors.toList());
		long mediumIssuesCount = mediumIssues.size();
		long mediumVulnMethodsCount = mediumIssues.stream()
				.filter(VeracodeScaIssue::isVulnerableMethod).count();
		// Get counts of low issues and low vuln methods
		List<VeracodeScaIssue> lowIssues = issues.stream()
				.filter(i -> i.getSeverityString().equals(VeracodeScaIssue.SEVERITY_LOW))
				.collect(Collectors.toList());
		long lowIssuesCount = lowIssues.size();
		long lowVulnMethodsCount = lowIssues.stream().filter(VeracodeScaIssue::isVulnerableMethod)
				.count();

		String issueString = "High: " + highIssuesCount + ", Med: " + mediumIssuesCount + ", Low: "
				+ lowIssuesCount;
		double riskScore = (highVulnMethodsCount * 10 + highIssuesCount * 6
				+ mediumVulnMethodsCount * 3 + mediumIssuesCount * 2
				+ lowVulnMethodsCount + lowIssuesCount * 0.5) * 1.0 / numProjects;
		return new ScorecardValue(issueString, getTrafficLight(riskScore));
	}

	private TrafficLight getTrafficLight(double riskScore) {
		// Get thresholds from database
		VeracodeScaThresholds thresholds;
		try {
			thresholds = thresholdsService.getThresholds();
		} catch (VeracodeScaThresholdsException e) {
			return TrafficLight.NONE;
		}

		if (riskScore <= thresholds.getGreenYellow()) {
			return TrafficLight.GREEN;
		} else if (riskScore <= thresholds.getYellowRed()) {
			return TrafficLight.YELLOW;
		} else {
			return TrafficLight.RED;
		}
	}
}
