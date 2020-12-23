package com.tracelink.prodsec.plugin.sonatype.controller;

import com.tracelink.prodsec.plugin.sonatype.SonatypePlugin;
import com.tracelink.prodsec.plugin.sonatype.model.SonatypeApp;
import com.tracelink.prodsec.plugin.sonatype.model.SonatypeMetrics;
import com.tracelink.prodsec.plugin.sonatype.service.SonatypeAppService;
import com.tracelink.prodsec.synapse.products.model.ProductLineModel;
import com.tracelink.prodsec.synapse.products.model.ProjectFilterModel;
import com.tracelink.prodsec.synapse.products.model.ProjectModel;
import com.tracelink.prodsec.synapse.products.service.ProductsService;
import com.tracelink.prodsec.synapse.test.TestSynapseBootApplication;

import org.hamcrest.Matchers;
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

import java.time.LocalDate;
import java.util.Collections;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestSynapseBootApplication.class)
@AutoConfigureMockMvc
public class SonatypeDashboardControllerTest {
	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private ProductsService productsService;

	@MockBean
	private SonatypeAppService appService;

	@Test
	@WithMockUser
	public void testGetDashboard() throws Exception {
		ProductLineModel productLine = new ProductLineModel();
		productLine.setName("Product Line");
		ProjectFilterModel projectFilter = new ProjectFilterModel();
		projectFilter.setName("Project Filter");
		ProjectModel project = new ProjectModel();
		project.setName("Project");

		SonatypeMetrics metrics = new SonatypeMetrics();
		metrics.setRecordedDate(LocalDate.now());
		metrics.setHighVios(4);
		metrics.setMedVios(3);
		SonatypeApp app = new SonatypeApp();
		app.setName("App");
		app.setSynapseProject(project);
		app.setMetrics(Collections.singletonList(metrics));

		BDDMockito.when(appService.getMappedApps()).thenReturn(Collections.singletonList(app));
		BDDMockito.when(productsService.getAllProductLines()).thenReturn(Collections.singletonList(productLine));
		BDDMockito.when(productsService.getAllProjectFilters()).thenReturn(Collections.singletonList(projectFilter));
		BDDMockito.when(productsService.getAllProjects()).thenReturn(Collections.singletonList(project));

		mockMvc.perform(MockMvcRequestBuilders.get(SonatypePlugin.DASHBOARD_PAGE))
				.andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
				.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("Product Line")))
				.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("Project Filter")))
				.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("Project")))
				.andExpect(MockMvcResultMatchers.model().attribute("coveredApps", 1))
				.andExpect(MockMvcResultMatchers.model().attribute("vulnerableApps", 1L))
				.andExpect(MockMvcResultMatchers.model().attribute("totalViolations", 7L))
				.andExpect(MockMvcResultMatchers.model().attribute("highViolations", 4L));
	}
}
