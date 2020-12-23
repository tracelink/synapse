package com.tracelink.prodsec.plugin.sonatype.service;

import com.tracelink.prodsec.plugin.sonatype.model.SonatypeApp;
import com.tracelink.prodsec.plugin.sonatype.model.SonatypeMetrics;
import com.tracelink.prodsec.plugin.sonatype.repository.SonatypeMetricsRepository;
import com.tracelink.prodsec.plugin.sonatype.util.ThreatLevel;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
public class SonatypeMetricsServiceTest {

	@MockBean
	private SonatypeMetricsRepository metricsRepository;

	private SonatypeMetricsService metricsService;
	private Map<ThreatLevel, Integer> threatLevels;
	private SonatypeApp app;

	@Before
	public void setup() {
		metricsService = new SonatypeMetricsService(metricsRepository);
		threatLevels = new HashMap<>();
		threatLevels.put(ThreatLevel.HIGH, 1);
		threatLevels.put(ThreatLevel.MEDIUM, 2);
		threatLevels.put(ThreatLevel.LOW, 3);
		threatLevels.put(ThreatLevel.INFO, 4);

		app = new SonatypeApp();
		app.setName("app");
	}

	@Test
	public void testGetEarliestMetricsDate() {
		BDDMockito.when(metricsRepository.findFirstByOrderByRecordedDateAsc()).thenReturn(null);
		Assert.assertEquals(0, metricsService.getEarliestMetricsDate().compareTo(LocalDate.now()));

		SonatypeMetrics metrics = new SonatypeMetrics();
		metrics.setRecordedDate(LocalDate.now().minusDays(1));
		BDDMockito.when(metricsRepository.findFirstByOrderByRecordedDateAsc()).thenReturn(metrics);

		LocalDate date = metricsService.getEarliestMetricsDate();
		Assert.assertTrue(date.isBefore(LocalDate.now()));

		Assert.assertEquals(date, metricsService.getEarliestMetricsDate());
	}

	@Test
	public void testStoreMetrics() {
		metricsService.storeMetrics(app, threatLevels);

		ArgumentCaptor<SonatypeMetrics> captor = ArgumentCaptor.forClass(SonatypeMetrics.class);
		BDDMockito.verify(metricsRepository, Mockito.times(1)).saveAndFlush(captor.capture());

		SonatypeMetrics metrics = captor.getValue();
		Assert.assertEquals(1, metrics.getHighVios());
		Assert.assertEquals(2, metrics.getMedVios());
		Assert.assertEquals(3, metrics.getLowVios());
		Assert.assertEquals(4, metrics.getInfoVios());
		Assert.assertEquals("app", metrics.getApp().getName());
	}

	@Test
	public void testStoreMetricsExistingMetrics() {
		LocalDate today = LocalDate.now();
		SonatypeMetrics metrics = new SonatypeMetrics();
		metrics.setApp(app);
		metrics.setRecordedDate(today);
		metrics.setHighVios(0);
		metrics.setMedVios(0);
		metrics.setLowVios(0);
		metrics.setInfoVios(0);

		BDDMockito.when(metricsRepository.findFirstByAppOrderByRecordedDateDesc(app)).thenReturn(metrics);

		metricsService.storeMetrics(app, threatLevels);

		ArgumentCaptor<SonatypeMetrics> captor = ArgumentCaptor.forClass(SonatypeMetrics.class);
		BDDMockito.verify(metricsRepository, Mockito.times(1)).saveAndFlush(captor.capture());

		Assert.assertEquals(metrics, captor.getValue());
		Assert.assertEquals(today, metrics.getRecordedDate());
		Assert.assertEquals("app", metrics.getApp().getName());
		Assert.assertEquals(1, metrics.getHighVios());
		Assert.assertEquals(2, metrics.getMedVios());
		Assert.assertEquals(3, metrics.getLowVios());
		Assert.assertEquals(4, metrics.getInfoVios());
	}
}
