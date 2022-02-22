package com.tracelink.prodsec.plugin.veracode.dast.service;

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

import com.tracelink.appsec.plugin.veracode.dast.model.VeracodeDastFlawModel;
import com.tracelink.appsec.plugin.veracode.dast.repository.VeracodeDastFlawRepository;
import com.tracelink.appsec.plugin.veracode.dast.service.VeracodeDastFlawService;


@RunWith(SpringRunner.class)
public class VeracodeDastFlawServiceTest {

	@MockBean
	private VeracodeDastFlawRepository mockFlawRepo;

	private VeracodeDastFlawService flawService;

	@Before
	public void setup() {
		this.flawService = new VeracodeDastFlawService(mockFlawRepo);
	}

	@Test
	public void testSaveFlaws() {
		VeracodeDastFlawModel flaw = new VeracodeDastFlawModel();
		flawService.saveFlaws(Collections.singletonList(flaw));
		@SuppressWarnings("unchecked")
		ArgumentCaptor<List<VeracodeDastFlawModel>> captor = ArgumentCaptor.forClass(List.class);
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

}
