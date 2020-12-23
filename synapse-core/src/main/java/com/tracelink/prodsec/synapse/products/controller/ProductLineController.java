package com.tracelink.prodsec.synapse.products.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.tracelink.prodsec.synapse.auth.SynapseAdminAuthDictionary;
import com.tracelink.prodsec.synapse.mvc.SynapseModelAndView;
import com.tracelink.prodsec.synapse.products.ProductsNotFoundException;
import com.tracelink.prodsec.synapse.products.service.ProductsService;
import com.tracelink.prodsec.synapse.products.BadProductNameException;
import com.tracelink.prodsec.synapse.products.OrphanedException;

/**
 * Handles CRUD operations on product lines and projects
 * 
 * @author csmith
 *
 */
@Controller
public class ProductLineController {
	@Autowired
	private ProductsService productsService;

	private SynapseModelAndView productsMAV() {
		SynapseModelAndView mav = new SynapseModelAndView("products");
		mav.addScriptReference("/scripts/products.js");
		return mav;
	}

	@GetMapping("/products")
	public SynapseModelAndView productsHome() {
		SynapseModelAndView mav = productsMAV();
		mav.addObject("productLines", productsService.getAllProductLines());
		return mav;
	}

	@GetMapping("/products/{productLine}")
	public SynapseModelAndView productLineView(@PathVariable String productLine) {
		SynapseModelAndView mav = productsMAV();
		mav.addObject("projects", productsService.getAllProjects());
		mav.addObject("productLines", productsService.getAllProductLines());
		mav.addObject("productLine", productsService.getProductLine(productLine));
		return mav;
	}

	/////////////////
	// Create
	/////////////////
	@PostMapping("/products/createproductline")
	@PreAuthorize("hasAuthority('" + SynapseAdminAuthDictionary.ADMIN_PRIV + "')")
	public String createProductLine(@RequestParam String productLineName, RedirectAttributes redirectAttributes) {
		try {
			productsService.createProductLine(productLineName);
		} catch (BadProductNameException e) {
			redirectAttributes.addFlashAttribute(SynapseModelAndView.FAILURE_FLASH, e.getMessage());
		}
		return "redirect:/products/" + productLineName;
	}

	@PostMapping("/products/{productLine}/createproject")
	@PreAuthorize("hasAuthority('" + SynapseAdminAuthDictionary.ADMIN_PRIV + "')")
	public String createProject(@PathVariable String productLine, @RequestParam String projectName,
			RedirectAttributes redirectAttributes) {
		try {
			productsService.createProject(projectName, productLine);
		} catch (BadProductNameException | ProductsNotFoundException e) {
			redirectAttributes.addFlashAttribute(SynapseModelAndView.FAILURE_FLASH, e.getMessage());
		}
		return "redirect:/products/" + productLine;
	}

	/////////////////
	// Updates
	/////////////////
	@PostMapping("/products/{productLine}/renameproductline")
	@PreAuthorize("hasAuthority('" + SynapseAdminAuthDictionary.ADMIN_PRIV + "')")
	public String renameProductLine(@PathVariable String productLine, @RequestParam String productLineName,
			RedirectAttributes redirectAttributes) {
		try {
			productsService.renameProductLine(productLine, productLineName);
			redirectAttributes.addFlashAttribute(SynapseModelAndView.SUCCESS_FLASH,
					productLine + " renamed to " + productLineName);
		} catch (BadProductNameException | ProductsNotFoundException e) {
			redirectAttributes.addFlashAttribute(SynapseModelAndView.FAILURE_FLASH, e.getMessage());
			return "redirect:/products/" + productLine;
		}
		return "redirect:/products/" + productLineName;
	}

	@PostMapping("/products/{productLine}/renameproject")
	@PreAuthorize("hasAuthority('" + SynapseAdminAuthDictionary.ADMIN_PRIV + "')")
	public String renameProject(@PathVariable String productLine, @RequestParam String oldProjectName,
			@RequestParam String projectName, RedirectAttributes redirectAttributes) {
		try {
			productsService.renameProject(oldProjectName, projectName);
			redirectAttributes.addFlashAttribute(SynapseModelAndView.SUCCESS_FLASH,
					oldProjectName + " renamed to " + projectName);
		} catch (BadProductNameException | ProductsNotFoundException e) {
			redirectAttributes.addFlashAttribute(SynapseModelAndView.FAILURE_FLASH, e.getMessage());
		}
		return "redirect:/products/" + productLine;
	}

	@PostMapping("/products/{productLine}/moveproject")
	@PreAuthorize("hasAuthority('" + SynapseAdminAuthDictionary.ADMIN_PRIV + "')")
	public String moveProject(@PathVariable String productLine, @RequestParam String projectName,
			@RequestParam String productLineName, RedirectAttributes redirectAttributes) {
		try {
			productsService.moveProject(projectName, productLineName);
			redirectAttributes.addFlashAttribute(SynapseModelAndView.SUCCESS_FLASH,
					projectName + " successfully moved to Product Line " + productLineName);

		} catch (BadProductNameException | ProductsNotFoundException e) {
			redirectAttributes.addFlashAttribute(SynapseModelAndView.FAILURE_FLASH, e.getMessage());
		}
		return "redirect:/products/" + productLine;
	}

	/////////////////
	// Delete
	/////////////////
	@PostMapping("/products/{productLine}/deleteproductline")
	@PreAuthorize("hasAuthority('" + SynapseAdminAuthDictionary.ADMIN_PRIV + "')")
	public String deleteProductLine(@PathVariable String productLine, RedirectAttributes redirectAttributes) {
		try {
			productsService.deleteProductLine(productLine);
			redirectAttributes.addFlashAttribute(SynapseModelAndView.SUCCESS_FLASH,
					productLine + " deleted successfully.");
		} catch (OrphanedException | ProductsNotFoundException e) {
			redirectAttributes.addFlashAttribute(SynapseModelAndView.FAILURE_FLASH, e.getMessage());
			return "redirect:/products/" + productLine;
		}
		return "redirect:/products";
	}

	@PostMapping("/products/{productLine}/deleteproject")
	@PreAuthorize("hasAuthority('" + SynapseAdminAuthDictionary.ADMIN_PRIV + "')")
	public String deleteProject(@PathVariable String productLine, @RequestParam String projectName,
			RedirectAttributes redirectAttributes) {
		try {
			productsService.deleteProject(projectName);
		} catch (ProductsNotFoundException e) {
			redirectAttributes.addFlashAttribute(SynapseModelAndView.FAILURE_FLASH, e.getMessage());
		}
		return "redirect:/products/" + productLine;
	}
}
