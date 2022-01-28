package com.tracelink.prodsec.plugin.veracode.dast.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tracelink.prodsec.lib.veracode.xml.api.VeracodeXmlApiClient;
import com.tracelink.prodsec.lib.veracode.xml.api.VeracodeXmlApiException;
import com.tracelink.prodsec.lib.veracode.xml.api.data.applist.AppType;
import com.tracelink.prodsec.lib.veracode.xml.api.data.applist.Applist;
import com.tracelink.prodsec.lib.veracode.xml.api.data.buildlist.BuildType;
import com.tracelink.prodsec.lib.veracode.xml.api.data.buildlist.Buildlist;
import com.tracelink.prodsec.lib.veracode.xml.api.data.detailedreport.AnalysisType;
import com.tracelink.prodsec.lib.veracode.xml.api.data.detailedreport.CweType;
import com.tracelink.prodsec.lib.veracode.xml.api.data.detailedreport.Detailedreport;
import com.tracelink.prodsec.lib.veracode.xml.api.data.detailedreport.FlawType;
import com.tracelink.prodsec.plugin.veracode.dast.model.VeracodeDastAppModel;
import com.tracelink.prodsec.plugin.veracode.dast.model.VeracodeDastFlawModel;
import com.tracelink.prodsec.plugin.veracode.dast.model.VeracodeDastReportModel;

/**
 * Handles business logic for Updating the apps, reports, and flaws with new
 * builds from Veracode. Note that this takes a "snapshot" and will not go back
 * in time to re-map old reports, even if the findings have been modified
 *
 * @author csmith
 */
@Service
public class VeracodeDastUpdateService {

	private static final Logger LOG = LoggerFactory.getLogger(VeracodeDastUpdateService.class);

	private final VeracodeDastAppService appService;

	private final VeracodeDastReportService reportService;

	private final VeracodeDastFlawService flawService;

	private final VeracodeDastClientConfigService configService;
	private final DateTimeFormatter reportDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");

	public VeracodeDastUpdateService(@Autowired VeracodeDastAppService appService,
			@Autowired VeracodeDastFlawService flawService, @Autowired VeracodeDastReportService reportService,
			@Autowired VeracodeDastClientConfigService configService) {
		this.appService = appService;
		this.flawService = flawService;
		this.reportService = reportService;
		this.configService = configService;
	}

	/**
	 * pulls new data from Veracode and syncs it with the current data
	 */
	public void syncAllData() {
		LOG.info("Beginning Veracode DAST data update");
		try {
			// get the current client
			VeracodeXmlApiClient client = configService.getApiClient();
			if (client == null) {
				LOG.error("No Configuration for Veracode DAST client");
				return;
			}
			// start the sync operation
			dataSync(client);

		} catch (Exception e) {
			LOG.error("Veracode DAST data update failed due to error: " + e.getMessage());
		}
		LOG.info("Veracode DAST data update complete");
	}

	/**
	 * The following code follows this process:
	 * <ol>
	 * <li>Get all Applications for this client</li>
	 * <li>Get a list of builds for this app</li>
	 * <li>Get the associated Veracode report for this build</li>
	 * <li>Get or create an App Model for the Application</li>
	 * <li>Get or create a Report Model for each Veracode report</li>
	 * <li>Update or create the flaws associated with this Veracode report</li>
	 * <li>Save the flaws</li>
	 * <li>Save the reports</li>
	 * <li>Save the app</li>
	 * </ol>
	 *
	 * @param client The API Client to use
	 */
	private void dataSync(VeracodeXmlApiClient client) {
		Applist apps;
		try {
			// Get all Applications for this client
			apps = client.getApplications();
		} catch (VeracodeXmlApiException e) {
			LOG.error("Could not get Apps due to exception: " + e.getMessage());
			return;
		}
		for (AppType app : apps.getApp()) {
			String appName = app.getAppName();
			LOG.debug("doing app: " + appName);

			try {
				// Get a list of builds for this app
				Buildlist builds = client.getBuildList(String.valueOf(app.getAppId()));
				saveAppBuilds(client, appName, builds);
			} catch (VeracodeXmlApiException e) {
				LOG.error("Could not get sandbox builds for app: " + appName + " due to exception: " + e.getMessage());
			}
		}
	}

	/**
	 * The following code follows this process:
	 * <ol>
	 * <li>Get the associated Veracode report for this build</li>
	 * <li>Get or create an App Model for the Application</li>
	 * <li>Get or create a Report Model for each Veracode report</li>
	 * <li>Update or create the flaws associated with this Veracode report</li>
	 * <li>Save the flaws</li>
	 * <li>Save the reports</li>
	 * </ol>
	 *
	 * @param client The API Client to use
	 * @param builds the list of builds to get reports for
	 */
	private void saveAppBuilds(VeracodeXmlApiClient client, String appName, Buildlist builds) {
		for (BuildType build : builds.getBuild()) {
			long buildId = build.getBuildId();
			if (build.getDynamicScanType() != null) {
				LOG.debug("No dynamic analysis report found for app: " + appName + " on build id: " + buildId);
				continue;
			}
			Detailedreport report;
			try {
				// Get the associated Veracode report for this build
				report = client.getDetailedReport(String.valueOf(buildId));
			} catch (VeracodeXmlApiException e) {
				LOG.error("Could not get detailed report due to exception: " + e.getMessage());
				continue;
			}

			if (report.getDynamicAnalysis() == null) {
				LOG.debug("No dynamic analysis report found for app: " + appName + " on build id: " + buildId);
				continue;
			}

			saveAppReport(appName, report);
		}

	}

	/**
	 * The following code follows this process:
	 * <ol>
	 * <li>Get or create an App Model for the Application</li>
	 * <li>Get or create a Report Model for each Veracode report</li>
	 * <li>Update or create the flaws associated with this Veracode report</li>
	 * <li>Save the flaws</li>
	 * <li>Save the reports</li>
	 * </ol>
	 *
	 * @param appName the Veracode App name for these reports
	 * @param report  the Veracode report to save
	 */
	private void saveAppReport(String appName, Detailedreport report) {
		// Get or create an App Model for the Application
		VeracodeDastAppModel appModel = appService.getDastApp(appName);
		if (appModel == null) {
			appModel = new VeracodeDastAppModel();
			appModel.setName(appName);
			/*
			 * N.B. the app must be saved first to have a valid identifier created for the
			 * reports to have a relationship with it.
			 */
			appModel = appService.save(appModel);
		}

		// Get or create a Report Model for each Veracode report
		VeracodeDastReportModel reportModel = reportService.getReportForAnalysisAndBuild(report.getAnalysisId(),
				report.getBuildId());
		if (reportModel == null) {
			reportModel = new VeracodeDastReportModel();
			reportModel.setApp(appModel);
		}

		List<CweType> cwes = report.getSeverity().stream().flatMap(s -> s.getCategory().stream())
				.flatMap(c -> c.getCwe().stream()).collect(Collectors.toList());
		Map<Integer, Integer> severityCounts = new HashMap<>();
		List<VeracodeDastFlawModel> flawModels = new ArrayList<>();
		for (CweType cwe : cwes) {
			if (cwe.getDynamicflaws() == null) {
				LOG.info("No Dynamic flaws found for cwe: " + cwe.getCweid() + " in app: " + appModel.getName());
				continue;
			}
			for (FlawType flaw : cwe.getDynamicflaws().getFlaw()) {
				// Update or create the flaws associated with this Veracode report
				VeracodeDastFlawModel flawModel = flawService.getFlawForIssueId(report.getAnalysisId(),
						flaw.getIssueid().longValue());
				if (flawModel == null) {
					flawModel = new VeracodeDastFlawModel();
					flawModel.setReport(reportModel);
					flawModel.setAnalysisId(report.getAnalysisId());
				}
				populateFlawModel(flawModel, flaw, cwe);

				if (!flawModel.isRemediated()) {
					int severity = flaw.getSeverity();
					int countSev = severityCounts.getOrDefault(severity, 0);
					severityCounts.put(severity, countSev + flaw.getCount());
				}
				flawModels.add(flawModel);
			}
		}
		populateReportModel(reportModel, report, severityCounts);

		// Save the reports
		/*
		 * N.B. the report must be saved first to have a valid identifier created for
		 * the flaws to have a relationship with it.
		 */
		reportService.save(reportModel);
		// Save the flaws
		flawService.saveFlaws(flawModels);
		// Save the app
		/*
		 * N.B. This save confirms the associations with all of the reports
		 */
		appService.save(appModel);
	}

	private void populateFlawModel(VeracodeDastFlawModel flawModel, FlawType flaw, CweType cwe) {
		flawModel.setIssueId(flaw.getIssueid().longValue());
		flawModel.setCategoryName(flaw.getCategoryname());
		flawModel.setCweId(cwe.getCweid().longValue());
		flawModel.setCweName(cwe.getCwename());
		flawModel.setRemediationStatus(flaw.getRemediationStatus());
		flawModel.setMitigationStatus(flaw.getMitigationStatus());
		flawModel.setSeverity(flaw.getSeverity());
		flawModel.setCount(flaw.getCount());
	}

	private void populateReportModel(VeracodeDastReportModel reportModel, Detailedreport report,
			Map<Integer, Integer> severityCounts) {
		reportModel.setAnalysisId(report.getAnalysisId());
		reportModel.setReportDate(
				LocalDateTime.parse(report.getDynamicAnalysis().getPublishedDate(), reportDateFormatter));
		reportModel.setBuildId(report.getBuildId());
		AnalysisType dynamicAnalysisResult = report.getDynamicAnalysis();
		reportModel.setScore(dynamicAnalysisResult.getScore().longValue());
		reportModel.setVeryHighVios(severityCounts.getOrDefault(5, 0));
		reportModel.setHighVios(severityCounts.getOrDefault(4, 0));
		reportModel.setMedVios(severityCounts.getOrDefault(3, 0));
		reportModel.setLowVios(severityCounts.getOrDefault(2, 0));
		reportModel.setVeryLowVios(severityCounts.getOrDefault(1, 0));
		reportModel.setInfoVios(severityCounts.getOrDefault(0, 0));
	}

}
