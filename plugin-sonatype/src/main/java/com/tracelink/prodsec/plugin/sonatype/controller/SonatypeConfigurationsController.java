package com.tracelink.prodsec.plugin.sonatype.controller;

import com.tracelink.prodsec.plugin.sonatype.SonatypePlugin;
import com.tracelink.prodsec.plugin.sonatype.exception.SonatypeClientException;
import com.tracelink.prodsec.plugin.sonatype.exception.SonatypeThresholdsException;
import com.tracelink.prodsec.plugin.sonatype.model.SonatypeClient;
import com.tracelink.prodsec.plugin.sonatype.model.SonatypeThresholds;
import com.tracelink.prodsec.plugin.sonatype.service.SonatypeClientService;
import com.tracelink.prodsec.plugin.sonatype.service.SonatypeThresholdsService;
import com.tracelink.prodsec.synapse.auth.SynapseAdminAuthDictionary;
import com.tracelink.prodsec.synapse.mvc.SynapseModelAndView;
import java.util.concurrent.CompletableFuture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * The Sonatype configurations controller handles requests to the page for
 * viewing and editing configurations for connections to the Sonatype Nexus IQ
 * server and the risk tolerance.
 *
 * @author mcool
 */
@Controller
@RequestMapping(SonatypePlugin.CONFIGURATIONS_PAGE)
@PreAuthorize("hasAuthority('" + SynapseAdminAuthDictionary.ADMIN_PRIV + "')")
public class SonatypeConfigurationsController {

	private static final String REDIRECT = "redirect:" + SonatypePlugin.CONFIGURATIONS_PAGE;
	private final SonatypeClientService clientService;
	private final SonatypeThresholdsService thresholdsService;

	public SonatypeConfigurationsController(@Autowired SonatypeClientService clientService,
			@Autowired SonatypeThresholdsService thresholdsService) {
		this.clientService = clientService;
		this.thresholdsService = thresholdsService;
	}

	/**
	 * Returns necessary model objects and content view in a {@link
	 * SynapseModelAndView} object to render the Sonatype configurations page.
	 *
	 * @return {@link SynapseModelAndView} containing all info to render the
	 * Sonatype configurations page
	 */
	@GetMapping("")
	public SynapseModelAndView getConfigurations() {
		SynapseModelAndView mv = new SynapseModelAndView("sonatype-configure");
		// Sonatype client
		try {
			SonatypeClient client = clientService.getClient();
			mv.addObject("client", client);
		} catch (SonatypeClientException e) {
			// Do nothing
		}
		// Sonatype thresholds
		try {
			SonatypeThresholds thresholds = thresholdsService.getThresholds();
			mv.addObject("thresholds", thresholds);
		} catch (SonatypeThresholdsException e) {
			// Do nothing
		}
		return mv;
	}

	/**
	 * Sets the values for the API client that will fetch data from the Sonatype
	 * Nexus IQ server. All inputs must be present.
	 *
	 * @param apiUrl             URL where the Nexus IQ server is
	 *                           hosted
	 * @param user               username for the Nexus IQ server
	 * @param auth               authentication for the Nexus IQ server
	 * @param redirectAttributes redirect attributes to render Flash attributes
	 *                           for success or failure
	 * @return string redirecting to the configurations page
	 */
	@PostMapping("client")
	public String setApiClient(@RequestParam String apiUrl, @RequestParam String user,
			@RequestParam String auth,
			RedirectAttributes redirectAttributes) {
		if ("".equals(apiUrl) || "".equals(user) || "".equals(auth)) {
			redirectAttributes.addFlashAttribute(SynapseModelAndView.FAILURE_FLASH,
					"Please provide all inputs.");
			return REDIRECT;
		}

		if (clientService.setClient(apiUrl, user, auth)) {
			redirectAttributes
					.addFlashAttribute(SynapseModelAndView.SUCCESS_FLASH, "Configured API client.");
		} else {
			redirectAttributes.addFlashAttribute(SynapseModelAndView.FAILURE_FLASH,
					"Invalid API client URL.");
		}
		return REDIRECT;
	}

	/**
	 * Test connection to Sonatype Nexus IQ server to make sure that API client
	 * is correctly configured.
	 *
	 * @param redirectAttributes redirect attributes to render Flash attributes
	 *                           for success or failure
	 * @return string redirecting to the configurations page
	 */
	@GetMapping("test")
	public String testConnection(RedirectAttributes redirectAttributes) {
		if (clientService.testConnection()) {
			redirectAttributes
					.addFlashAttribute(SynapseModelAndView.SUCCESS_FLASH, "Connection successful.");
		} else {
			redirectAttributes
					.addFlashAttribute(SynapseModelAndView.FAILURE_FLASH, "Connection failed.");
		}
		return REDIRECT;
	}

	/**
	 * Fetch most recent data from the Sonatype Nexus IQ server. Runs
	 * asynchronously to prevent lag.
	 *
	 * @param redirectAttributes redirect attributes to render Flash attributes
	 *                           for success
	 * @return string redirecting to the configurations page
	 */
	@PostMapping("fetch")
	public String fetch(RedirectAttributes redirectAttributes) {
		CompletableFuture.runAsync(clientService::fetchData);
		redirectAttributes.addFlashAttribute(SynapseModelAndView.SUCCESS_FLASH,
				"Sonatype data fetch in progress.");
		return REDIRECT;
	}

	/**
	 * Sets the values for the risk tolerance thresholds. All inputs must
	 * be present.
	 *
	 * @param greenYellow        threshold between green and yellow scorecard
	 *                           traffic lights; must be greater than {@code
	 *                           yellowRed} and greater than zero
	 * @param yellowRed          threshold between yellow and red scorecard
	 *                           traffic lights; must be less than {@code
	 *                           greenYellow} and greater than zero
	 * @param redirectAttributes redirect attributes to render Flash attributes
	 *                           for success or failure
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
		return REDIRECT;
	}
}
