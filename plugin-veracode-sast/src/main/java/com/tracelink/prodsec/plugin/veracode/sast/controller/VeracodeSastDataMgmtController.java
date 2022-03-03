package com.tracelink.prodsec.plugin.veracode.sast.controller;

import com.tracelink.prodsec.plugin.veracode.sast.VeracodeSastPlugin;
import com.tracelink.prodsec.plugin.veracode.sast.model.VeracodeSastAppModel;
import com.tracelink.prodsec.plugin.veracode.sast.model.VeracodeSastProductException;
import com.tracelink.prodsec.plugin.veracode.sast.service.VeracodeSastAppService;
import com.tracelink.prodsec.synapse.auth.SynapseAdminAuthDictionary;
import com.tracelink.prodsec.synapse.mvc.SynapseModelAndView;
import java.util.Comparator;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * The Veracode SAST data management controller handles requests to the page for editing visibility
 * or deleting Veracode SAST apps.
 *
 * @author mcool
 */
@Controller
@RequestMapping(VeracodeSastPlugin.DATA_MGMT_PAGE)
@PreAuthorize("hasAuthority('" + SynapseAdminAuthDictionary.ADMIN_PRIV + "')")
public class VeracodeSastDataMgmtController {

	private static final String DATA_MGMT_REDIRECT =
			"redirect:" + VeracodeSastPlugin.DATA_MGMT_PAGE;

	private final VeracodeSastAppService appService;

	public VeracodeSastDataMgmtController(@Autowired VeracodeSastAppService appService) {
		this.appService = appService;
	}

	/**
	 * Returns necessary model objects and content view in a {@link SynapseModelAndView} object to
	 * render the Veracode SAST data management page.
	 *
	 * @return {@link SynapseModelAndView} containing all info to render the Veracode SAST data
	 * management page
	 */
	@GetMapping("")
	public SynapseModelAndView getDataMgmt() {
		SynapseModelAndView mav = new SynapseModelAndView("veracode-sast-data-mgmt");

		List<VeracodeSastAppModel> apps = appService.getAllApps();
		apps.sort(Comparator.comparing(VeracodeSastAppModel::getDisplayName));

		mav.addObject("apps", apps);
		mav.addObject("help", getHelp());
		mav.addScriptReference("/scripts/veracodesast/datatable.js");
		mav.addScriptReference("/scripts/veracodesast/datamgmt.js");
		return mav;
	}

	/**
	 * Sets the list of included Veracode SAST apps using the given list of app IDs. If an app is
	 * not included, any flaws associated with it will not be displayed or factored into Synapse
	 * stats.
	 *
	 * @param appIds             list of IDs of Veracode SAST apps to include, if they exist
	 * @param redirectAttributes redirect attributes to render Flash attributes for success
	 * @return string redirecting to the data management page
	 */
	@PostMapping("apps/include")
	public String setIncludedApps(@RequestParam List<Long> appIds,
			RedirectAttributes redirectAttributes) {
		try {
			appService.setIncluded(appIds);
			redirectAttributes.addFlashAttribute(SynapseModelAndView.SUCCESS_FLASH,
					"Successfully updated included apps");
		} catch (IllegalArgumentException e) {
			redirectAttributes.addFlashAttribute(SynapseModelAndView.FAILURE_FLASH,
					"Cannot update included apps. " + e.getMessage());
		}
		return DATA_MGMT_REDIRECT;
	}

	/**
	 * Deletes the app with the given ID, if it exists.
	 *
	 * @param appId              the ID of the app to delete
	 * @param redirectAttributes redirect attributes to render Flash attributes for success
	 * @return string redirecting to the data management page
	 */
	@PostMapping("app/delete")
	public String deleteApp(@RequestParam Long appId, RedirectAttributes redirectAttributes) {
		try {
			appService.deleteApp(appId);
			redirectAttributes.addFlashAttribute(SynapseModelAndView.SUCCESS_FLASH,
					"Successfully deleted app");
		} catch (IllegalArgumentException | VeracodeSastProductException e) {
			redirectAttributes.addFlashAttribute(SynapseModelAndView.FAILURE_FLASH,
					"Cannot delete app. " + e.getMessage());
		}
		return DATA_MGMT_REDIRECT;
	}

	private static String getHelp() {
		return "Apps and sandboxes can be configured so that they are excluded from Synapse metrics and displays. "
				+ "Use the dropdown below to choose all apps and sandboxes that you would like to include in metrics, and then click 'Apply' when you are satisfied with your selections. "
				+ "The table below will update to reflect which apps and sandboxes are currently included.";

	}
}
