package com.tracelink.prodsec.plugin.sonatype.service;

import com.tracelink.prodsec.plugin.sonatype.exception.SonatypeThresholdsException;
import com.tracelink.prodsec.plugin.sonatype.model.SonatypeThresholds;
import com.tracelink.prodsec.plugin.sonatype.repository.SonatypeThresholdsRepository;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;

@RunWith(SpringRunner.class)
public class SonatypeThresholdsServiceTest {
	@MockBean
	private SonatypeThresholdsRepository thresholdsRepository;

	private SonatypeThresholdsService thresholdsService;

	@Before
	public void setup() {
		thresholdsService = new SonatypeThresholdsService(thresholdsRepository);
	}

	@Test(expected = SonatypeThresholdsException.class)
	public void testGetThresholdsEmpty() {
		BDDMockito.when(thresholdsRepository.findAll()).thenReturn(Collections.emptyList());
		thresholdsService.getThresholds();
	}

	@Test
	public void testGetThresholds() {
		SonatypeThresholds thresholds = new SonatypeThresholds();
		thresholds.setGreenYellow(50);
		thresholds.setYellowRed(100);
		BDDMockito.when(thresholdsRepository.findAll()).thenReturn(Collections.singletonList(thresholds));

		Assert.assertEquals(thresholds, thresholdsService.getThresholds());
	}

	@Test
	public void testSetThresholdsEmpty() {
		BDDMockito.when(thresholdsRepository.findAll()).thenReturn(Collections.emptyList());
		thresholdsService.setThresholds(50, 100);

		ArgumentCaptor<SonatypeThresholds> captor = ArgumentCaptor.forClass(SonatypeThresholds.class);
		BDDMockito.verify(thresholdsRepository, Mockito.times(1)).saveAndFlush(captor.capture());

		SonatypeThresholds thresholds = captor.getValue();
		Assert.assertEquals(50, thresholds.getGreenYellow());
		Assert.assertEquals(100, thresholds.getYellowRed());
	}

	@Test
	public void testSetThresholds() {
		SonatypeThresholds thresholds = new SonatypeThresholds();
		thresholds.setGreenYellow(20);
		thresholds.setYellowRed(60);
		BDDMockito.when(thresholdsRepository.findAll()).thenReturn(Collections.singletonList(thresholds));
		thresholdsService.setThresholds(50, 100);

		BDDMockito.verify(thresholdsRepository, Mockito.times(1)).saveAndFlush(thresholds);
		Assert.assertEquals(50, thresholds.getGreenYellow());
		Assert.assertEquals(100, thresholds.getYellowRed());
	}
}
