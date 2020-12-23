package com.tracelink.prodsec.synapse.products.service;

import com.tracelink.prodsec.synapse.products.BadProductNameException;
import com.tracelink.prodsec.synapse.products.OrphanedException;
import com.tracelink.prodsec.synapse.products.ProductsNotFoundException;
import com.tracelink.prodsec.synapse.products.model.ProductLineModel;
import com.tracelink.prodsec.synapse.products.model.ProjectFilterModel;
import com.tracelink.prodsec.synapse.products.model.ProjectModel;
import com.tracelink.prodsec.synapse.products.repository.ProductLineRepo;
import com.tracelink.prodsec.synapse.products.repository.ProjectFilterRepo;
import com.tracelink.prodsec.synapse.products.repository.ProjectRepo;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class ProductsServiceTest {

	@MockBean
	private ProductLineRepo mockProductLineRepo;

	@MockBean
	private ProjectFilterRepo mockProjectFilterRepo;

	@MockBean
	private ProjectRepo mockProjectRepo;

	private ProductsService productsService;

	@Before
	public void setup() {
		productsService = new ProductsService(mockProductLineRepo, mockProjectFilterRepo,
				mockProjectRepo);
	}

	/////////////////
	// ProductLine
	/////////////////
	@Test
	public void createProductLineTestSuccess() throws BadProductNameException {
		BDDMockito.when(mockProductLineRepo.saveAndFlush(BDDMockito.any()))
				.thenAnswer(answer -> answer.getArgument(0));
		String productLine = "foobar";

		ProductLineModel plm = productsService.createProductLine(productLine);
		Assert.assertEquals(productLine, plm.getName());
	}

	@Test(expected = BadProductNameException.class)
	public void createProductLineTestFailEmpty() throws BadProductNameException {
		productsService.createProductLine("");
	}

	@Test(expected = BadProductNameException.class)
	public void createProductLineTestFailExists() throws BadProductNameException {
		BDDMockito.when(mockProductLineRepo.findByName(BDDMockito.anyString()))
				.thenReturn(new ProductLineModel());

		productsService.createProductLine("foobar");
	}

	@Test
	public void getProductLineTest() {
		productsService.getProductLine("");
		BDDMockito.verify(mockProductLineRepo).findByName(BDDMockito.anyString());
	}

	@Test
	public void getAllProductLinesTest() {
		productsService.getAllProductLines();
		BDDMockito.verify(mockProductLineRepo).findAllByOrderByNameAsc();
	}

	@Test
	public void renameProductLineTestSuccess()
			throws BadProductNameException, ProductsNotFoundException {
		String productLine = "foobar";
		String newProductLine = "foobar2";
		BDDMockito.when(mockProductLineRepo.saveAndFlush(BDDMockito.any()))
				.thenAnswer(answer -> answer.getArgument(0));
		BDDMockito.when(mockProductLineRepo.findByName(BDDMockito.endsWith(productLine)))
				.thenReturn(new ProductLineModel());
		BDDMockito.when(mockProductLineRepo.findByName(BDDMockito.endsWith(newProductLine)))
				.thenReturn(null);

		ProductLineModel plm = productsService.renameProductLine(productLine, newProductLine);
		Assert.assertEquals(newProductLine, plm.getName());
	}

	@Test(expected = BadProductNameException.class)
	public void renameProductLineTestFailEmpty()
			throws BadProductNameException, ProductsNotFoundException {
		String productLine = "foobar";
		String newProductLine = "";
		productsService.renameProductLine(productLine, newProductLine);
	}

	@Test(expected = BadProductNameException.class)
	public void renameProductLineTestFailExists()
			throws BadProductNameException, ProductsNotFoundException {
		BDDMockito.when(mockProductLineRepo.findByName(BDDMockito.anyString()))
				.thenReturn(new ProductLineModel());
		String productLine = "foobar";
		String newProductLine = "foobar2";
		productsService.renameProductLine(productLine, newProductLine);
	}

	@Test(expected = ProductsNotFoundException.class)
	public void renameProductLineTestFailOldNotExist()
			throws BadProductNameException, ProductsNotFoundException {
		String productLine = "foobar";
		String newProductLine = "foobar2";
		BDDMockito.when(mockProductLineRepo.saveAndFlush(BDDMockito.any()))
				.thenAnswer(answer -> answer.getArgument(0));
		BDDMockito.when(mockProductLineRepo.findByName(BDDMockito.endsWith(newProductLine)))
				.thenReturn(null);
		BDDMockito.when(mockProductLineRepo.findByName(BDDMockito.endsWith(productLine)))
				.thenReturn(null);

		productsService.renameProductLine(productLine, newProductLine);
	}

	@Test
	public void deleteProductLineTestSuccess() throws OrphanedException, ProductsNotFoundException {
		ProductLineModel plm = new ProductLineModel();
		plm.setProjects(new ArrayList<>());

		BDDMockito.when(mockProductLineRepo.findByName(BDDMockito.anyString())).thenReturn(plm);

		productsService.deleteProductLine("productLine");
		ArgumentCaptor<ProductLineModel> captor = ArgumentCaptor.forClass(ProductLineModel.class);
		BDDMockito.verify(mockProductLineRepo).delete(captor.capture());

		Assert.assertEquals(plm, captor.getValue());
	}

	@Test(expected = ProductsNotFoundException.class)
	public void deleteProductLineTestFailUnknown()
			throws OrphanedException, ProductsNotFoundException {
		String productLine = "foobar";
		productsService.deleteProductLine(productLine);
	}

	@Test(expected = OrphanedException.class)
	public void deleteProductLineTestFailOrphaned()
			throws OrphanedException, ProductsNotFoundException {
		ProductLineModel plm = new ProductLineModel();
		plm.setProjects(Arrays.asList(new ProjectModel()));
		BDDMockito.when(mockProductLineRepo.findByName(BDDMockito.anyString())).thenReturn(plm);

		String productLine = "foobar";
		productsService.deleteProductLine(productLine);
	}

	/////////////////
// ProjectFilter
/////////////////
	@Test
	public void createProjectFilterTestSuccess() throws BadProductNameException {
		BDDMockito.when(mockProjectFilterRepo.saveAndFlush(BDDMockito.any()))
				.thenAnswer(answer -> answer.getArgument(0));
		String projectFilter = "foobar";

		ProjectFilterModel plm = productsService.createProjectFilter(projectFilter);
		Assert.assertEquals(projectFilter, plm.getName());
	}

	@Test(expected = BadProductNameException.class)
	public void createProjectFilterTestFailEmpty() throws BadProductNameException {
		productsService.createProjectFilter("");
	}

	@Test(expected = BadProductNameException.class)
	public void createProjectFilterTestFailExists() throws BadProductNameException {
		BDDMockito.when(mockProjectFilterRepo.findByName(BDDMockito.anyString()))
				.thenReturn(new ProjectFilterModel());

		productsService.createProjectFilter("foobar");
	}

	@Test
	public void getProjectFilterTest() {
		productsService.getProjectFilter("");
		BDDMockito.verify(mockProjectFilterRepo).findByName(BDDMockito.anyString());
	}

	@Test
	public void getAllProjectFiltersTest() {
		productsService.getAllProjectFilters();
		BDDMockito.verify(mockProjectFilterRepo).findAllByOrderByNameAsc();
	}

	@Test
	public void renameProjectFilterTestSuccess()
			throws BadProductNameException, ProductsNotFoundException {
		String productLine = "foobar";
		String newProductLine = "foobar2";
		BDDMockito.when(mockProjectFilterRepo.saveAndFlush(BDDMockito.any()))
				.thenAnswer(answer -> answer.getArgument(0));
		BDDMockito.when(mockProjectFilterRepo.findByName(BDDMockito.endsWith(productLine)))
				.thenReturn(new ProjectFilterModel());
		BDDMockito.when(mockProjectFilterRepo.findByName(BDDMockito.endsWith(newProductLine)))
				.thenReturn(null);

		ProjectFilterModel plm = productsService.renameProjectFilter(productLine, newProductLine);
		Assert.assertEquals(newProductLine, plm.getName());
	}

	@Test(expected = BadProductNameException.class)
	public void renameProjectFilterTestFailEmpty()
			throws BadProductNameException, ProductsNotFoundException {
		String productLine = "foobar";
		String newProductLine = "";
		productsService.renameProjectFilter(productLine, newProductLine);
	}

	@Test(expected = BadProductNameException.class)
	public void renameProjectFilterTestFailExists()
			throws BadProductNameException, ProductsNotFoundException {
		BDDMockito.when(mockProjectFilterRepo.findByName(BDDMockito.anyString()))
				.thenReturn(new ProjectFilterModel());
		String productLine = "foobar";
		String newProductLine = "foobar2";
		productsService.renameProjectFilter(productLine, newProductLine);
	}

	@Test(expected = ProductsNotFoundException.class)
	public void renameProjectFilterTestOldNotExist()
			throws BadProductNameException, ProductsNotFoundException {
		String productLine = "foobar";
		String newProductLine = "foobar2";
		BDDMockito.when(mockProjectFilterRepo.saveAndFlush(BDDMockito.any()))
				.thenAnswer(answer -> answer.getArgument(0));
		BDDMockito.when(mockProjectFilterRepo.findByName(BDDMockito.endsWith(productLine)))
				.thenReturn(null);
		BDDMockito.when(mockProjectFilterRepo.findByName(BDDMockito.endsWith(newProductLine)))
				.thenReturn(null);

		productsService.renameProjectFilter(productLine, newProductLine);
	}

	@Test
	public void deleteProjectFilterTestSuccess() throws ProductsNotFoundException {
		ProjectFilterModel pfm = new ProjectFilterModel();
		pfm.setProjects(new ArrayList<>());

		BDDMockito.when(mockProjectFilterRepo.findByName(BDDMockito.anyString())).thenReturn(pfm);

		productsService.deleteProjectFilter("productLine");
		ArgumentCaptor<ProjectFilterModel> captor = ArgumentCaptor
				.forClass(ProjectFilterModel.class);
		BDDMockito.verify(mockProjectFilterRepo).delete(captor.capture());

		Assert.assertEquals(pfm, captor.getValue());
	}

	@Test(expected = ProductsNotFoundException.class)
	public void deleteProjectFilterTestFailUnknown() throws ProductsNotFoundException {
		String productLine = "foobar";
		productsService.deleteProjectFilter(productLine);
	}

	@Test
	public void deleteProjectFilterTestFailOrphaned() throws ProductsNotFoundException {
		ProjectFilterModel pfm = new ProjectFilterModel();
		pfm.setProjects(Arrays.asList(new ProjectModel()));
		BDDMockito.when(mockProjectFilterRepo.findByName(BDDMockito.anyString())).thenReturn(pfm);

		String productLine = "foobar";
		productsService.deleteProjectFilter(productLine);
	}

	/////////////////
// Project
/////////////////
	@Test
	public void createProjectTestSuccess()
			throws BadProductNameException, ProductsNotFoundException {
		ProductLineModel plm = new ProductLineModel();
		BDDMockito.when(mockProductLineRepo.findByName(BDDMockito.anyString())).thenReturn(plm);
		BDDMockito.when(mockProjectRepo.saveAndFlush(BDDMockito.any()))
				.thenAnswer(answer -> answer.getArgument(0));
		String project = "foobar";
		String productLine = "productLine";
		ProjectModel pm = productsService.createProject(project, productLine);
		Assert.assertEquals(project, pm.getName());
		Assert.assertEquals(plm, pm.getOwningProductLine());
	}

	@Test(expected = BadProductNameException.class)
	public void createProjectTestFailEmpty()
			throws BadProductNameException, ProductsNotFoundException {
		productsService.createProject("", "");
	}

	@Test(expected = BadProductNameException.class)
	public void createProjectTestFailExists()
			throws BadProductNameException, ProductsNotFoundException {
		BDDMockito.when(mockProjectRepo.findByName(BDDMockito.anyString()))
				.thenReturn(new ProjectModel());

		productsService.createProject("foobar", "");
	}

	@Test(expected = ProductsNotFoundException.class)
	public void createProjectTestFailPLMDoesntExist()
			throws BadProductNameException, ProductsNotFoundException {
		BDDMockito.when(mockProductLineRepo.findByName(BDDMockito.anyString())).thenReturn(null);
		productsService.createProject("foobar", "foo");
	}

	@Test
	public void getProjectTest() {
		productsService.getProject("");
		BDDMockito.verify(mockProjectRepo).findByName(BDDMockito.anyString());
	}

	@Test
	public void getAllProjectsTest() {
		productsService.getAllProjects();
		BDDMockito.verify(mockProjectRepo).findAllByOrderByNameAsc();
	}

	@Test
	public void renameProjectTestSuccess()
			throws BadProductNameException, ProductsNotFoundException {
		String project = "foobar";
		String newProject = "foobar2";
		BDDMockito.when(mockProjectRepo.saveAndFlush(BDDMockito.any()))
				.thenAnswer(answer -> answer.getArgument(0));
		BDDMockito.when(mockProjectRepo.findByName(BDDMockito.endsWith(project)))
				.thenReturn(new ProjectModel());
		BDDMockito.when(mockProjectRepo.findByName(BDDMockito.endsWith(newProject)))
				.thenReturn(null);

		ProjectModel pm = productsService.renameProject(project, newProject);
		Assert.assertEquals(newProject, pm.getName());
	}

	@Test(expected = BadProductNameException.class)
	public void renameProjectTestFailEmpty()
			throws BadProductNameException, ProductsNotFoundException {
		String project = "foobar";
		String newProject = "";
		productsService.renameProject(project, newProject);
	}

	@Test(expected = BadProductNameException.class)
	public void renameProjectTestFailExists()
			throws BadProductNameException, ProductsNotFoundException {
		BDDMockito.when(mockProjectRepo.findByName(BDDMockito.anyString()))
				.thenReturn(new ProjectModel());
		String project = "foobar";
		String newProject = "foobar2";
		productsService.renameProject(project, newProject);
	}

	@Test(expected = ProductsNotFoundException.class)
	public void renameProjectTestOldNotExist()
			throws BadProductNameException, ProductsNotFoundException {
		String project = "foobar";
		String newProject = "foobar2";
		BDDMockito.when(mockProjectRepo.saveAndFlush(BDDMockito.any()))
				.thenAnswer(answer -> answer.getArgument(0));
		BDDMockito.when(mockProjectRepo.findByName(BDDMockito.endsWith(project))).thenReturn(null);
		BDDMockito.when(mockProjectRepo.findByName(BDDMockito.endsWith(newProject)))
				.thenReturn(null);

		productsService.renameProject(project, newProject);
	}

	@Test
	public void moveProjectTestSuccess() throws BadProductNameException, ProductsNotFoundException {
		String project = "foobar";
		ProjectModel pm = new ProjectModel();
		pm.setName(project);

		String newProductLine = "foobar2";
		ProductLineModel plm = new ProductLineModel();
		plm.setName(newProductLine);

		BDDMockito.when(mockProjectRepo.saveAndFlush(BDDMockito.any()))
				.thenAnswer(answer -> answer.getArgument(0));
		BDDMockito.when(mockProjectRepo.findByName(BDDMockito.anyString())).thenReturn(pm);
		BDDMockito.when(mockProductLineRepo.findByName(BDDMockito.endsWith(newProductLine)))
				.thenReturn(plm);

		ProjectModel pmNew = productsService.moveProject(project, newProductLine);
		Assert.assertEquals(plm, pmNew.getOwningProductLine());
		Assert.assertEquals(project, pmNew.getName());
	}

	@Test(expected = BadProductNameException.class)
	public void moveProjectTestFailEmpty()
			throws BadProductNameException, ProductsNotFoundException {
		String project = "";
		String newProductLine = "foobar2";
		productsService.moveProject(project, newProductLine);
	}

	@Test(expected = ProductsNotFoundException.class)
	public void moveProjectTestFailProjectDoesntExist()
			throws BadProductNameException, ProductsNotFoundException {
		String project = "foobar";
		String newProject = "foobar2";
		productsService.moveProject(project, newProject);
	}

	@Test(expected = ProductsNotFoundException.class)
	public void moveProjectTestFailProductLineDoesntExist()
			throws BadProductNameException, ProductsNotFoundException {
		String project = "foobar";
		String newProject = "foobar2";
		BDDMockito.when(mockProjectRepo.findByName(BDDMockito.anyString()))
				.thenReturn(new ProjectModel());
		productsService.moveProject(project, newProject);
	}

	@Test
	public void setProjectsForFilterTestSuccess()
			throws BadProductNameException, ProductsNotFoundException {
		String project1 = "proj1";
		String project2 = "proj2";
		ProjectModel pm1 = new ProjectModel();
		pm1.setName(project1);
		ProjectModel pm2 = new ProjectModel();
		pm2.setName(project2);

		String filterName = "filter";
		ProjectFilterModel pfm = new ProjectFilterModel();
		pfm.setName(filterName);

		BDDMockito.when(mockProjectFilterRepo.findByName(filterName)).thenReturn(pfm);

		BDDMockito.when(mockProjectRepo.findByName(project1)).thenReturn(pm1);
		BDDMockito.when(mockProjectRepo.findByName(project2)).thenReturn(pm2);

		BDDMockito.when(mockProjectFilterRepo.saveAndFlush(BDDMockito.any()))
				.thenAnswer(answer -> answer.getArgument(0));

		ProjectFilterModel pfmNew = productsService
				.setProjectsForFilter(Arrays.asList(project1, project2), filterName);
		Assert.assertTrue(pfmNew.getProjectNames().containsAll(Arrays.asList(project1, project2)));
		Assert.assertEquals(filterName, pfmNew.getName());
	}

	@Test
	public void setProjectsForFilterTestSuccessEmptyProjects()
			throws BadProductNameException, ProductsNotFoundException {
		String project1 = "proj1";
		ProjectModel pm1 = new ProjectModel();
		pm1.setName(project1);

		String filterName = "filter";
		ProjectFilterModel pfm = new ProjectFilterModel();
		pfm.setName(filterName);
		pfm.setProjects(Arrays.asList(pm1));

		BDDMockito.when(mockProjectFilterRepo.findByName(filterName)).thenReturn(pfm);

		BDDMockito.when(mockProjectFilterRepo.saveAndFlush(BDDMockito.any()))
				.thenAnswer(answer -> answer.getArgument(0));

		ProjectFilterModel pfmNew = productsService.setProjectsForFilter(null, filterName);
		Assert.assertTrue(pfmNew.getProjectNames().isEmpty());
		Assert.assertEquals(filterName, pfmNew.getName());
	}

	@Test(expected = BadProductNameException.class)
	public void setProjectsForFilterTestFailEmpty()
			throws BadProductNameException, ProductsNotFoundException {
		productsService.setProjectsForFilter(new ArrayList<>(), "");
	}

	@Test(expected = ProductsNotFoundException.class)
	public void setProjectsForFilterTestFailBadFilter()
			throws BadProductNameException, ProductsNotFoundException {
		String filterName = "filter";
		BDDMockito.when(mockProjectFilterRepo.findByName(filterName)).thenReturn(null);

		productsService.setProjectsForFilter(new ArrayList<>(), filterName);
	}

	@Test(expected = ProductsNotFoundException.class)
	public void setProjectsForFilterTestFailBadProject()
			throws BadProductNameException, ProductsNotFoundException {
		String project1 = "proj1";

		String filterName = "filter";
		ProjectFilterModel pfm = new ProjectFilterModel();
		pfm.setName(filterName);

		BDDMockito.when(mockProjectFilterRepo.findByName(filterName)).thenReturn(pfm);

		BDDMockito.when(mockProjectRepo.findByName(project1)).thenReturn(null);

		productsService.setProjectsForFilter(Arrays.asList(project1), filterName);
	}

	@Test
	public void removeProjectFromFilterTestSuccess()
			throws BadProductNameException, ProductsNotFoundException {
		String filterName = "filter";
		ProjectFilterModel pfm = new ProjectFilterModel();
		pfm.setName(filterName);

		String project1 = "proj1";
		ProjectModel pm1 = new ProjectModel();
		pm1.setName(project1);
		pm1.setFilters(new ArrayList<>(Arrays.asList(pfm)));

		BDDMockito.when(mockProjectFilterRepo.findByName(filterName)).thenReturn(pfm);
		BDDMockito.when(mockProjectRepo.findByName(project1)).thenReturn(pm1);
		BDDMockito.when(mockProjectRepo.saveAndFlush(BDDMockito.any()))
				.thenAnswer(answer -> answer.getArgument(0));

		ProjectModel pmRemoved = productsService.removeProjectFromFilter(project1, filterName);
		Assert.assertTrue(pmRemoved.getFilters().isEmpty());
		Assert.assertEquals(project1, pmRemoved.getName());
	}

	@Test(expected = BadProductNameException.class)
	public void removeProjectFromFilterTestFailEmpty()
			throws BadProductNameException, ProductsNotFoundException {
		productsService.removeProjectFromFilter("", "");
	}

	@Test(expected = ProductsNotFoundException.class)
	public void removeProjectFromFilterTestFailBadFilter()
			throws BadProductNameException, ProductsNotFoundException {
		String filterName = "filter";
		String project1 = "proj1";

		productsService.removeProjectFromFilter(project1, filterName);
	}

	@Test(expected = ProductsNotFoundException.class)
	public void removeProjectFromFilterTestFailBadProject()
			throws BadProductNameException, ProductsNotFoundException {
		String filterName = "filter";
		ProjectFilterModel pfm = new ProjectFilterModel();
		pfm.setName(filterName);

		String project1 = "proj1";

		BDDMockito.when(mockProjectFilterRepo.findByName(filterName)).thenReturn(pfm);

		productsService.removeProjectFromFilter(project1, filterName);
	}

	@Test
	public void deleteProjectTestSuccess() throws ProductsNotFoundException {
		String project1 = "proj1";
		ProjectModel pm1 = new ProjectModel();
		pm1.setName(project1);
		pm1.setFilters(new ArrayList<>());
		BDDMockito.when(mockProjectRepo.findByName(project1)).thenReturn(pm1);
		productsService.deleteProject(project1);
		BDDMockito.verify(mockProjectRepo).delete(pm1);
	}

	@Test(expected = ProductsNotFoundException.class)
	public void deleteProjectTestFail() throws ProductsNotFoundException {
		String project1 = "proj1";
		productsService.deleteProject(project1);
	}
}
