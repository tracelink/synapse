package com.tracelink.prodsec.plugin.sonatype;

import com.tracelink.prodsec.plugin.sonatype.exception.SonatypeThresholdsException;
import com.tracelink.prodsec.plugin.sonatype.model.SonatypeApp;
import com.tracelink.prodsec.plugin.sonatype.model.SonatypeMetrics;
import com.tracelink.prodsec.plugin.sonatype.model.SonatypeThresholds;
import com.tracelink.prodsec.plugin.sonatype.service.SonatypeAppService;
import com.tracelink.prodsec.plugin.sonatype.service.SonatypeThresholdsService;
import com.tracelink.prodsec.synapse.products.model.ProductLineModel;
import com.tracelink.prodsec.synapse.products.model.ProjectModel;
import com.tracelink.prodsec.synapse.products.service.ProductsService;
import com.tracelink.prodsec.synapse.test.TestSynapseBootApplication;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Arrays;
import java.util.Collections;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestSynapseBootApplication.class)
@AutoConfigureMockMvc
public class SonatypePluginScorecardTest {
	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private SonatypeAppService appService;

	@MockBean
	private SonatypeThresholdsService thresholdsService;

	@MockBean
	private ProductsService productsService;

	private ProductLineModel productLine;
	private ProjectModel project;
	private SonatypeApp app;
	private SonatypeMetrics metrics;
	private SonatypeThresholds thresholds;

	@Before
	public void setup() {
		project = new ProjectModel();
		project.setName("Project 2");

		productLine = new ProductLineModel();
		productLine.setName("Product Line 1");
		productLine.setProjects(Collections.singletonList(project));

		app = new SonatypeApp();
		app.setSynapseProject(project);

		metrics = new SonatypeMetrics();

		thresholds = new SonatypeThresholds();
		thresholds.setGreenYellow(100);
		thresholds.setYellowRed(200);
	}

	@Test
	@WithMockUser
	public void testGetScorecardValueForProductLineTrafficGreen() throws Exception {
		metrics.setHighVios(1);
		metrics.setMedVios(2);
		metrics.setLowVios(3);

		BDDMockito.when(productsService.getAllProductLines()).thenReturn(Collections.singletonList(productLine));
		BDDMockito.when(appService.getMappedApps()).thenReturn(Collections.singletonList(app));
		BDDMockito.when(appService.getMostRecentMetricsForProductLine(productLine))
				.thenReturn(Collections.singletonList(metrics));
		BDDMockito.when(thresholdsService.getThresholds()).thenReturn(thresholds);

		mockMvc.perform(MockMvcRequestBuilders.get("/")).andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
				.andExpect(MockMvcResultMatchers.content().string(Matchers.stringContainsInOrder(
						Arrays.asList("Product Line 1", "traffic-light-green", "High: 1, Med: 2, Low: 3"))));
	}

	@Test
	@WithMockUser
	public void testGetScorecardValueForProductLineTrafficYellow() throws Exception {
		metrics.setHighVios(5);
		metrics.setMedVios(2);
		metrics.setLowVios(3);

		BDDMockito.when(productsService.getAllProductLines()).thenReturn(Collections.singletonList(productLine));
		BDDMockito.when(appService.getMappedApps()).thenReturn(Collections.singletonList(app));
		BDDMockito.when(appService.getMostRecentMetricsForProductLine(productLine))
				.thenReturn(Collections.singletonList(metrics));
		BDDMockito.when(thresholdsService.getThresholds()).thenReturn(thresholds);

		mockMvc.perform(MockMvcRequestBuilders.get("/")).andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
				.andExpect(MockMvcResultMatchers.content().string(Matchers.stringContainsInOrder(
						Arrays.asList("Product Line 1", "traffic-light-yellow", "High: 5, Med: 2, Low: 3"))));
	}

	@Test
	@WithMockUser
	public void testGetScorecardValueForProjectTrafficRed() throws Exception {
		metrics.setHighVios(10);
		metrics.setMedVios(2);
		metrics.setLowVios(3);

		BDDMockito.when(productsService.getProductLine(productLine.getName())).thenReturn(productLine);
		BDDMockito.when(appService.getMostRecentMetricsForProject(project))
				.thenReturn(Collections.singletonList(metrics));
		BDDMockito.when(thresholdsService.getThresholds()).thenReturn(thresholds);

		mockMvc.perform(
				MockMvcRequestBuilders.get("/").param("filterType", "productLine").param("name", "Product Line 1"))
				.andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
				.andExpect(MockMvcResultMatchers.content().string(Matchers.stringContainsInOrder(
						Arrays.asList("Project 2", "traffic-light-red", "High: 10, Med: 2, Low: 3"))));
	}

	@Test
	@WithMockUser
	public void testGetScorecardValueForProjectNoThresholds() throws Exception {
		metrics.setHighVios(10);
		metrics.setMedVios(2);
		metrics.setLowVios(3);

		BDDMockito.when(productsService.getProductLine(productLine.getName())).thenReturn(productLine);
		BDDMockito.when(appService.getMostRecentMetricsForProject(project))
				.thenReturn(Collections.singletonList(metrics));
		BDDMockito.when(thresholdsService.getThresholds()).thenThrow(SonatypeThresholdsException.class);

		mockMvc.perform(
				MockMvcRequestBuilders.get("/").param("filterType", "productLine").param("name", "Product Line 1"))
				.andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
				.andExpect(MockMvcResultMatchers.content().string(Matchers.stringContainsInOrder(
						Arrays.asList("Project 2", "traffic-light-none", "High: 10, Med: 2, Low: 3"))));
	}

	@Test
	@WithMockUser
	public void testGetScorecardValueNoData() throws Exception {
		BDDMockito.when(productsService.getProductLine(productLine.getName())).thenReturn(productLine);
		BDDMockito.when(appService.getMostRecentMetricsForProject(project)).thenReturn(null);

		mockMvc.perform(
				MockMvcRequestBuilders.get("/").param("filterType", "productLine").param("name", "Product Line 1"))
				.andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
				.andExpect(MockMvcResultMatchers.content().string(
						Matchers.stringContainsInOrder(Arrays.asList("Project 2", "traffic-light-none", "No data"))));
	}
}
