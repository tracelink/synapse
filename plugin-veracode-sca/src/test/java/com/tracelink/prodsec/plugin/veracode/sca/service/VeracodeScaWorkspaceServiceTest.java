package com.tracelink.prodsec.plugin.veracode.sca.service;

import static org.mockito.Mockito.times;

import java.util.Arrays;
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

import com.tracelink.prodsec.lib.veracode.rest.api.model.Workspace;
import com.tracelink.prodsec.plugin.veracode.sca.exception.VeracodeScaProductException;
import com.tracelink.prodsec.plugin.veracode.sca.model.VeracodeScaWorkspace;
import com.tracelink.prodsec.plugin.veracode.sca.repository.VeracodeScaWorkspaceRepository;

@RunWith(SpringRunner.class)
public class VeracodeScaWorkspaceServiceTest {

	@MockBean
	private VeracodeScaWorkspaceRepository workspaceRepository;

	@MockBean
	private VeracodeScaProjectService projectService;

	private VeracodeScaWorkspaceService workspaceService;
	private VeracodeScaWorkspace workspace;
	private UUID uuid;

	@Before
	public void setup() {
		workspaceService = new VeracodeScaWorkspaceService(workspaceRepository, projectService);
		workspace = new VeracodeScaWorkspace();
		uuid = UUID.randomUUID();
		workspace.setId(uuid);
	}

	@Test
	public void testGetWorkspaces() {
		BDDMockito.when(workspaceRepository.findAll()).thenReturn(Collections.emptyList());
		List<VeracodeScaWorkspace> returnedWorkspaces = workspaceService.getWorkspaces();
		Assert.assertTrue(returnedWorkspaces.isEmpty());

		VeracodeScaWorkspace workspace = new VeracodeScaWorkspace();
		BDDMockito.when(workspaceRepository.findAll())
				.thenReturn(Collections.singletonList(workspace));
		returnedWorkspaces = workspaceService.getWorkspaces();
		Assert.assertEquals(1, returnedWorkspaces.size());
		Assert.assertEquals(workspace, returnedWorkspaces.get(0));
	}

	@Test
	public void testUpdateWorkspaces() {
		Workspace workspace = new Workspace();
		workspace.setId(UUID.randomUUID());
		workspace.setName("Workspace1");
		workspace.setSiteId("ABCdef");

		List<VeracodeScaWorkspace> workspaces = workspaceService
				.updateWorkspaces(Collections.singletonList(workspace));

		@SuppressWarnings("unchecked")
		ArgumentCaptor<List<VeracodeScaWorkspace>> workspacesCaptor = ArgumentCaptor
				.forClass(List.class);
		BDDMockito.verify(workspaceRepository, times(1)).saveAll(workspacesCaptor.capture());
		Assert.assertFalse(workspacesCaptor.getValue().isEmpty());
		Assert.assertEquals(1, workspaces.size());
		Assert.assertEquals(workspaces.get(0), workspacesCaptor.getValue().get(0));
		Assert.assertEquals(workspace.getId(), workspaces.get(0).getId());
		Assert.assertEquals(workspace.getName(), workspaces.get(0).getName());
		Assert.assertEquals(workspace.getSiteId(), workspaces.get(0).getSiteId());
		Assert.assertTrue(workspaces.get(0).isIncluded());
	}

	@Test
	public void testSetIncludedWorkspacesNull() {
		try {
			workspaceService.setIncluded(null);
			Assert.fail("Exception should have been thrown");
		} catch (IllegalArgumentException e) {
			Assert.assertEquals("Please provide non-null workspace IDs to include", e.getMessage());
		}

		try {
			workspaceService.setIncluded(Collections.singletonList(null));
			Assert.fail("Exception should have been thrown");
		} catch (IllegalArgumentException e) {
			Assert.assertEquals("Please provide non-null workspace IDs to include", e.getMessage());
		}
	}

	@Test
	public void testSetIncluded() {
		VeracodeScaWorkspace workspace1 = new VeracodeScaWorkspace();
		workspace1.setId(uuid);
		workspace1.setIncluded(false);
		VeracodeScaWorkspace workspace2 = new VeracodeScaWorkspace();
		workspace2.setId(UUID.randomUUID());
		Page<VeracodeScaWorkspace> page = new PageImpl<>(Arrays.asList(workspace1, workspace2));
		BDDMockito.when(workspaceRepository.findAll(BDDMockito.any(Pageable.class)))
				.thenReturn(page);

		workspaceService.setIncluded(Collections.singletonList(uuid));
		Assert.assertTrue(workspace1.isIncluded());
		Assert.assertFalse(workspace2.isIncluded());
		BDDMockito.verify(workspaceRepository).saveAll(BDDMockito.anyIterable());
		BDDMockito.verify(workspaceRepository).flush();
	}

	@Test
	public void testSetIncludedMultiplePages() {
		VeracodeScaWorkspace workspace1 = new VeracodeScaWorkspace();
		workspace1.setId(uuid);
		workspace1.setIncluded(false);
		VeracodeScaWorkspace workspace2 = new VeracodeScaWorkspace();
		workspace2.setId(UUID.randomUUID());
		Pageable pageable1 = PageRequest.of(0, 1);
		Page<VeracodeScaWorkspace> page1 = new PageImpl<>(Collections.singletonList(workspace1),
				pageable1, 2);
		Pageable pageable2 = PageRequest.of(1, 1);
		Page<VeracodeScaWorkspace> page2 = new PageImpl<>(Collections.singletonList(workspace2),
				pageable2, 2);
		BDDMockito.when(workspaceRepository.findAll(PageRequest.of(0, 50)))
				.thenReturn(page1);
		BDDMockito.when(workspaceRepository.findAll(page1.nextPageable()))
				.thenReturn(page2);

		workspaceService.setIncluded(Collections.singletonList(uuid));
		Assert.assertTrue(workspace1.isIncluded());
		Assert.assertFalse(workspace2.isIncluded());
		BDDMockito.verify(workspaceRepository, times(2)).saveAll(BDDMockito.anyIterable());
		BDDMockito.verify(workspaceRepository).flush();
	}

	@Test
	public void testDeleteWorkspace() {
		BDDMockito.when(workspaceRepository.findById(uuid)).thenReturn(Optional.of(workspace));
		workspaceService.deleteWorkspace(uuid);
		BDDMockito.when(workspaceRepository.findById(uuid)).thenReturn(Optional.of(workspace));
		BDDMockito.verify(projectService).deleteProjectsByWorkspace(workspace);
		BDDMockito.verify(workspaceRepository).delete(workspace);
		BDDMockito.verify(workspaceRepository).flush();
	}

	@Test
	public void testDeleteWorkspaceDoesNotExist() {
		try {
			workspaceService.deleteWorkspace(uuid);
			Assert.fail("Exception should have been thrown");
		} catch (VeracodeScaProductException e) {
			Assert.assertEquals("No workspace with the given ID exists", e.getMessage());
		}
	}

	@Test
	public void testDeleteWorkspaceNullId() {
		try {
			workspaceService.deleteWorkspace(null);
			Assert.fail("Exception should have been thrown");
		} catch (IllegalArgumentException e) {
			Assert.assertEquals("Please provide a non-null workspace ID to delete", e.getMessage());
		}
	}
}
