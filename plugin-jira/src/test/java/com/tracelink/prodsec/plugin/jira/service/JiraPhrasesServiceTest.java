package com.tracelink.prodsec.plugin.jira.service;

import com.tracelink.prodsec.plugin.jira.exception.JiraPhrasesException;
import com.tracelink.prodsec.plugin.jira.model.JiraPhraseDataFormat;
import com.tracelink.prodsec.plugin.jira.model.JiraPhrases;
import com.tracelink.prodsec.plugin.jira.repo.JiraSearchPhrasesRepo;
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
public class JiraPhrasesServiceTest {

	@MockBean
	private JiraSearchPhrasesRepo searchPhrasesRepo;

	private JiraPhrasesService jiraPhrasesService;

	@Before
	public void setup() {
		jiraPhrasesService = new JiraPhrasesService(searchPhrasesRepo);
	}

	@Test(expected = JiraPhrasesException.class)
	public void testGetPhrasesEmpty() {
		BDDMockito.when(searchPhrasesRepo.findAll()).thenReturn(Collections.emptyList());
		jiraPhrasesService.getPhrases();
	}

	@Test
	public void testGetPhrases() {
		JiraPhrases phrase = new JiraPhrases();
		phrase.setDataFormat(JiraPhraseDataFormat.SCRUM);
		BDDMockito.when(searchPhrasesRepo.findAll()).thenReturn(Collections.singletonList(phrase));
		Assert.assertEquals(phrase, jiraPhrasesService.getPhrases().get(0));
		Assert.assertEquals(1, jiraPhrasesService.getPhrases().size());
	}

	@Test
	public void testSetPhrasesEmpty() {
		String jql = "project = \"AS\" AND type = Story AND status = %s";
		BDDMockito.when(searchPhrasesRepo.findAll()).thenReturn(Collections.emptyList());
		jiraPhrasesService.setPhraseForDataFormat(jql, JiraPhraseDataFormat.SCRUM);

		ArgumentCaptor<JiraPhrases> captor = ArgumentCaptor.forClass(JiraPhrases.class);
		BDDMockito.verify(searchPhrasesRepo, Mockito.times(1)).saveAndFlush(captor.capture());

		JiraPhrases phraseValue = captor.getValue();
		Assert.assertEquals("Scrum", phraseValue.getDataFormat().getDisplayName());
		Assert.assertEquals(jql, phraseValue.getJQL());
	}

	@Test
	public void testSetAllowedSla() {
		String jql = "project = \"AS\" AND type = Story AND status = %s";
		String newJql = "type = Story AND status = %s";
		JiraPhrases phrase = new JiraPhrases();
		phrase.setDataFormat(JiraPhraseDataFormat.SCRUM);
		phrase.setJQL(jql);

		BDDMockito.when(searchPhrasesRepo.findByDataFormat(JiraPhraseDataFormat.SCRUM))
				.thenReturn(Collections.singletonList(phrase));
		BDDMockito.when(searchPhrasesRepo.saveAndFlush(BDDMockito.any()))
				.thenAnswer(e -> e.getArgument(0));

		jiraPhrasesService.setPhraseForDataFormat(newJql, JiraPhraseDataFormat.SCRUM);
		BDDMockito.verify(searchPhrasesRepo, Mockito.times(1)).saveAndFlush(phrase);
		Assert.assertEquals("Scrum", phrase.getDataFormat().getDisplayName());
		Assert.assertEquals(newJql, phrase.getJQL());
	}

	@Test
	public void testGetPhraseForData() {
		JiraPhrases phrase = new JiraPhrases();
		phrase.setJQL("type = Story AND status = %s");

		BDDMockito.when(searchPhrasesRepo.findByDataFormat(JiraPhraseDataFormat.SCRUM))
				.thenReturn(Collections.singletonList(phrase));
		Assert.assertEquals(jiraPhrasesService.getPhraseForData(JiraPhraseDataFormat.SCRUM),
				phrase.getJQL());
	}
}
