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
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service class to handle business logic for SMEs.
 *
 * @author csmith
 */
@Service
public class SMEService {

	private final SMERepo smeRepo;
	private final ProductsService productsService;

	public SMEService(@Autowired SMERepo smeRepo, @Autowired ProductsService productsService) {
		this.smeRepo = smeRepo;
		this.productsService = productsService;
	}

	/**
	 * Create a new SME to be assigned projects
	 *
	 * @param smeName the Name of the SME
	 * @throws SMEException if the SME already exists
	 */
	public void addNewSME(String smeName) throws SMEException {
		SMEEntity sme = smeRepo.findByName(smeName);
		if (sme != null) {
			throw new SMEException("That SME already exists");
		}

		SMEEntity entity = new SMEEntity();
		entity.setName(smeName);
		smeRepo.saveAndFlush(entity);
	}

	/**
	 * Set all projects that a SME is assigned to. All projects must exist and be
	 * assignable in order to get any projects assigned
	 *
	 * @param smeName      the SME to receive projects
	 * @param projectNames the projects to assign
	 * @throws SMEException              if the SME doesn't exist
	 * @throws ProductsNotFoundException if any project doesn't exist
	 */
	public void setProjectsForSME(String smeName, List<String> projectNames)
			throws SMEException, ProductsNotFoundException {
		if (projectNames == null) {
			return;
		}

		SMEEntity sme = smeRepo.findByName(smeName);
		if (sme == null) {
			throw new SMEException("Could not find SME by that name");
		}

		List<ProjectModel> projects = new ArrayList<>();
		for (String projectName : projectNames) {
			ProjectModel project = productsService.getProject(projectName);
			if (project == null) {
				throw new ProductsNotFoundException("Unknown Project");
			}
			projects.add(project);
		}

		sme.setProjects(projects);
		smeRepo.saveAndFlush(sme);
	}

	/**
	 * Remove an individual project from a SME. Can also be done via Set by removing
	 * this one project
	 *
	 * @param smeName     the SME to remove the project from
	 * @param projectName the project to remove
	 * @throws SMEException              if the SME doesn't exist
	 * @throws ProductsNotFoundException if any project doesn't exist
	 */
	public void removeProjectFromSME(String smeName, String projectName)
			throws ProductsNotFoundException, SMEException {
		SMEEntity sme = smeRepo.findByName(smeName);
		if (sme == null) {
			throw new SMEException("Could not find SME by that name");
		}

		ProjectModel project = productsService.getProject(projectName);
		if (project == null) {
			throw new ProductsNotFoundException("Unknown Project");
		}

		List<ProjectModel> projects = sme.getProjects();
		if (projects != null && projects.contains(project)) {
			projects.remove(project);
			sme.setProjects(projects);
			smeRepo.saveAndFlush(sme);
		}
	}

	public List<SMEEntity> getAllSMEs() {
		return smeRepo.findAll();
	}

	/**
	 * Returns the {@linkplain ScorecardValue} containing the name of the SME. If
	 * there is an assigned SME, the {@linkplain TrafficLight} is Green, otherwise
	 * the SME is "None" and Red
	 *
	 * @param project the project to get the {@linkplain ScorecardValue} for
	 * @return Green and a name if a SME is assigned, Red and "None" otherwise
	 */
	public ScorecardValue scorecardCallbackProject(ProjectModel project) {
		List<SMEEntity> entity = smeRepo.findByProjects(project);

		String name = "None";
		TrafficLight tl = TrafficLight.RED;

		if (entity != null && !entity.isEmpty()) {
			name = entity.stream().map(SMEEntity::getName).collect(Collectors.joining(", "));
			tl = TrafficLight.GREEN;
		}

		return new ScorecardValue(name, tl);
	}

	/**
	 * Returns the {@linkplain ScorecardValue} containing the fraction of smes
	 * assigned over projects in this product line. The {@linkplain TrafficLight} is
	 * Red if there are 20% or fewer smes assigned, Yellow for 20 to 90 % coverage, and
	 * Green for over 90%
	 *
	 * @param productLine the product line to get the {@linkplain ScorecardValue}
	 *                    for
	 * @return Fraction of SMEs/Projects, and Green, Yellow, Red for over 90%, 20 to 90%,
	 * and under 20%, respectively
	 */
	public ScorecardValue scorecardCallbackProduct(ProductLineModel productLine) {
		List<ScorecardValue> values = productLine.getProjects().stream()
				.map(this::scorecardCallbackProject)
				.collect(Collectors.toList());
		int count = values.size();
		long smes = values.stream().filter(v -> v.getColor().equals(TrafficLight.GREEN)).count();
		int percentage = count == 0 ? 0 : (int) (smes * 100L / count);

		TrafficLight color = TrafficLight.RED;
		if (percentage > 90) {
			color = TrafficLight.GREEN;
		} else if (percentage > 20) {
			color = TrafficLight.YELLOW;
		}

		return new ScorecardValue(smes + "/" + count, color);
	}

}
