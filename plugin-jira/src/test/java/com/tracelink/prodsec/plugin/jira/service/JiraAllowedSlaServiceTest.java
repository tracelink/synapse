package com.tracelink.prodsec.plugin.jira.service;

import com.tracelink.prodsec.plugin.jira.exception.JiraAllowedSlaException;
import com.tracelink.prodsec.plugin.jira.model.JiraAllowedSla;
import com.tracelink.prodsec.plugin.jira.repo.JiraAllowedSlaRepo;
import java.util.Collections;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class JiraAllowedSlaServiceTest {

	@MockBean
	private JiraAllowedSlaRepo allowedSlaRepo;

	private JiraAllowedSlaService allowedSlaService;

	@Before
	public void setup() {
		allowedSlaService = new JiraAllowedSlaService(allowedSlaRepo);
	}

	@Test(expected = JiraAllowedSlaException.class)
	public void testGetAllowedSlaEmpty() {
		BDDMockito.when(allowedSlaRepo.findAll()).thenReturn(Collections.emptyList());
		allowedSlaService.getAllAllowedSla();
	}

	@Test
	public void testGetAllowedSla() {
		JiraAllowedSla allowedSla = new JiraAllowedSla();
		allowedSla.setAllowedDays(10);
		allowedSla.setSeverity("High");
		BDDMockito.when(allowedSlaRepo.findAll()).thenReturn(Collections.singletonList(allowedSla));
		Assert.assertEquals(allowedSla, allowedSlaService.getAllAllowedSla().get(0));
		Assert.assertEquals(1, allowedSlaService.getAllAllowedSla().size());
	}

	@Test
	public void testSetAllowedSlaEmpty() {
		BDDMockito.when(allowedSlaRepo.findAll()).thenReturn(Collections.emptyList());
		allowedSlaService.setAllowedSla("Low", 100);

		ArgumentCaptor<JiraAllowedSla> captor = ArgumentCaptor.forClass(JiraAllowedSla.class);
		BDDMockito.verify(allowedSlaRepo, Mockito.times(1)).saveAndFlush(captor.capture());

		JiraAllowedSla slaValues = captor.getValue();
		Assert.assertEquals("Low", slaValues.getSeverity());
		Assert.assertEquals((Integer) 100, slaValues.getAllowedDays());
	}

	@Test
	public void testSetAllowedSla() {
		JiraAllowedSla allowedSla = new JiraAllowedSla();
		allowedSla.setSeverity("Unknown");
		allowedSla.setAllowedDays(100);
		BDDMockito.when(allowedSlaRepo.findAll()).thenReturn(Collections.singletonList(allowedSla));
		BDDMockito.when(allowedSlaRepo.saveAndFlush(BDDMockito.any()))
				.thenAnswer(e -> e.getArgument(0));

		allowedSlaService.setAllowedSla("Unknown", 50);
		BDDMockito.verify(allowedSlaRepo, Mockito.times(1)).saveAndFlush(allowedSla);
		Assert.assertEquals("Unknown", allowedSla.getSeverity());
		Assert.assertEquals((Integer) 50, allowedSla.getAllowedDays());
	}

	@Test
	public void testGetAllowedTimeBySev() {
		JiraAllowedSla allowedSla = new JiraAllowedSla();
		String severity = null;

		//Null check
		BDDMockito.when(allowedSlaRepo.findOneBySeverityEquals(severity)).thenReturn(null);
		allowedSlaService.getAllowedTimeBySev(severity);
		Assert.assertNull(allowedSla.getAllowedDays());

		//Valid Severity
		severity = "Critical";
		allowedSla.setSeverity(severity);
		allowedSla.setAllowedDays(1);

		BDDMockito.when(allowedSlaRepo.findOneBySeverityEquals(severity)).thenReturn(allowedSla);
		allowedSlaService.getAllowedTimeBySev(severity);

		Assert.assertEquals(severity, allowedSla.getSeverity());
		Assert.assertEquals((Integer) 1, allowedSla.getAllowedDays());
	}
}
