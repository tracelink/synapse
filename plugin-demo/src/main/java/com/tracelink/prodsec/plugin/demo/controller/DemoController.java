package com.tracelink.prodsec.plugin.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.tracelink.prodsec.plugin.demo.DemoPlugin;
import com.tracelink.prodsec.plugin.demo.service.DemoService;
import com.tracelink.prodsec.synapse.mvc.SynapseModelAndView;
import com.tracelink.prodsec.synapse.products.model.ProjectModel;
import com.tracelink.prodsec.synapse.products.service.ProductsService;

/**
 * The Controller is used to provide additional URL paths to respond on.
 * 
 * For the Demo Plugin:
 * 
 * This controller uses the {@link PreAuthorize} annotation with the plugin's
 * privilege to restrict what users can interact with this controller's paths.
 * 
 * It uses the Flash Attribute and redirect method to handle POST to GET errors
 * and display in the UI
 * 
 * It uses the {@link SynapseModelAndView} to link the "well" of the UI to the
 * implementation of this plugin controller's page
 * 
 * @author csmith
 *
 */
@Controller
@RequestMapping(DemoPlugin.PAGELINK)
@PreAuthorize("hasAuthority('" + DemoPlugin.PRIV + "')")
public class DemoController {

	private final DemoService demoService;
	private final ProductsService productsService;

	public DemoController(@Autowired DemoService demoService, @Autowired ProductsService productsService) {
		this.demoService = demoService;
		this.productsService = productsService;
	}

	@GetMapping
	public SynapseModelAndView demoHome() {
		SynapseModelAndView smav = new SynapseModelAndView("demo/home");
		smav.addObject("demoProjects", demoService.getFullDemoList());
		return smav;
	}

	@PostMapping
	public String assignVulns(@RequestParam String projectName, @RequestParam int vulns,
			RedirectAttributes redirectAttributes) {
		ProjectModel project = productsService.getProject(projectName);
		if (project == null) {
			redirectAttributes.addFlashAttribute(SynapseModelAndView.FAILURE_FLASH, "Could not find that project");
		} else {
			demoService.assignVulnsToProject(project, vulns);
			redirectAttributes.addFlashAttribute(SynapseModelAndView.SUCCESS_FLASH,
					"Successfully added vulns to project");
		}
		return "redirect:" + DemoPlugin.PAGELINK;
	}

}
