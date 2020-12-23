package com.tracelink.prodsec.plugin.veracode.sca.service;

import com.tracelink.prodsec.plugin.veracode.sca.exception.VeracodeScaThresholdsException;
import com.tracelink.prodsec.plugin.veracode.sca.model.VeracodeScaThresholds;
import com.tracelink.prodsec.plugin.veracode.sca.repository.VeracodeScaThresholdsRepository;
import java.util.Collections;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class VeracodeScaThresholdsServiceTest {

	@MockBean
	private VeracodeScaThresholdsRepository thresholdsRepository;

	private VeracodeScaThresholdsService thresholdsService;

	@Before
	public void setup() {
		thresholdsService = new VeracodeScaThresholdsService(thresholdsRepository);
	}

	@Test(expected = VeracodeScaThresholdsException.class)
	public void testGetThresholdsEmpty() {
		BDDMockito.when(thresholdsRepository.findAll()).thenReturn(Collections.emptyList());
		thresholdsService.getThresholds();
	}

	@Test
	public void testGetThresholds() {
		VeracodeScaThresholds thresholds = new VeracodeScaThresholds();
		thresholds.setGreenYellow(50);
		thresholds.setYellowRed(100);
		BDDMockito.when(thresholdsRepository.findAll())
			.thenReturn(Collections.singletonList(thresholds));

		Assert.assertEquals(thresholds, thresholdsService.getThresholds());
	}

	@Test
	public void testSetThresholdsEmpty() {
		BDDMockito.when(thresholdsRepository.findAll()).thenReturn(Collections.emptyList());
		thresholdsService.setThresholds(50, 100);

		ArgumentCaptor<VeracodeScaThresholds> captor = ArgumentCaptor
			.forClass(VeracodeScaThresholds.class);
		BDDMockito.verify(thresholdsRepository, Mockito.times(1)).saveAndFlush(captor.capture());

		VeracodeScaThresholds thresholds = captor.getValue();
		Assert.assertEquals(50, thresholds.getGreenYellow());
		Assert.assertEquals(100, thresholds.getYellowRed());
	}

	@Test
	public void testSetThresholds() {
		VeracodeScaThresholds thresholds = new VeracodeScaThresholds();
		thresholds.setGreenYellow(20);
		thresholds.setYellowRed(60);
		BDDMockito.when(thresholdsRepository.findAll())
			.thenReturn(Collections.singletonList(thresholds));
		thresholdsService.setThresholds(50, 100);

		BDDMockito.verify(thresholdsRepository, Mockito.times(1)).saveAndFlush(thresholds);
		Assert.assertEquals(50, thresholds.getGreenYellow());
		Assert.assertEquals(100, thresholds.getYellowRed());
	}
}
