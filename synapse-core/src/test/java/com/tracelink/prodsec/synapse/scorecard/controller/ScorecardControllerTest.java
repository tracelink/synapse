package com.tracelink.prodsec.synapse.scorecard.controller;

import com.tracelink.prodsec.synapse.mvc.SynapseModelAndView;
import com.tracelink.prodsec.synapse.products.ProductsNotFoundException;
import com.tracelink.prodsec.synapse.products.model.ProductLineModel;
import com.tracelink.prodsec.synapse.products.model.ProjectFilterModel;
import com.tracelink.prodsec.synapse.products.service.ProductsService;
import com.tracelink.prodsec.synapse.scheduler.service.SchedulerService;
import com.tracelink.prodsec.synapse.scorecard.model.Scorecard;
import com.tracelink.prodsec.synapse.scorecard.service.ScorecardService;
import com.tracelink.prodsec.synapse.test.TestSynapseBootApplicationCore;
import java.util.Arrays;
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

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestSynapseBootApplicationCore.class)
@AutoConfigureMockMvc
public class ScorecardControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private ScorecardService mockScorecardService;

	@MockBean
	private ProductsService mockProductsService;

	@Test
	@WithMockUser
	public void scorecardHomeTest() throws Exception {
		String plmName = "plm";
		ProductLineModel plm = new ProductLineModel();
		plm.setName(plmName);

		String pfmName = "pfm";
		ProjectFilterModel pfm = new ProjectFilterModel();
		pfm.setName(pfmName);

		BDDMockito.when(mockProductsService.getAllProductLines()).thenReturn(Arrays.asList(plm));
		BDDMockito.when(mockProductsService.getAllProjectFilters()).thenReturn(Arrays.asList(pfm));
		BDDMockito.when(mockScorecardService.getTopLevelScorecard()).thenReturn(new Scorecard());
		BDDMockito.when(mockScorecardService.isReady()).thenReturn(true);
mockMvc.perform(MockMvcRequestBuilders.get("/"))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("productLineNames", Matchers.contains(plmName)))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("filterNames", Matchers.contains(pfmName)))
				.andExpect(
						MockMvcResultMatchers.model()
								.attribute("styles", Matchers.contains("/styles/scorecard.css")))
				.andExpect(
						MockMvcResultMatchers.model()
								.attribute("scripts", Matchers.contains("/scripts/scorecard.js")))
				.andExpect(
						MockMvcResultMatchers.model().attribute("scorecardType", "Top Level View"));
	}

	@Test
	@WithMockUser
	public void scorecardHomeTestFiltersProductLine() throws Exception {
		String filterType = "productLine";
		String filterName = "plmFilter";
		String expectedContains = "Product Line View";

		String plmName = "plm";
		ProductLineModel plm = new ProductLineModel();
		plm.setName(plmName);

		String pfmName = "pfm";
		ProjectFilterModel pfm = new ProjectFilterModel();
		pfm.setName(pfmName);

		BDDMockito.when(mockProductsService.getAllProductLines()).thenReturn(Arrays.asList(plm));
		BDDMockito.when(mockProductsService.getAllProjectFilters()).thenReturn(Arrays.asList(pfm));
		BDDMockito.when(mockScorecardService.getScorecardForProductLine(BDDMockito.anyString()))
				.thenReturn(new Scorecard());
		BDDMockito.when(mockScorecardService.isReady()).thenReturn(true);

		mockMvc.perform(MockMvcRequestBuilders.get("/").param("filterType", filterType)
				.param("name", filterName))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("productLineNames", Matchers.contains(plmName)))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("filterNames", Matchers.contains(pfmName)))
				.andExpect(
						MockMvcResultMatchers.model()
								.attribute("styles", Matchers.contains("/styles/scorecard.css")))
				.andExpect(
						MockMvcResultMatchers.model()
								.attribute("scripts", Matchers.contains("/scripts/scorecard.js")))
				.andExpect(MockMvcResultMatchers.model().attribute("scorecardType",
						Matchers.containsString(expectedContains)));
	}

	@Test
	@WithMockUser
	public void scorecardHomeTestFiltersProjectFilter() throws Exception {
		String filterType = "filter";
		String filterName = "pfmFilter";
		String expectedContains = "Filter View";

		String plmName = "plm";
		ProductLineModel plm = new ProductLineModel();
		plm.setName(plmName);

		String pfmName = "pfm";
		ProjectFilterModel pfm = new ProjectFilterModel();
		pfm.setName(pfmName);

		BDDMockito.when(mockProductsService.getAllProductLines()).thenReturn(Arrays.asList(plm));
		BDDMockito.when(mockProductsService.getAllProjectFilters()).thenReturn(Arrays.asList(pfm));
		BDDMockito.when(mockScorecardService.getScorecardForFilter(BDDMockito.anyString()))
				.thenReturn(new Scorecard());
		BDDMockito.when(mockScorecardService.isReady()).thenReturn(true);

		mockMvc.perform(MockMvcRequestBuilders.get("/").param("filterType", filterType)
				.param("name", filterName))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("productLineNames", Matchers.contains(plmName)))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("filterNames", Matchers.contains(pfmName)))
				.andExpect(
						MockMvcResultMatchers.model()
								.attribute("styles", Matchers.contains("/styles/scorecard.css")))
				.andExpect(
						MockMvcResultMatchers.model()
								.attribute("scripts", Matchers.contains("/scripts/scorecard.js")))
				.andExpect(MockMvcResultMatchers.model().attribute("scorecardType",
						Matchers.containsString(expectedContains)));
	}

	@Test
	@WithMockUser
	public void scorecardHomeTestFiltersProject() throws Exception {
		String filterType = "project";
		String filterName = "pmFilter";
		String expectedContains = "Project View";

		String plmName = "plm";
		ProductLineModel plm = new ProductLineModel();
		plm.setName(plmName);

		String pfmName = "pfm";
		ProjectFilterModel pfm = new ProjectFilterModel();
		pfm.setName(pfmName);

		BDDMockito.when(mockProductsService.getAllProductLines()).thenReturn(Arrays.asList(plm));
		BDDMockito.when(mockProductsService.getAllProjectFilters()).thenReturn(Arrays.asList(pfm));
		BDDMockito.when(mockScorecardService.isReady()).thenReturn(true);
		BDDMockito.when(mockScorecardService.getScorecardForProject(BDDMockito.anyString()))
				.thenReturn(new Scorecard());

		mockMvc.perform(MockMvcRequestBuilders.get("/").param("filterType", filterType)
				.param("name", filterName))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("productLineNames", Matchers.contains(plmName)))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("filterNames", Matchers.contains(pfmName)))
				.andExpect(
						MockMvcResultMatchers.model()
								.attribute("styles", Matchers.contains("/styles/scorecard.css")))
				.andExpect(
						MockMvcResultMatchers.model()
								.attribute("scripts", Matchers.contains("/scripts/scorecard.js")))
				.andExpect(MockMvcResultMatchers.model().attribute("scorecardType",
						Matchers.containsString(expectedContains)));
	}

	@Test
	@WithMockUser
	public void scorecardHomeTestFailEmptyType() throws Exception {
		String filterType = null;
		String filterName = "pmFilter";

		String plmName = "plm";
		ProductLineModel plm = new ProductLineModel();
		plm.setName(plmName);

		String pfmName = "pfm";
		ProjectFilterModel pfm = new ProjectFilterModel();
		pfm.setName(pfmName);

		BDDMockito.when(mockProductsService.getAllProductLines())
				.thenReturn(Arrays.asList(plm));
		BDDMockito.when(mockProductsService.getAllProjectFilters())
				.thenReturn(Arrays.asList(pfm));
		BDDMockito.when(mockScorecardService.isReady()).thenReturn(true);

		mockMvc.perform(MockMvcRequestBuilders.get("/").param("filterType", filterType)
				.param("name", filterName))
				.andExpect(
						MockMvcResultMatchers.flash().attribute(SynapseModelAndView.FAILURE_FLASH,
								Matchers.containsString("filter type")));
	}

	@Test
	@WithMockUser
	public void scorecardHomeTestFailEmptyName() throws Exception {
		String filterType = "project";
		String filterName = null;

		String plmName = "plm";
		ProductLineModel plm = new ProductLineModel();
		plm.setName(plmName);

		String pfmName = "pfm";
		ProjectFilterModel pfm = new ProjectFilterModel();
		pfm.setName(pfmName);

		BDDMockito.when(mockProductsService.getAllProductLines())
				.thenReturn(Arrays.asList(plm));
		BDDMockito.when(mockProductsService.getAllProjectFilters())
				.thenReturn(Arrays.asList(pfm));
		BDDMockito.when(mockScorecardService.isReady()).thenReturn(true);

		mockMvc.perform(MockMvcRequestBuilders.get("/").param("filterType", filterType)
				.param("name", filterName))
				.andExpect(
						MockMvcResultMatchers.flash().attribute(SynapseModelAndView.FAILURE_FLASH,
								Matchers.containsString("filter name")));
	}

	@Test
	@WithMockUser
	public void scorecardHomeTestFailBadType() throws Exception {
		String filterType = "unknown";
		String filterName = "pmName";

		String plmName = "plm";
		ProductLineModel plm = new ProductLineModel();
		plm.setName(plmName);

		String pfmName = "pfm";
		ProjectFilterModel pfm = new ProjectFilterModel();
		pfm.setName(pfmName);

		BDDMockito.when(mockProductsService.getAllProductLines())
				.thenReturn(Arrays.asList(plm));
		BDDMockito.when(mockProductsService.getAllProjectFilters())
				.thenReturn(Arrays.asList(pfm));
		BDDMockito.when(mockScorecardService.isReady()).thenReturn(true);

		mockMvc.perform(MockMvcRequestBuilders.get("/").param("filterType", filterType)
				.param("name", filterName))
				.andExpect(
						MockMvcResultMatchers.flash().attribute(SynapseModelAndView.FAILURE_FLASH,
								Matchers.containsString("Unknown filter type")));
	}

	@Test
	@WithMockUser
	public void scorecardHomeTestErrorGetScorecard() throws Exception {
		String filterType = "project";
		String filterName = "pmFilter";

		String plmName = "plm";
		ProductLineModel plm = new ProductLineModel();
		plm.setName(plmName);

		String pfmName = "pfm";
		ProjectFilterModel pfm = new ProjectFilterModel();
		pfm.setName(pfmName);

		BDDMockito.when(mockProductsService.getAllProductLines())
				.thenReturn(Arrays.asList(plm));
		BDDMockito.when(mockProductsService.getAllProjectFilters())
				.thenReturn(Arrays.asList(pfm));
		BDDMockito.willThrow(new ProductsNotFoundException("")).given(mockScorecardService)
				.getScorecardForProject(BDDMockito.anyString());
		BDDMockito.when(mockScorecardService.getTopLevelScorecard())
				.thenReturn(new Scorecard());
		BDDMockito.when(mockScorecardService.isReady()).thenReturn(true);

		mockMvc.perform(MockMvcRequestBuilders.get("/").param("filterType", filterType)
				.param("name", filterName))
				.andExpect(
						MockMvcResultMatchers.flash().attribute(SynapseModelAndView.FAILURE_FLASH,
								Matchers.containsString("Could not get scorecard due to error")));
	}
}
