package com.tracelink.prodsec.plugin.veracode.dast.controller;

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

import com.tracelink.prodsec.plugin.veracode.dast.VeracodeDastPlugin;
import com.tracelink.prodsec.plugin.veracode.dast.model.VeracodeDastAppModel;
import com.tracelink.prodsec.plugin.veracode.dast.service.VeracodeDastAppService;
import com.tracelink.prodsec.plugin.veracode.dast.service.VeracodeDastReportService;
import com.tracelink.prodsec.synapse.products.model.ProductLineModel;
import com.tracelink.prodsec.synapse.products.model.ProjectModel;
import com.tracelink.prodsec.synapse.test.TestSynapseBootApplication;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestSynapseBootApplication.class)
@AutoConfigureMockMvc
public class VeracodeDastReportControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private VeracodeDastAppService mockAppService;

	@MockBean
	private VeracodeDastReportService mockReportService;

	@Test
	@WithMockUser(authorities = {VeracodeDastPlugin.FLAWS_VIEWER_PRIVILEGE})
	public void testGetFlaws() throws Exception {
		String plmName = "MyPLM";
		String app1Name = "App1";
		String app2Name = "App2";
		String app3Name = "App3";

		VeracodeDastAppModel app1 = new VeracodeDastAppModel();
		app1.setName(app1Name);
		VeracodeDastAppModel app2 = new VeracodeDastAppModel();
		app2.setName(app2Name);
		VeracodeDastAppModel app3 = new VeracodeDastAppModel();
		app3.setName(app3Name);

		ProjectModel app3Proj = new ProjectModel();
		ProductLineModel app3Plm = new ProductLineModel();
		app3Plm.setName(plmName);
		app3Proj.setOwningProductLine(app3Plm);
		app3.setSynapseProductLine(app3Plm);

		BDDMockito.when(mockAppService.getUnmappedApps()).thenReturn(Arrays.asList(app2, app1));
		BDDMockito.when(mockAppService.getMappedApps()).thenReturn(Arrays.asList(app3));
		mockMvc.perform(MockMvcRequestBuilders.get(VeracodeDastPlugin.FLAWS_PAGE))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("unmappedApps", Matchers.hasSize(2)))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("mappedApps", Matchers.hasSize(1)));
	}
}
