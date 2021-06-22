package com.tracelink.prodsec.plugin.veracode.sca.controller;

import com.tracelink.prodsec.plugin.veracode.sca.VeracodeScaPlugin;
import com.tracelink.prodsec.plugin.veracode.sca.exception.VeracodeScaClientException;
import com.tracelink.prodsec.plugin.veracode.sca.exception.VeracodeScaThresholdsException;
import com.tracelink.prodsec.plugin.veracode.sca.model.VeracodeScaClient;
import com.tracelink.prodsec.plugin.veracode.sca.model.VeracodeScaThresholds;
import com.tracelink.prodsec.plugin.veracode.sca.service.VeracodeScaClientService;
import com.tracelink.prodsec.plugin.veracode.sca.service.VeracodeScaThresholdsService;
import com.tracelink.prodsec.synapse.auth.SynapseAdminAuthDictionary;
import com.tracelink.prodsec.synapse.mvc.SynapseModelAndView;
import java.util.concurrent.CompletableFuture;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * The Veracode SCA configurations controller handles requests to the page for viewing and editing
 * configurations for connections to the Veracode SCA server and the risk tolerance.
 *
 * @author mcool
 */
@Controller
@RequestMapping(VeracodeScaPlugin.CONFIGURATIONS_PAGE)
@PreAuthorize("hasAuthority('" + SynapseAdminAuthDictionary.ADMIN_PRIV + "')")
public class VeracodeScaConfigurationsController {

	private static final String CONFIGURATIONS_REDIRECT =
			"redirect:" + VeracodeScaPlugin.CONFIGURATIONS_PAGE;
	private final VeracodeScaClientService clientService;
	private final VeracodeScaThresholdsService thresholdsService;

	public VeracodeScaConfigurationsController(@Autowired VeracodeScaClientService clientService,
			@Autowired VeracodeScaThresholdsService thresholdsService) {
		this.clientService = clientService;
		this.thresholdsService = thresholdsService;
	}

	/**
	 * Returns necessary model objects and content view in a {@link SynapseModelAndView} object to
	 * render the Veracode SCA configurations page.
	 *
	 * @return {@link SynapseModelAndView} containing all info to render the Veracode SCA
	 * configurations page
	 */
	@GetMapping("")
	public SynapseModelAndView getConfigurations() {
		SynapseModelAndView mv = new SynapseModelAndView("veracode-sca-configure");
		// Veracode SCA client
		try {
			VeracodeScaClient client = clientService.getClient();
			mv.addObject("client", client);
		} catch (VeracodeScaClientException e) {
			// Do nothing
		}
		// Veracode SCA thresholds
		try {
			VeracodeScaThresholds thresholds = thresholdsService.getThresholds();
			mv.addObject("thresholds", thresholds);
		} catch (VeracodeScaThresholdsException e) {
			// Do nothing
		}
		return mv;
	}

	/**
	 * Sets the values for the API client that will fetch data from the Veracode SCA server. All
	 * inputs must be present.
	 *
	 * @param apiId              API ID for the Veracode SCA server
	 * @param apiSecretKey       API secret key for the Veracode SCA server
	 * @param redirectAttributes redirect attributes to render Flash attributes for success or
	 *                           failure
	 * @return string redirecting to the configurations page
	 */
	@PostMapping("client")
	public String setApiClient(@RequestParam String apiId, @RequestParam String apiSecretKey,
			RedirectAttributes redirectAttributes) {
		if (StringUtils.isBlank(apiId) || StringUtils.isBlank(apiSecretKey)) {
			redirectAttributes.addFlashAttribute(SynapseModelAndView.FAILURE_FLASH,
					"Please provide all inputs.");
			return CONFIGURATIONS_REDIRECT;
		}

		if (clientService.setClient(apiId, apiSecretKey)) {
			redirectAttributes
					.addFlashAttribute(SynapseModelAndView.SUCCESS_FLASH, "Configured API client.");
		} else {
			redirectAttributes.addFlashAttribute(SynapseModelAndView.FAILURE_FLASH,
					"Invalid API client URL.");
		}
		return CONFIGURATIONS_REDIRECT;
	}

	/**
	 * Test connection to Veracode SCA server to make sure that API client is correctly configured.
	 *
	 * @param redirectAttributes redirect attributes to render Flash attributes for success or
	 *                           failure
	 * @return string redirecting to the configurations page
	 */
	@GetMapping("test")
	public String testConnection(RedirectAttributes redirectAttributes) {
		try {
			clientService.testConnection();
			redirectAttributes
					.addFlashAttribute(SynapseModelAndView.SUCCESS_FLASH, "Connection successful.");
		} catch (VeracodeScaClientException e) {
			redirectAttributes
					.addFlashAttribute(SynapseModelAndView.FAILURE_FLASH,
							"Connection failed: " + e.getMessage());
		}
		return CONFIGURATIONS_REDIRECT;
	}

	/**
	 * Fetch most recent data from the Veracode SCA server. Runs asynchronously to prevent lag.
	 *
	 * @param redirectAttributes redirect attributes to render Flash attributes for success
	 * @return string redirecting to the configurations page
	 */
	@PostMapping("fetch")
	public String fetch(RedirectAttributes redirectAttributes) {
		CompletableFuture.runAsync(clientService::fetchData);
		redirectAttributes.addFlashAttribute(SynapseModelAndView.SUCCESS_FLASH,
				"Veracode SCA data fetch in progress.");
		return CONFIGURATIONS_REDIRECT;
	}

	/**
	 * Sets the values for the risk tolerance thresholds. All inputs must be present.
	 *
	 * @param greenYellow        threshold between green and yellow scorecard traffic lights; must
	 *                           be greater than {@code yellowRed} and greater than zero
	 * @param yellowRed          threshold between yellow and red scorecard traffic lights; must be
	 *                           less than {@code greenYellow} and greater than zero
	 * @param redirectAttributes redirect attributes to render Flash attributes for success or
	 *                           failure
	 * @return string redirecting to the configurations page
	 */
	@PostMapping("thresholds")
	public String setThresholds(@RequestParam long greenYellow, @RequestParam long yellowRed,
			RedirectAttributes redirectAttributes) {
		if (greenYellow <= 0 || greenYellow >= yellowRed) {
			redirectAttributes.addFlashAttribute(SynapseModelAndView.FAILURE_FLASH,
					"Please provide risk thresholds greater than zero, where Green/Yellow is less than Yellow/Red.");
		} else {
			thresholdsService.setThresholds(greenYellow, yellowRed);
			redirectAttributes.addFlashAttribute(SynapseModelAndView.SUCCESS_FLASH,
					"Configured risk score thresholds.");
		}
		return CONFIGURATIONS_REDIRECT;
	}
}
