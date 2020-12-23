package com.tracelink.prodsec.synapse.products.controller;

import com.tracelink.prodsec.synapse.auth.SynapseAdminAuthDictionary;
import com.tracelink.prodsec.synapse.mvc.SynapseModelAndView;
import com.tracelink.prodsec.synapse.products.BadProductNameException;
import com.tracelink.prodsec.synapse.products.ProductsNotFoundException;
import com.tracelink.prodsec.synapse.products.model.ProductLineModel;
import com.tracelink.prodsec.synapse.products.model.ProjectFilterModel;
import com.tracelink.prodsec.synapse.products.model.ProjectModel;
import com.tracelink.prodsec.synapse.products.service.ProductsService;
import com.tracelink.prodsec.synapse.test.TestSynapseBootApplicationCore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestSynapseBootApplicationCore.class)
@AutoConfigureMockMvc
public class ProjectFilterControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private ProductsService mockProductsService;

	@Test
	@WithMockUser
	public void projectFilterHomeTest() throws Exception {
		List<ProjectFilterModel> filters = new ArrayList<>();
		BDDMockito.when(mockProductsService.getAllProjectFilters()).thenReturn(filters);

		mockMvc.perform(MockMvcRequestBuilders.get("/projectfilter"))
				.andExpect(MockMvcResultMatchers.model().attribute("filters", Matchers.is(filters)))
				.andExpect(MockMvcResultMatchers.model().attribute("scripts", Matchers.hasSize(1)));
	}

	@Test
	@WithMockUser
	public void filterViewTest() throws Exception {
		String filterName = "filterName";
		String projectName = "projectName";

		List<ProjectFilterModel> filters = new ArrayList<>();
		ProjectFilterModel filter = new ProjectFilterModel();
		filter.setName(filterName);

		List<ProjectModel> projects = new ArrayList<>();
		ProjectModel project = new ProjectModel();
		project.setName(projectName);
		project.setOwningProductLine(new ProductLineModel());
		filter.setProjects(Arrays.asList(project));

		BDDMockito.when(mockProductsService.getAllProjects()).thenReturn(projects);
		BDDMockito.when(mockProductsService.getAllProjectFilters()).thenReturn(filters);
		BDDMockito.when(mockProductsService.getProjectFilter(BDDMockito.anyString()))
				.thenReturn(filter);

		mockMvc.perform(MockMvcRequestBuilders.get("/projectfilter/name"))
				.andExpect(MockMvcResultMatchers.model().attribute("filters", Matchers.is(filters)))
				.andExpect(MockMvcResultMatchers.model().attribute("scripts", Matchers.hasSize(1)));
	}

	@Test
	@WithMockUser(authorities = SynapseAdminAuthDictionary.ADMIN_PRIV)
	public void createFilterTestSuccess() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.post("/projectfilter/createfilter")
				.param("filterName", "filterName")
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(
						MockMvcResultMatchers.flash().attribute(SynapseModelAndView.FAILURE_FLASH,
								Matchers.nullValue()));
	}

	@Test
	@WithMockUser(authorities = SynapseAdminAuthDictionary.ADMIN_PRIV)
	public void createFilterTestFail() throws Exception {
		BDDMockito.willThrow(new BadProductNameException("")).given(mockProductsService)
				.createProjectFilter(BDDMockito.anyString());

		mockMvc.perform(MockMvcRequestBuilders.post("/projectfilter/createfilter")
				.param("filterName", "filterName")
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.flash()
						.attributeExists(SynapseModelAndView.FAILURE_FLASH));
	}

	@Test
	@WithMockUser(authorities = SynapseAdminAuthDictionary.ADMIN_PRIV)
	public void moveProjectFilterTestSuccess() throws Exception {
		mockMvc.perform(
				MockMvcRequestBuilders.post("/projectfilter/name/rename").param("filter", "filter")
						.param("filterName", "filterName")
						.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(
						MockMvcResultMatchers.flash().attribute(SynapseModelAndView.SUCCESS_FLASH,
								Matchers.containsString("renamed")));
	}

	@Test
	@WithMockUser(authorities = SynapseAdminAuthDictionary.ADMIN_PRIV)
	public void moveProjectFilterTestFail() throws Exception {
		BDDMockito.willThrow(new BadProductNameException("")).given(mockProductsService)
				.renameProjectFilter(BDDMockito.anyString(), BDDMockito.anyString());

		mockMvc.perform(
				MockMvcRequestBuilders.post("/projectfilter/name/rename").param("filter", "filter")
						.param("filterName", "filterName")
						.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.flash()
						.attributeExists(SynapseModelAndView.FAILURE_FLASH));
	}

	@Test
	@WithMockUser(authorities = SynapseAdminAuthDictionary.ADMIN_PRIV)
	public void setProjectsForFilterTestSuccess() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.post("/projectfilter/name/setprojects")
				.param("filter", "filter")
				.param("projectNames", "projectNames")
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(
						MockMvcResultMatchers.flash().attribute(SynapseModelAndView.FAILURE_FLASH,
								Matchers.nullValue()));
	}

	@Test
	@WithMockUser(authorities = SynapseAdminAuthDictionary.ADMIN_PRIV)
	public void setProjectsForFilterTestFail() throws Exception {
		BDDMockito.willThrow(new BadProductNameException("")).given(mockProductsService)
				.setProjectsForFilter(BDDMockito.any(), BDDMockito.anyString());

		mockMvc.perform(MockMvcRequestBuilders.post("/projectfilter/name/setprojects")
				.param("filter", "filter")
				.param("projectNames", "projectNames")
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.flash()
						.attributeExists(SynapseModelAndView.FAILURE_FLASH));
	}

	@Test
	@WithMockUser(authorities = SynapseAdminAuthDictionary.ADMIN_PRIV)
	public void removeProjectFromFilterTestSuccess() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.post("/projectfilter/name/removeproject")
				.param("filter", "filter")
				.param("projectName", "projectName")
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(
						MockMvcResultMatchers.flash().attribute(SynapseModelAndView.SUCCESS_FLASH,
								Matchers.containsString("removed")));
	}

	@Test
	@WithMockUser(authorities = SynapseAdminAuthDictionary.ADMIN_PRIV)
	public void removeProjectFromFilterTestFail() throws Exception {
		BDDMockito.willThrow(new BadProductNameException("")).given(mockProductsService)
				.removeProjectFromFilter(BDDMockito.any(), BDDMockito.anyString());

		mockMvc.perform(MockMvcRequestBuilders.post("/projectfilter/name/removeproject")
				.param("filter", "filter")
				.param("projectName", "projectName")
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.flash()
						.attributeExists(SynapseModelAndView.FAILURE_FLASH));
	}

	@Test
	@WithMockUser(authorities = SynapseAdminAuthDictionary.ADMIN_PRIV)
	public void deleteFilterTestSuccess() throws Exception {
		mockMvc.perform(
				MockMvcRequestBuilders.post("/projectfilter/name/delete").param("filter", "filter")
						.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(
						MockMvcResultMatchers.flash().attribute(SynapseModelAndView.FAILURE_FLASH,
								Matchers.nullValue()));
	}

	@Test
	@WithMockUser(authorities = SynapseAdminAuthDictionary.ADMIN_PRIV)
	public void deleteFilterTestFail() throws Exception {
		BDDMockito.willThrow(new ProductsNotFoundException("")).given(mockProductsService)
				.deleteProjectFilter(BDDMockito.anyString());

		mockMvc.perform(
				MockMvcRequestBuilders.post("/projectfilter/name/delete").param("filter", "filter")
						.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.flash()
						.attributeExists(SynapseModelAndView.FAILURE_FLASH));
	}
}
