package com.tracelink.prodsec.plugin.demo.controller;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
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

import com.tracelink.prodsec.plugin.demo.DemoPlugin;
import com.tracelink.prodsec.plugin.demo.model.DemoListModel;
import com.tracelink.prodsec.plugin.demo.service.DemoService;
import com.tracelink.prodsec.synapse.mvc.SynapseModelAndView;
import com.tracelink.prodsec.synapse.products.model.ProjectModel;
import com.tracelink.prodsec.synapse.products.service.ProductsService;
import com.tracelink.prodsec.synapse.test.TestSynapseBootApplication;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestSynapseBootApplication.class)
@AutoConfigureMockMvc
public class DemoControllerTest {
	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private DemoService mockDemoService;

	@MockBean
	private ProductsService mockProductsService;

	@Test
	@WithMockUser(authorities = { DemoPlugin.PRIV })
	public void testHome() throws Exception {
		DemoListModel dlm = new DemoListModel();
		BDDMockito.when(mockDemoService.getFullDemoList()).thenReturn(dlm);

		mockMvc.perform(MockMvcRequestBuilders.get(DemoPlugin.PAGELINK))
				.andExpect(MockMvcResultMatchers.model().attribute(SynapseModelAndView.CONTENT_VIEW_NAME,
						Matchers.is("demo/home")))
				.andExpect(MockMvcResultMatchers.model().attribute("demoProjects", Matchers.is(dlm)));
	}

	@Test
	@WithMockUser(authorities = { DemoPlugin.PRIV })
	public void testAssignVulnsSuccess() throws Exception {
		ProjectModel project = new ProjectModel();
		Integer vulns = 2;
		BDDMockito.when(mockProductsService.getProject(BDDMockito.anyString())).thenReturn(project);

		mockMvc.perform(MockMvcRequestBuilders.post(DemoPlugin.PAGELINK).param("projectName", "foo")
				.param("vulns", String.valueOf(vulns)).with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.flash().attribute(SynapseModelAndView.SUCCESS_FLASH,
						Matchers.containsString("Successfully")));

		ArgumentCaptor<ProjectModel> projectCaptor = ArgumentCaptor.forClass(ProjectModel.class);
		ArgumentCaptor<Integer> vulnCaptor = ArgumentCaptor.forClass(Integer.class);
		BDDMockito.verify(mockDemoService).assignVulnsToProject(projectCaptor.capture(), vulnCaptor.capture());

		Assert.assertEquals(project, projectCaptor.getValue());
		Assert.assertEquals(vulns, vulnCaptor.getValue());
	}

	@Test
	@WithMockUser(authorities = { DemoPlugin.PRIV })
	public void testAssignVulnsFail() throws Exception {
		Integer vulns = 2;
		BDDMockito.when(mockProductsService.getProject(BDDMockito.anyString())).thenReturn(null);

		mockMvc.perform(MockMvcRequestBuilders.post(DemoPlugin.PAGELINK).param("projectName", "foo")
				.param("vulns", String.valueOf(vulns)).with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.flash().attribute(SynapseModelAndView.FAILURE_FLASH,
						Matchers.is("Could not find that project")));
	}
}
