package com.tracelink.prodsec.plugin.veracode.sast.service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import com.tracelink.prodsec.plugin.veracode.sast.model.ModelType;
import com.tracelink.prodsec.plugin.veracode.sast.model.VeracodeSastAppModel;
import com.tracelink.prodsec.plugin.veracode.sast.repository.VeracodeSastAppRepository;
import com.tracelink.prodsec.synapse.products.model.ProjectModel;

@RunWith(SpringRunner.class)
public class VeracodeSastAppServiceTest {

	@MockBean
	private VeracodeSastAppRepository mockAppRepo;

	private VeracodeSastAppService appService;

	@Before
	public void setup() {
		this.appService = new VeracodeSastAppService(mockAppRepo);
	}

	@Test
	public void testGetMappedApps() {
		VeracodeSastAppModel app = new VeracodeSastAppModel();
		BDDMockito.when(mockAppRepo.findAllBySynapseProjectNotNull()).thenReturn(Arrays.asList(app));

		List<VeracodeSastAppModel> apps = appService.getMappedApps();
		Assert.assertEquals(1, apps.size());
		Assert.assertEquals(app, apps.get(0));
	}

	@Test
	public void testGetAppsBySynapseProject() {
		VeracodeSastAppModel app = new VeracodeSastAppModel();
		BDDMockito.when(mockAppRepo.findBySynapseProject(BDDMockito.any())).thenReturn(Arrays.asList(app));

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
	public void testGetSastApp() {
		VeracodeSastAppModel app = new VeracodeSastAppModel();
		BDDMockito.when(mockAppRepo.findByNameAndModelType(BDDMockito.anyString(), BDDMockito.any(ModelType.class)))
				.thenReturn(app);

		VeracodeSastAppModel returnedApp = appService.getSastApp("", ModelType.SBX);
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
