package com.tracelink.prodsec.synapse.logging.controller;

import ch.qos.logback.classic.Level;
import com.tracelink.prodsec.synapse.auth.SynapseAdminAuthDictionary;
import com.tracelink.prodsec.synapse.logging.service.LoggingService;
import com.tracelink.prodsec.synapse.logging.service.PluginLogger;
import com.tracelink.prodsec.synapse.mvc.SynapseModelAndView;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for all Logging. Handles setting the log level and outputting
 * recent log messages for Synapse
 *
 * @author csmith
 */
@Controller
@PreAuthorize("hasAuthority('" + SynapseAdminAuthDictionary.ADMIN_PRIV + "')")
public class LoggingController {

	private static final Logger LOG = LoggerFactory.getLogger(LoggingController.class);

	private final LoggingService logsService;

	public LoggingController(@Autowired LoggingService logsService) {
		this.logsService = logsService;
	}

	@GetMapping("/logging")
	public SynapseModelAndView loggerView(
			@RequestParam(defaultValue = LoggingService.SYNAPSE_LOGGER_NAME) String logger) {
		SynapseModelAndView mav = new SynapseModelAndView("logging");
		PluginLogger pluginLogger = logsService.getLogger(logger);
		if (pluginLogger == null) {
			pluginLogger = logsService.getLogger(LoggingService.SYNAPSE_LOGGER_NAME);
		}
		mav.addObject("loggers", logsService.getLoggers());
		mav.addObject("logOptions", LoggingService.ALLOWED_LEVELS);
		mav.addObject("logger", pluginLogger);
		mav.addObject("currentLogLevel", pluginLogger.getLogLevel());
		mav.addObject("logs", pluginLogger.getLogs());
		mav.addScriptReference("/scripts/logging.js");
		return mav;
	}

	@PostMapping("/logging/set")
	public String setLogLevel(
			@RequestParam(defaultValue = LoggingService.SYNAPSE_LOGGER_NAME) String logger,
			@RequestParam String loglevel, RedirectAttributes redirectAttributes) {
		Level level = Level.toLevel(loglevel, Level.INFO);
		PluginLogger pluginLogger = logsService.getLogger(logger);
		if (pluginLogger == null) {
			redirectAttributes
					.addFlashAttribute(SynapseModelAndView.FAILURE_FLASH, "Unknown logger");
			return "redirect:/logging";
		}
		pluginLogger.setLogsLevel(level);
		return "redirect:/logging?logger=" + logger;
	}

	@GetMapping("/logging/download")
	public ResponseEntity<Object> downloadLogsFiles() {
		File file;
		Resource resource;
		try {
			file = logsService.generateLogsZip().toFile();
			resource = new InputStreamResource(new FileInputStream(file));
		} catch (IOException e) {
			LOG.error("Exception while zipping logs", e);
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Exception while zipping logs");
		}

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=watchtowerlogs.zip")
				.header(HttpHeaders.CONTENT_TYPE, "application/gzip").contentLength(file.length())
				.body(resource);
	}

}
