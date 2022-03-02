package com.tracelink.prodsec.plugin.veracode.sast.service;

import static org.mockito.Mockito.times;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit4.SpringRunner;

import com.tracelink.prodsec.plugin.veracode.sast.model.VeracodeSastAppModel;
import com.tracelink.prodsec.plugin.veracode.sast.model.VeracodeSastProductException;
import com.tracelink.prodsec.plugin.veracode.sast.repository.VeracodeSastAppRepository;
import com.tracelink.prodsec.synapse.products.model.ProjectModel;

@RunWith(SpringRunner.class)
public class VeracodeSastAppServiceTest {

	@MockBean
	private VeracodeSastAppRepository mockAppRepo;

	@MockBean
	private VeracodeSastReportService mockReportService;

	private VeracodeSastAppService appService;

	@Before
	public void setup() {
		this.appService = new VeracodeSastAppService(mockAppRepo, mockReportService);
	}

	@Test
	public void testGetMappedApps() {
		VeracodeSastAppModel app = new VeracodeSastAppModel();
		BDDMockito.when(mockAppRepo.findAllBySynapseProjectNotNull())
				.thenReturn(Arrays.asList(app));

		List<VeracodeSastAppModel> apps = appService.getMappedApps();
		Assert.assertEquals(1, apps.size());
		Assert.assertEquals(app, apps.get(0));
	}

	@Test
	public void testGetAppsBySynapseProject() {
		VeracodeSastAppModel app = new VeracodeSastAppModel();
		BDDMockito.when(mockAppRepo.findBySynapseProject(BDDMockito.any()))
				.thenReturn(Arrays.asList(app));

		List<VeracodeSastAppModel> apps = appService.getAppsBySynapseProject(new ProjectModel());
		Assert.assertEquals(1, apps.size());
		Assert.assertEquals(app, apps.get(0));
	}

	@Test
	public void testGetUnmappedApps() {
		VeracodeSastAppModel app = new VeracodeSastAppModel();
		BDDMockito.when(mockAppRepo.findAllBySynapseProjectIsNull()).thenReturn(Arrays.asList(app));

		List<VeracodeSastAppModel> apps = appService.getUnmappedApps();
		Assert.assertEquals(1, apps.size());
		Assert.assertEquals(app, apps.get(0));
	}

	@Test
	public void testGetAllApps() {
		VeracodeSastAppModel app = new VeracodeSastAppModel();
		BDDMockito.when(mockAppRepo.findAll()).thenReturn(Arrays.asList(app));

		List<VeracodeSastAppModel> apps = appService.getAllApps();
		Assert.assertEquals(1, apps.size());
		Assert.assertEquals(app, apps.get(0));
	}

	@Test
	public void testGetIncludedApps() {
		BDDMockito.when(mockAppRepo.findAll()).thenReturn(Collections.emptyList());
		List<VeracodeSastAppModel> returnedApps = appService.getIncludedApps();
		Assert.assertTrue(returnedApps.isEmpty());

		VeracodeSastAppModel app = new VeracodeSastAppModel();
		BDDMockito.when(mockAppRepo.findAll())
				.thenReturn(Collections.singletonList(app));

		returnedApps = appService.getIncludedApps();
		Assert.assertEquals(1, returnedApps.size());
		Assert.assertTrue(returnedApps.contains(app));

		app.setIncluded(false);
		returnedApps = appService.getIncludedApps();
		Assert.assertTrue(returnedApps.isEmpty());
	}

	@Test
	public void testGetSastApp() {
		VeracodeSastAppModel app = new VeracodeSastAppModel();
		BDDMockito.when(mockAppRepo
				.findByName(BDDMockito.anyString()))
				.thenReturn(app);

		VeracodeSastAppModel returnedApp = appService.getSastApp("");
		Assert.assertEquals(app, returnedApp);
	}

	@Test
	public void testSave() {
		VeracodeSastAppModel app = new VeracodeSastAppModel();
		BDDMockito.when(mockAppRepo.saveAndFlush(BDDMockito.any())).thenReturn(app);

		VeracodeSastAppModel returnedApp = appService.save(app);
		Assert.assertEquals(app, returnedApp);
	}

	@Test
	public void testSetIncludedAppsNull() {
		try {
			appService.setIncluded(null);
			Assert.fail("Exception should have been thrown");
		} catch (IllegalArgumentException e) {
			Assert.assertEquals("Please provide non-null app IDs to include", e.getMessage());
		}

		try {
			appService.setIncluded(Collections.singletonList(null));
			Assert.fail("Exception should have been thrown");
		} catch (IllegalArgumentException e) {
			Assert.assertEquals("Please provide non-null app IDs to include", e.getMessage());
		}
	}

	@Test
	public void testSetIncluded() {
		VeracodeSastAppModel app1 = BDDMockito.mock(VeracodeSastAppModel.class);
		BDDMockito.when(app1.getId()).thenReturn(1L);
		VeracodeSastAppModel app2 = BDDMockito.mock(VeracodeSastAppModel.class);
		BDDMockito.when(app2.getId()).thenReturn(2L);
		Page<VeracodeSastAppModel> page = new PageImpl<>(Arrays.asList(app1, app2));
		BDDMockito.when(mockAppRepo.findAll(BDDMockito.any(Pageable.class)))
				.thenReturn(page);

		appService.setIncluded(Collections.singletonList(1L));
		BDDMockito.verify(app1).setIncluded(true);
		BDDMockito.verify(app2).setIncluded(false);
		BDDMockito.verify(mockAppRepo).saveAll(BDDMockito.anyIterable());
		BDDMockito.verify(mockAppRepo).flush();
	}

	@Test
	public void testSetIncludedMultiplePages() {
		VeracodeSastAppModel app1 = BDDMockito.mock(VeracodeSastAppModel.class);
		BDDMockito.when(app1.getId()).thenReturn(1L);
		VeracodeSastAppModel app2 = BDDMockito.mock(VeracodeSastAppModel.class);
		BDDMockito.when(app2.getId()).thenReturn(2L);
		Pageable pageable1 = PageRequest.of(0, 1);
		Page<VeracodeSastAppModel> page1 = new PageImpl<>(Collections.singletonList(app1),
				pageable1, 2);
		Pageable pageable2 = PageRequest.of(1, 1);
		Page<VeracodeSastAppModel> page2 = new PageImpl<>(Collections.singletonList(app2),
				pageable2, 2);
		BDDMockito.when(mockAppRepo.findAll(PageRequest.of(0, 50)))
				.thenReturn(page1);
		BDDMockito.when(mockAppRepo.findAll(page1.nextPageable()))
				.thenReturn(page2);

		appService.setIncluded(Collections.singletonList(1L));
		BDDMockito.verify(app1).setIncluded(true);
		BDDMockito.verify(app2).setIncluded(false);
		BDDMockito.verify(mockAppRepo, times(2)).saveAll(BDDMockito.anyIterable());
		BDDMockito.verify(mockAppRepo).flush();
	}

	@Test
	public void testDeleteApp() {
		VeracodeSastAppModel app = new VeracodeSastAppModel();
		BDDMockito.when(mockAppRepo.findById(1L)).thenReturn(Optional.of(app));
		appService.deleteApp(1L);
		BDDMockito.verify(mockReportService).deleteReportsByApp(app);
		BDDMockito.verify(mockAppRepo).delete(app);
		BDDMockito.verify(mockAppRepo).flush();
	}

	@Test
	public void testDeleteAppDoesNotExist() {
		try {
			appService.deleteApp(1L);
			Assert.fail("Exception should have been thrown");
		} catch (VeracodeSastProductException e) {
			Assert.assertEquals("No app with the given ID exists", e.getMessage());
		}
	}

	@Test
	public void testDeleteAppNullId() {
		try {
			appService.deleteApp(null);
			Assert.fail("Exception should have been thrown");
		} catch (IllegalArgumentException e) {
			Assert.assertEquals("Please provide a non-null app ID to delete", e.getMessage());
		}
	}

	@Test
	public void testCreateMapping() {
		VeracodeSastAppModel app = new VeracodeSastAppModel();
		BDDMockito.when(mockAppRepo.findById(BDDMockito.anyLong())).thenReturn(Optional.of(app));
		ProjectModel pm = new ProjectModel();
		appService.createMapping(pm, 1L);
		Assert.assertEquals(pm, app.getSynapseProject());
		BDDMockito.verify(mockAppRepo).saveAndFlush(app);
	}

	@Test
	public void testCreateMappingNulls() {
		appService.createMapping(null, null);
		appService.createMapping(new ProjectModel(), null);
		appService.createMapping(null, 1L);
		BDDMockito.verify(mockAppRepo, BDDMockito.times(0)).saveAndFlush(BDDMockito.any());
	}

	@Test
	public void testDeleteMapping() {
		VeracodeSastAppModel app = new VeracodeSastAppModel();
		ProjectModel pm = new ProjectModel();
		app.setSynapseProject(pm);
		BDDMockito.when(mockAppRepo.findById(BDDMockito.anyLong())).thenReturn(Optional.of(app));
		appService.deleteMapping(1L);
		Assert.assertNull(app.getSynapseProject());
		BDDMockito.verify(mockAppRepo).saveAndFlush(app);
	}

	@Test
	public void testDeleteMappingNulls() {
		appService.deleteMapping(1L);
		BDDMockito.verify(mockAppRepo, BDDMockito.times(0)).saveAndFlush(BDDMockito.any());
	}

}
