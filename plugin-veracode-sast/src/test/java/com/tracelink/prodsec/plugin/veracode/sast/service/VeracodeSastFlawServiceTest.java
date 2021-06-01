package com.tracelink.prodsec.plugin.veracode.sast.service;

import com.tracelink.prodsec.plugin.veracode.sast.model.VeracodeSastFlawModel;
import com.tracelink.prodsec.plugin.veracode.sast.model.VeracodeSastReportModel;
import com.tracelink.prodsec.plugin.veracode.sast.repository.VeracodeSastFlawRepository;
import java.util.Collections;
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
public class VeracodeSastFlawServiceTest {

	@MockBean
	private VeracodeSastFlawRepository mockFlawRepo;

	private VeracodeSastFlawService flawService;

	@Before
	public void setup() {
		this.flawService = new VeracodeSastFlawService(mockFlawRepo);
	}

	@Test
	public void testSaveFlaws() {
		VeracodeSastFlawModel flaw = new VeracodeSastFlawModel();
		flawService.saveFlaws(Collections.singletonList(flaw));
		@SuppressWarnings("unchecked")
		ArgumentCaptor<List<VeracodeSastFlawModel>> captor = ArgumentCaptor.forClass(List.class);
		BDDMockito.verify(mockFlawRepo).saveAll(captor.capture());
		Assert.assertEquals(flaw, captor.getValue().get(0));
		BDDMockito.verify(mockFlawRepo).flush();
	}

	@Test
	public void testGetFlawForIssueId() {
		long analysisId = 123;
		long issueId = 456;
		flawService.getFlawForIssueId(analysisId, issueId);
		BDDMockito.verify(mockFlawRepo).findByAnalysisIdAndIssueId(analysisId, issueId);
	}

	@Test
	public void testDeleteFlawsByReportNull() {
		try {
			flawService.deleteFlawsByReport(null);
			Assert.fail("Exception should have been thrown");
		} catch (IllegalArgumentException e) {
			Assert.assertEquals("Cannot delete flaws for a null report", e.getMessage());
		}
	}

	@Test
	public void testDeleteFlawsByReport() {
		VeracodeSastReportModel report = new VeracodeSastReportModel();
		flawService.deleteFlawsByReport(report);
		BDDMockito.verify(mockFlawRepo).deleteByReport(report);
		BDDMockito.verify(mockFlawRepo).flush();
	}
}
