package com.tracelink.prodsec.plugin.jira.service;

import com.tracelink.prodsec.plugin.jira.exception.JiraThresholdsException;
import com.tracelink.prodsec.plugin.jira.model.JiraThresholds;
import com.tracelink.prodsec.plugin.jira.repo.JiraThresholdsRepository;

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
public class JiraThresholdsServiceTest {

	@MockBean
	private JiraThresholdsRepository thresholdsRepository;

	private JiraThresholdsService thresholdsService;

	@Before
	public void setup() {
		thresholdsService = new JiraThresholdsService(thresholdsRepository);
	}

	@Test(expected = JiraThresholdsException.class)
	public void testGetThresholdsEmpty() {
		BDDMockito.when(thresholdsRepository.findAll()).thenReturn(Collections.emptyList());
		thresholdsService.getThresholds();
	}

	@Test
	public void testGetThresholds() {
		JiraThresholds thresholds = new JiraThresholds();
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

		ArgumentCaptor<JiraThresholds> captor = ArgumentCaptor.forClass(JiraThresholds.class);
		BDDMockito.verify(thresholdsRepository, Mockito.times(1)).saveAndFlush(captor.capture());

		JiraThresholds thresholds = captor.getValue();
		Assert.assertEquals(50, thresholds.getGreenYellow());
		Assert.assertEquals(100, thresholds.getYellowRed());
	}

	@Test
	public void testSetThresholds() {
		JiraThresholds thresholds = new JiraThresholds();
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
