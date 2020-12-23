package com.tracelink.prodsec.plugin.demo.service;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.tracelink.prodsec.plugin.demo.model.DemoItemModel;
import com.tracelink.prodsec.plugin.demo.model.DemoListModel;
import com.tracelink.prodsec.plugin.demo.model.DemoProjectEntity;
import com.tracelink.prodsec.plugin.demo.repo.DemoRepo;
import com.tracelink.prodsec.synapse.products.model.ProductLineModel;
import com.tracelink.prodsec.synapse.products.model.ProjectModel;
import com.tracelink.prodsec.synapse.products.service.ProductsService;
import com.tracelink.prodsec.synapse.scorecard.model.ScorecardValue;
import com.tracelink.prodsec.synapse.scorecard.model.ScorecardValue.TrafficLight;
import java.util.Arrays;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class DemoServiceTest {

	@MockBean
	private DemoRepo mockDemoRepo;

	@MockBean
	private ProductsService mockProductsService;

	private DemoService demoService;

	@Before
	public void setup() {
		demoService = new DemoService(mockDemoRepo, mockProductsService);
	}

	@Test
	public void testGetDemoListAndCache() {
		String projectName = "projectName";
		ProjectModel project = new ProjectModel();
		project.setName(projectName);

		String prodLine = "prodLine";
		ProductLineModel owningProductLine = new ProductLineModel();
		owningProductLine.setName(prodLine);

		project.setOwningProductLine(owningProductLine);

		BDDMockito.when(mockDemoRepo.findBySynapseProject(project)).thenReturn(null);

		BDDMockito.when(mockProductsService.getAllProjects()).thenReturn(Arrays.asList(project));
		DemoListModel dlm = demoService.getFullDemoList();

		BDDMockito.verify(mockDemoRepo).findBySynapseProject(BDDMockito.any());
		Assert.assertTrue(dlm.getMapping().containsKey(prodLine));
		Assert.assertEquals(1, dlm.getMapping().get(prodLine).size());

		DemoItemModel dim = dlm.getMapping().get(prodLine).get(0);
		Assert.assertEquals(projectName, dim.getProjectName());
		Assert.assertFalse(dim.isConfigured());
		Assert.assertEquals(0, dim.getVulns());
	}

	@Test
	public void testGetDemoListNotConfigured() {
		String projectName = "projectName";
		ProjectModel project = new ProjectModel();
		project.setName(projectName);

		String prodLine = "prodLine";
		ProductLineModel owningProductLine = new ProductLineModel();
		owningProductLine.setName(prodLine);

		project.setOwningProductLine(owningProductLine);

		int vulns = 2;
		DemoProjectEntity dpm = new DemoProjectEntity();
		dpm.setVuln(vulns);

		BDDMockito.when(mockDemoRepo.findBySynapseProject(project)).thenReturn(dpm);
		BDDMockito.when(mockProductsService.getAllProjects()).thenReturn(Arrays.asList(project));

		DemoListModel dlm = demoService.getFullDemoList();

		BDDMockito.verify(mockDemoRepo).findBySynapseProject(BDDMockito.any());
		Assert.assertTrue(dlm.getMapping().containsKey(prodLine));
		Assert.assertEquals(1, dlm.getMapping().get(prodLine).size());

		DemoItemModel dim = dlm.getMapping().get(prodLine).get(0);
		Assert.assertEquals(projectName, dim.getProjectName());
		Assert.assertTrue(dim.isConfigured());
		Assert.assertEquals(vulns, dim.getVulns());
	}

	@Test
	public void testAssignVulnsToProjectNew() {
		ProjectModel project = new ProjectModel();
		int vulns = 2;

		BDDMockito.when(mockDemoRepo.findBySynapseProject(BDDMockito.any())).thenReturn(null);

		ArgumentCaptor<DemoProjectEntity> dpmCaptor = ArgumentCaptor
				.forClass(DemoProjectEntity.class);

		demoService.assignVulnsToProject(project, vulns);

		BDDMockito.verify(mockDemoRepo).saveAndFlush(dpmCaptor.capture());

		Assert.assertEquals(project, dpmCaptor.getValue().getProjectModel());
		Assert.assertEquals(vulns, dpmCaptor.getValue().getVuln());
	}

	@Test
	public void testAssignVulnsToProjectUpdate() {
		ProjectModel project = new ProjectModel();
		int vulns = 2;

		DemoProjectEntity dpm = new DemoProjectEntity();
		dpm.setProjectModel(project);
		dpm.setVuln(5);

		BDDMockito.when(mockDemoRepo.findBySynapseProject(BDDMockito.any())).thenReturn(dpm);

		ArgumentCaptor<DemoProjectEntity> dpmCaptor = ArgumentCaptor
				.forClass(DemoProjectEntity.class);

		demoService.assignVulnsToProject(project, vulns);

		BDDMockito.verify(mockDemoRepo).saveAndFlush(dpmCaptor.capture());

		Assert.assertEquals(project, dpmCaptor.getValue().getProjectModel());
		Assert.assertEquals(vulns, dpmCaptor.getValue().getVuln());
	}

	@Test
	public void testGetVulnsForProjectSuccess() throws DemoNotFoundException {
		int vuln = 2;
		DemoProjectEntity dpm = new DemoProjectEntity();
		dpm.setVuln(vuln);

		BDDMockito.when(mockDemoRepo.findBySynapseProject(BDDMockito.any())).thenReturn(dpm);
		int retVuln = demoService.getVulnsForProject(new ProjectModel());

		Assert.assertEquals(vuln, retVuln);
	}

	@Test(expected = DemoNotFoundException.class)
	public void testGetVulnsForProjectFail() throws DemoNotFoundException {
		BDDMockito.when(mockDemoRepo.findBySynapseProject(BDDMockito.any())).thenReturn(null);
		demoService.getVulnsForProject(new ProjectModel());
	}

	@Test
	public void testLogVulns() {
		int vuln = 2;
		DemoProjectEntity dpm = new DemoProjectEntity();
		dpm.setVuln(vuln);

		BDDMockito.when(mockDemoRepo.findAll()).thenReturn(Arrays.asList(dpm));

		Logger logger = (Logger) LoggerFactory.getLogger(DemoService.class);
		ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
		listAppender.start();
		logger.addAppender(listAppender);

		demoService.logVulns();

		Assert.assertEquals(1, listAppender.list.size());
		Assert.assertTrue(listAppender.list.get(0).getMessage()
				.contains("Found " + vuln + " vulns across 1"));
	}

	@Test
	public void testProductLineCallback() {
		int vuln = 2;
		ScorecardValue value = productCallbackTest(vuln);
		Assert.assertEquals("Vulns: " + vuln, value.getValue());
		Assert.assertEquals(TrafficLight.GREEN, value.getColor());

		vuln = 3;
		value = productCallbackTest(vuln);
		Assert.assertEquals("Vulns: " + vuln, value.getValue());
		Assert.assertEquals(TrafficLight.YELLOW, value.getColor());

		vuln = 10;
		value = productCallbackTest(vuln);
		Assert.assertEquals("Vulns: " + vuln, value.getValue());
		Assert.assertEquals(TrafficLight.RED, value.getColor());
	}

	private ScorecardValue productCallbackTest(int vulns) {
		DemoProjectEntity dpm = new DemoProjectEntity();
		dpm.setVuln(vulns);

		BDDMockito.when(mockDemoRepo.findBySynapseProject(BDDMockito.any())).thenReturn(dpm);

		ProductLineModel plm = new ProductLineModel();
		plm.setProjects(Arrays.asList(new ProjectModel()));

		return demoService.productLineCallback(plm);
	}

	@Test
	public void testProductLineCallbackUnknown() {
		BDDMockito.when(mockDemoRepo.findBySynapseProject(BDDMockito.any())).thenReturn(null);

		ProductLineModel plm = new ProductLineModel();
		plm.setProjects(Arrays.asList(new ProjectModel()));

		ScorecardValue value = demoService.productLineCallback(plm);
		Assert.assertEquals("Vulns: 0", value.getValue());
		Assert.assertEquals(TrafficLight.GREEN, value.getColor());
	}

	@Test
	public void testProjectCallback() {
		int vuln = 2;
		ScorecardValue value = projectCallbackTest(vuln);
		Assert.assertEquals("Vulns: " + vuln, value.getValue());
		Assert.assertEquals(TrafficLight.GREEN, value.getColor());

		vuln = 3;
		value = projectCallbackTest(vuln);
		Assert.assertEquals("Vulns: " + vuln, value.getValue());
		Assert.assertEquals(TrafficLight.YELLOW, value.getColor());

		vuln = 10;
		value = projectCallbackTest(vuln);
		Assert.assertEquals("Vulns: " + vuln, value.getValue());
		Assert.assertEquals(TrafficLight.RED, value.getColor());
	}

	private ScorecardValue projectCallbackTest(int vulns) {
		DemoProjectEntity dpm = new DemoProjectEntity();
		dpm.setVuln(vulns);

		BDDMockito.when(mockDemoRepo.findBySynapseProject(BDDMockito.any())).thenReturn(dpm);

		return demoService.projectCallback(new ProjectModel());
	}

	@Test
	public void testProjectCallbackUnknown() {
		BDDMockito.when(mockDemoRepo.findBySynapseProject(BDDMockito.any())).thenReturn(null);

		ScorecardValue value = demoService.projectCallback(new ProjectModel());
		Assert.assertEquals("Unconfigured", value.getValue());
		Assert.assertEquals(TrafficLight.NONE, value.getColor());
	}
}
