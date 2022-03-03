package com.tracelink.prodsec.plugin.veracode.dast.service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import com.tracelink.prodsec.lib.veracode.api.VeracodeApiClient;
import com.tracelink.prodsec.lib.veracode.api.rest.model.AnalysisType;
import com.tracelink.prodsec.lib.veracode.api.rest.model.Application;
import com.tracelink.prodsec.lib.veracode.api.rest.model.ApplicationProfile;
import com.tracelink.prodsec.lib.veracode.api.rest.model.ApplicationScan.ScanTypeEnum;
import com.tracelink.prodsec.lib.veracode.api.rest.model.EmbeddedApplication;
import com.tracelink.prodsec.lib.veracode.api.rest.model.Module;
import com.tracelink.prodsec.lib.veracode.api.rest.model.ModuleType;
import com.tracelink.prodsec.lib.veracode.api.rest.model.PagedResourceOfApplication;
import com.tracelink.prodsec.lib.veracode.api.rest.model.SummaryReport;
import com.tracelink.prodsec.lib.veracode.api.xml.data.buildlist.BuildType;
import com.tracelink.prodsec.lib.veracode.api.xml.data.buildlist.Buildlist;
import com.tracelink.prodsec.plugin.veracode.dast.model.VeracodeDastAppModel;
import com.tracelink.prodsec.plugin.veracode.dast.model.VeracodeDastReportModel;
import com.tracelink.prodsec.plugin.veracode.dast.service.VeracodeDastUpdateService.SyncType;

@RunWith(SpringRunner.class)
public class VeracodeDastUpdateServiceTest {

	@MockBean
	private VeracodeDastAppService mockAppService;

	@MockBean
	private VeracodeDastReportService mockReportService;

	@MockBean
	private VeracodeDastClientConfigService mockConfigService;

	private VeracodeDastUpdateService updateService;

	@Before
	public void setup() {
		this.updateService = new VeracodeDastUpdateService(mockAppService, mockReportService, mockConfigService);
	}

	@Test
	public void testSyncAllDataSuccess() throws Exception {
		VeracodeApiClient mockApiClient = BDDMockito.mock(VeracodeApiClient.class);
		BDDMockito.when(mockConfigService.getApiClient()).thenReturn(mockApiClient);

		BDDMockito.when(mockAppService.save(BDDMockito.any())).thenAnswer(e -> e.getArgument(0));

		// setup apps
		String appName = "MyApp";
		int appId = 123;
		PagedResourceOfApplication pageApp = new PagedResourceOfApplication();
		EmbeddedApplication embedApp = new EmbeddedApplication();
		Application app = new Application();
		ApplicationProfile profile = new ApplicationProfile();
		profile.setName(appName);
		app.setId(appId);
		app.setProfile(profile);
		embedApp.setApplications(Arrays.asList(app));
		pageApp.setEmbedded(embedApp);
		BDDMockito.when(mockApiClient.getRestApplications(ScanTypeEnum.DYNAMIC, 0)).thenReturn(pageApp);

		// setup builds
		long buildId = 789L;
		Buildlist builds = BDDMockito.mock(Buildlist.class);
		BuildType build = new BuildType();
		build.setBuildId(buildId);
		BDDMockito.when(builds.getBuild()).thenReturn(Arrays.asList(build));
		BDDMockito.when(mockApiClient.getXMLBuildList(BDDMockito.anyString())).thenReturn(builds);

		// setup Report
		String date = "2020-01-01 11:23:45 EST";
		long score = 12;
		long totalFlaws = 32;
		long unmitFlaws = 1;
		long analysisId = 999;
		ModuleType modType = new ModuleType();
		modType.setNumflawssev5(5L);
		modType.setNumflawssev4(4L);
		modType.setNumflawssev3(3L);
		modType.setNumflawssev2(2L);
		modType.setNumflawssev1(1L);
		modType.setNumflawssev0(0L);

		Module module = new Module();
		module.setModule(Arrays.asList(modType));

		AnalysisType dynAnalysis = new AnalysisType();
		dynAnalysis.setMitigatedScore(score);
		dynAnalysis.setPublishedDate(date);
		dynAnalysis.setModules(module);

		SummaryReport report = new SummaryReport();
		report.setAnalysisId(new BigDecimal(analysisId));
		report.setAppId(new BigDecimal(appId));
		report.setDynamicAnalysis(dynAnalysis);
		report.setBuildId(new BigDecimal(buildId));
		report.setTotalFlaws(totalFlaws);
		report.setFlawsNotMitigated(unmitFlaws);
		BDDMockito.when(mockApiClient.getRestSummaryReport(BDDMockito.anyString(), BDDMockito.anyString()))
				.thenReturn(report);

		BDDMockito.given(mockAppService.save(BDDMockito.any())).willAnswer(c -> c.getArgument(0));

		updateService.syncData(SyncType.RECENT);

		ArgumentCaptor<VeracodeDastReportModel> reportCaptor = ArgumentCaptor.forClass(VeracodeDastReportModel.class);
		BDDMockito.verify(mockReportService).save(reportCaptor.capture());

		VeracodeDastReportModel reportModel = reportCaptor.getValue();
		MatcherAssert.assertThat(reportModel.getAnalysisId(), Matchers.is(analysisId));
		MatcherAssert.assertThat(reportModel.getTotalFlaws(), Matchers.is(totalFlaws));
		MatcherAssert.assertThat(reportModel.getScore(), Matchers.is(score));
		MatcherAssert.assertThat(reportModel.getUnmitigatedFlaws(), Matchers.is(unmitFlaws));
		MatcherAssert.assertThat(reportModel.getvHigh(), Matchers.is(5L));
		MatcherAssert.assertThat(reportModel.getHigh(), Matchers.is(4L));
		MatcherAssert.assertThat(reportModel.getMedium(), Matchers.is(3L));
		MatcherAssert.assertThat(reportModel.getLow(), Matchers.is(2L));
		MatcherAssert.assertThat(reportModel.getvLow(), Matchers.is(1L));
		MatcherAssert.assertThat(reportModel.getInfo(), Matchers.is(0L));
	}

}
