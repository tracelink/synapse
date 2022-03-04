package com.tracelink.prodsec.plugin.veracode.dast.service;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import com.tracelink.prodsec.plugin.veracode.dast.model.VeracodeDastAppModel;
import com.tracelink.prodsec.plugin.veracode.dast.repository.VeracodeDastAppRepository;
import com.tracelink.prodsec.synapse.products.model.ProductLineModel;

@RunWith(SpringRunner.class)
public class VeracodeDastAppServiceTest {

	@MockBean
	private VeracodeDastAppRepository mockAppRepo;

	private VeracodeDastAppService appService;

	@Before
	public void setup() {
		this.appService = new VeracodeDastAppService(mockAppRepo);
	}

	@Test
	public void testGetMappedApps() {
		VeracodeDastAppModel app = new VeracodeDastAppModel();
		BDDMockito.when(mockAppRepo.findAllBySynapseProductLineNotNull())
				.thenReturn(Arrays.asList(app));

		List<VeracodeDastAppModel> apps = appService.getMappedApps();
		Assert.assertEquals(1, apps.size());
		Assert.assertEquals(app, apps.get(0));
	}

	@Test
	public void testGetAppsBySynapseProject() {
		VeracodeDastAppModel app = new VeracodeDastAppModel();
		BDDMockito.when(mockAppRepo.findBySynapseProductLine(BDDMockito.any()))
				.thenReturn(Arrays.asList(app));

		List<VeracodeDastAppModel> apps = appService
				.getAppsBySynapseProductLine(new ProductLineModel());
		Assert.assertEquals(1, apps.size());
		Assert.assertEquals(app, apps.get(0));
	}

	@Test
	public void testGetUnmappedApps() {
		VeracodeDastAppModel app = new VeracodeDastAppModel();
		BDDMockito.when(mockAppRepo.findAllBySynapseProductLineIsNull())
				.thenReturn(Arrays.asList(app));

		List<VeracodeDastAppModel> apps = appService.getUnmappedApps();
		Assert.assertEquals(1, apps.size());
		Assert.assertEquals(app, apps.get(0));
	}

	@Test
	public void testGetAllApps() {
		VeracodeDastAppModel app = new VeracodeDastAppModel();
		BDDMockito.when(mockAppRepo.findAll()).thenReturn(Arrays.asList(app));

		List<VeracodeDastAppModel> apps = appService.getAllApps();
		Assert.assertEquals(1, apps.size());
		Assert.assertEquals(app, apps.get(0));
	}

	@Test
	public void testGetDastApp() {
		VeracodeDastAppModel app = new VeracodeDastAppModel();
		BDDMockito.when(mockAppRepo.findByName(BDDMockito.anyString())).thenReturn(app);

		VeracodeDastAppModel returnedApp = appService.getDastApp("");
		Assert.assertEquals(app, returnedApp);
	}

	@Test
	public void testSave() {
		VeracodeDastAppModel app = new VeracodeDastAppModel();
		BDDMockito.when(mockAppRepo.saveAndFlush(BDDMockito.any())).thenReturn(app);

		VeracodeDastAppModel returnedApp = appService.save(app);
		Assert.assertEquals(app, returnedApp);
	}

	@Test
	public void testCreateMapping() {
		VeracodeDastAppModel app = new VeracodeDastAppModel();
		BDDMockito.when(mockAppRepo.findByName(BDDMockito.anyString())).thenReturn(app);
		ProductLineModel plm = new ProductLineModel();
		appService.createMapping(plm, "");
		Assert.assertEquals(plm, app.getSynapseProductLine());
		BDDMockito.verify(mockAppRepo).saveAndFlush(app);
	}

	@Test
	public void testCreateMappingNulls() {
		appService.createMapping(null, null);
		appService.createMapping(new ProductLineModel(), null);
		appService.createMapping(null, "");
		BDDMockito.verify(mockAppRepo, BDDMockito.times(0)).saveAndFlush(BDDMockito.any());
	}

	@Test
	public void testDeleteMapping() {
		VeracodeDastAppModel app = new VeracodeDastAppModel();
		ProductLineModel plm = new ProductLineModel();
		app.setSynapseProductLine(plm);
		BDDMockito.when(mockAppRepo.findByName(BDDMockito.anyString())).thenReturn(app);
		appService.deleteMapping("");
		Assert.assertNull(app.getSynapseProductLine());
		BDDMockito.verify(mockAppRepo).saveAndFlush(app);
	}

	@Test
	public void testDeleteMappingNulls() {
		appService.deleteMapping("");
		BDDMockito.verify(mockAppRepo, BDDMockito.times(0)).saveAndFlush(BDDMockito.any());
	}

}
