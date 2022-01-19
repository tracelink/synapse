package com.tracelink.prodsec.synapse.scorecard.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Function;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import com.tracelink.prodsec.synapse.products.ProductsNotFoundException;
import com.tracelink.prodsec.synapse.products.model.ProductLineModel;
import com.tracelink.prodsec.synapse.products.model.ProjectFilterModel;
import com.tracelink.prodsec.synapse.products.model.ProjectModel;
import com.tracelink.prodsec.synapse.products.service.ProductsService;
import com.tracelink.prodsec.synapse.scheduler.service.SchedulerService;
import com.tracelink.prodsec.synapse.scorecard.model.Scorecard;
import com.tracelink.prodsec.synapse.scorecard.model.ScorecardColumn;
import com.tracelink.prodsec.synapse.scorecard.model.ScorecardValue;
import com.tracelink.prodsec.synapse.scorecard.model.ScorecardValue.TrafficLight;
import com.tracelink.prodsec.synapse.scorecard.model.SimpleScorecardColumn;

@RunWith(SpringRunner.class)
public class ScorecardServiceTest {

	private final Function<ProjectModel, ScorecardValue> projectCallback = (project) -> new ScorecardValue(
			project.getName(), TrafficLight.GREEN);
	private final Function<ProductLineModel, ScorecardValue> productLineCallback = (productLine) -> new ScorecardValue(
			productLine.getName(), TrafficLight.GREEN);

	@MockBean
	private ProductsService mockProductsService;

	@MockBean
	private SchedulerService mockSchedulerService;

	private ScorecardService scorecardService;

	@Before
	public void setup() {
		scorecardService = new ScorecardService(mockProductsService, mockSchedulerService);
	}

	@Test
	public void getTopLevelScorecardTest() {
		String plColumnName = "plColumn";
		String pColumnName = "pColumn";
		ScorecardColumn plColumn = new SimpleScorecardColumn(plColumnName).withProductLineCallback(productLineCallback);
		ScorecardColumn pColumn = new SimpleScorecardColumn(pColumnName).withProjectCallback(projectCallback);

		scorecardService.addColumn(pColumn);
		scorecardService.addColumn(plColumn);

		String plmName = "plm";
		ProductLineModel plm = new ProductLineModel();
		plm.setName(plmName);

		BDDMockito.when(mockProductsService.getAllProductLines()).thenReturn(Arrays.asList(plm)).thenReturn(new ArrayList<>());
		scorecardService.updateAll();
		Scorecard scorecard = scorecardService.getTopLevelScorecard();
		Assert.assertEquals(1, scorecard.getColumns().size());
		Assert.assertEquals(plColumnName, scorecard.getColumns().get(0).getColumnName());
		Assert.assertEquals(1, scorecard.getRows().size());
		Assert.assertEquals(plmName, scorecard.getRows().get(0).getRowName());
		Assert.assertEquals(1, scorecard.getRows().get(0).getResults().size());
		Assert.assertEquals(plmName, scorecard.getRows().get(0).getResults().get(0).getValue());
	}

	@Test
	public void getScorecardForProductLineTest() throws ProductsNotFoundException {
		String plColumnName = "plColumn";
		String pColumnName = "pColumn";
		ScorecardColumn plColumn = new SimpleScorecardColumn(plColumnName).withProductLineCallback(productLineCallback);
		ScorecardColumn pColumn = new SimpleScorecardColumn(pColumnName).withProjectCallback(projectCallback);

		scorecardService.addColumn(pColumn);
		scorecardService.addColumn(plColumn);

		String pmName = "pm";
		ProjectModel pm = new ProjectModel();
		pm.setName(pmName);

		String plmName = "plm";
		ProductLineModel plm = new ProductLineModel();
		plm.setName(plmName);
		plm.setProjects(Arrays.asList(pm));

		BDDMockito.when(mockProductsService.getAllProductLines()).thenReturn(Arrays.asList(plm));
		scorecardService.updateAll();
		Scorecard scorecard = scorecardService.getScorecardForProductLine(plmName);
		Assert.assertEquals(1, scorecard.getColumns().size());
		Assert.assertEquals(pColumnName, scorecard.getColumns().get(0).getColumnName());
		Assert.assertEquals(1, scorecard.getRows().size());
		Assert.assertEquals(pmName, scorecard.getRows().get(0).getRowName());
		Assert.assertEquals(1, scorecard.getRows().get(0).getResults().size());
		Assert.assertEquals(pmName, scorecard.getRows().get(0).getResults().get(0).getValue());
	}

	@Test(expected = ProductsNotFoundException.class)
	public void getScorecardForProductLineTestFail() throws ProductsNotFoundException {
		BDDMockito.when(mockProductsService.getProductLine(BDDMockito.anyString())).thenReturn(null);

		scorecardService.getScorecardForProductLine("foo");
	}

	@Test
	public void getScorecardForFilterTest() throws ProductsNotFoundException {
		String plColumnName = "plColumn";
		String pColumnName = "pColumn";
		ScorecardColumn plColumn = new SimpleScorecardColumn(plColumnName).withProductLineCallback(productLineCallback);
		ScorecardColumn pColumn = new SimpleScorecardColumn(pColumnName).withProjectCallback(projectCallback);

		scorecardService.addColumn(pColumn);
		scorecardService.addColumn(plColumn);

		String pmName = "pm";
		ProjectModel pm = new ProjectModel();
		pm.setName(pmName);

		String pfmName = "pfm";
		ProjectFilterModel pfm = new ProjectFilterModel();
		pfm.setName(pfmName);
		pfm.setProjects(Arrays.asList(pm));

		BDDMockito.when(mockProductsService.getAllProjectFilters()).thenReturn(Arrays.asList(pfm));
		scorecardService.updateAll();
		Scorecard scorecard = scorecardService.getScorecardForFilter(pfmName);
		Assert.assertEquals(1, scorecard.getColumns().size());
		Assert.assertEquals(pColumnName, scorecard.getColumns().get(0).getColumnName());
		Assert.assertEquals(1, scorecard.getRows().size());
		Assert.assertEquals(pmName, scorecard.getRows().get(0).getRowName());
		Assert.assertEquals(1, scorecard.getRows().get(0).getResults().size());
		Assert.assertEquals(pmName, scorecard.getRows().get(0).getResults().get(0).getValue());
	}

	@Test(expected = ProductsNotFoundException.class)
	public void getScorecardForFilterTestFail() throws ProductsNotFoundException {
		BDDMockito.when(mockProductsService.getProjectFilter(BDDMockito.anyString())).thenReturn(null);

		scorecardService.getScorecardForFilter("foo");
	}

	@Test
	public void getScorecardForProjectTest() throws ProductsNotFoundException {
		String plColumnName = "plColumn";
		String pColumnName = "pColumn";
		ScorecardColumn plColumn = new SimpleScorecardColumn(plColumnName).withProductLineCallback(productLineCallback);
		ScorecardColumn pColumn = new SimpleScorecardColumn(pColumnName).withProjectCallback(projectCallback);

		scorecardService.addColumn(pColumn);
		scorecardService.addColumn(plColumn);

		String pmName = "pm";
		ProjectModel pm = new ProjectModel();
		pm.setName(pmName);

		BDDMockito.when(mockProductsService.getAllProjects()).thenReturn(Arrays.asList(pm));
		scorecardService.updateAll();
		Scorecard scorecard = scorecardService.getScorecardForProject(pmName);
		Assert.assertEquals(1, scorecard.getColumns().size());
		Assert.assertEquals(pColumnName, scorecard.getColumns().get(0).getColumnName());
		Assert.assertEquals(1, scorecard.getRows().size());
		Assert.assertEquals(pmName, scorecard.getRows().get(0).getRowName());
		Assert.assertEquals(1, scorecard.getRows().get(0).getResults().size());
		Assert.assertEquals(pmName, scorecard.getRows().get(0).getResults().get(0).getValue());
	}

	@Test(expected = ProductsNotFoundException.class)
	public void getScorecardForProjectTestFail() throws ProductsNotFoundException {
		BDDMockito.when(mockProductsService.getProject(BDDMockito.anyString())).thenReturn(null);

		scorecardService.getScorecardForProject("foo");
	}
}
