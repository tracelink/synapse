package com.tracelink.prodsec.plugin.bsimm.controller;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Paths;

import org.apache.commons.io.IOUtils;
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

import com.google.gson.JsonObject;
import com.tracelink.prodsec.plugin.bsimm.BSIMMPlugin;
import com.tracelink.prodsec.plugin.bsimm.service.BsimmResponseService;
import com.tracelink.prodsec.plugin.bsimm.service.SurveyException;
import com.tracelink.prodsec.synapse.auth.SynapseAdminAuthDictionary;
import com.tracelink.prodsec.synapse.test.TestSynapseBootApplication;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestSynapseBootApplication.class)
@AutoConfigureMockMvc
public class BSIMMSurveyRestControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private BsimmResponseService mockBsimmResponseService;

	@Test
	@WithMockUser(authorities = SynapseAdminAuthDictionary.ADMIN_PRIV)
	public void testDownloadModel() throws Exception {
		ByteArrayOutputStream baos;
		try (InputStream is = new FileInputStream(
				Paths.get(getClass().getResource("/xmlmodel/surveymodel.xml").toURI()).toFile())) {
			baos = new ByteArrayOutputStream();
			IOUtils.copy(is, baos);
		}

		mockMvc.perform(MockMvcRequestBuilders.post(BSIMMPlugin.PAGELINK + "/rest/downloadSurveyModel")
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.content().bytes(baos.toByteArray()));
	}

	@Test
	@WithMockUser
	public void testGetLatestResponse() throws Exception {
		JsonObject json = new JsonObject();
		json.addProperty("test", "test");
		BDDMockito.when(mockBsimmResponseService.generateResponsesAndComparisons(BDDMockito.any(), BDDMockito.any()))
				.thenReturn(json);

		mockMvc.perform(MockMvcRequestBuilders.get(BSIMMPlugin.PAGELINK + "/rest/response")
				.param("responses", "1").param("comparisons", "2"))
				.andExpect(MockMvcResultMatchers.content().json(json.toString()));
	}

	@Test
	@WithMockUser
	public void testGetLatestResponseFail() throws Exception {
		String message = "exceptionMessage";

		JsonObject json = new JsonObject();
		json.addProperty("error", message);

		SurveyException exception = new SurveyException(message);
		BDDMockito.given(mockBsimmResponseService.generateResponsesAndComparisons(BDDMockito.any(), BDDMockito.any()))
				.willThrow(exception);

		mockMvc.perform(MockMvcRequestBuilders.get(BSIMMPlugin.PAGELINK + "/rest/response")
				.param("responses", "1").param("comparisons", "2"))
				.andExpect(MockMvcResultMatchers.content().json(json.toString()));
	}

}
