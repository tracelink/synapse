package com.tracelink.prodsec.plugin.owasprisk.controller;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.tracelink.prodsec.synapse.mvc.SynapseModelAndView;
import com.tracelink.prodsec.synapse.test.TestSynapseBootApplication;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestSynapseBootApplication.class)
@AutoConfigureMockMvc
public class RiskControllerAuthenticatedTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	@WithMockUser
	public void testGetRiskRatingAuthenticated() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/risk_rating")).andExpect(MockMvcResultMatchers.status().is(200))
				.andExpect(MockMvcResultMatchers.model().attribute("scripts",
						Matchers.contains("/scripts/risk/riskrating.js")))
				.andExpect(MockMvcResultMatchers.model().attribute("styles",
						Matchers.contains("/styles/risk/riskrating.css")))
				.andExpect(MockMvcResultMatchers.model().attribute(SynapseModelAndView.CONTENT_VIEW_NAME,
						Matchers.is("risk/risk_rating")));
	}

}
