package com.tracelink.prodsec.plugin.veracode.dast.service;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import com.tracelink.prodsec.plugin.veracode.dast.model.VeracodeDastThresholdModel;
import com.tracelink.prodsec.plugin.veracode.dast.repository.VeracodeDastThresholdsRepository;

@RunWith(SpringRunner.class)
public class VeracodeDastThresholdsServiceTest {

	@MockBean
	private VeracodeDastThresholdsRepository mockThresholdsRepository;

	private VeracodeDastThresholdsService thresholdsService;

	@Before
	public void setup() {
		this.thresholdsService = new VeracodeDastThresholdsService(mockThresholdsRepository);
	}

	@Test
	public void testGetThresholdsNull() {
		BDDMockito.when(mockThresholdsRepository.findAll()).thenReturn(new ArrayList<>());
		Assert.assertNull(this.thresholdsService.getThresholds());
	}

	@Test
	public void testGetThresholds() {
		VeracodeDastThresholdModel threshold = new VeracodeDastThresholdModel();
		BDDMockito.when(mockThresholdsRepository.findAll()).thenReturn(Arrays.asList(threshold));
		Assert.assertEquals(threshold, this.thresholdsService.getThresholds());
	}

	@Test
	public void testSetThresholdsNew() {
		int greenYellow = 80;
		int yellowRed = 30;
		BDDMockito.when(mockThresholdsRepository.findAll()).thenReturn(new ArrayList<>());
		BDDMockito.when(mockThresholdsRepository.saveAndFlush(BDDMockito.any())).thenAnswer(e -> e.getArgument(0));
		VeracodeDastThresholdModel threshold = this.thresholdsService.setThresholds(greenYellow, yellowRed);
		Assert.assertEquals(greenYellow, threshold.getGreenYellow());
		Assert.assertEquals(yellowRed, threshold.getYellowRed());
	}

	@Test
	public void testSetThresholdsReplace() {
		VeracodeDastThresholdModel threshold = new VeracodeDastThresholdModel();
		int greenYellow = 80;
		int yellowRed = 30;
		threshold.setGreenYellow(greenYellow);
		threshold.setYellowRed(yellowRed);
		int newGreenYellow = 90;
		int newYellowRed = 40;
		BDDMockito.when(mockThresholdsRepository.findAll()).thenReturn(Arrays.asList(threshold));
		BDDMockito.when(mockThresholdsRepository.saveAndFlush(BDDMockito.any())).thenAnswer(e -> e.getArgument(0));
		VeracodeDastThresholdModel newThreshold = this.thresholdsService.setThresholds(newGreenYellow, newYellowRed);
		Assert.assertEquals(newGreenYellow, newThreshold.getGreenYellow());
		Assert.assertEquals(newYellowRed, newThreshold.getYellowRed());
	}
}
