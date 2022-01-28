package com.tracelink.prodsec.plugin.veracode.sca.service;

import static org.mockito.Mockito.times;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit4.SpringRunner;

import com.tracelink.prodsec.lib.veracode.rest.api.model.Project;
import com.tracelink.prodsec.plugin.veracode.sca.exception.VeracodeScaProductException;
import com.tracelink.prodsec.plugin.veracode.sca.mock.VeracodeScaMocks;
import com.tracelink.prodsec.plugin.veracode.sca.model.VeracodeScaIssue;
import com.tracelink.prodsec.plugin.veracode.sca.model.VeracodeScaProject;
import com.tracelink.prodsec.plugin.veracode.sca.model.VeracodeScaWorkspace;
import com.tracelink.prodsec.plugin.veracode.sca.model.issue.IssueStatus;
import com.tracelink.prodsec.plugin.veracode.sca.repository.VeracodeScaProjectRepository;
import com.tracelink.prodsec.synapse.products.model.ProductLineModel;
import com.tracelink.prodsec.synapse.products.model.ProjectModel;

@RunWith(SpringRunner.class)
public class VeracodeScaProjectServiceTest {

	@MockBean
	private VeracodeScaProjectRepository projectRepository;

	@MockBean
	private VeracodeScaIssueService issueService;

	private VeracodeScaProjectService projectService;
	private VeracodeScaWorkspace workspace;
	private VeracodeScaProject project;
	private VeracodeScaIssue issue;
	private Project baseApiProject;
	private UUID uuid;

	@Before
	public void setup() {
		projectService = new VeracodeScaProjectService(projectRepository, issueService);
		project = VeracodeScaMocks.mockProject();

		issue = VeracodeScaMocks.mockVulnerabilityIssue();

		baseApiProject = new Project();
		baseApiProject.setId(UUID.randomUUID());
		baseApiProject.setName("API Project");
		baseApiProject.setLastScanDate("2020-06-11T21:38:06.443+0000");
		baseApiProject.setSiteId("ZYXabc");

		workspace = new VeracodeScaWorkspace();
		workspace.setName("Mock Workspace");
		project.setSiteId("ABCzyx");
		project.setWorkspace(workspace);

		uuid = UUID.randomUUID();
	}

	@Test
	public void testGetProjects() {
		BDDMockito.when(projectRepository.findAll())
				.thenReturn(Collections.singletonList(project));

		List<VeracodeScaProject> returnedProjects = projectService.getProjects();
		Assert.assertEquals(1, returnedProjects.size());
		Assert.assertTrue(returnedProjects.contains(project));
	}

	@Test
	public void testGetProjectsIdsList() {
		BDDMockito.when(projectRepository.findByIdIn(BDDMockito.anyList()))
				.thenReturn(Collections.singletonList(project));

		List<VeracodeScaProject> returnedProjects = projectService
				.getProjects(Collections.singletonList(uuid));
		Assert.assertEquals(1, returnedProjects.size());
		Assert.assertTrue(returnedProjects.contains(project));
	}

	@Test
	public void testGetIncludedProjects() {
		BDDMockito.when(projectRepository.findAll()).thenReturn(Collections.emptyList());
		List<VeracodeScaProject> returnedProjects = projectService.getIncludedProjects();
		Assert.assertTrue(returnedProjects.isEmpty());

		BDDMockito.when(projectRepository.findAll())
				.thenReturn(Collections.singletonList(project));

		returnedProjects = projectService.getIncludedProjects();
		Assert.assertEquals(1, returnedProjects.size());
		Assert.assertTrue(returnedProjects.contains(project));

		project.setIncluded(false);
		returnedProjects = projectService.getIncludedProjects();
		Assert.assertEquals(0, returnedProjects.size());
		Assert.assertTrue(returnedProjects.isEmpty());
	}

	@Test
	public void testGetMappedProjects() {
		BDDMockito.when(projectRepository.findAllBySynapseProjectNotNull())
				.thenReturn(Collections.emptyList());

		List<VeracodeScaProject> returnedProjects = projectService.getMappedProjects();
		Assert.assertTrue(returnedProjects.isEmpty());

		BDDMockito.when(projectRepository.findAllBySynapseProjectNotNull())
				.thenReturn(Collections.singletonList(project));

		returnedProjects = projectService.getMappedProjects();
		Assert.assertEquals(1, returnedProjects.size());
		Assert.assertTrue(returnedProjects.contains(project));

		project.setIncluded(false);
		returnedProjects = projectService.getMappedProjects();
		Assert.assertTrue(returnedProjects.isEmpty());
	}

	@Test
	public void testGetUnmappedProjects() {
		BDDMockito.when(projectRepository.findAllBySynapseProjectIsNull())
				.thenReturn(Collections.emptyList());

		List<VeracodeScaProject> returnedProjects = projectService.getUnmappedProjects();
		Assert.assertTrue(returnedProjects.isEmpty());

		BDDMockito.when(projectRepository.findAllBySynapseProjectIsNull())
				.thenReturn(Collections.singletonList(project));

		returnedProjects = projectService.getUnmappedProjects();
		Assert.assertEquals(1, returnedProjects.size());
		Assert.assertTrue(returnedProjects.contains(project));

		project.setIncluded(false);
		returnedProjects = projectService.getUnmappedProjects();
		Assert.assertEquals(0, returnedProjects.size());
		Assert.assertTrue(returnedProjects.isEmpty());
	}

	@Test
	public void testGetProject() {
		BDDMockito.when(projectRepository.findById(BDDMockito.any(UUID.class)))
				.thenReturn(Optional.empty());
		VeracodeScaProject returnedProject = projectService.getProject(UUID.randomUUID());
		Assert.assertNull(returnedProject);

		BDDMockito.when(projectRepository.findById(BDDMockito.any(UUID.class)))
				.thenReturn(Optional.of(project));
		returnedProject = projectService.getProject(UUID.randomUUID());
		Assert.assertEquals(project, returnedProject);
	}

	@Test
	public void testUpdateProjectsNewProjectVisibleMain() {

		BDDMockito.when(projectRepository.findById(BDDMockito.any(UUID.class))).thenReturn(
				Optional.empty());

		projectService.updateProjects(Collections.singletonList(baseApiProject), workspace,
				Collections.singletonMap(baseApiProject.getId(), "main"));

		@SuppressWarnings("unchecked")
		ArgumentCaptor<Collection<VeracodeScaProject>> projectCaptor = ArgumentCaptor
				.forClass(Collection.class);
		BDDMockito.verify(projectRepository, times(1)).saveAll(projectCaptor.capture());

		Assert.assertFalse(projectCaptor.getValue().isEmpty());
		VeracodeScaProject storedProject = projectCaptor.getValue().iterator().next();
		Assert.assertEquals(baseApiProject.getId(), storedProject.getId());
		Assert.assertEquals(baseApiProject.getName(), storedProject.getName());
		Assert.assertEquals(baseApiProject.getSiteId(), storedProject.getSiteId());
		Assert.assertEquals("2020-06-11", storedProject.getLastScanDate().format(
				DateTimeFormatter.ofPattern("yyyy-MM-dd")));
		Assert.assertEquals("main", storedProject.getVisibleBranch());
		Assert.assertEquals(workspace, storedProject.getWorkspace());
	}

	@Test
	public void testUpdateProjectsNewProjectVisibleNull() {
		baseApiProject.setBranches(Collections.singletonList("master"));

		BDDMockito.when(projectRepository.findById(BDDMockito.any(UUID.class))).thenReturn(
				Optional.empty());

		projectService.updateProjects(Collections.singletonList(baseApiProject), workspace,
				Collections.emptyMap());

		@SuppressWarnings("unchecked")
		ArgumentCaptor<Collection<VeracodeScaProject>> projectCaptor = ArgumentCaptor
				.forClass(Collection.class);
		BDDMockito.verify(projectRepository, times(1)).saveAll(projectCaptor.capture());

		Assert.assertFalse(projectCaptor.getValue().isEmpty());
		VeracodeScaProject storedProject = projectCaptor.getValue().iterator().next();
		Assert.assertNull("", storedProject.getVisibleBranch());
	}

	@Test
	public void testUpdateProjectsExistingProject() {
		BDDMockito.when(projectRepository.findById(BDDMockito.any(UUID.class))).thenReturn(
				Optional.of(project));

		projectService.updateProjects(Collections.singletonList(baseApiProject), workspace,
				Collections.singletonMap(baseApiProject.getId(), "main"));

		@SuppressWarnings("unchecked")
		ArgumentCaptor<Collection<VeracodeScaProject>> projectCaptor = ArgumentCaptor
				.forClass(Collection.class);
		BDDMockito.verify(projectRepository, times(1)).saveAll(projectCaptor.capture());

		Assert.assertFalse(projectCaptor.getValue().isEmpty());
		VeracodeScaProject storedProject = projectCaptor.getValue().iterator().next();
		Assert.assertEquals(project, storedProject);
		Assert.assertEquals("main", project.getVisibleBranch());
	}

	@Test
	public void testGetUnresolvedIssuesForProductLineNull() {
		// Case where there are no projects in a product line
		ProductLineModel productLine = new ProductLineModel();
		productLine.setProjects(Collections.emptyList());

		Assert.assertNull(projectService.getUnresolvedIssuesForProductLine(productLine));

		// Case where all projects in a product line are unmapped
		ProjectModel project = new ProjectModel();
		project.setName("project");
		productLine.setProjects(Collections.singletonList(project));

		BDDMockito.when(projectRepository.findBySynapseProject(project))
				.thenReturn(Collections.emptyList());
		Assert.assertNull(projectService.getUnresolvedIssuesForProductLine(productLine));
	}

	@Test
	public void testGetUnresolvedIssuesForProductLineEmpty() {
		ProjectModel synapseProject = new ProjectModel();
		synapseProject.setName("project");

		ProductLineModel productLine = new ProductLineModel();
		productLine.setProjects(Collections.singletonList(synapseProject));

		BDDMockito.when(projectRepository.findBySynapseProject(synapseProject))
				.thenReturn(Collections.singletonList(project));

		// Case where project has no issues
		project.setIssues(Collections.emptyList());
		Assert.assertTrue(projectService.getUnresolvedIssuesForProductLine(productLine).isEmpty());

		// Case where issues are fixed
		issue.setIssueStatus(IssueStatus.FIXED);
		project.setIssues(Collections.singletonList(issue));

		Assert.assertTrue(projectService.getUnresolvedIssuesForProductLine(productLine).isEmpty());
	}

	@Test
	public void testGetUnresolvedIssuesForProductLineNotEmpty() {
		ProjectModel synapseProject = new ProjectModel();
		synapseProject.setName("project");

		ProductLineModel productLine = new ProductLineModel();
		productLine.setProjects(Collections.singletonList(synapseProject));

		BDDMockito.when(projectRepository.findBySynapseProject(synapseProject))
				.thenReturn(Collections.singletonList(project));

		// Case where there is a single mapped project
		project.setIssues(Collections.singletonList(issue));
		List<VeracodeScaIssue> returnedIssues = projectService
				.getUnresolvedIssuesForProductLine(productLine);
		Assert.assertEquals(1, returnedIssues.size());
		Assert.assertTrue(returnedIssues.contains(issue));

		// Case where there are two mapped projects
		ProjectModel synapseProject2 = new ProjectModel();
		synapseProject2.setName("synapseProject2");

		productLine.setProjects(Arrays.asList(synapseProject, synapseProject2));

		BDDMockito.when(projectRepository.findBySynapseProject(synapseProject2))
				.thenReturn(Collections.singletonList(project));

		returnedIssues = projectService.getUnresolvedIssuesForProductLine(productLine);
		Assert.assertEquals(2, returnedIssues.size());
		Assert.assertTrue(returnedIssues.contains(issue));
	}

	@Test
	public void testGetUnresolvedIssuesForProjectNull() {
		ProjectModel project = new ProjectModel();
		project.setName("project");

		BDDMockito.when(projectRepository.findBySynapseProject(project))
				.thenReturn(Collections.emptyList());
		Assert.assertNull(projectService.getUnresolvedIssuesForProject(project));
	}

	@Test
	public void testGetUnresolvedIssuesForProjectEmpty() {
		ProjectModel synapseProject = new ProjectModel();
		synapseProject.setName("synapseProject");

		BDDMockito.when(projectRepository.findBySynapseProject(synapseProject))
				.thenReturn(Collections.singletonList(project));

		// Case where project has no issues
		project.setIssues(Collections.emptyList());
		Assert.assertTrue(projectService.getUnresolvedIssuesForProject(synapseProject).isEmpty());

		// Case where issues are fixed
		issue.setIssueStatus(IssueStatus.FIXED);
		project.setIssues(Collections.singletonList(issue));

		Assert.assertTrue(projectService.getUnresolvedIssuesForProject(synapseProject).isEmpty());
	}

	@Test
	public void testGetUnresolvedIssuesForProjectNotEmpty() {
		ProjectModel synapseProject = new ProjectModel();
		synapseProject.setName("synapseProject");

		BDDMockito.when(projectRepository.findBySynapseProject(synapseProject))
				.thenReturn(Collections.singletonList(project));

		// Case where there is a single issue
		project.setIssues(Collections.singletonList(issue));
		List<VeracodeScaIssue> returnedIssue = projectService
				.getUnresolvedIssuesForProject(synapseProject);
		Assert.assertEquals(1, returnedIssue.size());
		Assert.assertTrue(returnedIssue.contains(issue));

		// Case where there are two issues
		VeracodeScaIssue issue2 = VeracodeScaMocks.mockVulnerabilityIssue();
		issue2.setIgnored(true);

		project.setIssues(Arrays.asList(issue2, issue));

		returnedIssue = projectService.getUnresolvedIssuesForProject(synapseProject);
		Assert.assertEquals(1, returnedIssue.size());
		Assert.assertFalse(returnedIssue.contains(issue2));
		Assert.assertTrue(returnedIssue.contains(issue));
	}

	@Test
	public void testCreateMappingNull() {
		projectService.createMapping(null, project.getName());
		BDDMockito.verify(projectRepository, times(0)).saveAndFlush(BDDMockito.any());

		projectService.createMapping(new ProjectModel(), null);
		BDDMockito.verify(projectRepository, times(0)).saveAndFlush(BDDMockito.any());

		projectService.createMapping(null, null);
		BDDMockito.verify(projectRepository, times(0)).saveAndFlush(BDDMockito.any());
	}

	@Test
	public void testCreateMapping() {
		ProjectModel synapseProject = new ProjectModel();
		synapseProject.setName("synapseProject");

		BDDMockito.when(projectRepository.findByName(BDDMockito.anyString())).thenReturn(project);
		projectService.createMapping(synapseProject, project.getName());
		BDDMockito.verify(projectRepository, times(1)).saveAndFlush(BDDMockito.any());
		Assert.assertEquals(synapseProject, project.getSynapseProject());
	}

	@Test
	public void testDeleteMappingNull() {
		projectService.deleteMapping(null);
		BDDMockito.verify(projectRepository, times(0)).saveAndFlush(BDDMockito.any());
	}

	@Test
	public void testDeleteMapping() {
		BDDMockito.when(projectRepository.findByName(BDDMockito.anyString())).thenReturn(project);
		projectService.deleteMapping(project.getName());
		BDDMockito.verify(projectRepository, times(1)).saveAndFlush(BDDMockito.any());
		Assert.assertNull(project.getSynapseProject());
	}

	@Test
	public void testSetIncludedProjectsNull() {
		try {
			projectService.setIncluded(null);
			Assert.fail("Exception should have been thrown");
		} catch (IllegalArgumentException e) {
			Assert.assertEquals("Please provide non-null project IDs to include", e.getMessage());
		}

		try {
			projectService.setIncluded(Collections.singletonList(null));
			Assert.fail("Exception should have been thrown");
		} catch (IllegalArgumentException e) {
			Assert.assertEquals("Please provide non-null project IDs to include", e.getMessage());
		}
	}

	@Test
	public void testSetIncluded() {
		VeracodeScaProject project1 = new VeracodeScaProject();
		project1.setId(uuid);
		project1.setIncluded(false);
		VeracodeScaProject project2 = new VeracodeScaProject();
		project2.setId(UUID.randomUUID());
		Page<VeracodeScaProject> page = new PageImpl<>(Arrays.asList(project1, project2));
		BDDMockito.when(projectRepository.findAll(BDDMockito.any(Pageable.class)))
				.thenReturn(page);

		projectService.setIncluded(Collections.singletonList(uuid));
		Assert.assertTrue(project1.isIncluded());
		Assert.assertFalse(project2.isIncluded());
		BDDMockito.verify(projectRepository).saveAll(BDDMockito.anyIterable());
		BDDMockito.verify(projectRepository).flush();
	}

	@Test
	public void testSetIncludedMultiplePages() {
		VeracodeScaProject project1 = new VeracodeScaProject();
		project1.setId(uuid);
		project1.setIncluded(false);
		VeracodeScaProject project2 = new VeracodeScaProject();
		project2.setId(UUID.randomUUID());
		Pageable pageable1 = PageRequest.of(0, 1);
		Page<VeracodeScaProject> page1 = new PageImpl<>(Collections.singletonList(project1),
				pageable1, 2);
		Pageable pageable2 = PageRequest.of(1, 1);
		Page<VeracodeScaProject> page2 = new PageImpl<>(Collections.singletonList(project2),
				pageable2, 2);
		BDDMockito.when(projectRepository.findAll(PageRequest.of(0, 50)))
				.thenReturn(page1);
		BDDMockito.when(projectRepository.findAll(page1.nextPageable()))
				.thenReturn(page2);

		projectService.setIncluded(Collections.singletonList(uuid));
		Assert.assertTrue(project1.isIncluded());
		Assert.assertFalse(project2.isIncluded());
		BDDMockito.verify(projectRepository, times(2)).saveAll(BDDMockito.anyIterable());
		BDDMockito.verify(projectRepository).flush();
	}

	@Test
	public void testDeleteProject() {
		BDDMockito.when(projectRepository.findById(uuid)).thenReturn(Optional.of(project));
		projectService.deleteProject(uuid);
		BDDMockito.when(projectRepository.findById(uuid)).thenReturn(Optional.of(project));
		BDDMockito.verify(issueService).deleteIssuesByProject(project);
		BDDMockito.verify(projectRepository).delete(project);
		BDDMockito.verify(projectRepository).flush();
	}

	@Test
	public void testDeleteProjectDoesNotExist() {
		try {
			projectService.deleteProject(uuid);
			Assert.fail("Exception should have been thrown");
		} catch (VeracodeScaProductException e) {
			Assert.assertEquals("No project with the given ID exists", e.getMessage());
		}
	}

	@Test
	public void testDeleteProjectNullId() {
		try {
			projectService.deleteProject(null);
			Assert.fail("Exception should have been thrown");
		} catch (IllegalArgumentException e) {
			Assert.assertEquals("Please provide a non-null project ID to delete", e.getMessage());
		}
	}

	@Test
	public void testDeleteProjectsByWorkspaceNull() {
		try {
			projectService.deleteProjectsByWorkspace(null);
			Assert.fail("Exception should have been thrown");
		} catch (IllegalArgumentException e) {
			Assert.assertEquals("Cannot delete projects for a null workspace", e.getMessage());
		}
	}

	@Test
	public void testDeleteProjectsByWorkspace() {
		VeracodeScaProject project1 = new VeracodeScaProject();
		VeracodeScaProject project2 = new VeracodeScaProject();
		Page<VeracodeScaProject> page = new PageImpl<>(Arrays.asList(project1, project2));
		BDDMockito.when(projectRepository
				.findAllByWorkspace(BDDMockito.any(VeracodeScaWorkspace.class),
						BDDMockito.any(Pageable.class)))
				.thenReturn(page);

		projectService.deleteProjectsByWorkspace(workspace);
		BDDMockito.verify(issueService).deleteIssuesByProject(project1);
		BDDMockito.verify(issueService).deleteIssuesByProject(project2);
		BDDMockito.verify(projectRepository).deleteByWorkspace(workspace);
		BDDMockito.verify(projectRepository).flush();
	}

	@Test
	public void testDeleteProjectsByWorkspaceMultiplePages() {
		VeracodeScaProject project1 = new VeracodeScaProject();
		VeracodeScaProject project2 = new VeracodeScaProject();
		Pageable pageable1 = PageRequest.of(0, 1);
		Page<VeracodeScaProject> page1 = new PageImpl<>(Collections.singletonList(project1),
				pageable1, 2);
		Pageable pageable2 = PageRequest.of(1, 1);
		Page<VeracodeScaProject> page2 = new PageImpl<>(Collections.singletonList(project2),
				pageable2, 2);
		BDDMockito.when(projectRepository.findAllByWorkspace(workspace, PageRequest.of(0, 100)))
				.thenReturn(page1);
		BDDMockito.when(projectRepository.findAllByWorkspace(workspace, page1.nextPageable()))
				.thenReturn(page2);

		projectService.deleteProjectsByWorkspace(workspace);
		BDDMockito.verify(issueService).deleteIssuesByProject(project1);
		BDDMockito.verify(issueService).deleteIssuesByProject(project2);
		BDDMockito.verify(projectRepository).deleteByWorkspace(workspace);
		BDDMockito.verify(projectRepository).flush();
	}
}
