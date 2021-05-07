package com.tracelink.prodsec.plugin.bsimm.controller;

import com.google.gson.JsonObject;
import com.tracelink.prodsec.plugin.bsimm.BSIMMPlugin;
import com.tracelink.prodsec.plugin.bsimm.service.BsimmResponseService;
import com.tracelink.prodsec.plugin.bsimm.service.SurveyException;
import com.tracelink.prodsec.synapse.auth.SynapseAdminAuthDictionary;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller to get BSIMM response data and download survey models.
 *
 * @author csmith
 */
@RestController
@RequestMapping(BSIMMPlugin.PAGELINK + "/rest")
public class BSIMMSurveyRestController {
	private static final Logger LOG = LoggerFactory.getLogger(BSIMMSurveyRestController.class);

	private final BsimmResponseService bsimmResponseService;

	public BSIMMSurveyRestController(@Autowired BsimmResponseService bsimmResponseService) {
		this.bsimmResponseService = bsimmResponseService;
	}

	@PostMapping("/downloadSurveyModel")
	@PreAuthorize("hasAuthority('" + SynapseAdminAuthDictionary.ADMIN_PRIV + "')")
	public void downloadModel(HttpServletResponse response) {
		try (InputStream is = new FileInputStream(Paths.get(getClass().getResource("/xmlmodel/surveymodel.xml").toURI()).toFile())) {
			// copy it to response's OutputStream
			response.addHeader("Content-Disposition", "attachment; filename=\"surveymodel.xml\"");
			response.setContentType(MediaType.APPLICATION_XML_VALUE);
			IOUtils.copy(is, response.getOutputStream());
			response.flushBuffer();
		} catch (IOException | URISyntaxException ex) {
			LOG.error("Error writing survey model file to output stream", ex);
		}
	}

	@GetMapping("/response")
	public ResponseEntity<String> getResponseData(@RequestParam List<Long> responses, @RequestParam List<Long> comparisons) {
		JsonObject json = new JsonObject();

		try {
			json = bsimmResponseService.generateResponsesAndComparisons(responses, comparisons);
		} catch (SurveyException e) {
			json.addProperty("error", e.getMessage());
			return ResponseEntity.badRequest().body(json.toString());
		}

		return ResponseEntity.ok(json.toString());
	}

}
