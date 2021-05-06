package com.tracelink.prodsec.synapse.scheduler.controller;

import java.util.Collections;

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

import com.tracelink.prodsec.synapse.scheduler.model.JobDto;
import com.tracelink.prodsec.synapse.scheduler.service.SchedulerService;
import com.tracelink.prodsec.synapse.test.TestSynapseBootApplicationCore;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestSynapseBootApplicationCore.class)
@AutoConfigureMockMvc
public class JobDisplayControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private SchedulerService schedulerService;

	@Test
	@WithMockUser
	public void testJobDisplay() throws Exception {
		JobDto mockJob = BDDMockito.mock(JobDto.class);
		BDDMockito.when(schedulerService.getAllJobs()).thenReturn(Collections.singletonList(mockJob));

		mockMvc.perform(MockMvcRequestBuilders.get("/jobs"))
				.andExpect(MockMvcResultMatchers.model().attribute("jobs", Collections.singletonList(mockJob)));
	}
}
