package com.tracelink.prodsec.plugin.sonatype;

import com.tracelink.prodsec.plugin.sonatype.exception.SonatypeThresholdsException;
import com.tracelink.prodsec.plugin.sonatype.model.SonatypeApp;
import com.tracelink.prodsec.plugin.sonatype.model.SonatypeMetrics;
import com.tracelink.prodsec.plugin.sonatype.model.SonatypeThresholds;
import com.tracelink.prodsec.plugin.sonatype.service.SonatypeAppService;
import com.tracelink.prodsec.plugin.sonatype.service.SonatypeClientService;
import com.tracelink.prodsec.plugin.sonatype.service.SonatypeThresholdsService;
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
 * The Sonatype Plugin gathers data from the Sonatype Nexus IQ Server on
 * SCA violations. The plugin allows mapping a {@link SonatypeApp} on the Nexus
 * IQ server to a {@link ProjectModel} in Synapse in a one-to-one relationship.
 * The plugin will then display violation statistics per product line, project
 * filter, and project on the scorecard.
 *
 * @author mcool
 */
@SynapsePluginDatabaseEnabled
public class SonatypePlugin extends PluginWithDatabase {

	public static final String SCHEMA = "sonatype_schema";

	public static final String DASHBOARD_PAGE = "/sonatype";

	public static final String CONFIGURATIONS_PAGE = DASHBOARD_PAGE + "/configure";

	public static final String MAPPINGS_PAGE = DASHBOARD_PAGE + "/mappings";

	private static final long PERIOD = 1;
	private static final TimeUnit TIME_UNIT = TimeUnit.HOURS;

	private final SonatypeAppService appService;
	private final SonatypeClientService clientService;
	private final SonatypeThresholdsService thresholdsService;

	public SonatypePlugin(@Autowired SonatypeAppService appService,
			@Autowired SonatypeClientService clientService,
			@Autowired SonatypeThresholdsService thresholdsService) {
		this.appService = appService;
		this.clientService = clientService;
		this.thresholdsService = thresholdsService;
	}

	@Override
	protected String getSchemaName() {
		return SCHEMA;
	}

	@Override
	protected String getMigrationsLocation() {
		return "db/sonatype";
	}

	@Override
	protected PluginDisplayGroup getPluginDisplayGroup() {
		return new PluginDisplayGroup("Sonatype Nexus IQ", "layers");
	}

	@Override
	protected List<SchedulerJob> getJobsForScheduler() {
		SchedulerJob fetchViolationsJob = new SimpleSchedulerJob("Fetch Sonatype Data")
				.withJob(clientService::fetchData)
				.onSchedule(new PeriodicSchedule(PERIOD, TIME_UNIT));
		return Collections.singletonList(fetchViolationsJob);
	}

	@Override
	protected List<ScorecardColumn> getColumnsForScorecard() {
		ScorecardColumn violations = new SimpleScorecardColumn("Sonatype Violations")
				.withPageLink(DASHBOARD_PAGE)
				.withProductLineCallback(this::getScorecardValueForProductLine)
				.withProjectCallback(this::getScorecardValueForProject);
		return Collections.singletonList(violations);
	}

	@Override
	protected List<SidebarLink> getLinksForSidebar() {
		// Dashboard page
		SidebarLink dashboard = new SimpleSidebarLink("Dashboard").withMaterialIcon("dashboard")
				.withPageLink(DASHBOARD_PAGE);

		// Configurations page
		SidebarLink configurations = new SimpleSidebarLink("Configurations")
				.withMaterialIcon("settings_applications")
				.withPageLink(CONFIGURATIONS_PAGE)
				.withPrivileges(SynapseAdminAuthDictionary.ADMIN_PRIV);

		// Mappings page
		SidebarLink mappings = new SimpleSidebarLink("Mappings").withMaterialIcon("swap_horiz")
				.withPageLink(MAPPINGS_PAGE).withPrivileges(SynapseAdminAuthDictionary.ADMIN_PRIV);

		return Arrays.asList(dashboard, configurations, mappings);
	}

	@Override
	protected List<String> getPrivileges() {
		return Collections.emptyList();
	}

	private ScorecardValue getScorecardValueForProductLine(ProductLineModel synapseProductLine) {
		List<SonatypeMetrics> metrics = appService
				.getMostRecentMetricsForProductLine(synapseProductLine);
		return getScorecardValue(metrics,
				synapseProductLine.getProjects().stream()
						.filter(p -> appService.getMappedApps().stream()
								.map(SonatypeApp::getSynapseProject).collect(Collectors.toList())
								.contains(p)).count());
	}

	private ScorecardValue getScorecardValueForProject(ProjectModel synapseProject) {
		List<SonatypeMetrics> metrics = appService.getMostRecentMetricsForProject(synapseProject);
		return getScorecardValue(metrics, 1);
	}

	private ScorecardValue getScorecardValue(List<SonatypeMetrics> metrics, long numApps) {
		// No data configured
		if (metrics == null || numApps == 0) {
			return new ScorecardValue("No data", TrafficLight.NONE);
		}

		long highVios = metrics.stream().mapToLong(SonatypeMetrics::getHighVios).sum();
		long medVios = metrics.stream().mapToLong(SonatypeMetrics::getMedVios).sum();
		long lowVios = metrics.stream().mapToLong(SonatypeMetrics::getLowVios).sum();

		String violations = "High: " + highVios + ", Med: " + medVios + ", Low: " + lowVios;
		double riskScore =
				metrics.stream().mapToDouble(SonatypeMetrics::getRiskScore).sum() / numApps;
		return new ScorecardValue(violations, getTrafficLight(riskScore));
	}

	private TrafficLight getTrafficLight(double riskScore) {
		// Get thresholds from database
		SonatypeThresholds thresholds;
		try {
			thresholds = thresholdsService.getThresholds();
		} catch (SonatypeThresholdsException e) {
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
