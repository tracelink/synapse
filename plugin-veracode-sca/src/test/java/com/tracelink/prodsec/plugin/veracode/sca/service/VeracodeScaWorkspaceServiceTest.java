package com.tracelink.prodsec.plugin.veracode.sca.service;

import static org.mockito.Mockito.times;

import com.tracelink.prodsec.plugin.veracode.sca.exception.VeracodeScaProductException;
import com.tracelink.prodsec.plugin.veracode.sca.model.VeracodeScaWorkspace;
import com.tracelink.prodsec.plugin.veracode.sca.repository.VeracodeScaWorkspaceRepository;
import com.tracelink.prodsec.plugin.veracode.sca.util.model.Workspace;
import java.util.Collections;
import java.util.List;
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
public class VeracodeScaWorkspaceServiceTest {

	@MockBean
	private VeracodeScaWorkspaceRepository workspaceRepository;

	private VeracodeScaWorkspaceService workspaceService;

	@Before
	public void setup() {
		workspaceService = new VeracodeScaWorkspaceService(workspaceRepository);
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
	public void testSetIncludedWorkspaceDoesNotExist() {
		try {
			workspaceService.setIncluded("Workspace1", true);
			Assert.fail("Exception should have been thrown");
		} catch (VeracodeScaProductException e) {
			Assert.assertEquals("No workspace found with the name: Workspace1", e.getMessage());
		}
	}

	@Test
	public void testSetExcluded() {
		VeracodeScaWorkspace workspace = new VeracodeScaWorkspace();
		BDDMockito.when(workspaceRepository.findByName(BDDMockito.anyString()))
				.thenReturn(workspace);
		Assert.assertTrue(workspace.isIncluded());
		workspaceService.setIncluded("Workspace1", false);
		Assert.assertFalse(workspace.isIncluded());
		BDDMockito.verify(workspaceRepository, times(1)).saveAndFlush(workspace);
	}
}
