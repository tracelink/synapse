package com.tracelink.prodsec.plugin.veracode.sca.controller;

import com.tracelink.prodsec.plugin.veracode.sca.VeracodeScaPlugin;
import com.tracelink.prodsec.plugin.veracode.sca.model.VeracodeScaProject;
import com.tracelink.prodsec.plugin.veracode.sca.service.VeracodeScaProjectService;
import com.tracelink.prodsec.synapse.auth.SynapseAdminAuthDictionary;
import com.tracelink.prodsec.synapse.mvc.SynapseModelAndView;
import com.tracelink.prodsec.synapse.products.model.ProductLineModel;
import com.tracelink.prodsec.synapse.products.model.ProjectModel;
import com.tracelink.prodsec.synapse.products.service.ProductsService;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * The Veracode SCA mappings controller handles requests to the page for viewing and editing
 * mappings between Synapse {@link ProjectModel}s and {@link VeracodeScaProject}s.
 *
 * @author mcool
 */
@Controller
@RequestMapping(VeracodeScaPlugin.MAPPINGS_PAGE)
@PreAuthorize("hasAuthority('" + SynapseAdminAuthDictionary.ADMIN_PRIV + "')")
public class VeracodeScaMappingsController {

	private static final String MAPPINGS_REDIRECT = "redirect:" + VeracodeScaPlugin.MAPPINGS_PAGE;

	private final ProductsService productsService;
	private final VeracodeScaProjectService projectService;

	public VeracodeScaMappingsController(@Autowired ProductsService productsService,
			@Autowired VeracodeScaProjectService projectService) {
		this.productsService = productsService;
		this.projectService = projectService;
	}

	/**
	 * Returns necessary model objects and content view in a {@link SynapseModelAndView} object to
	 * render the Veracode SCA project mappings page.
	 *
	 * @return {@link SynapseModelAndView} containing all info to render the
	 * Veracode SCA project mappings page
	 */
	@GetMapping("")
	public SynapseModelAndView getMappings() {
		SynapseModelAndView mv = new SynapseModelAndView("veracode-sca-mappings");

		List<ProjectModel> synapseProjects = productsService.getAllProductLines().stream()
				.map(ProductLineModel::getProjects).flatMap(List::stream)
				.collect(Collectors.toList());
		List<VeracodeScaProject> mappedProjects = projectService.getMappedProjects();
		mappedProjects.sort(Comparator.comparing(VeracodeScaProject::getDisplayName));
		List<VeracodeScaProject> unmappedProjects = projectService.getUnmappedProjects();
		unmappedProjects.sort(Comparator.comparing(VeracodeScaProject::getDisplayName));

		mv.addObject("synapseProjects", synapseProjects);
		mv.addObject("mappedProjects", mappedProjects);
		mv.addObject("unmappedProjects", unmappedProjects);
		mv.addScriptReference("/scripts/veracode/sca/datatable.js");
		return mv;
	}

	/**
	 * Creates a mapping between the given Synapse project and Veracode SCA project.
	 *
	 * @param synapseProject     name of the Synapse project to link; if it does not exist, no
	 *                           mapping is created
	 * @param veracodeScaProject name of the Veracode SCA project to link; if it does not exist, no
	 *                           mapping is created
	 * @param redirectAttributes redirect attributes to render Flash attributes for success
	 * @return string redirecting to the mappings page
	 */
	@PostMapping("create")
	public String createMapping(@RequestParam String synapseProject,
			@RequestParam String veracodeScaProject, RedirectAttributes redirectAttributes) {
		projectService
				.createMapping(productsService.getProject(synapseProject), veracodeScaProject);
		redirectAttributes
				.addFlashAttribute(SynapseModelAndView.SUCCESS_FLASH, "Created new mapping.");
		return MAPPINGS_REDIRECT;
	}

	/**
	 * Deletes the Synapse mapping associated with the given Veracode SCA project.
	 *
	 * @param project            name of the Veracode SCA project to delete mapping from, if it
	 *                           exists
	 * @param redirectAttributes redirect attributes to render Flash attributes for success
	 * @return string redirecting to the mappings page
	 */
	@PostMapping("delete")
	public String deleteMapping(@RequestParam String project,
			RedirectAttributes redirectAttributes) {
		projectService.deleteMapping(project);
		redirectAttributes.addFlashAttribute(SynapseModelAndView.SUCCESS_FLASH, "Removed mapping.");
		return MAPPINGS_REDIRECT;
	}
}
