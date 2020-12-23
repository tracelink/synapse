package com.tracelink.prodsec.plugin.sme.controllers;

import com.tracelink.prodsec.plugin.sme.SMEPlugin;
import com.tracelink.prodsec.plugin.sme.model.SMEEntity;
import com.tracelink.prodsec.plugin.sme.service.SMEException;
import com.tracelink.prodsec.plugin.sme.service.SMEService;
import com.tracelink.prodsec.synapse.auth.SynapseAdminAuthDictionary;
import com.tracelink.prodsec.synapse.mvc.SynapseModelAndView;
import com.tracelink.prodsec.synapse.products.model.ProductLineModel;
import com.tracelink.prodsec.synapse.products.service.ProductsService;
import com.tracelink.prodsec.synapse.test.TestSynapseBootApplication;
import java.util.ArrayList;
import java.util.List;
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

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestSynapseBootApplication.class)
@AutoConfigureMockMvc
public class SMEControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private SMEService mockSmeService;

	@MockBean
	private ProductsService mockProductsService;

	@Test
	@WithMockUser
	public void testHome() throws Exception {
		List<SMEEntity> entities = new ArrayList<>();
		List<ProductLineModel> productLines = new ArrayList<>();

		BDDMockito.when(mockSmeService.getAllSMEs()).thenReturn(entities);
		BDDMockito.when(mockProductsService.getAllProductLines()).thenReturn(productLines);

		mockMvc.perform(MockMvcRequestBuilders.get(SMEPlugin.PAGELINK))
				.andExpect(MockMvcResultMatchers.model().attribute("smes", Matchers.is(entities)))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("productLines", Matchers.is(productLines)))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("scripts", Matchers.hasItem("/scripts/sme.js")))
				.andExpect(MockMvcResultMatchers.model()
						.attribute(SynapseModelAndView.CONTENT_VIEW_NAME,
								Matchers.is("sme/sme")));
	}

	@Test
	@WithMockUser(authorities = {SynapseAdminAuthDictionary.ADMIN_PRIV})
	public void testCreateSMESuccess() throws Exception {
		String smeName = "Foo";
		mockMvc.perform(MockMvcRequestBuilders.post(SMEPlugin.PAGELINK + "/create")
				.param("smeName", smeName)
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.redirectedUrl(SMEPlugin.PAGELINK))
				.andExpect(MockMvcResultMatchers
						.flash().attribute(SynapseModelAndView.FAILURE_FLASH,
								Matchers.emptyOrNullString()));

		ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
		BDDMockito.verify(mockSmeService).addNewSME(captor.capture());
		Assert.assertEquals(smeName, captor.getValue());
	}

	@Test
	@WithMockUser(authorities = {SynapseAdminAuthDictionary.ADMIN_PRIV})
	public void testCreateSMEFail() throws Exception {
		String smeName = "Bar";
		BDDMockito.willThrow(new SMEException("")).given(mockSmeService)
				.addNewSME(BDDMockito.any());
		mockMvc.perform(MockMvcRequestBuilders.post(SMEPlugin.PAGELINK + "/create")
				.param("smeName", smeName)
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.redirectedUrl(SMEPlugin.PAGELINK))
				.andExpect(MockMvcResultMatchers.flash()
						.attributeExists(SynapseModelAndView.FAILURE_FLASH));

		ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
		BDDMockito.verify(mockSmeService).addNewSME(captor.capture());
		Assert.assertEquals(smeName, captor.getValue());
	}

	@Test
	@WithMockUser(authorities = {SynapseAdminAuthDictionary.ADMIN_PRIV})
	public void testSetProjectSuccess() throws Exception {
		String smeName = "Foo";
		mockMvc.perform(MockMvcRequestBuilders.post(SMEPlugin.PAGELINK + "/setProjects")
				.param("smeName", smeName)
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.redirectedUrl(SMEPlugin.PAGELINK))
				.andExpect(MockMvcResultMatchers
						.flash().attribute(SynapseModelAndView.FAILURE_FLASH,
								Matchers.emptyOrNullString()));

		ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
		BDDMockito.verify(mockSmeService).setProjectsForSME(captor.capture(), BDDMockito.any());
		Assert.assertEquals(smeName, captor.getValue());
	}

	@Test
	@WithMockUser(authorities = {SynapseAdminAuthDictionary.ADMIN_PRIV})
	public void testSetProjectFail() throws Exception {
		String smeName = "Bar";
		BDDMockito.willThrow(new SMEException("")).given(mockSmeService)
				.setProjectsForSME(BDDMockito.any(),
						BDDMockito.any());
		mockMvc.perform(MockMvcRequestBuilders.post(SMEPlugin.PAGELINK + "/setProjects")
				.param("smeName", smeName)
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.redirectedUrl(SMEPlugin.PAGELINK))
				.andExpect(MockMvcResultMatchers.flash()
						.attributeExists(SynapseModelAndView.FAILURE_FLASH));

		ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
		BDDMockito.verify(mockSmeService).setProjectsForSME(captor.capture(), BDDMockito.any());
		Assert.assertEquals(smeName, captor.getValue());
	}

	@Test
	@WithMockUser(authorities = {SynapseAdminAuthDictionary.ADMIN_PRIV})
	public void testRemoveProjectSuccess() throws Exception {
		String smeName = "Foo";
		mockMvc.perform(MockMvcRequestBuilders.post(SMEPlugin.PAGELINK + "/removeProject")
				.param("smeName", smeName)
				.param("projectName", "").with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.redirectedUrl(SMEPlugin.PAGELINK))
				.andExpect(MockMvcResultMatchers
						.flash().attribute(SynapseModelAndView.FAILURE_FLASH,
								Matchers.emptyOrNullString()));

		ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
		BDDMockito.verify(mockSmeService).removeProjectFromSME(captor.capture(), BDDMockito.any());
		Assert.assertEquals(smeName, captor.getValue());
	}

	@Test
	@WithMockUser(authorities = {SynapseAdminAuthDictionary.ADMIN_PRIV})
	public void testRemoveProjectFail() throws Exception {
		String smeName = "Bar";
		BDDMockito.willThrow(new SMEException("")).given(mockSmeService)
				.removeProjectFromSME(BDDMockito.any(),
						BDDMockito.any());
		mockMvc.perform(MockMvcRequestBuilders.post(SMEPlugin.PAGELINK + "/removeProject")
				.param("smeName", smeName)
				.param("projectName", "").with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.redirectedUrl(SMEPlugin.PAGELINK))
				.andExpect(MockMvcResultMatchers.flash()
						.attributeExists(SynapseModelAndView.FAILURE_FLASH));

		ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
		BDDMockito.verify(mockSmeService).removeProjectFromSME(captor.capture(), BDDMockito.any());
		Assert.assertEquals(smeName, captor.getValue());
	}
}
