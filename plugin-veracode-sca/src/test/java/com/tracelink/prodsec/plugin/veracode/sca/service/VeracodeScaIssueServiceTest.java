package com.tracelink.prodsec.plugin.veracode.sca.service;

import static org.mockito.Mockito.times;

import com.tracelink.prodsec.plugin.veracode.sca.mock.VeracodeScaMocks;
import com.tracelink.prodsec.plugin.veracode.sca.model.VeracodeScaIssue;
import com.tracelink.prodsec.plugin.veracode.sca.model.VeracodeScaProject;
import com.tracelink.prodsec.plugin.veracode.sca.model.issue.IssueStatus;
import com.tracelink.prodsec.plugin.veracode.sca.model.issue.IssueType;
import com.tracelink.prodsec.plugin.veracode.sca.repository.VeracodeScaIssueRepository;
import com.tracelink.prodsec.plugin.veracode.sca.util.model.IssueSummary;
import com.tracelink.prodsec.plugin.veracode.sca.util.model.IssueSummary.IssueStatusEnum;
import com.tracelink.prodsec.plugin.veracode.sca.util.model.IssueSummary.IssueTypeEnum;
import com.tracelink.prodsec.plugin.veracode.sca.util.model.VulnerabilitySummary;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class VeracodeScaIssueServiceTest {

	@MockBean
	private VeracodeScaProjectService projectService;

	@MockBean
	private VeracodeScaIssueRepository issueRepository;

	private VeracodeScaIssueService issueService;
	private VeracodeScaProject project;
	private IssueSummary baseIssueSummary;


	@Before
	public void setup() {
		issueService = new VeracodeScaIssueService(projectService, issueRepository);

		project = VeracodeScaMocks.mockProject();

		baseIssueSummary = new IssueSummary();
		baseIssueSummary.setId(UUID.randomUUID());
		baseIssueSummary.setProjectId(project.getId());
		baseIssueSummary.setCreatedDate("2020-06-11T21:38:06.443+0000");
		baseIssueSummary.setProjectBranch("develop");
		baseIssueSummary.setSeverity(8.2f);
		baseIssueSummary.setIgnored(false);
		baseIssueSummary.setIssueStatus(IssueStatusEnum.OPEN);
	}

	@Test
	public void testGetEarliestIssueDate() {
		BDDMockito.when(issueRepository.findFirstByOrderByCreatedDateAsc()).thenReturn(null);
		Assert.assertEquals(LocalDate.now(), issueService.getEarliestIssueDate().toLocalDate());

		VeracodeScaIssue issue = new VeracodeScaIssue();
		issue.setCreatedDate(LocalDateTime.now().minusDays(1));
		BDDMockito.when(issueRepository.findFirstByOrderByCreatedDateAsc()).thenReturn(issue);

		LocalDateTime date = issueService.getEarliestIssueDate();
		Assert.assertTrue(date.isBefore(LocalDateTime.now()));

		Assert.assertEquals(date, issueService.getEarliestIssueDate());
	}

	@Test
	public void testUpdateIssuesNewLibraryIssue() {
		baseIssueSummary.setIssueType(IssueTypeEnum.LIBRARY);

		BDDMockito.when(issueRepository.findById(BDDMockito.any(UUID.class))).thenReturn(
				Optional.empty());
		BDDMockito.when(projectService.getProject(BDDMockito.any(UUID.class))).thenReturn(project);

		issueService.updateIssues(Collections.singletonList(baseIssueSummary));

		@SuppressWarnings("unchecked")
		ArgumentCaptor<Set<VeracodeScaIssue>> issuesCaptor = ArgumentCaptor.forClass(Set.class);
		BDDMockito.verify(issueRepository, times(1)).saveAll(issuesCaptor.capture());

		Assert.assertFalse(issuesCaptor.getValue().isEmpty());
		VeracodeScaIssue storedIssue = issuesCaptor.getValue().iterator().next();
		Assert.assertEquals(baseIssueSummary.getId(), storedIssue.getId());
		Assert.assertEquals(project, storedIssue.getProject());
		Assert.assertEquals("2020-06-11", storedIssue.getCreatedDate().format(
				DateTimeFormatter.ofPattern("yyyy-MM-dd")));
		Assert.assertEquals(IssueType.LIBRARY, storedIssue.getIssueType());
		Assert.assertEquals(baseIssueSummary.getProjectBranch(), storedIssue.getProjectBranch());
		Assert.assertEquals(baseIssueSummary.getSeverity(), storedIssue.getSeverity(), 0.001);
		Assert.assertEquals(baseIssueSummary.isIgnored(), storedIssue.isIgnored());
		Assert.assertEquals(IssueStatus.OPEN, storedIssue.getIssueStatus());
	}

	@Test
	public void testUpdateIssuesExistingLicenseIssueNewIgnored() {
		baseIssueSummary.setIssueType(IssueTypeEnum.LICENSE);
		baseIssueSummary.setIgnored(true);

		VeracodeScaIssue issueModel = VeracodeScaMocks.mockLicenseIssue();
		issueModel.setIgnored(false);

		BDDMockito.when(issueRepository.findById(BDDMockito.any(UUID.class))).thenReturn(
				Optional.of(issueModel));
		BDDMockito.when(projectService.getProject(BDDMockito.any(UUID.class))).thenReturn(project);

		issueService.updateIssues(Collections.singletonList(baseIssueSummary));

		@SuppressWarnings("unchecked")
		ArgumentCaptor<Set<VeracodeScaIssue>> issuesCaptor = ArgumentCaptor.forClass(Set.class);
		BDDMockito.verify(issueRepository, times(1)).saveAll(issuesCaptor.capture());

		Assert.assertFalse(issuesCaptor.getValue().isEmpty());
		VeracodeScaIssue storedIssue = issuesCaptor.getValue().iterator().next();
		Assert.assertEquals(issueModel, storedIssue);
		Assert.assertEquals(IssueType.LICENSE, issueModel.getIssueType());
		Assert.assertEquals(baseIssueSummary.isIgnored(), issueModel.isIgnored());
		Assert.assertEquals(LocalDate.now(), issueModel.getIgnoredDate().toLocalDate());
	}

	@Test
	public void testUpdateIssuesExistingLicenseIssueRevertIgnored() {
		baseIssueSummary.setIssueType(IssueTypeEnum.LICENSE);
		baseIssueSummary.setIgnored(false);

		VeracodeScaIssue issueModel = VeracodeScaMocks.mockLicenseIssue();
		issueModel.setIgnored(true);

		BDDMockito.when(issueRepository.findById(BDDMockito.any(UUID.class))).thenReturn(
				Optional.of(issueModel));
		BDDMockito.when(projectService.getProject(BDDMockito.any(UUID.class))).thenReturn(project);

		issueService.updateIssues(Collections.singletonList(baseIssueSummary));

		@SuppressWarnings("unchecked")
		ArgumentCaptor<Set<VeracodeScaIssue>> issuesCaptor = ArgumentCaptor.forClass(Set.class);
		BDDMockito.verify(issueRepository, times(1)).saveAll(issuesCaptor.capture());

		Assert.assertFalse(issuesCaptor.getValue().isEmpty());
		VeracodeScaIssue storedIssue = issuesCaptor.getValue().iterator().next();
		Assert.assertEquals(issueModel, storedIssue);
		Assert.assertEquals(IssueType.LICENSE, issueModel.getIssueType());
		Assert.assertEquals(baseIssueSummary.isIgnored(), issueModel.isIgnored());
		Assert.assertNull(issueModel.getIgnoredDate());
	}

	@Test
	public void testUpdateIssuesExistingVulnerabilityIssueNewFixed() {
		VulnerabilitySummary vulnerabilitySummary = new VulnerabilitySummary();
		vulnerabilitySummary.setTitle("RCE");

		baseIssueSummary.setIssueType(IssueTypeEnum.VULNERABILITY);
		baseIssueSummary.setVulnerability(vulnerabilitySummary);
		baseIssueSummary.setVulnerableMethod(true);
		baseIssueSummary.setIgnored(false);
		baseIssueSummary.setIssueStatus(IssueStatusEnum.FIXED);

		VeracodeScaIssue issueModel = VeracodeScaMocks.mockVulnerabilityIssue();
		issueModel.setIssueStatus(IssueStatus.OPEN);

		BDDMockito.when(issueRepository.findById(BDDMockito.any(UUID.class))).thenReturn(
				Optional.of(issueModel));
		BDDMockito.when(projectService.getProject(BDDMockito.any(UUID.class))).thenReturn(project);

		issueService.updateIssues(Collections.singletonList(baseIssueSummary));

		@SuppressWarnings("unchecked")
		ArgumentCaptor<Set<VeracodeScaIssue>> issuesCaptor = ArgumentCaptor.forClass(Set.class);
		BDDMockito.verify(issueRepository, times(1)).saveAll(issuesCaptor.capture());

		Assert.assertFalse(issuesCaptor.getValue().isEmpty());
		VeracodeScaIssue storedIssue = issuesCaptor.getValue().iterator().next();
		Assert.assertEquals(issueModel, storedIssue);
		Assert.assertEquals(IssueType.VULNERABILITY, issueModel.getIssueType());
		Assert.assertEquals(IssueStatus.FIXED, issueModel.getIssueStatus());
		Assert.assertEquals(LocalDate.now(), issueModel.getFixedDate().toLocalDate());
		Assert.assertEquals(vulnerabilitySummary.getTitle(), issueModel.getVulnerability());
		Assert.assertEquals(baseIssueSummary.isVulnerableMethod(), issueModel.isVulnerableMethod());
	}

	@Test
	public void testUpdateIssuesUnknownProject() {
		IssueSummary issueSummary = new IssueSummary();
		issueSummary.setId(UUID.randomUUID());
		issueSummary.setProjectId(project.getId());

		issueService.updateIssues(Collections.singletonList(issueSummary));

		@SuppressWarnings("unchecked")
		ArgumentCaptor<Set<VeracodeScaIssue>> issuesCaptor = ArgumentCaptor.forClass(Set.class);
		BDDMockito.verify(issueRepository, times(1)).saveAll(issuesCaptor.capture());

		Assert.assertTrue(issuesCaptor.getValue().isEmpty());
	}

	@Test
	public void testUpdateIssuesException() {
		baseIssueSummary.setIssueType(null);

		BDDMockito.when(projectService.getProject(BDDMockito.any(UUID.class))).thenReturn(project);

		issueService.updateIssues(Collections.singletonList(baseIssueSummary));

		@SuppressWarnings("unchecked")
		ArgumentCaptor<Set<VeracodeScaIssue>> issuesCaptor = ArgumentCaptor.forClass(Set.class);
		BDDMockito.verify(issueRepository, times(1)).saveAll(issuesCaptor.capture());

		Assert.assertTrue(issuesCaptor.getValue().isEmpty());
	}
}
