package com.tracelink.prodsec.plugin.veracode.sast.service;

import com.tracelink.prodsec.plugin.veracode.sast.api.ApiClient;
import com.tracelink.prodsec.plugin.veracode.sast.api.data.applist.AppType;
import com.tracelink.prodsec.plugin.veracode.sast.api.data.applist.Applist;
import com.tracelink.prodsec.plugin.veracode.sast.api.data.buildlist.BuildType;
import com.tracelink.prodsec.plugin.veracode.sast.api.data.buildlist.Buildlist;
import com.tracelink.prodsec.plugin.veracode.sast.api.data.detailedreport.AnalysisType;
import com.tracelink.prodsec.plugin.veracode.sast.api.data.detailedreport.CategoryType;
import com.tracelink.prodsec.plugin.veracode.sast.api.data.detailedreport.CweType;
import com.tracelink.prodsec.plugin.veracode.sast.api.data.detailedreport.Detailedreport;
import com.tracelink.prodsec.plugin.veracode.sast.api.data.detailedreport.FlawListType;
import com.tracelink.prodsec.plugin.veracode.sast.api.data.detailedreport.FlawType;
import com.tracelink.prodsec.plugin.veracode.sast.api.data.detailedreport.SeverityType;
import com.tracelink.prodsec.plugin.veracode.sast.api.data.sandboxlist.SandboxType;
import com.tracelink.prodsec.plugin.veracode.sast.api.data.sandboxlist.Sandboxlist;
import com.tracelink.prodsec.plugin.veracode.sast.model.VeracodeSastAppModel;
import com.tracelink.prodsec.plugin.veracode.sast.model.VeracodeSastFlawModel;
import com.tracelink.prodsec.plugin.veracode.sast.model.VeracodeSastReportModel;
import java.math.BigInteger;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class VeracodeSastUpdateServiceTest {

	@MockBean
	private VeracodeSastAppService mockAppService;

	@MockBean
	private VeracodeSastFlawService mockFlawService;

	@MockBean
	private VeracodeSastReportService mockReportService;

	@MockBean
	private VeracodeSastClientConfigService mockConfigService;

	private VeracodeSastUpdateService updateService;

	@Before
	public void setup() {
		this.updateService = new VeracodeSastUpdateService(mockAppService, mockFlawService,
				mockReportService,
				mockConfigService);
	}

	private void mockApps(ApiClient mockApiClient, String appName, String appId) throws Exception {

		Applist apps = new Applist();
		AppType app = new AppType();
		app.setAppName(appName);
		app.setAppId(Long.valueOf(appId));
		apps.getApp().add(app);

		BDDMockito.when(mockApiClient.getApplications()).thenReturn(apps);
		BDDMockito.when(mockApiClient.getBuildList(BDDMockito.anyString()))
				.thenReturn(new Buildlist());
	}

	private void mockSandboxes(ApiClient mockApiClient, String appId, String sbxName, String sbxId)
			throws Exception {
		Sandboxlist sbxs = new Sandboxlist();
		SandboxType sbx = new SandboxType();
		sbx.setSandboxName(sbxName);
		sbx.setSandboxId(Long.parseLong(sbxId));
		sbxs.getSandbox().add(sbx);
		BDDMockito.when(mockApiClient.getSandboxes(appId)).thenReturn(sbxs);
	}

	private void mockBuilds(ApiClient mockApiClient, String appId, String sbxId, String buildId)
			throws Exception {
		Buildlist builds = new Buildlist();
		BuildType build = new BuildType();
		build.setBuildId(Long.valueOf(buildId));
		builds.getBuild().add(build);
		BDDMockito.when(mockApiClient.getBuildList(appId, sbxId)).thenReturn(builds);
	}

	private FlawType createFlaw(long issueId, String remediationStatus, String catName,
			String sourcePath,
			String sourceFile, long line, String mitigation, int sev, int count) {
		FlawType flaw = new FlawType();
		flaw.setRemediationStatus(remediationStatus);
		flaw.setIssueid(BigInteger.valueOf(issueId));
		flaw.setCategoryname(catName);
		flaw.setSourcefilepath(sourcePath);
		flaw.setSourcefile(sourceFile);
		flaw.setLine(BigInteger.valueOf(line));
		flaw.setMitigationStatus(mitigation);
		flaw.setSeverity(sev);
		flaw.setCount(count);
		return flaw;
	}

	private void mockReport(ApiClient mockApiClient, String buildId, long analysisId, long score,
			String publishedDate,
			long cweId, String cweName, FlawType... flaws) throws Exception {
		Detailedreport report = new Detailedreport();
		report.setAnalysisId(analysisId);
		report.setBuildId(Long.parseLong(buildId));
		AnalysisType staticAnalysis = new AnalysisType();
		staticAnalysis.setScore(BigInteger.valueOf(score));
		staticAnalysis.setPublishedDate(publishedDate);
		SeverityType severity = new SeverityType();
		CategoryType category = new CategoryType();
		CweType cwe = new CweType();
		cwe.setCweid(BigInteger.valueOf(cweId));
		cwe.setCwename(cweName);
		if (flaws != null) {
			FlawListType flawsList = new FlawListType();
			flawsList.getFlaw().addAll(Arrays.asList(flaws));
			cwe.setStaticflaws(flawsList);
		}
		category.getCwe().add(cwe);
		severity.getCategory().add(category);
		report.getSeverity().add(severity);
		report.setStaticAnalysis(staticAnalysis);

		BDDMockito.when(mockApiClient.getDetailedReport(buildId)).thenReturn(report);
	}

	@Test
	public void testSyncAllDataSuccess() throws Exception {
		ApiClient mockApiClient = BDDMockito.mock(ApiClient.class);
		BDDMockito.when(mockConfigService.getApiClient()).thenReturn(mockApiClient);

		BDDMockito.when(mockAppService.save(BDDMockito.any())).thenAnswer(e -> e.getArgument(0));

		// setup apps
		String appName = "MyApp";
		String appId = "123";
		mockApps(mockApiClient, appName, appId);

		// setup Sandboxes
		String sbxName = "SandboxName";
		String sbxId = "456";
		mockSandboxes(mockApiClient, appId, sbxName, sbxId);

		// setup builds
		String buildId = "789";
		mockBuilds(mockApiClient, appId, sbxId, buildId);

		// setup Flaws
		long remIssueId = 111;
		String remRemediation = "Remediated";
		String remCatName = "Bad Code";
		String remSourcePath = "/com/foo/test";
		String remSourceFile = "vuln.java";
		long remLine = 12;
		String remMitigation = "accepted";
		int remSev = 3;
		int remCount = 1;

		FlawType remediated = createFlaw(remIssueId, remRemediation, remCatName, remSourcePath,
				remSourceFile, remLine,
				remMitigation, remSev, remCount);

		long vulnIssueId = 111;
		String vulnRemediation = "New";
		String vulnCatName = "Bad Code";
		String vulnSourcePath = "/com/foo/test";
		String vulnSourceFile = "vuln.java";
		long vulnLine = 12;
		String vulnMitigation = "accepted";
		int vulnSev = 5;
		int vulnCount = 1;
		FlawType vuln = createFlaw(vulnIssueId, vulnRemediation, vulnCatName, vulnSourcePath,
				vulnSourceFile, vulnLine,
				vulnMitigation, vulnSev, vulnCount);

		// setup Report
		String date = "2020-01-01 11:23:45 EST";
		long score = 12;
		long cweId = 23;
		String cweName = "foo";
		long analysisId = 999;

		mockReport(mockApiClient, buildId, analysisId, score, date, cweId, cweName, remediated,
				vuln);

		updateService.syncAllData();

		// test App model
		ArgumentCaptor<VeracodeSastAppModel> appCaptor = ArgumentCaptor
				.forClass(VeracodeSastAppModel.class);
		BDDMockito.verify(mockAppService, BDDMockito.times(4)).save(appCaptor.capture());
		Assert.assertEquals(4, appCaptor.getAllValues().size());
		VeracodeSastAppModel app = appCaptor.getAllValues().get(3);
		Assert.assertEquals(sbxName, app.getName());
		Assert.assertEquals(appName, app.getProductLineName());

		// test Report model
		ArgumentCaptor<VeracodeSastReportModel> reportCaptor = ArgumentCaptor
				.forClass(VeracodeSastReportModel.class);
		BDDMockito.verify(mockReportService).save(reportCaptor.capture());
		VeracodeSastReportModel report = reportCaptor.getValue();
		testReportModel(report, buildId, score, analysisId, 1, 0, 0, 0, 0, 0, 2, app);

		// test both Flaw models
		@SuppressWarnings("unchecked")
		ArgumentCaptor<List<VeracodeSastFlawModel>> flawCaptor = ArgumentCaptor
				.forClass(List.class);
		BDDMockito.verify(mockFlawService).saveFlaws(flawCaptor.capture());
		List<VeracodeSastFlawModel> flaws = flawCaptor.getValue();
		Assert.assertEquals(2, flaws.size());

		VeracodeSastFlawModel remModel = flaws.get(0);

		testFlawModel(remModel, remIssueId, remRemediation, remCatName, remSourcePath,
				remSourceFile, remLine,
				remMitigation, remSev, remCount);
		VeracodeSastFlawModel vulnModel = flaws.get(1);
		testFlawModel(vulnModel, vulnIssueId, vulnRemediation, vulnCatName, vulnSourcePath,
				vulnSourceFile, vulnLine,
				vulnMitigation, vulnSev, vulnCount);
	}

	private void testFlawModel(VeracodeSastFlawModel flawModel, long issueId, String remediation,
			String catName,
			String sourcePath, String sourceFile, long line, String mitigation, int sev,
			int count) {
		Assert.assertEquals(issueId, flawModel.getIssueId());
		Assert.assertEquals(remediation, flawModel.getRemediationStatus());
		Assert.assertEquals(catName, flawModel.getCategoryName());
		Assert.assertEquals(Paths.get(sourcePath, sourceFile).toString(),
				flawModel.getSourceFile());
		Assert.assertEquals(line, flawModel.getLine());
		Assert.assertEquals(mitigation, flawModel.getMitigationStatus());
		Assert.assertEquals(sev, flawModel.getSeverity());
		Assert.assertEquals(count, flawModel.getCount());

	}

	private void testReportModel(VeracodeSastReportModel report, String buildId, long score,
			long analysisId, int numVH,
			int numH, int numM, int numL, int numVL, int numI, int flaws,
			VeracodeSastAppModel app) {
		Assert.assertEquals(app, report.getApp());
		Assert.assertEquals(analysisId, report.getAnalysisId());
		Assert.assertEquals(buildId, String.valueOf(report.getBuildId()));
		Assert.assertEquals(score, report.getScore());
		Assert.assertEquals(numVH, report.getVeryHighVios());
		Assert.assertEquals(numH, report.getHighVios());
		Assert.assertEquals(numM, report.getMedVios());
		Assert.assertEquals(numL, report.getLowVios());
		Assert.assertEquals(numVL, report.getVeryLowVios());
		Assert.assertEquals(numI, report.getInfoVios());
		Assert.assertEquals(numVH + numH + numM + numL + numVL + numI,
				report.getVulnerabilitiesCount());
	}

}
