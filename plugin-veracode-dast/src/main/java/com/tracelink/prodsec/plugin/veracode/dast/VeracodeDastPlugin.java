package com.tracelink.prodsec.plugin.veracode.dast;

import com.tracelink.prodsec.plugin.veracode.dast.model.VeracodeDastThresholdModel;
import com.tracelink.prodsec.plugin.veracode.dast.service.VeracodeDastAppService;
import com.tracelink.prodsec.plugin.veracode.dast.service.VeracodeDastThresholdsService;
import com.tracelink.prodsec.plugin.veracode.dast.service.VeracodeDastUpdateService;
import com.tracelink.prodsec.plugin.veracode.dast.service.VeracodeDastUpdateService.SyncType;
import com.tracelink.prodsec.synapse.auth.SynapseAdminAuthDictionary;
import com.tracelink.prodsec.synapse.products.model.ProductLineModel;
import com.tracelink.prodsec.synapse.scheduler.job.SchedulerJob;
import com.tracelink.prodsec.synapse.scheduler.job.SimpleSchedulerJob;
import com.tracelink.prodsec.synapse.scheduler.service.schedule.DelayedSchedule;
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
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The Veracode DAST Plugin allows for gathering data about a Veracode DAST
 * setup. The plugin allows reading data about found flaws and graphing both
 * severities and CWEs by App Datasets.
 *
 * @author csmith
 */
@SynapsePluginDatabaseEnabled
public class VeracodeDastPlugin extends PluginWithDatabase {

	public static final String SCHEMA = "veracode_dast_schema";

	public static final String DASHBOARD_PAGE = "/veracodedast";

	public static final String CONFIGURATIONS_PAGE = DASHBOARD_PAGE + "/configure";

	public static final String MAPPINGS_PAGE = DASHBOARD_PAGE + "/mappings";

	public static final String FLAWS_PAGE = DASHBOARD_PAGE + "/flaws";

	public static final String FLAWS_VIEWER_PRIVILEGE = "VeracodeDastFlawViewer";

	private final VeracodeDastAppService appService;

	private final VeracodeDastUpdateService updateService;

	private final VeracodeDastThresholdsService thresholdsService;

	public VeracodeDastPlugin(@Autowired VeracodeDastAppService appService,
			@Autowired VeracodeDastUpdateService updateService,
			@Autowired VeracodeDastThresholdsService thresholdsService) {
		this.appService = appService;
		this.updateService = updateService;
		this.thresholdsService = thresholdsService;
	}

	@Override
	protected String getSchemaName() {
		return SCHEMA;
	}

	@Override
	protected String getMigrationsLocation() {
		return "db/veracodedast";
	}

	@Override
	protected PluginDisplayGroup getPluginDisplayGroup() {
		return new PluginDisplayGroup("Veracode DAST", "zoom_in");
	}

	@Override
	protected List<SchedulerJob> getJobsForScheduler() {
		return Arrays.asList(
				new SimpleSchedulerJob("Veracode DAST Updater - Recents")
						.onSchedule(new PeriodicSchedule(1, TimeUnit.HOURS))
						.withJob(() -> updateService.syncData(SyncType.RECENT)),
				new SimpleSchedulerJob("Veracode DAST Updater - Full Sync")
						.onSchedule(new DelayedSchedule(30, 30, TimeUnit.DAYS))
						.withJob(() -> updateService.syncData(SyncType.ALL)));
	}

	@Override
	protected List<ScorecardColumn> getColumnsForScorecard() {
		return Arrays.asList(new SimpleScorecardColumn("Veracode DAST").withPageLink(DASHBOARD_PAGE)
				.withProductLineCallback(this::getScorecardForProductLine));
	}

	@Override
	protected List<SidebarLink> getLinksForSidebar() {
		// Dashboard page
		SidebarLink dashboard = new SimpleSidebarLink("Dashboard").withMaterialIcon("dashboard")
				.withPageLink(DASHBOARD_PAGE);

		// Flaw Report page
		SidebarLink flaws = new SimpleSidebarLink("Flaw Reports").withMaterialIcon("report_problem")
				.withPageLink(FLAWS_PAGE).withPrivileges(FLAWS_VIEWER_PRIVILEGE);

		// Configurations page
		SidebarLink configurations = new SimpleSidebarLink("Configurations").withMaterialIcon("settings_applications")
				.withPageLink(CONFIGURATIONS_PAGE).withPrivileges(SynapseAdminAuthDictionary.ADMIN_PRIV);

		// Mappings page
		SidebarLink mappings = new SimpleSidebarLink("Mappings").withMaterialIcon("swap_horiz")
				.withPageLink(MAPPINGS_PAGE).withPrivileges(SynapseAdminAuthDictionary.ADMIN_PRIV);

		return Arrays.asList(dashboard, flaws, configurations, mappings);
	}

	@Override
	protected List<String> getPrivileges() {
		return Arrays.asList(FLAWS_VIEWER_PRIVILEGE);
	}

	private ScorecardValue getScorecardForProductLine(ProductLineModel productLine) {
		LongSummaryStatistics sumStats = appService.getAppsBySynapseProductLine(productLine).stream()
				.filter(app -> app != null && app.getCurrentReport() != null)
				.collect(Collectors.summarizingLong(app -> app.getCurrentReport().getScore()));

		if (sumStats.getCount() == 0) {
			return new ScorecardValue("No Data", TrafficLight.NONE);
		}
		long score = sumStats.getSum() / sumStats.getCount();
		return new ScorecardValue(score + "/100", getTrafficLight(score));
	}

	private TrafficLight getTrafficLight(long score) {
		VeracodeDastThresholdModel thresholds = thresholdsService.getThresholds();
		if (thresholds == null) {
			return TrafficLight.NONE;
		} else if (score > thresholds.getGreenYellow()) {
			return TrafficLight.GREEN;
		} else if (score > thresholds.getYellowRed()) {
			return TrafficLight.YELLOW;
		} else {
			return TrafficLight.RED;
		}

	}
}
