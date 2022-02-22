package com.tracelink.prodsec.plugin.veracode.dast.controller;

import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.tracelink.prodsec.plugin.veracode.dast.VeracodeDastPlugin;
import com.tracelink.prodsec.plugin.veracode.dast.model.VeracodeDastClientConfigModel;
import com.tracelink.prodsec.plugin.veracode.dast.service.VeracodeDastClientConfigService;
import com.tracelink.prodsec.plugin.veracode.dast.service.VeracodeDastThresholdsService;
import com.tracelink.prodsec.plugin.veracode.dast.service.VeracodeDastUpdateService;
import com.tracelink.prodsec.plugin.veracode.dast.service.VeracodeDastUpdateService.SyncType;
import com.tracelink.prodsec.synapse.auth.SynapseAdminAuthDictionary;
import com.tracelink.prodsec.synapse.mvc.SynapseModelAndView;

/**
 * The Veracode DAST configurations controller handles requests to the page for
 * viewing and editing configurations for connections to Veracode's servers and
 * the editing the risk tolerance.
 *
 * @author csmith
 */
@Controller
@RequestMapping(VeracodeDastPlugin.CONFIGURATIONS_PAGE)
@PreAuthorize("hasAuthority('" + SynapseAdminAuthDictionary.ADMIN_PRIV + "')")
public class VeracodeDastConfigurationController {

	private static final String CONFIG_REDIRECT = "redirect:" + VeracodeDastPlugin.CONFIGURATIONS_PAGE;
	private final VeracodeDastClientConfigService configService;
	private final VeracodeDastUpdateService metricsService;
	private final VeracodeDastThresholdsService thresholdService;

	public VeracodeDastConfigurationController(@Autowired VeracodeDastClientConfigService configService,
			@Autowired VeracodeDastUpdateService metricsService,
			@Autowired VeracodeDastThresholdsService thresholdService) {
		this.configService = configService;
		this.metricsService = metricsService;
		this.thresholdService = thresholdService;
	}

	@GetMapping("")
	public SynapseModelAndView getConfigurationsPage() {
		SynapseModelAndView smav = new SynapseModelAndView("veracode-dast-config");
		smav.addObject("config", configService.getClientConfig());
		smav.addObject("thresholds", thresholdService.getThresholds());
		return smav;
	}

	@PostMapping("")
	public String updateConfig(@RequestParam String apiId, @RequestParam String apiKey,
			RedirectAttributes redirectAttributes) {
		if (StringUtils.isEmpty(apiId) || StringUtils.isEmpty(apiKey)) {
			redirectAttributes.addFlashAttribute(SynapseModelAndView.FAILURE_FLASH,
					"Must fill out client API ID and API Key");
		} else {
			configService.setClientConfig(apiId, apiKey);
			redirectAttributes.addFlashAttribute(SynapseModelAndView.SUCCESS_FLASH, "Configured API client");
		}
		return CONFIG_REDIRECT;
	}

	@GetMapping("test")
	public String testConfig(RedirectAttributes redirectAttributes) {
		try {
			configService.testAccess();
			redirectAttributes.addFlashAttribute(SynapseModelAndView.SUCCESS_FLASH, "Client Configured Correctly");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute(SynapseModelAndView.FAILURE_FLASH, e.getMessage());
		}
		return CONFIG_REDIRECT;
	}

	@PostMapping("fetch")
	public String fetchData(RedirectAttributes redirectAttributes) {
		VeracodeDastClientConfigModel config = configService.getClientConfig();
		if (config == null) {
			redirectAttributes.addFlashAttribute(SynapseModelAndView.FAILURE_FLASH, "Client has not been configured");
			return CONFIG_REDIRECT;
		}

		CompletableFuture.runAsync(()->metricsService.syncData(SyncType.RECENT));
		redirectAttributes.addFlashAttribute(SynapseModelAndView.SUCCESS_FLASH,
				"Veracode DAST data fetch in progress.");
		return CONFIG_REDIRECT;
	}
	
	@PostMapping("sync")
	public String syncData(RedirectAttributes redirectAttributes) {
		VeracodeDastClientConfigModel config = configService.getClientConfig();
		if (config == null) {
			redirectAttributes.addFlashAttribute(SynapseModelAndView.FAILURE_FLASH, "Client has not been configured");
			return CONFIG_REDIRECT;
		}

		CompletableFuture.runAsync(()->metricsService.syncData(SyncType.ALL));
		redirectAttributes.addFlashAttribute(SynapseModelAndView.SUCCESS_FLASH,
				"Veracode DAST data fetch in progress.");
		return CONFIG_REDIRECT;
	}

	@PostMapping("thresholds")
	public String setThresholds(@RequestParam int greenYellow, @RequestParam int yellowRed,
			RedirectAttributes redirectAttributes) {
		if (yellowRed <= 0) {
			redirectAttributes.addFlashAttribute(SynapseModelAndView.FAILURE_FLASH,
					"Please provide a yellow risk threshold greater than zero");
		} else if (greenYellow >= 100) {
			redirectAttributes.addFlashAttribute(SynapseModelAndView.FAILURE_FLASH,
					"Please provide a green risk threshold < 100");
		} else if (greenYellow <= yellowRed) {
			redirectAttributes.addFlashAttribute(SynapseModelAndView.FAILURE_FLASH,
					"Please provide risk thresholds where Green/Yellow is greater than Yellow/Red.");
		} else {
			thresholdService.setThresholds(greenYellow, yellowRed);
			redirectAttributes.addFlashAttribute(SynapseModelAndView.SUCCESS_FLASH, "Thresholds updated successfully");
		}
		return CONFIG_REDIRECT;
	}
}
