package com.tracelink.prodsec.plugin.veracode.sast.service;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import com.tracelink.prodsec.plugin.veracode.sast.model.VeracodeSastThresholdModel;
import com.tracelink.prodsec.plugin.veracode.sast.repository.VeracodeSastThresholdsRepository;

@RunWith(SpringRunner.class)
public class VeracodeSastThresholdsServiceTest {

	@MockBean
	private VeracodeSastThresholdsRepository mockThresholdsRepository;

	private VeracodeSastThresholdsService thresholdsService;

	@Before
	public void setup() {
		this.thresholdsService = new VeracodeSastThresholdsService(mockThresholdsRepository);
	}

	@Test
	public void testGetThresholdsNull() {
		BDDMockito.when(mockThresholdsRepository.findAll()).thenReturn(new ArrayList<>());
		Assert.assertNull(this.thresholdsService.getThresholds());
	}

	@Test
	public void testGetThresholds() {
		VeracodeSastThresholdModel threshold = new VeracodeSastThresholdModel();
		BDDMockito.when(mockThresholdsRepository.findAll()).thenReturn(Arrays.asList(threshold));
		Assert.assertEquals(threshold, this.thresholdsService.getThresholds());
	}

	@Test
	public void testSetThresholdsNew() {
		int greenYellow = 80;
		int yellowRed = 30;
		BDDMockito.when(mockThresholdsRepository.findAll()).thenReturn(new ArrayList<>());
		BDDMockito.when(mockThresholdsRepository.saveAndFlush(BDDMockito.any())).thenAnswer(e -> e.getArgument(0));
		VeracodeSastThresholdModel threshold = this.thresholdsService.setThresholds(greenYellow, yellowRed);
		Assert.assertEquals(greenYellow, threshold.getGreenYellow());
		Assert.assertEquals(yellowRed, threshold.getYellowRed());
	}

	@Test
	public void testSetThresholdsReplace() {
		VeracodeSastThresholdModel threshold = new VeracodeSastThresholdModel();
		int greenYellow = 80;
		int yellowRed = 30;
		threshold.setGreenYellow(greenYellow);
		threshold.setYellowRed(yellowRed);
		int newGreenYellow = 90;
		int newYellowRed = 40;
		BDDMockito.when(mockThresholdsRepository.findAll()).thenReturn(Arrays.asList(threshold));
		BDDMockito.when(mockThresholdsRepository.saveAndFlush(BDDMockito.any())).thenAnswer(e -> e.getArgument(0));
		VeracodeSastThresholdModel newThreshold = this.thresholdsService.setThresholds(newGreenYellow, newYellowRed);
		Assert.assertEquals(newGreenYellow, newThreshold.getGreenYellow());
		Assert.assertEquals(newYellowRed, newThreshold.getYellowRed());
	}
}
