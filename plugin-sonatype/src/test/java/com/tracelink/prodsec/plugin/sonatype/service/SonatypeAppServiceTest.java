package com.tracelink.prodsec.plugin.sonatype.service;

import com.tracelink.prodsec.plugin.sonatype.model.SonatypeApp;
import com.tracelink.prodsec.plugin.sonatype.model.SonatypeMetrics;
import com.tracelink.prodsec.plugin.sonatype.repository.SonatypeAppRepository;
import com.tracelink.prodsec.synapse.products.model.ProductLineModel;
import com.tracelink.prodsec.synapse.products.model.ProjectModel;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.times;

@RunWith(SpringRunner.class)
public class SonatypeAppServiceTest {

	@MockBean
	private SonatypeAppRepository appRepository;

	private SonatypeAppService appService;

	private SonatypeApp app;

	private SonatypeMetrics metrics;

	@Before
	public void setup() {
		appService = new SonatypeAppService(appRepository);
		app = new SonatypeApp();
		app.setId("123");
		app.setName("bar");

		metrics = new SonatypeMetrics();
		metrics.setRecordedDate(LocalDate.now());
	}

	@Test
	public void testGetAppForIdAppPresent() {
		BDDMockito.when(appRepository.findById(BDDMockito.anyString())).thenReturn(Optional.of(app));

		SonatypeApp returnedApp = appService.getAppForId("any", "any");
		Assert.assertEquals(app, returnedApp);
	}

	@Test
	public void testGetAppForIdAppNotPresent() {
		BDDMockito.when(appRepository.findById(BDDMockito.anyString())).thenReturn(Optional.empty());

		SonatypeApp returnedApp = appService.getAppForId("123", "bar");
		Assert.assertEquals(returnedApp.getId(), "123");
		Assert.assertEquals(returnedApp.getName(), "bar");
	}

	@Test
	public void testGetMappedApps() {
		BDDMockito.when(appRepository.findAllBySynapseProjectNotNull()).thenReturn(Collections.emptyList());

		List<SonatypeApp> returnedApps = appService.getMappedApps();
		Assert.assertTrue(returnedApps.isEmpty());

		BDDMockito.when(appRepository.findAllBySynapseProjectNotNull()).thenReturn(Collections.singletonList(app));

		returnedApps = appService.getMappedApps();
		Assert.assertEquals(1, returnedApps.size());
		Assert.assertTrue(returnedApps.contains(app));
	}

	@Test
	public void testGetUnmappedApps() {
		BDDMockito.when(appRepository.findAllBySynapseProjectIsNull()).thenReturn(Collections.emptyList());

		List<SonatypeApp> returnedApps = appService.getUnmappedApps();
		Assert.assertTrue(returnedApps.isEmpty());

		BDDMockito.when(appRepository.findAllBySynapseProjectIsNull()).thenReturn(Collections.singletonList(app));

		returnedApps = appService.getUnmappedApps();
		Assert.assertEquals(1, returnedApps.size());
		Assert.assertTrue(returnedApps.contains(app));
	}

	@Test
	public void testGetMostRecentMetricsForProductLineNull() {
		// Case where there are no projects in a product line
		ProductLineModel productLine = new ProductLineModel();
		productLine.setProjects(Collections.emptyList());

		Assert.assertNull(appService.getMostRecentMetricsForProductLine(productLine));

		// Case where all projects in a product line are unmapped
		ProjectModel project = new ProjectModel();
		project.setName("project");
		productLine.setProjects(Collections.singletonList(project));

		BDDMockito.when(appRepository.findBySynapseProject(project)).thenReturn(null);
		Assert.assertNull(appService.getMostRecentMetricsForProductLine(productLine));
	}

	@Test
	public void testGetMostRecentMetricsForProductLineEmpty() {
		ProjectModel project = new ProjectModel();
		project.setName("project");

		ProductLineModel productLine = new ProductLineModel();
		productLine.setProjects(Collections.singletonList(project));

		BDDMockito.when(appRepository.findBySynapseProject(project)).thenReturn(app);

		// Case where app has no metrics
		app.setMetrics(Collections.emptyList());
		Assert.assertTrue(appService.getMostRecentMetricsForProductLine(productLine).isEmpty());

		// Case where metrics are too old
		metrics.setRecordedDate(LocalDate.now().minusDays(1));
		app.setMetrics(Collections.singletonList(metrics));

		Assert.assertTrue(appService.getMostRecentMetricsForProductLine(productLine).isEmpty());
	}

	@Test
	public void testGetMostRecentMetricsForProductLineNotEmpty() {
		ProjectModel project = new ProjectModel();
		project.setName("project");

		ProductLineModel productLine = new ProductLineModel();
		productLine.setProjects(Collections.singletonList(project));

		BDDMockito.when(appRepository.findBySynapseProject(project)).thenReturn(app);

		// Case where there is a single mapped project
		app.setMetrics(Collections.singletonList(metrics));
		List<SonatypeMetrics> returnedMetrics = appService.getMostRecentMetricsForProductLine(productLine);
		Assert.assertEquals(1, returnedMetrics.size());
		Assert.assertTrue(returnedMetrics.contains(metrics));

		// Case where there are two mapped projects
		ProjectModel project2 = new ProjectModel();
		project2.setName("project2");

		productLine.setProjects(Arrays.asList(project, project2));

		BDDMockito.when(appRepository.findBySynapseProject(project2)).thenReturn(app);

		returnedMetrics = appService.getMostRecentMetricsForProductLine(productLine);
		Assert.assertEquals(2, returnedMetrics.size());
		Assert.assertTrue(returnedMetrics.contains(metrics));
	}

	@Test
	public void testGetMostRecentMetricsForProjectNull() {
		ProjectModel project = new ProjectModel();
		project.setName("project");

		BDDMockito.when(appRepository.findBySynapseProject(project)).thenReturn(null);
		Assert.assertNull(appService.getMostRecentMetricsForProject(project));
	}

	@Test
	public void testGetMostRecentMetricsForProjectEmpty() {
		ProjectModel project = new ProjectModel();
		project.setName("project");

		BDDMockito.when(appRepository.findBySynapseProject(project)).thenReturn(app);

		// Case where app has no metrics
		app.setMetrics(Collections.emptyList());
		Assert.assertTrue(appService.getMostRecentMetricsForProject(project).isEmpty());

		// Case where metrics are too old
		metrics.setRecordedDate(LocalDate.now().minusDays(1));
		app.setMetrics(Collections.singletonList(metrics));

		Assert.assertTrue(appService.getMostRecentMetricsForProject(project).isEmpty());
	}

	@Test
	public void testGetMostRecentMetricsForProjectNotEmpty() {
		ProjectModel project = new ProjectModel();
		project.setName("project");

		BDDMockito.when(appRepository.findBySynapseProject(project)).thenReturn(app);

		// Case where there is a single metrics
		app.setMetrics(Collections.singletonList(metrics));
		List<SonatypeMetrics> returnedMetrics = appService.getMostRecentMetricsForProject(project);
		Assert.assertEquals(1, returnedMetrics.size());
		Assert.assertTrue(returnedMetrics.contains(metrics));

		// Case where there are two metrics
		SonatypeMetrics metrics2 = new SonatypeMetrics();
		metrics2.setRecordedDate(LocalDate.now());

		app.setMetrics(Arrays.asList(metrics2, metrics));

		returnedMetrics = appService.getMostRecentMetricsForProject(project);
		Assert.assertEquals(1, returnedMetrics.size());
		Assert.assertTrue(returnedMetrics.contains(metrics2));
		Assert.assertFalse(returnedMetrics.contains(metrics));
	}

	@Test
	public void testCreateMappingNull() {
		appService.createMapping(null, app.getName());
		BDDMockito.verify(appRepository, times(0)).saveAndFlush(BDDMockito.any());

		appService.createMapping(new ProjectModel(), null);
		BDDMockito.verify(appRepository, times(0)).saveAndFlush(BDDMockito.any());

		appService.createMapping(null, null);
		BDDMockito.verify(appRepository, times(0)).saveAndFlush(BDDMockito.any());
	}

	@Test
	public void testCreateMapping() {
		ProjectModel project = new ProjectModel();
		project.setName("project");

		BDDMockito.when(appRepository.findByName(BDDMockito.anyString())).thenReturn(app);
		appService.createMapping(project, app.getName());
		BDDMockito.verify(appRepository, times(1)).saveAndFlush(BDDMockito.any());
		Assert.assertEquals(project, app.getSynapseProject());
	}

	@Test
	public void testDeleteMappingNull() {
		appService.deleteMapping(null);
		BDDMockito.verify(appRepository, times(0)).saveAndFlush(BDDMockito.any());
	}

	@Test
	public void testDeleteMapping() {
		BDDMockito.when(appRepository.findByName(BDDMockito.anyString())).thenReturn(app);
		appService.deleteMapping(app.getName());
		BDDMockito.verify(appRepository, times(1)).saveAndFlush(BDDMockito.any());
		Assert.assertNull(app.getSynapseProject());
	}
}
