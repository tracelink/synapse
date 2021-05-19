package com.tracelink.prodsec.plugin.veracode.sast.service;

import com.tracelink.prodsec.plugin.veracode.sast.model.VeracodeSastAppModel;
import com.tracelink.prodsec.plugin.veracode.sast.model.VeracodeSastReportModel;
import com.tracelink.prodsec.plugin.veracode.sast.repository.VeracodeSastReportRepository;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class VeracodeSastReportServiceTest {

	@MockBean
	private VeracodeSastReportRepository mockReportRepo;

	@MockBean
	private VeracodeSastFlawService mockFlawService;

	private VeracodeSastReportService reportService;
	private VeracodeSastAppModel app;

	@Before
	public void setup() {
		this.reportService = new VeracodeSastReportService(mockReportRepo, mockFlawService);
		this.app = new VeracodeSastAppModel();
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

	@Test
	public void testDeleteReportsByAppNull() {
		try {
			reportService.deleteReportsByApp(null);
			Assert.fail("Exception should have been thrown");
		} catch (IllegalArgumentException e) {
			Assert.assertEquals("Cannot delete reports for a null app", e.getMessage());
		}
	}

	@Test
	public void testDeleteReportsByApp() {
		VeracodeSastReportModel report1 = new VeracodeSastReportModel();
		VeracodeSastReportModel report2 = new VeracodeSastReportModel();
		Page<VeracodeSastReportModel> page = new PageImpl<>(Arrays.asList(report1, report2));
		BDDMockito.when(mockReportRepo
				.findAllByApp(BDDMockito.any(VeracodeSastAppModel.class),
						BDDMockito.any(Pageable.class)))
				.thenReturn(page);

		reportService.deleteReportsByApp(app);
		BDDMockito.verify(mockFlawService).deleteFlawsByReport(report1);
		BDDMockito.verify(mockFlawService).deleteFlawsByReport(report2);
		BDDMockito.verify(mockReportRepo).deleteByApp(app);
		BDDMockito.verify(mockReportRepo).flush();
	}

	@Test
	public void testDeleteReportsByAppMultiplePages() {
		VeracodeSastReportModel report1 = new VeracodeSastReportModel();
		VeracodeSastReportModel report2 = new VeracodeSastReportModel();
		Pageable pageable1 = PageRequest.of(0, 1);
		Page<VeracodeSastReportModel> page1 = new PageImpl<>(Collections.singletonList(report1),
				pageable1, 2);
		Pageable pageable2 = PageRequest.of(1, 1);
		Page<VeracodeSastReportModel> page2 = new PageImpl<>(Collections.singletonList(report2),
				pageable2, 2);
		BDDMockito.when(mockReportRepo.findAllByApp(app, PageRequest.of(0, 100)))
				.thenReturn(page1);
		BDDMockito.when(mockReportRepo.findAllByApp(app, page1.nextPageable()))
				.thenReturn(page2);

		reportService.deleteReportsByApp(app);
		BDDMockito.verify(mockFlawService).deleteFlawsByReport(report1);
		BDDMockito.verify(mockFlawService).deleteFlawsByReport(report2);
		BDDMockito.verify(mockReportRepo).deleteByApp(app);
		BDDMockito.verify(mockReportRepo).flush();
	}
}
