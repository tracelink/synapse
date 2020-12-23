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
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Handles business logic around CRUD operations on Projects, ProductLines, and
 * ProjectFilters
 *
 * @author csmith
 */
@Service
public class ProductsService {

	private final ProductLineRepo productLineRepo;
	private final ProjectFilterRepo projectFilterRepo;
	private final ProjectRepo projectRepo;

	public ProductsService(@Autowired ProductLineRepo productLineRepo,
			@Autowired ProjectFilterRepo projectFilterRepo,
			@Autowired ProjectRepo projectRepo) {
		this.productLineRepo = productLineRepo;
		this.projectFilterRepo = projectFilterRepo;
		this.projectRepo = projectRepo;
	}

	/////////////////
	// ProductLine
	/////////////////

	/**
	 * Create a new product line from the given name
	 *
	 * @param productLine the desired name of the productline
	 * @return the created productline
	 * @throws BadProductNameException if the productLine is blank/null or if the
	 *                                 productline already exists
	 */
	public ProductLineModel createProductLine(String productLine) throws BadProductNameException {
		// productLine name must be a real name
		if (StringUtils.isEmpty(productLine)) {
			throw new BadProductNameException("Product Line name may not be null/empty");
		}

		// productLine cannot be a duplicate
		if (getProductLine(productLine) != null) {
			throw new BadProductNameException(
					"Product Line name: " + productLine + " already exists");
		}
		ProductLineModel prodLine = new ProductLineModel();
		prodLine.setName(productLine);
		return productLineRepo.saveAndFlush(prodLine);
	}

	public ProductLineModel getProductLine(String productLine) {
		return productLineRepo.findByName(productLine);
	}

	public List<ProductLineModel> getAllProductLines() {
		return productLineRepo.findAllByOrderByNameAsc();
	}

	/**
	 * Rename a ProductLine
	 *
	 * @param oldProductLine the current name of a product line
	 * @param newProductLine the new name of the product line
	 * @return the newly renamed productline
	 * @throws BadProductNameException   if the newProductLine is blank/null, or if
	 *                                   the new name is already taken
	 * @throws ProductsNotFoundException if the old name doesn't match a ProductLine
	 */
	public ProductLineModel renameProductLine(String oldProductLine, String newProductLine)
			throws BadProductNameException, ProductsNotFoundException {
		// Product Line name must be a real name
		if (StringUtils.isEmpty(newProductLine)) {
			throw new BadProductNameException("Product Line name may not be null/empty");
		}

		// Product Line cannot be a duplicate
		if (getProductLine(newProductLine) != null) {
			throw new BadProductNameException(
					"Product Line name: " + newProductLine + " already exists");
		}
		ProductLineModel model = getProductLine(oldProductLine);

		// old Product Line must exist
		if (model == null) {
			throw new ProductsNotFoundException(
					"Product Line name:" + oldProductLine + " does not exist");
		}
		model.setName(newProductLine);
		return productLineRepo.saveAndFlush(model);
	}

	/**
	 * Delete a product line
	 *
	 * @param productLine the name of the productline to delete
	 * @throws OrphanedException         if this operation will leave any project
	 *                                   without a parent (also lists the projects)
	 * @throws ProductsNotFoundException if the product line doesn't exist
	 */
	public void deleteProductLine(String productLine)
			throws OrphanedException, ProductsNotFoundException {
		// do not orphan Projects under this product line
		ProductLineModel model = getProductLine(productLine);
		if (model == null) {
			throw new ProductsNotFoundException("Unknown Product Line: " + productLine);
		}
		if (!model.getProjects().isEmpty()) {
			throw new OrphanedException(
					"Cannot orphan Projects by deleting this Product Line. Projects: "
							+ String.join(", ", model.getProjectNames()));
		}
		productLineRepo.delete(model);
	}

	/////////////////
	// ProjectFilter
	/////////////////

	/**
	 * Create a new ProjectFilter from the given name
	 *
	 * @param projectFilterName the desired name of the ProjectFilter
	 * @return the created ProjectFilter
	 * @throws BadProductNameException if the projectFilterName is blank/null or if
	 *                                 the ProjectFilter already exists
	 */
	public ProjectFilterModel createProjectFilter(String projectFilterName)
			throws BadProductNameException {
		// project filter name must be real
		if (StringUtils.isEmpty(projectFilterName)) {
			throw new BadProductNameException("Project Filter name may not be null/empty");
		}
		// project filter cannot be a duplicate
		if (projectFilterRepo.findByName(projectFilterName) != null) {
			throw new BadProductNameException(
					"Project Filter name: " + projectFilterName + " already exists");
		}
		ProjectFilterModel pfm = new ProjectFilterModel();
		pfm.setName(projectFilterName);
		return projectFilterRepo.saveAndFlush(pfm);
	}

	public ProjectFilterModel getProjectFilter(String projectFilterName) {
		return projectFilterRepo.findByName(projectFilterName);
	}

	public List<ProjectFilterModel> getAllProjectFilters() {
		return projectFilterRepo.findAllByOrderByNameAsc();
	}

	/**
	 * Rename a ProjectFilter
	 *
	 * @param oldProjectFilterName the current name of a ProjectFilter
	 * @param newProjectFilterName the new name of the ProjectFilter
	 * @return the newly renamed ProjectFilter
	 * @throws BadProductNameException   if the newProjectFilterName is blank/null,
	 *                                   or if the new name is already taken
	 * @throws ProductsNotFoundException if the old name doesn't match a
	 *                                   ProjectFilter
	 */
	public ProjectFilterModel renameProjectFilter(String oldProjectFilterName,
			String newProjectFilterName)
			throws BadProductNameException, ProductsNotFoundException {
		// projectFilter name must be a real name
		if (StringUtils.isEmpty(newProjectFilterName)) {
			throw new BadProductNameException("Project Filter name may not be null/empty");
		}

		// projectFilter cannot be a duplicate
		if (getProjectFilter(newProjectFilterName) != null) {
			throw new BadProductNameException(
					"Project Filter name: " + newProjectFilterName + " already exists");
		}
		ProjectFilterModel model = getProjectFilter(oldProjectFilterName);
		if (model == null) {
			throw new ProductsNotFoundException("Unknown Project Filter: " + oldProjectFilterName);
		}
		model.setName(newProjectFilterName);
		return projectFilterRepo.saveAndFlush(model);
	}

	/**
	 * Delete a ProjectFilter
	 *
	 * @param projectFilterName the name of the ProjectFilter to delete
	 * @throws ProductsNotFoundException if the ProjectFilter doesn't exist
	 */
	public void deleteProjectFilter(String projectFilterName) throws ProductsNotFoundException {
		ProjectFilterModel model = getProjectFilter(projectFilterName);
		if (model == null) {
			throw new ProductsNotFoundException("Unknown Project Filter: " + projectFilterName);
		}
		projectFilterRepo.delete(model);
		projectFilterRepo.flush();
	}

	/////////////////
	// Project
	/////////////////

	/**
	 * Create a new Project from the given name
	 *
	 * @param projectName     the desired name of the Project
	 * @param productLineName the name of the product line to attach this Project to
	 * @return the created Project
	 * @throws BadProductNameException   if the projectName is blank/null or if the
	 *                                   Project already exists
	 * @throws ProductsNotFoundException if the productline doesn't exist
	 */
	public ProjectModel createProject(String projectName, String productLineName)
			throws BadProductNameException, ProductsNotFoundException {
		// project name must be real
		if (StringUtils.isEmpty(projectName)) {
			throw new BadProductNameException("Project name may not be null/empty");
		}

		// project cannot be a duplicate
		if (projectRepo.findByName(projectName) != null) {
			throw new BadProductNameException("Project name: " + projectName + " already exists");
		}

		// product line parent must exist
		ProductLineModel productLine = getProductLine(productLineName);
		if (productLine == null) {
			throw new ProductsNotFoundException(
					"Product Line: " + productLineName + " does not exist");
		}

		ProjectModel pm = new ProjectModel();
		pm.setName(projectName);
		pm.setOwningProductLine(productLine);
		return projectRepo.saveAndFlush(pm);
	}

	public ProjectModel getProject(String projectName) {
		return projectRepo.findByName(projectName);
	}

	public List<ProjectModel> getAllProjects() {
		return projectRepo.findAllByOrderByNameAsc();
	}

	/**
	 * Rename a Project
	 *
	 * @param oldProjectName the current name of a Project
	 * @param newProjectName the new name of the Project
	 * @return the newly renamed Project
	 * @throws BadProductNameException   if the newProjectName is blank/null, or if
	 *                                   the new name is already taken
	 * @throws ProductsNotFoundException if the old name doesn't match a
	 *                                   ProjectFilter
	 */
	public ProjectModel renameProject(String oldProjectName, String newProjectName)
			throws BadProductNameException, ProductsNotFoundException {
		// project name must be a real name
		if (StringUtils.isEmpty(newProjectName)) {
			throw new BadProductNameException("Project name may not be null/empty");
		}

		// project cannot be a duplicate
		if (getProject(newProjectName) != null) {
			throw new BadProductNameException(
					"Project name: " + newProjectName + " already exists");
		}
		ProjectModel model = getProject(oldProjectName);
		if (model == null) {
			throw new ProductsNotFoundException(
					"Project name: " + oldProjectName + " does not exist");
		}
		model.setName(newProjectName);
		return projectRepo.saveAndFlush(model);
	}

	/**
	 * Moves a project from one owning productline to another productline
	 *
	 * @param projectName        the name of the Project to move
	 * @param newProductLineName the name of the productline to move the project to
	 * @return the newly moved project
	 * @throws BadProductNameException   if the projectName is blank/null
	 * @throws ProductsNotFoundException if the project does not exist, or if the
	 *                                   new productline doesn't exist
	 */
	public ProjectModel moveProject(String projectName, String newProductLineName)
			throws BadProductNameException, ProductsNotFoundException {
		// Project name must be a real name
		if (StringUtils.isEmpty(projectName)) {
			throw new BadProductNameException("Project name may not be null/empty");
		}

		ProjectModel project = getProject(projectName);
		// Project must exist
		if (project == null) {
			throw new ProductsNotFoundException("Project: " + projectName + " does not exist");
		}

		ProductLineModel productLine = getProductLine(newProductLineName);
		if (productLine == null) {
			throw new ProductsNotFoundException(
					"Product Line: " + newProductLineName + " does not exist");
		}

		project.setOwningProductLine(productLine);
		return projectRepo.saveAndFlush(project);
	}

	/**
	 * Configure the list of projects that should be in a projectfilter
	 *
	 * @param projectNames the names of all projects to be configured in this
	 *                     project filter
	 * @param filterName   the name of the filter to configure against
	 * @return the projectfilter with the projects attached
	 * @throws BadProductNameException   if the filter name is blank/null
	 * @throws ProductsNotFoundException if the filter does not exist or if any
	 *                                   project in the list of names does not
	 *                                   exist. NOTE: if any project throws an
	 *                                   exception, the filter will not be updated
	 *                                   at all. This is an all-or-nothing operation
	 */
	public ProjectFilterModel setProjectsForFilter(List<String> projectNames, String filterName)
			throws BadProductNameException, ProductsNotFoundException {

		// new filter name must be real and exist
		if (StringUtils.isEmpty(filterName)) {
			throw new BadProductNameException("Filter name may not be null/empty");
		}

		ProjectFilterModel filter = getProjectFilter(filterName);
		if (filter == null) {
			throw new ProductsNotFoundException("Unknown Filter: " + filterName);
		}

		List<ProjectModel> currentProjects = new ArrayList<>();
		if (projectNames != null) {
			for (String projectName : projectNames) {
				// project must exist
				ProjectModel model = getProject(projectName);
				if (model == null) {
					throw new ProductsNotFoundException("Unknown Project name: " + projectName);
				}
				currentProjects.add(model);
			}
		}
		filter.setProjects(currentProjects);
		return projectFilterRepo.saveAndFlush(filter);
	}

	/**
	 * Remove a single project from a filter
	 *
	 * @param projectName   the project to remove
	 * @param oldFilterName the filter to remove from
	 * @return the project that was removed
	 * @throws BadProductNameException   if the filter name is blank/null
	 * @throws ProductsNotFoundException if the project or filter does not exist
	 */
	public ProjectModel removeProjectFromFilter(String projectName, String oldFilterName)
			throws BadProductNameException, ProductsNotFoundException {
		// old filter name must be real and exist
		if (StringUtils.isEmpty(oldFilterName)) {
			throw new BadProductNameException("Old Filter name may not be null/empty");
		}

		ProjectFilterModel oldFilter = getProjectFilter(oldFilterName);
		if (oldFilter == null) {
			throw new ProductsNotFoundException("Unknown Project Filter: " + oldFilterName);
		}

		// projectName must exist
		ProjectModel project = getProject(projectName);
		if (project == null) {
			throw new ProductsNotFoundException("Unknown Project name: " + projectName);
		}

		List<ProjectFilterModel> filters = project.getFilters();
		filters.remove(oldFilter);
		return projectRepo.saveAndFlush(project);
	}

	/**
	 * Delete a Project
	 *
	 * @param projectName the name of the Project to delete
	 * @throws ProductsNotFoundException if the Project doesn't exist
	 */
	public void deleteProject(String projectName) throws ProductsNotFoundException {
		ProjectModel model = getProject(projectName);
		if (model == null) {
			throw new ProductsNotFoundException("Unknown Project: " + projectName);
		}
		List<ProjectFilterModel> filters = model.getFilters();
		model.setFilters(new ArrayList<>());
		projectRepo.save(model);
		for (ProjectFilterModel filter : filters) {
			filter.getProjects().remove(model);
			projectFilterRepo.save(filter);
		}

		projectRepo.delete(model);
		projectRepo.flush();
	}
}
