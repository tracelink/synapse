package com.tracelink.prodsec.plugin.jira.controller;

import com.tracelink.prodsec.plugin.jira.JiraPlugin;
import com.tracelink.prodsec.plugin.jira.exception.JiraAllowedSlaException;
import com.tracelink.prodsec.plugin.jira.exception.JiraClientException;
import com.tracelink.prodsec.plugin.jira.exception.JiraPhrasesException;
import com.tracelink.prodsec.plugin.jira.exception.JiraThresholdsException;
import com.tracelink.prodsec.plugin.jira.model.JiraAllowedSla;
import com.tracelink.prodsec.plugin.jira.model.JiraClient;
import com.tracelink.prodsec.plugin.jira.model.JiraPhraseDataFormat;
import com.tracelink.prodsec.plugin.jira.model.JiraPhrases;
import com.tracelink.prodsec.plugin.jira.model.JiraThresholds;
import com.tracelink.prodsec.plugin.jira.service.JiraAllowedSlaService;
import com.tracelink.prodsec.plugin.jira.service.JiraClientConfigService;
import com.tracelink.prodsec.plugin.jira.service.JiraPhrasesService;
import com.tracelink.prodsec.plugin.jira.service.JiraThresholdsService;
import com.tracelink.prodsec.plugin.jira.service.JiraUpdateService;
import com.tracelink.prodsec.synapse.auth.SynapseAdminAuthDictionary;
import com.tracelink.prodsec.synapse.mvc.SynapseModelAndView;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
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

/**
 * The Jira configurations controller handles requests to the page for viewing and editing
 * client configurations for the Jira server, and creating a connection to it as well.
 * Threshold configuration, search phrase configuration, and allowed time in SLA configuration
 * are also managed from this page, allowing an admin to adjust acceptable thresholds for
 * vulnerabilities, the search phrases used to query the client, and the acceptable amount of
 * time vulnerabilities have to be resolved within a company's Service Level Agreement (SLA)
 *
 * @author bhoran
 */
@Controller
@RequestMapping(JiraPlugin.CONFIGURATIONS_PAGE)
@PreAuthorize("hasAuthority('" + SynapseAdminAuthDictionary.ADMIN_PRIV + "')")
public class JiraConfigurationsController {

	private static final String REDIRECT = "redirect:" + JiraPlugin.CONFIGURATIONS_PAGE;
	private final JiraClientConfigService clientService;
	private final JiraThresholdsService thresholdsService;
	private final JiraUpdateService updateService;
	private final JiraPhrasesService searchJqlService;
	private final JiraAllowedSlaService allowedSlaService;

	// Accepted values to describe vulnerability type
	private static final List<String> ACCEPTED_STRINGS = (Arrays
			.asList("Critical", "High", "Medium", "Low", "Informational", "Unknown"));

	public JiraConfigurationsController(@Autowired JiraClientConfigService clientService,
			@Autowired JiraThresholdsService thresholdsService,
			@Autowired JiraUpdateService updateService,
			@Autowired JiraPhrasesService searchJqlService,
			@Autowired JiraAllowedSlaService allowedSlaService) {
		this.clientService = clientService;
		this.thresholdsService = thresholdsService;
		this.updateService = updateService;
		this.searchJqlService = searchJqlService;
		this.allowedSlaService = allowedSlaService;
	}

	/**
	 * Returns necessary model objects and content view in a {@link
	 * SynapseModelAndView} object to render the Jira configurations page.
	 *
	 * @return {@link SynapseModelAndView} containing all info to render the
	 * Jira configurations page
	 */
	@GetMapping("")
	public SynapseModelAndView getConfigurations() {
		SynapseModelAndView mv = new SynapseModelAndView("jira/configure");
		// Jira client
		try {
			JiraClient client = clientService.getClient();
			mv.addObject("client", client);
		} catch (JiraClientException e) {
			// Do nothing
		}
		// Jira thresholds
		try {
			JiraThresholds thresholds = thresholdsService.getThresholds();
			mv.addObject("thresholds", thresholds);
		} catch (JiraThresholdsException e) {
			// Do nothing
		}
		// Jira Phrases
		try {
			List<JiraPhrases> phrases = searchJqlService.getPhrases();
			mv.addObject("phrases", phrases);
		} catch (JiraPhrasesException e) {
			// Do nothing
		}

		mv.addObject("dataFormats", JiraPhraseDataFormat.values());

		// Jira Days Allowed
		try {
			List<JiraAllowedSla> allowedSlas = allowedSlaService.getAllAllowedSla();
			mv.addObject("allowedSla", allowedSlas);
		} catch (JiraAllowedSlaException e) {
			// Do nothing
		}
		mv.addScriptReference("/scripts/jira/configure.js");
		return mv;
	}

	@PostMapping("client")
	public String setApiClient(@RequestParam URL apiUrl, @RequestParam String user,
			@RequestParam String auth,
			RedirectAttributes redirectAttributes) {
		if (StringUtils.isEmpty(apiUrl) || StringUtils.isEmpty(user) || StringUtils.isEmpty(auth)) {
			redirectAttributes.addFlashAttribute(SynapseModelAndView.FAILURE_FLASH,
					"Please provide all inputs.");
			return REDIRECT;
		}

		if (clientService.setClient(apiUrl, user, auth) == null) {
			redirectAttributes.addFlashAttribute(SynapseModelAndView.FAILURE_FLASH,
					"Invalid API client URL.");
		} else {
			redirectAttributes
					.addFlashAttribute(SynapseModelAndView.SUCCESS_FLASH, "Configured API client.");
		}

		return REDIRECT;
	}

	/**
	 * Test connection to Jira server to make sure that API client
	 * is correctly configured.
	 *
	 * @param redirectAttributes redirect attributes to render Flash attributes
	 *                           for success or failure
	 * @return string redirecting to the configurations page
	 */
	@GetMapping("test")
	public String testConnection(RedirectAttributes redirectAttributes) {
		JiraClient config = clientService.getClient();
		if (config == null) {
			redirectAttributes.addFlashAttribute(SynapseModelAndView.FAILURE_FLASH,
					"Client has not been configured");
			return REDIRECT;
		}

		if (updateService.testConnection()) {
			redirectAttributes
					.addFlashAttribute(SynapseModelAndView.SUCCESS_FLASH, "Connection successful.");
		} else {
			redirectAttributes
					.addFlashAttribute(SynapseModelAndView.FAILURE_FLASH, "Connection failed.");
		}
		return REDIRECT;
	}

	/**
	 * Fetch most recent data from the Jira server. Runs
	 * asynchronously to prevent lag.
	 *
	 * @param redirectAttributes redirect attributes to render Flash attributes
	 *                           for success or failure
	 * @return string redirecting to the configurations page
	 */
	@PostMapping("fetch")
	public String fetch(RedirectAttributes redirectAttributes) {
		JiraClient config = clientService.getClient();
		if (config == null) {
			redirectAttributes.addFlashAttribute(SynapseModelAndView.FAILURE_FLASH,
					"Client has not been configured");
			return REDIRECT;
		}

		CompletableFuture.runAsync(updateService::syncAllData);
		redirectAttributes.addFlashAttribute(SynapseModelAndView.SUCCESS_FLASH,
				"Jira data fetch in progress.");
		return REDIRECT;
	}

	/**
	 * Sets the values for the risk tolerance thresholds. All inputs must
	 * be present.
	 *
	 * @param greenYellow        threshold between green and yellow scorecard
	 *                           traffic lights; must be less than {@code
	 *                           yellowRed} and greater than zero
	 * @param yellowRed          threshold between yellow and red scorecard
	 *                           traffic lights; must be greater than {@code
	 *                           greenYellow}
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

	/**
	 * Sets a new Jira Query Language (JQL) Phrase for the associated data format. A data format
	 * corresponds with the service created (i.e. JiraScrumMetricsService, JiraVulnMetricsService)
	 * to correctly parse and interpret the data for this phrase. The dataFormat string given to
	 * set a new JQL phrase corresponds to a format represented in the enumerated type
	 * JiraPhraseDataFormat.
	 *
	 * @param jqlString          the JQL string to set for the given data format
	 * @param dataFormat         the data format that the given JQL string should be assigned to
	 * @param redirectAttributes model and view redirect attributes to indicate success or failure
	 * @return string redirecting to the configurations page
	 */
	@PostMapping("jqlPhrase")
	public String setJQLSearchForDataFormat(@RequestParam String jqlString,
			@RequestParam String dataFormat, RedirectAttributes redirectAttributes) {
		if (StringUtils.isEmpty(jqlString) || StringUtils.isEmpty(dataFormat)) {
			redirectAttributes.addFlashAttribute(SynapseModelAndView.FAILURE_FLASH,
					"Please provide all inputs.");
			return REDIRECT;
		}
		JiraPhraseDataFormat format = JiraPhraseDataFormat.ofFormat(dataFormat);
		if (format == null) {
			redirectAttributes.addFlashAttribute(SynapseModelAndView.FAILURE_FLASH,
					"Data format is unknown");
			return REDIRECT;
		}

		if (searchJqlService.setPhraseForDataFormat(jqlString, format) == null) {
			redirectAttributes.addFlashAttribute(SynapseModelAndView.FAILURE_FLASH,
					"Error configuring JQL String for data format");
		} else {
			redirectAttributes.addFlashAttribute(SynapseModelAndView.SUCCESS_FLASH,
					"Configured JQL String for " + dataFormat + " data.");
		}
		return REDIRECT;
	}

	/**
	 * Sets a new value for the number of days a given issue severity can remain acceptably
	 * unresolved as specified in the company's Service Level Agreement (SLA).
	 *
	 * @param allowedSlaInput    The number of days issues should be resolved by. If not provided
	 *                           value defaults to null, meaning the amount of days is not
	 *                           applicable
	 * @param severity           the associated severity level to the number of allowed days
	 * @param redirectAttributes model and view redirect attributes to indicate success or failure
	 * @return string redirecting to the configurations page
	 */
	@PostMapping("updateAllowedDays")
	public String setAllowedDays(@RequestParam(required = false) Integer allowedSlaInput,
			@RequestParam String severity,
			RedirectAttributes redirectAttributes) {
		if (StringUtils.isEmpty(severity)) {
			redirectAttributes.addFlashAttribute(SynapseModelAndView.FAILURE_FLASH,
					"Please provide severity value.");
			return REDIRECT;
		}

		if (allowedSlaInput != null && allowedSlaInput < 0) {
			redirectAttributes.addFlashAttribute(SynapseModelAndView.FAILURE_FLASH,
					"Please provide a number of days that is greater than zero");
		} else if (ACCEPTED_STRINGS.contains(severity)) {
			allowedSlaService.setAllowedSla(severity, allowedSlaInput);
			redirectAttributes.addFlashAttribute(SynapseModelAndView.SUCCESS_FLASH,
					"Allowed days in SLA updated successfully for severity " + severity);
		} else {
			redirectAttributes.addFlashAttribute(SynapseModelAndView.FAILURE_FLASH,
					"Please provide a valid string representing vulnerability severity");
		}
		return REDIRECT;
	}
}
