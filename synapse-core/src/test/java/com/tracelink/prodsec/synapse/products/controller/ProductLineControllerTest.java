package com.tracelink.prodsec.synapse.products.controller;

import com.tracelink.prodsec.synapse.auth.SynapseAdminAuthDictionary;
import com.tracelink.prodsec.synapse.mvc.SynapseModelAndView;
import com.tracelink.prodsec.synapse.products.BadProductNameException;
import com.tracelink.prodsec.synapse.products.OrphanedException;
import com.tracelink.prodsec.synapse.products.ProductsNotFoundException;
import com.tracelink.prodsec.synapse.products.model.ProductLineModel;
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
public class ProductLineControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private ProductsService mockProductsService;

	@Test
	@WithMockUser
	public void productsHomeTest() throws Exception {
		List<ProductLineModel> products = new ArrayList<>();
		BDDMockito.when(mockProductsService.getAllProductLines()).thenReturn(products);
		mockMvc.perform(MockMvcRequestBuilders.get("/products"))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("productLines", Matchers.is(products)))
				.andExpect(MockMvcResultMatchers.model().attribute("scripts", Matchers.hasSize(1)));
	}

	@Test
	@WithMockUser
	public void productsLineViewTest() throws Exception {
		String productName = "productName";
		String projectName = "projectName";

		ProjectModel project = new ProjectModel();
		project.setName(projectName);

		List<ProjectModel> projects = new ArrayList<>();
		projects.add(project);

		ProductLineModel product = new ProductLineModel();
		product.setName(productName);
		product.setProjects(Arrays.asList(project));

		project.setOwningProductLine(product);

		List<ProductLineModel> products = new ArrayList<>();
		products.add(product);

		BDDMockito.when(mockProductsService.getAllProjects()).thenReturn(projects);
		BDDMockito.when(mockProductsService.getAllProductLines()).thenReturn(products);
		BDDMockito.when(mockProductsService.getProductLine(BDDMockito.anyString()))
				.thenReturn(product);

		mockMvc.perform(MockMvcRequestBuilders.get("/products/productLine"))
				.andExpect(
						MockMvcResultMatchers.model().attribute("projects", Matchers.is(projects)))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("productLines", Matchers.is(products)))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("productLine", Matchers.is(product)));
	}

	@Test
	@WithMockUser(authorities = SynapseAdminAuthDictionary.ADMIN_PRIV)
	public void createProductLineTestSuccess() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.post("/products/createproductline")
				.param("productLineName", "prodLine")
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(
						MockMvcResultMatchers.flash().attribute(SynapseModelAndView.FAILURE_FLASH,
								Matchers.nullValue()));
	}

	@Test
	@WithMockUser(authorities = SynapseAdminAuthDictionary.ADMIN_PRIV)
	public void createProductLineTestFail() throws Exception {
		BDDMockito.willThrow(new BadProductNameException("")).given(mockProductsService)
				.createProductLine(BDDMockito.anyString());

		mockMvc.perform(MockMvcRequestBuilders.post("/products/createproductline")
				.param("productLineName", "prodLine")
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.flash()
						.attributeExists(SynapseModelAndView.FAILURE_FLASH));
	}

	@Test
	@WithMockUser(authorities = SynapseAdminAuthDictionary.ADMIN_PRIV)
	public void createProjectTestSuccess() throws Exception {
		mockMvc.perform(
				MockMvcRequestBuilders.post("/products/projectName/createproject")
						.param("productLine", "prodLine")
						.param("projectName", "projLine")
						.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(
						MockMvcResultMatchers.flash().attribute(SynapseModelAndView.FAILURE_FLASH,
								Matchers.nullValue()));
	}

	@Test
	@WithMockUser(authorities = SynapseAdminAuthDictionary.ADMIN_PRIV)
	public void createProjectTestFail() throws Exception {
		BDDMockito.willThrow(new BadProductNameException("")).given(mockProductsService)
				.createProject(BDDMockito.anyString(), BDDMockito.anyString());

		mockMvc.perform(
				MockMvcRequestBuilders.post("/products/projectName/createproject")
						.param("productLine", "prodLine")
						.param("projectName", "projLine")
						.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.flash()
						.attributeExists(SynapseModelAndView.FAILURE_FLASH));
	}

	@Test
	@WithMockUser(authorities = SynapseAdminAuthDictionary.ADMIN_PRIV)
	public void renameProductLineTestSuccess() throws Exception {
		mockMvc.perform(
				MockMvcRequestBuilders.post("/products/projectName/renameproductline")
						.param("productLine", "prodLine")
						.param("productLineName", "prodLineNew")
						.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(
						MockMvcResultMatchers.flash().attribute(SynapseModelAndView.SUCCESS_FLASH,
								Matchers.containsString("renamed")));
	}

	@Test
	@WithMockUser(authorities = SynapseAdminAuthDictionary.ADMIN_PRIV)
	public void renameProductLineTestFail() throws Exception {
		BDDMockito.willThrow(new BadProductNameException("")).given(mockProductsService)
				.renameProductLine(BDDMockito.anyString(), BDDMockito.anyString());

		mockMvc.perform(
				MockMvcRequestBuilders.post("/products/projectName/renameproductline")
						.param("productLine", "prodLine")
						.param("productLineName", "prodLineNew")
						.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.flash()
						.attributeExists(SynapseModelAndView.FAILURE_FLASH));
	}

	@Test
	@WithMockUser(authorities = SynapseAdminAuthDictionary.ADMIN_PRIV)
	public void renameProjectTestSuccess() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.post("/products/projectName/renameproject")
				.param("productLine", "productLine").param("oldProjectName", "oldProjectName")
				.param("projectName", "projectName")
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(
						MockMvcResultMatchers.flash().attribute(SynapseModelAndView.SUCCESS_FLASH,
								Matchers.containsString("renamed")));
	}

	@Test
	@WithMockUser(authorities = SynapseAdminAuthDictionary.ADMIN_PRIV)
	public void renameProjectTestFail() throws Exception {
		BDDMockito.willThrow(new BadProductNameException("")).given(mockProductsService)
				.renameProject(BDDMockito.anyString(), BDDMockito.anyString());

		mockMvc.perform(MockMvcRequestBuilders.post("/products/productName/renameproject")
				.param("productLine", "productLine").param("oldProjectName", "oldProjectName")
				.param("projectName", "projectName")
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.flash()
						.attributeExists(SynapseModelAndView.FAILURE_FLASH));
	}

	@Test
	@WithMockUser(authorities = SynapseAdminAuthDictionary.ADMIN_PRIV)
	public void moveProjectTestSuccess() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.post("/products/productName/moveproject")
				.param("productLine", "productLine").param("productLineName", "productLineName")
				.param("projectName", "projectName")
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(
						MockMvcResultMatchers.flash().attribute(SynapseModelAndView.SUCCESS_FLASH,
								Matchers.containsString("successfully moved")));
	}

	@Test
	@WithMockUser(authorities = SynapseAdminAuthDictionary.ADMIN_PRIV)
	public void moveProjectTestFail() throws Exception {
		BDDMockito.willThrow(new BadProductNameException("")).given(mockProductsService)
				.moveProject(BDDMockito.anyString(), BDDMockito.anyString());

		mockMvc.perform(MockMvcRequestBuilders.post("/products/projectName/moveproject")
				.param("productLine", "productLine").param("productLineName", "productLineName")
				.param("projectName", "projectName")
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.flash()
						.attributeExists(SynapseModelAndView.FAILURE_FLASH));
	}

	@Test
	@WithMockUser(authorities = SynapseAdminAuthDictionary.ADMIN_PRIV)
	public void deleteProductLineTestSuccess() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.post("/products/productName/deleteproductline")
				.param("productLine", "productLine")
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(
						MockMvcResultMatchers.flash().attribute(SynapseModelAndView.SUCCESS_FLASH,
								Matchers.containsString("deleted")));
	}

	@Test
	@WithMockUser(authorities = SynapseAdminAuthDictionary.ADMIN_PRIV)
	public void deleteProductLineTestFail() throws Exception {
		BDDMockito.willThrow(new OrphanedException("")).given(mockProductsService)
				.deleteProductLine(BDDMockito.anyString());

		mockMvc.perform(MockMvcRequestBuilders.post("/products/productName/deleteproductline")
				.param("productLine", "productLine")
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.flash()
						.attributeExists(SynapseModelAndView.FAILURE_FLASH));
	}

	@Test
	@WithMockUser(authorities = SynapseAdminAuthDictionary.ADMIN_PRIV)
	public void deleteProjectTestSuccess() throws Exception {
		mockMvc.perform(
				MockMvcRequestBuilders.post("/products/productName/deleteproject")
						.param("productLine", "productLine")
						.param("projectName", "projectName")
						.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(
						MockMvcResultMatchers.flash().attribute(SynapseModelAndView.FAILURE_FLASH,
								Matchers.nullValue()));
	}

	@Test
	@WithMockUser(authorities = SynapseAdminAuthDictionary.ADMIN_PRIV)
	public void deleteProjectTestFail() throws Exception {
		BDDMockito.willThrow(new ProductsNotFoundException("")).given(mockProductsService)
				.deleteProject(BDDMockito.anyString());

		mockMvc.perform(
				MockMvcRequestBuilders.post("/products/productName/deleteproject")
						.param("productLine", "productLine")
						.param("projectName", "projectName")
						.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.flash()
						.attributeExists(SynapseModelAndView.FAILURE_FLASH));
	}
}
