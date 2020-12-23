package com.tracelink.prodsec.plugin.sme.service;

import com.tracelink.prodsec.plugin.sme.model.SMEEntity;
import com.tracelink.prodsec.plugin.sme.repositories.SMERepo;
import com.tracelink.prodsec.synapse.products.ProductsNotFoundException;
import com.tracelink.prodsec.synapse.products.model.ProductLineModel;
import com.tracelink.prodsec.synapse.products.model.ProjectModel;
import com.tracelink.prodsec.synapse.products.service.ProductsService;
import com.tracelink.prodsec.synapse.scorecard.model.ScorecardValue;
import com.tracelink.prodsec.synapse.scorecard.model.ScorecardValue.TrafficLight;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class SMEServiceTest {

	@MockBean
	private SMERepo mockSmeRepo;

	@MockBean
	private ProductsService mockProductsService;

	private SMEService smeService;

	@Before
	public void setup() {
		smeService = new SMEService(mockSmeRepo, mockProductsService);
	}

	@Test
	public void addSMESuccess() throws SMEException {
		BDDMockito.when(mockSmeRepo.findByName(BDDMockito.anyString())).thenReturn(null);
		String smeName = "name";
		smeService.addNewSME(smeName);
		ArgumentCaptor<SMEEntity> captor = ArgumentCaptor.forClass(SMEEntity.class);
		BDDMockito.verify(mockSmeRepo).saveAndFlush(captor.capture());
		Assert.assertEquals(smeName, captor.getValue().getName());
	}

	@Test
	public void addSMEFail() {
		BDDMockito.when(mockSmeRepo.findByName(BDDMockito.anyString()))
				.thenReturn(BDDMockito.mock(SMEEntity.class));
		String smeName = "name";
		try {
			smeService.addNewSME(smeName);
			Assert.fail("Expected SME Exception");
		} catch (SMEException e) {
			Assert.assertTrue(e.getMessage().contains("already exists"));
		}
	}

	@Test
	public void setProjectsSuccess() throws SMEException, ProductsNotFoundException {
		SMEEntity entity = new SMEEntity();
		BDDMockito.when(mockSmeRepo.findByName(BDDMockito.anyString())).thenReturn(entity);

		String projectName = "projectName";
		List<String> projectNames = new ArrayList<>();
		projectNames.add(projectName);

		ProjectModel project = new ProjectModel();
		project.setName(projectName);

		BDDMockito.when(mockProductsService.getProject(projectName)).thenReturn(project);

		smeService.setProjectsForSME("smeName", projectNames);

		ArgumentCaptor<SMEEntity> captor = ArgumentCaptor.forClass(SMEEntity.class);
		BDDMockito.verify(mockSmeRepo).saveAndFlush(captor.capture());
		Assert.assertEquals(entity, captor.getValue());
		Assert.assertEquals(project, entity.getProjects().get(0));
		Assert.assertEquals(projectName, entity.getProjects().get(0).getName());
	}

	@Test
	public void setProjectsFailSME() {
		BDDMockito.when(mockSmeRepo.findByName(BDDMockito.anyString())).thenReturn(null);
		try {
			smeService.setProjectsForSME("smeName", new ArrayList<>());
			Assert.fail("Should have exception");
		} catch (SMEException e) {
			Assert.assertTrue(e.getMessage().contains("Could not find"));
		} catch (ProductsNotFoundException e) {
			Assert.fail("Wrong exception");
		}
	}

	@Test
	public void setProjectsFailProject() {
		SMEEntity entity = new SMEEntity();
		BDDMockito.when(mockSmeRepo.findByName(BDDMockito.anyString())).thenReturn(entity);
		BDDMockito.when(mockProductsService.getProject(BDDMockito.anyString())).thenReturn(null);
		try {
			smeService.setProjectsForSME("smeName", Arrays.asList("project"));
			Assert.fail("Should have exception");
		} catch (SMEException e) {
			Assert.fail("Wrong exception");
		} catch (ProductsNotFoundException e) {
			Assert.assertTrue(e.getMessage().contains("Unknown Project"));
		}
	}

	@Test
	public void setProjectsNoProjects() throws SMEException, ProductsNotFoundException {
		smeService.setProjectsForSME("smeName", null);
		BDDMockito.verify(mockSmeRepo, BDDMockito.never()).findByName(BDDMockito.anyString());
	}

	@Test
	public void removeProjectSuccess() throws ProductsNotFoundException, SMEException {
		SMEEntity entity = new SMEEntity();
		BDDMockito.when(mockSmeRepo.findByName(BDDMockito.anyString())).thenReturn(entity);

		String projectName = "projectName";

		ProjectModel project = new ProjectModel();
		project.setName(projectName);

		entity.setProjects(new ArrayList<>(Arrays.asList(project)));

		BDDMockito.when(mockProductsService.getProject(projectName)).thenReturn(project);

		smeService.removeProjectFromSME("smeName", projectName);

		ArgumentCaptor<SMEEntity> captor = ArgumentCaptor.forClass(SMEEntity.class);
		BDDMockito.verify(mockSmeRepo).saveAndFlush(captor.capture());
		Assert.assertEquals(entity, captor.getValue());
		Assert.assertEquals(0, entity.getProjects().size());
	}

	@Test
	public void removeProjectProjectNotInSMEList() throws ProductsNotFoundException, SMEException {
		SMEEntity entity = new SMEEntity();
		BDDMockito.when(mockSmeRepo.findByName(BDDMockito.anyString())).thenReturn(entity);

		String projectName = "projectName";

		ProjectModel project = new ProjectModel();
		project.setName(projectName);
		entity.setProjects(new ArrayList<>(Arrays.asList(project)));

		ProjectModel project2 = new ProjectModel();

		BDDMockito.when(mockProductsService.getProject(BDDMockito.anyString()))
				.thenReturn(project2);

		smeService.removeProjectFromSME("smeName", projectName);

		BDDMockito.verify(mockSmeRepo, BDDMockito.never()).saveAndFlush(BDDMockito.any());
		Assert.assertEquals(1, entity.getProjects().size());
		Assert.assertEquals(project, entity.getProjects().get(0));
	}

	@Test
	public void removeProjectSMEHasNoProjects() throws ProductsNotFoundException, SMEException {
		SMEEntity entity = new SMEEntity();
		BDDMockito.when(mockSmeRepo.findByName(BDDMockito.anyString())).thenReturn(entity);

		String projectName = "projectName";

		ProjectModel project = new ProjectModel();
		project.setName(projectName);
		entity.setProjects(null);

		BDDMockito.when(mockProductsService.getProject(BDDMockito.anyString())).thenReturn(project);

		smeService.removeProjectFromSME("smeName", projectName);

		BDDMockito.verify(mockSmeRepo, BDDMockito.never()).saveAndFlush(BDDMockito.any());
		Assert.assertNull(entity.getProjects());
	}

	@Test
	public void removeProjectFailSME() {
		BDDMockito.when(mockSmeRepo.findByName(BDDMockito.anyString())).thenReturn(null);
		try {
			smeService.removeProjectFromSME("smeName", "");
			Assert.fail("Should have had an exception");
		} catch (ProductsNotFoundException e) {
			Assert.fail("Wrong exception");
		} catch (SMEException e) {
			Assert.assertTrue(e.getMessage().contains("Could not find SME"));
		}
	}

	@Test
	public void removeProjectFailUnknown() {
		SMEEntity entity = new SMEEntity();
		BDDMockito.when(mockSmeRepo.findByName(BDDMockito.anyString())).thenReturn(entity);

		BDDMockito.when(mockProductsService.getProject(BDDMockito.anyString())).thenReturn(null);

		try {
			smeService.removeProjectFromSME("smeName", "");
			Assert.fail("Should have had an exception");
		} catch (ProductsNotFoundException e) {
			Assert.assertTrue(e.getMessage().contains("Unknown Project"));
		} catch (SMEException e) {
			Assert.fail("Wrong exception");
		}
	}

	@Test
	public void getAllSmeTest() {
		smeService.getAllSMEs();
		BDDMockito.verify(mockSmeRepo).findAll();
	}

	@Test
	public void callbackProjectRedNull() {
		BDDMockito.when(mockSmeRepo.findByProjects(BDDMockito.any())).thenReturn(null);
		ProjectModel project = null;
		ScorecardValue value = smeService.scorecardCallbackProject(project);
		Assert.assertEquals("None", value.getValue());
		Assert.assertEquals(TrafficLight.RED, value.getColor());
	}

	@Test
	public void callbackProjectRedEmpty() {
		BDDMockito.when(mockSmeRepo.findByProjects(BDDMockito.any())).thenReturn(new ArrayList<>());
		ProjectModel project = null;
		ScorecardValue value = smeService.scorecardCallbackProject(project);
		Assert.assertEquals("None", value.getValue());
		Assert.assertEquals(TrafficLight.RED, value.getColor());
	}

	@Test
	public void callbackProjectGreen() {
		String e1Name = "Chris";
		String e2Name = "Maddie";

		SMEEntity entity1 = new SMEEntity();
		entity1.setName(e1Name);
		SMEEntity entity2 = new SMEEntity();
		entity2.setName(e2Name);

		BDDMockito.when(mockSmeRepo.findByProjects(BDDMockito.any()))
				.thenReturn(Arrays.asList(entity1, entity2));
		ProjectModel project = null;
		ScorecardValue value = smeService.scorecardCallbackProject(project);
		Assert.assertTrue(value.getValue().contains(e1Name));
		Assert.assertTrue(value.getValue().contains(e2Name));
		Assert.assertEquals(TrafficLight.GREEN, value.getColor());
	}

	@Test
	public void callbackProductGreen() {
		ProductLineModel plm = new ProductLineModel();
		List<ProjectModel> projList = new ArrayList<>();
		projList.add(new ProjectModel());
		plm.setProjects(projList);
		BDDMockito.when(mockSmeRepo.findByProjects(BDDMockito.any()))
				.thenReturn(Arrays.asList(new SMEEntity()));
		ScorecardValue value = smeService.scorecardCallbackProduct(plm);
		Assert.assertEquals(TrafficLight.GREEN, value.getColor());
	}

	@Test
	public void callbackProductYellow() {
		ProductLineModel plm = new ProductLineModel();
		ProjectModel pm = new ProjectModel();
		pm.setName("project1");
		ProjectModel pm2 = new ProjectModel();
		pm.setName("project2");
		List<ProjectModel> projList = Arrays.asList(pm, pm2, pm2, pm2, pm2);
		plm.setProjects(projList);
		BDDMockito.when(mockSmeRepo.findByProjects(pm2)).thenReturn(Arrays.asList(new SMEEntity()));
		ScorecardValue value = smeService.scorecardCallbackProduct(plm);
		Assert.assertEquals(TrafficLight.YELLOW, value.getColor());
	}

	@Test
	public void callbackProductRed() {
		ProductLineModel plm = new ProductLineModel();
		List<ProjectModel> projList = new ArrayList<>();
		projList.add(new ProjectModel());
		plm.setProjects(projList);
		BDDMockito.when(mockSmeRepo.findByProjects(BDDMockito.any())).thenReturn(null);
		ScorecardValue value = smeService.scorecardCallbackProduct(plm);
		Assert.assertEquals(TrafficLight.RED, value.getColor());
	}

}
