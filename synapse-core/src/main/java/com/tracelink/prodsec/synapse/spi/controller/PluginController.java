package com.tracelink.prodsec.synapse.spi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.tracelink.prodsec.synapse.auth.SynapseAdminAuthDictionary;
import com.tracelink.prodsec.synapse.mvc.SynapseModelAndView;
import com.tracelink.prodsec.synapse.spi.model.PluginModel;
import com.tracelink.prodsec.synapse.spi.service.PluginService;

@Controller
@RequestMapping("/plugins")
@PreAuthorize("hasAuthority('" + SynapseAdminAuthDictionary.ADMIN_PRIV + "')")
public class PluginController {

	private final PluginService pluginService;

	public PluginController(@Autowired PluginService pluginService) {
		this.pluginService = pluginService;
	}

	@GetMapping
	public SynapseModelAndView plugin() {
		SynapseModelAndView mav = new SynapseModelAndView("plugin");
		mav.addObject("plugins", pluginService.getPlugins());
		return mav;
	}

	@PostMapping
	public String activation(@RequestParam boolean enable, @RequestParam long pluginId,
			RedirectAttributes redirectAttributes) {
		PluginModel pluginModel = pluginService.getPlugin(pluginId);
		if (pluginModel == null) {
			redirectAttributes.addFlashAttribute(SynapseModelAndView.FAILURE_FLASH, "Unknown Plugin");
		} else {
			String message = pluginModel.getPluginName() + " has been successfully ";
			if (enable) {
				pluginService.activate(pluginModel);
				message += "activated";
			} else {
				pluginService.deactivate(pluginModel);
				message += "deactivated";
			}
			redirectAttributes.addFlashAttribute(SynapseModelAndView.SUCCESS_FLASH, message);
		}
		return "redirect:/plugins";
	}
}
