package com.tracelink.prodsec.plugin.veracode.sast.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import com.tracelink.prodsec.plugin.veracode.sast.model.VeracodeSastReportModel;
import com.tracelink.prodsec.plugin.veracode.sast.repository.VeracodeSastReportRepository;

@RunWith(SpringRunner.class)
public class VeracodeSastReportServiceTest {

	@MockBean
	private VeracodeSastReportRepository mockReportRepo;

	private VeracodeSastReportService reportService;

	@Before
	public void setup() {
		this.reportService = new VeracodeSastReportService(mockReportRepo);
	}

	@Test
	public void testSave() {
		VeracodeSastReportModel report = new VeracodeSastReportModel();
		reportService.save(report);
		BDDMockito.verify(mockReportRepo).saveAndFlush(report);
	}

	@Test
	public void testGetReportById() {
		long id = 123;
		reportService.getReportById(id);
		BDDMockito.verify(mockReportRepo).findById(id);
	}

	@Test
	public void testGetReportForAnalysisAndBuild() {
		long analysisId = 123;
		long buildId = 456;
		reportService.getReportForAnalysisAndBuild(analysisId, buildId);
		BDDMockito.verify(mockReportRepo).findByAnalysisIdAndBuildId(analysisId, buildId);
	}
}
