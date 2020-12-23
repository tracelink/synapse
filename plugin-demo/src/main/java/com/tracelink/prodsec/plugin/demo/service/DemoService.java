package com.tracelink.prodsec.plugin.demo.service;

import com.tracelink.prodsec.plugin.demo.model.DemoItemModel;
import com.tracelink.prodsec.plugin.demo.model.DemoListModel;
import com.tracelink.prodsec.plugin.demo.model.DemoProjectEntity;
import com.tracelink.prodsec.plugin.demo.repo.DemoRepo;
import com.tracelink.prodsec.synapse.products.model.ProductLineModel;
import com.tracelink.prodsec.synapse.products.model.ProjectModel;
import com.tracelink.prodsec.synapse.products.service.ProductsService;
import com.tracelink.prodsec.synapse.scorecard.model.ScorecardValue;
import com.tracelink.prodsec.synapse.scorecard.model.ScorecardValue.TrafficLight;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service that handles all data flows for this demo plugin.
 *
 * @author csmith
 */
@Service
public class DemoService {

	/**
	 * Demo how to include the SLF4J logger
	 */
	private static final Logger LOG = LoggerFactory.getLogger(DemoService.class);

	private final DemoRepo demoRepo;

	/*
	 * Using Spring's autowired DI on the constructor (recommended), you can pull in
	 * Synapse Services, if needed
	 */
	private final ProductsService productsService;

	public DemoService(@Autowired DemoRepo demoRepo, @Autowired ProductsService productsService) {
		this.demoRepo = demoRepo;
		this.productsService = productsService;
	}

	/**
	 * Return a demo list for all projects
	 *
	 * @return the DemoList for all projects
	 */
	public DemoListModel getFullDemoList() {
		List<ProjectModel> projects = productsService.getAllProjects();

		DemoListModel dlm = new DemoListModel();
		for (ProjectModel pm : projects) {
			DemoProjectEntity demoProject = demoRepo.findBySynapseProject(pm);
			boolean configured = demoProject != null;
			int vulns = configured ? demoProject.getVuln() : 0;
			dlm.addToModel(pm.getOwningProductLine().getName(),
					new DemoItemModel(pm.getName(), configured, vulns));
		}
		return dlm;
	}

	/**
	 * sets the vulnerability count to a DemoProjectEntity for a project
	 *
	 * @param project the synapse project to assign against
	 * @param vulns   the number of vulns
	 */
	public void assignVulnsToProject(ProjectModel project, int vulns) {
		DemoProjectEntity dpm = this.demoRepo.findBySynapseProject(project);
		if (dpm == null) {
			dpm = new DemoProjectEntity();
			dpm.setProjectModel(project);
		}
		dpm.setVuln(vulns);
		demoRepo.saveAndFlush(dpm);
	}

	/**
	 * return the number of vulns for a project
	 *
	 * @param project the synapse project to search against
	 * @return the number of vulns for the project
	 * @throws DemoNotFoundException if the synapse project doesn't exist
	 */
	public int getVulnsForProject(ProjectModel project) throws DemoNotFoundException {
		DemoProjectEntity dpm = this.demoRepo.findBySynapseProject(project);
		if (dpm == null) {
			throw new DemoNotFoundException("Could not find vulns for that project");
		}
		return dpm.getVuln();
	}

	/**
	 * For the Plugin's scheduled option, log the vuln count across all apps
	 */
	public void logVulns() {
		List<DemoProjectEntity> demos = this.demoRepo.findAll();

		int sum = demos.stream().map(DemoProjectEntity::getVuln).mapToInt(Integer::intValue).sum();
		long count = demos.size();

		LOG.info("Found " + sum + " vulns across " + count + " configured apps");
	}

	private TrafficLight getTrafficLight(int vulns) {
		TrafficLight color;
		if (vulns > 5) {
			color = TrafficLight.RED;
		} else if (vulns > 2) {
			color = TrafficLight.YELLOW;
		} else {
			color = TrafficLight.GREEN;
		}
		return color;
	}

	/**
	 * Method for the product line callback. Gets the count of vulns in a product
	 * line
	 *
	 * @param product the Synapse Product Line
	 * @return the number of vulns and severity for that number
	 */
	public ScorecardValue productLineCallback(ProductLineModel product) {
		// count vulns in projects
		int vulns = product.getProjects().stream().map(project -> {
			int val = 0;
			try {
				val = getVulnsForProject(project);
			} catch (DemoNotFoundException e) {
				// if we don't know about it, skip it
			}
			return val;
		}).mapToInt(Integer::intValue).sum();

		return new ScorecardValue("Vulns: " + vulns, getTrafficLight(vulns));
	}

	/**
	 * Method for the project callback. Gets the count of vulns in a project
	 *
	 * @param project the Synapse Project
	 * @return the number of vulns and severity for that number
	 */
	public ScorecardValue projectCallback(ProjectModel project) {
		ScorecardValue value = new ScorecardValue("Unconfigured", TrafficLight.NONE);
		try {
			int vulns = getVulnsForProject(project);
			value = new ScorecardValue("Vulns: " + vulns, getTrafficLight(vulns));
		} catch (DemoNotFoundException e) {
			// toss back as unconfigured
		}

		return value;
	}

}
