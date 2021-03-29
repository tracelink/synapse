package com.tracelink.prodsec.plugin.veracode.sca.controller;

import com.tracelink.prodsec.plugin.veracode.sca.VeracodeScaPlugin;
import com.tracelink.prodsec.plugin.veracode.sca.exception.VeracodeScaProductException;
import com.tracelink.prodsec.plugin.veracode.sca.model.VeracodeScaProject;
import com.tracelink.prodsec.plugin.veracode.sca.model.VeracodeScaWorkspace;
import com.tracelink.prodsec.plugin.veracode.sca.service.VeracodeScaProjectService;
import com.tracelink.prodsec.plugin.veracode.sca.service.VeracodeScaWorkspaceService;
import com.tracelink.prodsec.synapse.auth.SynapseAdminAuthDictionary;
import com.tracelink.prodsec.synapse.mvc.SynapseModelAndView;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * The Veracode SCA data management controller handles requests to the page for editing visibility
 * or deleting Veracode SCA workspaces and projects.
 *
 * @author mcool
 */
@Controller
@RequestMapping(VeracodeScaPlugin.DATA_MGMT_PAGE)
@PreAuthorize("hasAuthority('" + SynapseAdminAuthDictionary.ADMIN_PRIV + "')")
public class VeracodeScaDataMgmtController {

	private static final String DATA_MGMT_REDIRECT = "redirect:" + VeracodeScaPlugin.DATA_MGMT_PAGE;

	private final VeracodeScaWorkspaceService workspaceService;
	private final VeracodeScaProjectService projectService;

	public VeracodeScaDataMgmtController(@Autowired VeracodeScaWorkspaceService workspaceService,
			@Autowired VeracodeScaProjectService projectService) {
		this.workspaceService = workspaceService;
		this.projectService = projectService;
	}

	/**
	 * Returns necessary model objects and content view in a {@link SynapseModelAndView} object to
	 * render the Veracode SCA data management page.
	 *
	 * @return {@link SynapseModelAndView} containing all info to render the
	 * Veracode SCA data management page
	 */
	@GetMapping("")
	public SynapseModelAndView getDataMgmt() {
		SynapseModelAndView mav = new SynapseModelAndView("veracode-sca-data-mgmt");

		List<VeracodeScaWorkspace> workspaces = workspaceService.getWorkspaces();
		List<VeracodeScaProject> projects = projectService.getProjects();
		projects.sort(Comparator.comparing(VeracodeScaProject::getDisplayName));

		mav.addObject("workspaces", workspaces);
		mav.addObject("projects", projects);
		mav.addObject("help", getHelp());
		mav.addScriptReference("/scripts/veracode/sca/datatable.js");
		mav.addScriptReference("/scripts/veracode/sca/datamgmt.js");
		return mav;
	}

	/**
	 * Sets the list of included Veracode SCA workspaces using the given list of workspace IDs. If a
	 * workspace is not included, any projects or issues associated with it will not be displayed or
	 * factored into Synapse stats.
	 *
	 * @param workspaceIds       list of IDs of Veracode SCA workspaces to include, if they exist
	 * @param redirectAttributes redirect attributes to render Flash attributes for success
	 * @return string redirecting to the data management page
	 */
	@PostMapping("workspaces/include")
	public String setIncludedWorkspaces(@RequestParam List<UUID> workspaceIds,
			RedirectAttributes redirectAttributes) {
		try {
			workspaceService.setIncluded(workspaceIds);
			redirectAttributes.addFlashAttribute(SynapseModelAndView.SUCCESS_FLASH,
					"Successfully updated included workspaces");
		} catch (IllegalArgumentException e) {
			redirectAttributes.addFlashAttribute(SynapseModelAndView.FAILURE_FLASH,
					"Cannot update included workspaces. " + e.getMessage());
		}
		return DATA_MGMT_REDIRECT;
	}

	/**
	 * Deletes the workspace with the given ID, if it exists.
	 *
	 * @param workspaceId        the ID of the workspace to delete
	 * @param redirectAttributes redirect attributes to render Flash attributes for success
	 * @return string redirecting to the data management page
	 */
	@PostMapping("workspace/delete")
	public String deleteWorkspace(@RequestParam UUID workspaceId,
			RedirectAttributes redirectAttributes) {
		try {
			workspaceService.deleteWorkspace(workspaceId);
			redirectAttributes.addFlashAttribute(SynapseModelAndView.SUCCESS_FLASH,
					"Successfully deleted workspace");
		} catch (IllegalArgumentException | VeracodeScaProductException e) {
			redirectAttributes.addFlashAttribute(SynapseModelAndView.FAILURE_FLASH,
					"Cannot delete workspace. " + e.getMessage());
		}
		return DATA_MGMT_REDIRECT;
	}

	/**
	 * Sets the list of included Veracode SCA projects using the given list of project IDs. If a
	 * project is not included, any issues associated with it will not be displayed or factored
	 * into Synapse stats. Note that even if a project is included here, if the project's workspace
	 * is excluded, the project will not be displayed in Synapse.
	 *
	 * @param projectIds         list of IDs of Veracode SCA projects to include, if they exist
	 * @param redirectAttributes redirect attributes to render Flash attributes for success
	 * @return string redirecting to the data management page
	 */
	@PostMapping("projects/include")
	public String setIncludedProjects(@RequestParam List<UUID> projectIds,
			RedirectAttributes redirectAttributes) {
		try {
			projectService.setIncluded(projectIds);
			redirectAttributes.addFlashAttribute(SynapseModelAndView.SUCCESS_FLASH,
					"Successfully updated included projects");
		} catch (IllegalArgumentException e) {
			redirectAttributes.addFlashAttribute(SynapseModelAndView.FAILURE_FLASH,
					"Cannot update included projects. " + e.getMessage());
		}
		return DATA_MGMT_REDIRECT;
	}

	/**
	 * Deletes the project with the given ID, if it exists.
	 *
	 * @param projectId          the ID of the project to delete
	 * @param redirectAttributes redirect attributes to render Flash attributes for success
	 * @return string redirecting to the data management page
	 */
	@PostMapping("project/delete")
	public String deleteProject(@RequestParam UUID projectId,
			RedirectAttributes redirectAttributes) {
		try {
			projectService.deleteProject(projectId);
			redirectAttributes.addFlashAttribute(SynapseModelAndView.SUCCESS_FLASH,
					"Successfully deleted project");
		} catch (IllegalArgumentException | VeracodeScaProductException e) {
			redirectAttributes.addFlashAttribute(SynapseModelAndView.FAILURE_FLASH,
					"Cannot delete project. " + e.getMessage());
		}
		return DATA_MGMT_REDIRECT;
	}

	private static String getHelp() {
		return "Workspaces and projects can be configured so that they are excluded from Synapse metrics and displays. "
				+ "Use the dropdowns below to choose all workspaces or projects that you would like to include in metrics, and then click 'Apply' when you are satisfied with your selections. "
				+ "The tables to the right and below will update to reflect which workspaces and projects are currently included. "
				+ "Note that even if a project is marked as included in the table below, it may be excluded from metrics if the workspace the project is associated with is excluded.";

	}
}
