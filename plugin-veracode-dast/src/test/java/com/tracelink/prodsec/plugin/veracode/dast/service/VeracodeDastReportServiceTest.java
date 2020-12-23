package com.tracelink.prodsec.plugin.veracode.dast.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import com.tracelink.prodsec.plugin.veracode.dast.model.VeracodeDastReportModel;
import com.tracelink.prodsec.plugin.veracode.dast.repository.VeracodeDastReportRepository;

@RunWith(SpringRunner.class)
public class VeracodeDastReportServiceTest {

	@MockBean
	private VeracodeDastReportRepository mockReportRepo;

	private VeracodeDastReportService reportService;

	@Before
	public void setup() {
		this.reportService = new VeracodeDastReportService(mockReportRepo);
	}

	@Test
	public void testSave() {
		VeracodeDastReportModel report = new VeracodeDastReportModel();
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
