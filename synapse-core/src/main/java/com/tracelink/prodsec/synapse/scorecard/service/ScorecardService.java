package com.tracelink.prodsec.synapse.scorecard.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tracelink.prodsec.synapse.products.ProductsNotFoundException;
import com.tracelink.prodsec.synapse.products.model.ProductLineModel;
import com.tracelink.prodsec.synapse.products.model.ProjectFilterModel;
import com.tracelink.prodsec.synapse.products.model.ProjectModel;
import com.tracelink.prodsec.synapse.products.service.ProductsService;
import com.tracelink.prodsec.synapse.scheduler.job.SimpleSchedulerJob;
import com.tracelink.prodsec.synapse.scheduler.service.SchedulerService;
import com.tracelink.prodsec.synapse.scheduler.service.schedule.PeriodicSchedule;
import com.tracelink.prodsec.synapse.scorecard.model.Scorecard;
import com.tracelink.prodsec.synapse.scorecard.model.ScorecardColumn;
import com.tracelink.prodsec.synapse.scorecard.model.ScorecardRow;
import com.tracelink.prodsec.synapse.scorecard.model.ScorecardValue;

/**
 * The Scorecard Service handles business logic for the scorecard generation.
 *
 * @author csmith
 */
@Service
public class ScorecardService {

	public static final String NOT_READY_MESSAGE = "Scorecards are being generated and will be available soon";

	private final List<ScorecardColumn> scorecardColumnDefs = new ArrayList<>();

	private final ProductsService productsService;

	private boolean ready = false;

	private Scorecard topLevelCache = null;
	private Map<String, Scorecard> productLineScorecardCache = new HashMap<String, Scorecard>();
	private Map<String, Scorecard> filterScorecardCache = new HashMap<String, Scorecard>();
	private Map<String, Scorecard> projectScorecardCache = new HashMap<String, Scorecard>();

	public ScorecardService(@Autowired ProductsService productsService, @Autowired SchedulerService scheduler) {
		this.productsService = productsService;
		scheduler.scheduleInternalJob(new SimpleSchedulerJob("Scorecard Updater")
				.onSchedule(new PeriodicSchedule(15, TimeUnit.MINUTES)).withJob(() -> {
					updateAll();
				}));
	}

	/**
	 * Adds a column to the Synapse Scorecard.
	 *
	 * @param column the column to add
	 */
	public void addColumn(ScorecardColumn column) {
		scorecardColumnDefs.add(column);
	}

	/**
	 * Marked ready only after the first time all scorecards are generated. The get
	 * methods in this class will return nulls until this is true
	 * 
	 * @return true if the service is ready to give scorecards, false otherwise.
	 */
	public boolean isReady() {
		return ready;
	}

	public void updateAll() {
		//skip if scorecard columns haven't been defined
		if(scorecardColumnDefs.isEmpty()) {
			return;
		}
		updateTopLevelScorecard();
		productsService.getAllProductLines().forEach(plm -> updateProductLineScorecard(plm));
		productsService.getAllProjects().forEach(p -> updateProjectScorecard(p));
		productsService.getAllProjectFilters().forEach(f -> updateFilterScorecard(f));
		ready = true;
	}

	/**
	 * The top level scorecard is the overview of the productlines
	 *
	 * @return the scorecard for all productlines
	 */
	public Scorecard getTopLevelScorecard() {
		return topLevelCache;
	}

	private void updateTopLevelScorecard() {
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
		topLevelCache = scorecard;
	}

	/**
	 * Generate a scorecard for a given productline
	 *
	 * @param productLineName the name of the productline
	 * @return the scorecard for this productline
	 * @throws ProductsNotFoundException if the productline does not exist
	 */
	public Scorecard getScorecardForProductLine(String productLineName) throws ProductsNotFoundException {
		Scorecard scorecard = productLineScorecardCache.get(productLineName);
		if (scorecard == null) {
			if(productsService.getProductLine(productLineName) == null) {
				throw new ProductsNotFoundException("Unknown Product Line Name");
			}
			throw new ProductsNotFoundException("Scorecard has not been generated yet");
		}
		return scorecard;
	}

	private void updateProductLineScorecard(ProductLineModel productLine) {
		Scorecard scorecard = new Scorecard();
		scorecardColumnDefs.forEach(scorecard::addColumn);

		productLine.getProjects().forEach(project -> {
			ScorecardRow row = new ScorecardRow(project.getName());
			scorecardColumnDefs.forEach(def -> {
				ScorecardValue result = def.hasProjectCallback() ? def.getProjectCallbackFunction().apply(project)
						: ScorecardValue.BLANK;
				row.addOrderedResult(result);
			});
			scorecard.addRow(row);
		});
		scorecard.finalizeScorecard();
		productLineScorecardCache.put(productLine.getName(), scorecard);
	}

	/**
	 * Generate a scorecard for a given project filter
	 *
	 * @param filterName the name of the project filter
	 * @return the scorecard for this project filter
	 * @throws ProductsNotFoundException if the project filter does not exist
	 */
	public Scorecard getScorecardForFilter(String filterName) throws ProductsNotFoundException {
		Scorecard scorecard = filterScorecardCache.get(filterName);
		if (scorecard == null) {
			if(productsService.getProjectFilter(filterName) == null) {
				throw new ProductsNotFoundException("Unknown Filter Name");
			}
			throw new ProductsNotFoundException("Scorecard has not been generated yet");
		}
		return scorecard;
	}

	private void updateFilterScorecard(ProjectFilterModel filter) {
		Scorecard scorecard = new Scorecard();
		scorecardColumnDefs.forEach(scorecard::addColumn);

		filter.getProjects().forEach(project -> {
			ScorecardRow row = new ScorecardRow(project.getName());
			scorecardColumnDefs.forEach(def -> {
				ScorecardValue result = def.hasProjectCallback() ? def.getProjectCallbackFunction().apply(project)
						: ScorecardValue.BLANK;
				row.addOrderedResult(result);
			});
			scorecard.addRow(row);
		});
		scorecard.finalizeScorecard();
		filterScorecardCache.put(filter.getName(), scorecard);
	}

	/**
	 * Generate a scorecard for a given project
	 *
	 * @param projectName the name of the project
	 * @return the scorecard for this project
	 * @throws ProductsNotFoundException if the project does not exist
	 */
	public Scorecard getScorecardForProject(String projectName) throws ProductsNotFoundException {
		Scorecard scorecard = projectScorecardCache.get(projectName);
		if (scorecard == null) {
			if(productsService.getProject(projectName) == null) {
				throw new ProductsNotFoundException("Unknown Project Name");
			}
			throw new ProductsNotFoundException("Scorecard has not been generated yet");
		}
		return scorecard;
	}

	private void updateProjectScorecard(ProjectModel project) {
		Scorecard scorecard = new Scorecard();
		scorecardColumnDefs.forEach(scorecard::addColumn);

		ScorecardRow row = new ScorecardRow(project.getName());
		scorecardColumnDefs.forEach(def -> {
			ScorecardValue result = def.hasProjectCallback() ? def.getProjectCallbackFunction().apply(project)
					: ScorecardValue.BLANK;
			row.addOrderedResult(result);
		});
		scorecard.addRow(row);
		scorecard.finalizeScorecard();
		projectScorecardCache.put(project.getName(), scorecard);
	}
}
