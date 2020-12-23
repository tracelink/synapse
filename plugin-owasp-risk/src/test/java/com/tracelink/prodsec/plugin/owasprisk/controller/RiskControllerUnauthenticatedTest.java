package com.tracelink.prodsec.plugin.owasprisk.controller;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.tracelink.prodsec.synapse.mvc.SynapsePublicRequestMatcherService;
import com.tracelink.prodsec.synapse.test.TestSynapseBootApplication;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestSynapseBootApplication.class)
@AutoConfigureMockMvc
public class RiskControllerUnauthenticatedTest {

	@TestConfiguration
	static class testConfig extends WebSecurityConfigurerAdapter {

		@Autowired
		private SynapsePublicRequestMatcherService requestService;

		@Override
		protected void configure(HttpSecurity http) throws Exception {
			http.authorizeRequests()
					// allow only things added to request service
					.requestMatchers(requestService).permitAll()
					// block all others
					.anyRequest().authenticated();
		}
	}

	@Autowired
	private MockMvc mockMvc;

	@Test
	public void testGetRiskRatingUnAuthenticated() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/risk_rating")).andExpect(MockMvcResultMatchers.status().is(200))
				.andExpect(MockMvcResultMatchers.model().attribute("scripts", Matchers.nullValue()))
				.andExpect(MockMvcResultMatchers.model().attribute("styles", Matchers.nullValue()))
				.andExpect(MockMvcResultMatchers.view().name("risk/risk_rating_public"));
	}

}
