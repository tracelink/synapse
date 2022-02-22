package com.tracelink.prodsec.lib.veracode.api.rest;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.tracelink.prodsec.lib.veracode.api.rest.model.ApplicationScan.ScanTypeEnum;
import com.tracelink.prodsec.lib.veracode.api.rest.model.PagedResourceOfApplication;
import com.tracelink.prodsec.lib.veracode.api.rest.model.PagedResourcesIssueSummary;
import com.tracelink.prodsec.lib.veracode.api.rest.model.PagedResourcesProject;
import com.tracelink.prodsec.lib.veracode.api.rest.model.PagedResourcesWorkspace;
import com.tracelink.prodsec.lib.veracode.api.rest.model.SummaryReport;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

/**
 * Wrapper for the Veracode SCA API methods that performs requests to the
 * Veracode SCA server and handles responses.
 *
 * @author mcool
 */
public class VeracodeRestApiClient {

	private static final Logger LOGGER = LoggerFactory.getLogger(VeracodeRestApiClient.class);
	private static final Gson GSON = new Gson();

	private static final String GET = "GET";
	private static final String QUERY_PARAMS = "?page=%s&size=%s";
	private static final int STANDARD_SIZE = 50;
	private static final int PAGE_ZERO = 0;
	private static final int SIZE_ONE = 1;

	private static final String WORKSPACES_URI = "/srcclr/v3/workspaces" + QUERY_PARAMS;
	private static final String PROJECTS_URI = "/srcclr/v3/workspaces/%s/projects" + QUERY_PARAMS;
	private static final String ISSUES_URI = "/srcclr/v3/workspaces/%s/issues" + QUERY_PARAMS
			+ "&status=open,fixed&project_id=%s";
	private static final String APPLICATIONS_URI = "/appsec/v1/applications" + QUERY_PARAMS
			+ "&scan_type=%s&scan_status=PUBLISHED";
	private static final String SUMMARY_URI = "/appsec/v2/applications/%s/summary_report?build_id=%s";

	private static final String BRANCH_PARAM = "&branch=%s";

	private static final int NUM_RETRIES = 5;

	private static final String REQUEST_ERROR = "Error sending request to Veracode Rest API: ";
	private static final String NO_RESPONSE = "Could not obtain response from Veracode Rest API";
	private static final String BAD_STATUS = "Received status code %d from Veracode Rest API endpoint %s: %s";
	private static final String RETRY = "Retrying request. Retries remaining: %d";
	private static final String BAD_JSON = "Received malformed JSON from Veracode Rest API endpoint %s: %s";

	private String apiUrl;
	private String apiId;
	private String secretKey;

	public VeracodeRestApiClient(String apiUrl, String apiId, String secretKey) {
		this.apiUrl = apiUrl;
		this.apiId = apiId;
		this.secretKey = secretKey;
	}

	public PagedResourcesWorkspace getWorkspaces(long page) throws VeracodeRestApiException {
		String url = String.format(WORKSPACES_URI, page, STANDARD_SIZE);
		String json = getRequest(url);
		try {
			return GSON.fromJson(json, PagedResourcesWorkspace.class);
		} catch (JsonSyntaxException e) {
			LOGGER.error(String.format(BAD_JSON, url, e.getMessage()));
			return new PagedResourcesWorkspace();
		}
	}

	public PagedResourcesProject getProjects(UUID workspaceId, long page) throws VeracodeRestApiException {
		String url = String.format(PROJECTS_URI, workspaceId, page, STANDARD_SIZE);
		String json = getRequest(url);
		try {
			return GSON.fromJson(json, PagedResourcesProject.class);
		} catch (JsonSyntaxException e) {
			LOGGER.error(String.format(BAD_JSON, url, e.getMessage()));
			return new PagedResourcesProject();
		}
	}

	public PagedResourcesIssueSummary getIssues(UUID workspaceId, UUID projectId, String branch, long page)
			throws VeracodeRestApiException {
		String url = String.format(ISSUES_URI, workspaceId, page, STANDARD_SIZE, projectId);
		if (!StringUtils.isBlank(branch)) {
			url = String.format(url + BRANCH_PARAM, branch);
		}
		String json = getRequest(url);
		try {
			return GSON.fromJson(json, PagedResourcesIssueSummary.class);
		} catch (JsonSyntaxException e) {
			LOGGER.error(String.format(BAD_JSON, url, e.getMessage()));
			return new PagedResourcesIssueSummary();
		}
	}

	public PagedResourcesIssueSummary getIssue(UUID workspaceId, UUID projectId) throws VeracodeRestApiException {
		String url = String.format(ISSUES_URI, workspaceId, PAGE_ZERO, SIZE_ONE, projectId);
		String json = getRequest(url);
		try {
			return GSON.fromJson(json, PagedResourcesIssueSummary.class);
		} catch (JsonSyntaxException e) {
			LOGGER.error(String.format(BAD_JSON, url, e.getMessage()));
			return new PagedResourcesIssueSummary();
		}
	}

	public PagedResourceOfApplication getApplications(ScanTypeEnum type, long page) {
		String url = String.format(APPLICATIONS_URI, page, STANDARD_SIZE, type.getValue());
		try {
			String json = getRequest(url);
			return GSON.fromJson(json, PagedResourceOfApplication.class);
		} catch (VeracodeRestApiException e) {
			LOGGER.error(String.format(REQUEST_ERROR, url, e.getMessage()));
			return new PagedResourceOfApplication();
		} catch (JsonSyntaxException e) {
			LOGGER.error(String.format(BAD_JSON, url, e.getMessage()));
			return new PagedResourceOfApplication();
		}
	}

	public SummaryReport getSummaryReport(String appId, String build) {
		String url = String.format(SUMMARY_URI, appId, build);
		try {
			String json = getRequest(url);
			return GSON.fromJson(json, SummaryReport.class);
		} catch (VeracodeRestApiException e) {
			LOGGER.error(String.format(REQUEST_ERROR, url, e.getMessage()));
			return new SummaryReport();
		} catch (JsonSyntaxException e) {
			LOGGER.error(String.format(BAD_JSON, url, e.getMessage()));
			return new SummaryReport();
		}
	}

	private String getRequest(String path) throws VeracodeRestApiException {
		HttpResponse<String> response = null;
		final String url = apiUrl + path;
		int retries = NUM_RETRIES;
		while (retries > 0) {
			try {
				final String authorizationHeader = HmacRequestSigner.getVeracodeAuthorizationHeader(apiId, secretKey,
						new URL(url), GET);
				response = Unirest.get(url).header("Authorization", authorizationHeader).asString();
			} catch (IllegalArgumentException | InvalidKeyException | NoSuchAlgorithmException
					| MalformedURLException e) {
				LOGGER.error(REQUEST_ERROR + e.getMessage());
				throw new VeracodeRestApiException(REQUEST_ERROR + e.getMessage());
			}
			retries--;

			// Handle response
			if (response == null) {
				throw new VeracodeRestApiException(NO_RESPONSE);
			} else if (response.isSuccess()) {
				return response.getBody();
			} else if (response.getStatus() / 100 == 5) {
				// If there is a 5xx status, retry
				if (retries > 0) {
					LOGGER.warn(String.format(BAD_STATUS, response.getStatus(), url, response.getBody()));
					LOGGER.info(String.format(RETRY, retries));
				}
			} else {
				// If there is any other unsuccessful status, exit
				break;
			}
		}
		throw new VeracodeRestApiException(String.format(BAD_STATUS, response.getStatus(), url, response.getBody()));
	}

}
