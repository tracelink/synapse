package com.tracelink.prodsec.plugin.veracode.sast.controller;

import java.util.Arrays;
import java.util.Optional;

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

import com.tracelink.prodsec.plugin.veracode.sast.VeracodeSastPlugin;
import com.tracelink.prodsec.plugin.veracode.sast.model.VeracodeSastAppModel;
import com.tracelink.prodsec.plugin.veracode.sast.model.VeracodeSastReportModel;
import com.tracelink.prodsec.plugin.veracode.sast.service.VeracodeSastAppService;
import com.tracelink.prodsec.plugin.veracode.sast.service.VeracodeSastReportService;
import com.tracelink.prodsec.synapse.mvc.SynapseModelAndView;
import com.tracelink.prodsec.synapse.products.model.ProductLineModel;
import com.tracelink.prodsec.synapse.products.model.ProjectModel;
import com.tracelink.prodsec.synapse.test.TestSynapseBootApplication;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestSynapseBootApplication.class)
@AutoConfigureMockMvc
public class VeracodeSastFlawControllerTest {
	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private VeracodeSastAppService mockAppService;

	@MockBean
	private VeracodeSastReportService mockReportService;

	@Test
	@WithMockUser(authorities = {VeracodeSastPlugin.FLAWS_VIEWER_PRIVILEGE})
	public void testGetFlaws() throws Exception {
		String plmName = "MyPLM";
		String app1Name = "App1";
		String app2Name = "App2";
		String app3Name = "App3";

		VeracodeSastAppModel app1 = new VeracodeSastAppModel();
		app1.setName(app1Name);
		VeracodeSastAppModel app2 = new VeracodeSastAppModel();
		app2.setName(app2Name);
		VeracodeSastAppModel app3 = new VeracodeSastAppModel();
		app3.setName(app3Name);

		ProjectModel app3Proj = new ProjectModel();
		app3Proj.setName(app3Name);
		ProductLineModel app3Plm = new ProductLineModel();
		app3Plm.setName(plmName);
		app3Proj.setOwningProductLine(app3Plm);
		app3.setSynapseProject(app3Proj);

		BDDMockito.when(mockAppService.getUnmappedApps()).thenReturn(Arrays.asList(app2, app1));
		BDDMockito.when(mockAppService.getMappedApps()).thenReturn(Arrays.asList(app3));
		mockMvc.perform(MockMvcRequestBuilders.get(VeracodeSastPlugin.FLAWS_PAGE))
				.andExpect(MockMvcResultMatchers.model().attribute("unmappedProjects", Matchers.hasSize(2)))
				.andExpect(MockMvcResultMatchers.model().attribute("mappedProjects", Matchers.hasSize(1)));
	}
}
