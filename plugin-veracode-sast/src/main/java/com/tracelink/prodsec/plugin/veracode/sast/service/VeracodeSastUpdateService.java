package com.tracelink.prodsec.plugin.veracode.sast.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tracelink.prodsec.lib.veracode.api.VeracodeApiClient;
import com.tracelink.prodsec.lib.veracode.api.VeracodeApiException;
import com.tracelink.prodsec.lib.veracode.api.rest.VeracodeRestPagedResourcesIterator;
import com.tracelink.prodsec.lib.veracode.api.rest.model.Application;
import com.tracelink.prodsec.lib.veracode.api.rest.model.ApplicationScan.ScanTypeEnum;
import com.tracelink.prodsec.lib.veracode.api.rest.model.PagedResourceOfApplication;
import com.tracelink.prodsec.lib.veracode.api.rest.model.SummaryReport;
import com.tracelink.prodsec.lib.veracode.api.xml.VeracodeXmlApiException;
import com.tracelink.prodsec.lib.veracode.api.xml.data.buildlist.BuildType;
import com.tracelink.prodsec.lib.veracode.api.xml.data.buildlist.Buildlist;
import com.tracelink.prodsec.plugin.veracode.sast.model.VeracodeSastAppModel;
import com.tracelink.prodsec.plugin.veracode.sast.model.VeracodeSastReportModel;

/**
 * Handles business logic for Updating the apps, reports, and flaws with new
 * builds from Veracode. Note that this takes a "snapshot" and will not go back
 * in time to re-map old reports, even if the findings have been modified
 *
 * @author csmith
 */
@Service
public class VeracodeSastUpdateService {

	private static final Logger LOG = LoggerFactory.getLogger(VeracodeSastUpdateService.class);


	private final VeracodeSastAppService appService;

	private final VeracodeSastReportService reportService;

	private final VeracodeSastClientConfigService configService;
	private final DateTimeFormatter reportDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");

	public VeracodeSastUpdateService(@Autowired VeracodeSastAppService appService,
			@Autowired VeracodeSastReportService reportService,
			@Autowired VeracodeSastClientConfigService configService) {
		this.appService = appService;
		this.reportService = reportService;
		this.configService = configService;

	}

	public enum SyncType {
		ALL(Integer.MAX_VALUE, 4), 
		RECENT(5, 2);
		
		int lookback;
		int threads;

		SyncType(int lookback, int threads) {
			this.lookback = lookback;
			this.threads = threads;
		}
	}

	/**
	 * pulls new data from Veracode and syncs it with the current data
	 */
	public void syncData(SyncType syncType) {
		LOG.info("Beginning Veracode SAST data update. Syncing " + syncType);
		try {
			// get the current client
			VeracodeApiClient client = configService.getApiClient();
			if (client == null) {
				LOG.error("No Configuration for Veracode SAST client");
				return;
			}
			// start the sync operation
			int threadSize = syncType.threads;
			ExecutorService executor = Executors.newFixedThreadPool(threadSize);
			dataSync(client, syncType, executor);
			executor.shutdown();
			executor.awaitTermination(1, TimeUnit.DAYS);
		} catch (Exception e) {
			LOG.error("Veracode SAST data update failed due to error: " + e.getMessage());
		}
		LOG.info("Veracode SAST data update complete. Synced " + syncType);
	}

	/**
	 * The following code follows this process:
	 * <ol>
	 * <li>Get all Applications for this client</li>
	 * <li>Get each Sandbox for each App</li>
	 * <li>Get or create an App Model for the Sandbox</li>
	 * <li>Get a list of builds for this sandbox</li>
	 * <li>Get the associated Veracode report for this build</li>
	 * <li>Get or create a Report Model for each Veracode report</li>
	 * <li>Update or create the flaws associated with this Veracode report</li>
	 * <li>Save the flaws</li>
	 * <li>Save the reports</li>
	 * <li>Save the app</li>
	 * </ol>
	 *
	 * @param client   The API Client to use
	 * @param syncType 
	 * @param executor
	 * @throws VeracodeXmlApiException
	 */
	private void dataSync(VeracodeApiClient client, SyncType syncType, ExecutorService executor) throws VeracodeXmlApiException {
		VeracodeRestPagedResourcesIterator<PagedResourceOfApplication> appIterator = new VeracodeRestPagedResourcesIterator<>(
				page -> client.getRestApplications(ScanTypeEnum.STATIC, page));
		while (appIterator.hasNext()) {
			List<Application> apps = appIterator.next().getEmbedded().getApplications();
			for (Application app : apps) {
				String appName = app.getProfile().getName();
				LOG.debug("doing app: " + appName);

				Buildlist builds = client.getXMLBuildList(String.valueOf(app.getId()));
				saveAppBuilds(client, app, builds, syncType, executor);
			}
		}
	}

	private void saveAppBuilds(VeracodeApiClient client, Application app, Buildlist buildList,
			SyncType syncType, ExecutorService executor) {
		String appName = app.getProfile().getName();
		List<BuildType> builds = buildList.getBuild();
		Collections.sort(builds, (b1, b2) -> b2.getBuildId().compareTo(b1.getBuildId()));
		
		int buildLookback = Math.min(syncType.lookback, builds.size());
		for (int i = 0; i < buildLookback; i++) {
			BuildType build = builds.get(i);
			executor.submit(() -> {
				try {
					long buildId = build.getBuildId();
					LOG.debug("Doing build " + buildId);
					SummaryReport report = client.getRestSummaryReport(String.valueOf(app.getGuid()),
							String.valueOf(buildId));
					if (report.getStaticAnalysis() == null) {
						LOG.debug("No static analysis report found for app: " + appName + " on build id: " + buildId);
						return;
					}
					saveAppReport(appName, report);
				} catch (VeracodeApiException e) {
					LOG.error("Exception while getting Summary Report for app " + appName + " and build "
							+ build.getBuildId(), e);
				}
			});
		}
	}

	/**
	 * The following code follows this process:
	 * <ol>
	 * <li>Get the associated Veracode report for this build</li>
	 * <li>Get or create a Report Model for each Veracode report</li>
	 * <li>Update or create the flaws associated with this Veracode report</li>
	 * <li>Save the flaws</li>
	 * <li>Save the reports</li>
	 * </ol>
	 *
	 * @param client   The API Client to use
	 * @param appModel the Veracode Plugin App Model for these reports
	 * @param builds   the list of builds to get reports for
	 */
	private void saveAppReport(String appName, SummaryReport report) {
		// Get or create an App Model for the Application
		VeracodeSastAppModel appModel = appService.getSastApp(appName);
		if (appModel == null) {
			appModel = new VeracodeSastAppModel();
			appModel.setName(appName);
			/*
			 * N.B. the app must be saved first to have a valid identifier created for the
			 * reports to have a relationship with it.
			 */
			appModel = appService.save(appModel);
		}

		// Get or create a Report Model for each Veracode report
		VeracodeSastReportModel reportModel = reportService
				.getReportForAnalysisAndBuild(report.getAnalysisId().longValue(), report.getBuildId().longValue());
		if (reportModel == null) {
			reportModel = new VeracodeSastReportModel();
			reportModel.setApp(appModel);
		}

		populateReportModel(reportModel, report);

		// Save the reports
		/*
		 * N.B. the report must be saved first to have a valid identifier created for
		 * the flaws to have a relationship with it.
		 */
		reportService.save(reportModel);
		// Save the app
		/*
		 * N.B. This save confirms the associations with all of the reports
		 */
		appService.save(appModel);
	}

	private void populateReportModel(VeracodeSastReportModel reportModel, SummaryReport report) {
		reportModel.setAnalysisId(report.getAnalysisId().longValue());
		reportModel
				.setReportDate(LocalDateTime.parse(report.getStaticAnalysis().getPublishedDate(), reportDateFormatter));
		reportModel.setBuildId(report.getBuildId().longValue());
		reportModel.setScore(report.getStaticAnalysis().getScore());
		reportModel.setTotalFlaws(report.getTotalFlaws());
		reportModel.setUnmitigatedFlaws(report.getFlawsNotMitigated());
		reportModel.setCoordinates(String.format("%s:%s:%s:%s", report.getAccountId(), report.getAppId(),report.getBuildId(),report.getAnalysisId()));
	}
}
