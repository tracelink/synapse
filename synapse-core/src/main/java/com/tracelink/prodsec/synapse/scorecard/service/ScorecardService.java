package com.tracelink.prodsec.synapse.scorecard.service;

import com.tracelink.prodsec.synapse.products.ProductsNotFoundException;
import com.tracelink.prodsec.synapse.products.model.ProductLineModel;
import com.tracelink.prodsec.synapse.products.model.ProjectFilterModel;
import com.tracelink.prodsec.synapse.products.model.ProjectModel;
import com.tracelink.prodsec.synapse.products.service.ProductsService;
import com.tracelink.prodsec.synapse.scorecard.model.Scorecard;
import com.tracelink.prodsec.synapse.scorecard.model.ScorecardColumn;
import com.tracelink.prodsec.synapse.scorecard.model.ScorecardRow;
import com.tracelink.prodsec.synapse.scorecard.model.ScorecardValue;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The Scorecard Service handles business logic for the scorecard generation.
 *
 * @author csmith
 */
@Service
public class ScorecardService {

	private final List<ScorecardColumn> scorecardColumnDefs = new ArrayList<>();

	private final ProductsService productsService;

	public ScorecardService(@Autowired ProductsService productsService) {
		this.productsService = productsService;
	}

	public void addColumn(ScorecardColumn column) {
		scorecardColumnDefs.add(column);
	}

	/**
	 * The top level scorecard is the overview of the productlines
	 *
	 * @return the scorecard for all productlines
	 */
	public Scorecard getTopLevelScorecard() {
		Scorecard scorecard = new Scorecard();

		scorecardColumnDefs.forEach(scorecard::addColumn);

		productsService.getAllProductLines().forEach(productLine -> {
			ScorecardRow row = new ScorecardRow(productLine.getName());
			scorecardColumnDefs.forEach(def -> {
				ScorecardValue result = def.hasProductLineCallback()
						? def.getProductLineCallbackFunction().apply(productLine)
						: ScorecardValue.BLANK;
				row.addOrderedResult(result);
			});
			scorecard.addRow(row);
		});
		scorecard.finalizeScorecard();
		return scorecard;
	}

	/**
	 * Generate a scorecard for a given productline
	 *
	 * @param productLineName the name of the productline
	 * @return the scorecard for this productline
	 * @throws ProductsNotFoundException if the productline does not exist
	 */
	public Scorecard getScorecardForProductLine(String productLineName)
			throws ProductsNotFoundException {
		Scorecard scorecard = new Scorecard();
		scorecardColumnDefs.forEach(scorecard::addColumn);

		ProductLineModel productLine = productsService.getProductLine(productLineName);
		if (productLine == null) {
			throw new ProductsNotFoundException("Unknown Product Line Name");
		}
		productLine.getProjects().forEach(project -> {
			ScorecardRow row = new ScorecardRow(project.getName());
			scorecardColumnDefs.forEach(def -> {
				ScorecardValue result =
						def.hasProjectCallback() ? def.getProjectCallbackFunction().apply(project)
								: ScorecardValue.BLANK;
				row.addOrderedResult(result);
			});
			scorecard.addRow(row);
		});
		scorecard.finalizeScorecard();
		return scorecard;
	}

	/**
	 * Generate a scorecard for a given project filter
	 *
	 * @param filterName the name of the project filter
	 * @return the scorecard for this project filter
	 * @throws ProductsNotFoundException if the project filter does not exist
	 */
	public Scorecard getScorecardForFilter(String filterName) throws ProductsNotFoundException {
		Scorecard scorecard = new Scorecard();
		scorecardColumnDefs.forEach(scorecard::addColumn);

		ProjectFilterModel filter = productsService.getProjectFilter(filterName);
		if (filter == null) {
			throw new ProductsNotFoundException("Unknown Filter Name");
		}
		filter.getProjects().forEach(project -> {
			ScorecardRow row = new ScorecardRow(project.getName());
			scorecardColumnDefs.forEach(def -> {
				ScorecardValue result =
						def.hasProjectCallback() ? def.getProjectCallbackFunction().apply(project)
								: ScorecardValue.BLANK;
				row.addOrderedResult(result);
			});
			scorecard.addRow(row);
		});
		scorecard.finalizeScorecard();
		return scorecard;
	}

	/**
	 * Generate a scorecard for a given project
	 *
	 * @param projectName the name of the project
	 * @return the scorecard for this project
	 * @throws ProductsNotFoundException if the project does not exist
	 */
	public Scorecard getScorecardForProject(String projectName) throws ProductsNotFoundException {
		Scorecard scorecard = new Scorecard();
		scorecardColumnDefs.forEach(scorecard::addColumn);

		ProjectModel project = productsService.getProject(projectName);
		if (project == null) {
			throw new ProductsNotFoundException("Unknown Project Name");
		}
		ScorecardRow row = new ScorecardRow(project.getName());
		scorecardColumnDefs.forEach(def -> {
			ScorecardValue result =
					def.hasProjectCallback() ? def.getProjectCallbackFunction().apply(project)
							: ScorecardValue.BLANK;
			row.addOrderedResult(result);
		});
		scorecard.addRow(row);
		scorecard.finalizeScorecard();
		return scorecard;
	}

}
