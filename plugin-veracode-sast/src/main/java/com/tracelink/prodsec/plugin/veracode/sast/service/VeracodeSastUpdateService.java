package com.tracelink.prodsec.plugin.veracode.sast.service;

import com.tracelink.prodsec.plugin.veracode.sast.api.ApiClient;
import com.tracelink.prodsec.plugin.veracode.sast.api.VeracodeClientException;
import com.tracelink.prodsec.plugin.veracode.sast.api.data.applist.AppType;
import com.tracelink.prodsec.plugin.veracode.sast.api.data.applist.Applist;
import com.tracelink.prodsec.plugin.veracode.sast.api.data.buildlist.BuildType;
import com.tracelink.prodsec.plugin.veracode.sast.api.data.buildlist.Buildlist;
import com.tracelink.prodsec.plugin.veracode.sast.api.data.detailedreport.AnalysisType;
import com.tracelink.prodsec.plugin.veracode.sast.api.data.detailedreport.CweType;
import com.tracelink.prodsec.plugin.veracode.sast.api.data.detailedreport.Detailedreport;
import com.tracelink.prodsec.plugin.veracode.sast.api.data.detailedreport.FlawType;
import com.tracelink.prodsec.plugin.veracode.sast.api.data.sandboxlist.SandboxType;
import com.tracelink.prodsec.plugin.veracode.sast.api.data.sandboxlist.Sandboxlist;
import com.tracelink.prodsec.plugin.veracode.sast.model.ModelType;
import com.tracelink.prodsec.plugin.veracode.sast.model.VeracodeSastAppModel;
import com.tracelink.prodsec.plugin.veracode.sast.model.VeracodeSastFlawModel;
import com.tracelink.prodsec.plugin.veracode.sast.model.VeracodeSastReportModel;
import java.nio.file.Path;
import java.nio.file.Paths;
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

	private final VeracodeSastFlawService flawService;

	private final VeracodeSastClientConfigService configService;
	private final DateTimeFormatter reportDateFormatter = DateTimeFormatter
			.ofPattern("yyyy-MM-dd HH:mm:ss z");

	public VeracodeSastUpdateService(@Autowired VeracodeSastAppService appService,
			@Autowired VeracodeSastFlawService flawService,
			@Autowired VeracodeSastReportService reportService,
			@Autowired VeracodeSastClientConfigService configService) {
		this.appService = appService;
		this.flawService = flawService;
		this.reportService = reportService;
		this.configService = configService;
	}

	/**
	 * pulls new data from Veracode and syncs it with the current data
	 */
	public void syncAllData() {
		LOG.info("Beginning Veracode SAST data update");
		try {
			// get the current client
			ApiClient client = configService.getApiClient();
			if (client == null) {
				LOG.error("No Configuration for Veracode SAST client");
				return;
			}
			// start the sync operation
			dataSync(client);

		} catch (Exception e) {
			LOG.error("Veracode SAST data update failed due to error: " + e.getMessage());
		}
		LOG.info("Veracode SAST data update complete");
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
	 * @param client The API Client to use
	 */
	private void dataSync(ApiClient client) {
		Applist apps;
		try {
			// Get all Applications for this client
			apps = client.getApplications();
		} catch (VeracodeClientException e) {
			LOG.error("Could not get Apps due to exception: " + e.getMessage());
			return;
		}
		for (AppType app : apps.getApp()) {
			String appName = app.getAppName();
			LOG.debug("doing app: " + appName);
			saveAppBuilds(client, app, appName);

			Sandboxlist sandboxes;
			try {
				// Get each Sandbox for each App
				sandboxes = client.getSandboxes(String.valueOf(app.getAppId()));
			} catch (VeracodeClientException e) {
				LOG.error("Could not update Sandboxes due to exception: " + e.getMessage());
				continue;
			}

			for (SandboxType sandbox : sandboxes.getSandbox()) {
				String sbxName = sandbox.getSandboxName();
				LOG.debug("doing sbx: " + sbxName);
				saveSbxBuilds(client, app, appName, sandbox, sbxName);
			}
		}
	}

	private void saveSbxBuilds(ApiClient client, AppType app, String appName, SandboxType sandbox,
			String sbxName) {
		VeracodeSastAppModel sbxAppModel = getOrCreateAppModel(sbxName, appName, ModelType.SBX);
		try {
			// Get a list of builds for this sandbox
			Buildlist sbxBuilds = client.getBuildList(String.valueOf(app.getAppId()),
					String.valueOf(sandbox.getSandboxId()));
			saveReports(client, sbxAppModel, sbxBuilds);
			// Save the app
			/*
			 * N.B. This save confirms the associations with all of the reports
			 */
			appService.save(sbxAppModel);
		} catch (VeracodeClientException e) {
			LOG.error("Could not get sandbox builds due to exception: " + e.getMessage());
		}
	}

	private void saveAppBuilds(ApiClient client, AppType app, String appName) {
		VeracodeSastAppModel appModel = getOrCreateAppModel(appName, appName, ModelType.APP);
		try {
			// Get a list of builds for this sandbox
			Buildlist appBuilds = client.getBuildList(String.valueOf(app.getAppId()));
			saveReports(client, appModel, appBuilds);
			// Save the app
			/*
			 * N.B. This save confirms the associations with all of the reports
			 */
			appService.save(appModel);
		} catch (VeracodeClientException e) {
			LOG.error("Could not get app builds due to exception: " + e.getMessage());
		}
	}

	private VeracodeSastAppModel getOrCreateAppModel(String appName, String productLineName,
			ModelType modelType) {
		// Get or create an App Model for the Sandbox
		VeracodeSastAppModel appModel = appService.getSastApp(appName, modelType);
		if (appModel == null) {
			appModel = new VeracodeSastAppModel();
			appModel.setName(appName);
			appModel.setModelType(modelType);
		}
		// Added here as this may change over time during renames
		appModel.setProductLineName(productLineName);
		/*
		 * N.B. the app must be saved first to have a valid identifier created for the
		 * reports to have a relationship with it.
		 */
		appModel = appService.save(appModel);
		return appModel;
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
	private void saveReports(ApiClient client, VeracodeSastAppModel appModel, Buildlist builds) {
		for (BuildType build : builds.getBuild()) {
			long buildId = build.getBuildId();
			Detailedreport report;
			try {
				// Get the associated Veracode report for this build
				report = client.getDetailedReport(String.valueOf(buildId));
			} catch (VeracodeClientException e) {
				LOG.error("Could not get detailed report due to exception: " + e.getMessage());
				continue;
			}

			if (report.getStaticAnalysis() == null) {
				LOG.debug("No static analysis report found for build id: " + buildId);
				continue;
			}

			saveReport(report, appModel);
		}
	}

	/**
	 * The following code follows this process:
	 * <ol>
	 * <li>Get or create a Report Model for each Veracode report</li>
	 * <li>Update or create the flaws associated with this Veracode report</li>
	 * <li>Save the flaws</li>
	 * <li>Save the reports</li>
	 * </ol>
	 *
	 * @param report   the Veracode report to save
	 * @param appModel the Veracode Plugin App Model for these reports
	 */
	private void saveReport(Detailedreport report, VeracodeSastAppModel appModel) {
		// Get or create a Report Model for each Veracode report
		VeracodeSastReportModel reportModel = reportService
				.getReportForAnalysisAndBuild(report.getAnalysisId(),
						report.getBuildId());

		if (reportModel == null) {
			reportModel = new VeracodeSastReportModel();
			reportModel.setApp(appModel);
		}

		List<CweType> cwes = report.getSeverity().stream().flatMap(s -> s.getCategory().stream())
				.flatMap(c -> c.getCwe().stream()).collect(Collectors.toList());
		Map<Integer, Integer> severityCounts = new HashMap<>();
		List<VeracodeSastFlawModel> flawModels = new ArrayList<>();
		for (CweType cwe : cwes) {
			if (cwe.getStaticflaws() == null) {
				LOG.info("No Static flaws found for cwe: " + cwe.getCweid() + " in app: " + appModel
						.getName());
				continue;
			}
			for (FlawType flaw : cwe.getStaticflaws().getFlaw()) {
				// Update or create the flaws associated with this Veracode report
				VeracodeSastFlawModel flawModel = flawService
						.getFlawForIssueId(report.getAnalysisId(),
								flaw.getIssueid().longValue());
				if (flawModel == null) {
					flawModel = new VeracodeSastFlawModel();
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
	}

	private void populateFlawModel(VeracodeSastFlawModel flawModel, FlawType flaw, CweType cwe) {
		flawModel.setIssueId(flaw.getIssueid().longValue());
		flawModel.setCategoryName(flaw.getCategoryname());
		flawModel.setCweId(cwe.getCweid().longValue());
		flawModel.setCweName(cwe.getCwename());
		flawModel.setRemediationStatus(flaw.getRemediationStatus());
		Path sourcePath = Paths.get(flaw.getSourcefilepath(), flaw.getSourcefile());
		flawModel.setSourceFile(sourcePath.toString());
		flawModel.setLine(flaw.getLine().longValue());
		flawModel.setMitigationStatus(flaw.getMitigationStatus());
		flawModel.setSeverity(flaw.getSeverity());
		flawModel.setCount(flaw.getCount());
	}

	private void populateReportModel(VeracodeSastReportModel reportModel, Detailedreport report,
			Map<Integer, Integer> severityCounts) {
		reportModel.setAnalysisId(report.getAnalysisId());
		reportModel
				.setReportDate(LocalDateTime
						.parse(report.getStaticAnalysis().getPublishedDate(), reportDateFormatter));
		reportModel.setBuildId(report.getBuildId());
		AnalysisType staticAnalysisResult = report.getStaticAnalysis();
		reportModel.setScore(staticAnalysisResult.getScore().longValue());
		reportModel.setVeryHighVios(severityCounts.getOrDefault(5, 0));
		reportModel.setHighVios(severityCounts.getOrDefault(4, 0));
		reportModel.setMedVios(severityCounts.getOrDefault(3, 0));
		reportModel.setLowVios(severityCounts.getOrDefault(2, 0));
		reportModel.setVeryLowVios(severityCounts.getOrDefault(1, 0));
		reportModel.setInfoVios(severityCounts.getOrDefault(0, 0));
	}

}
