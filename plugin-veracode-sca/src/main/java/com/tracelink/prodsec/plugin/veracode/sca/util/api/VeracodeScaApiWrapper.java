package com.tracelink.prodsec.plugin.veracode.sca.util.api;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.tracelink.prodsec.plugin.veracode.sca.model.VeracodeScaClient;
import com.tracelink.prodsec.plugin.veracode.sca.util.model.PagedResourcesIssueSummary;
import com.tracelink.prodsec.plugin.veracode.sca.util.model.PagedResourcesProject;
import com.tracelink.prodsec.plugin.veracode.sca.util.model.PagedResourcesWorkspace;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper for the Veracode SCA API methods that performs requests to the Veracode SCA server and
 * handles responses.
 *
 * @author mcool
 */
public class VeracodeScaApiWrapper {

	private static final Logger LOGGER = LoggerFactory.getLogger(VeracodeScaApiWrapper.class);
	private static final Gson GSON = new Gson();

	private static final String GET = "GET";
	private static final String QUERY_PARAMS = "?page=%s&size=%s";
	private static final int STANDARD_SIZE = 50;
	private static final int PAGE_ZERO = 0;
	private static final int SIZE_ONE = 1;
	private static final String WORKSPACES_URI = "/v3/workspaces" + QUERY_PARAMS;
	private static final String PROJECTS_URI = "/v3/workspaces/%s/projects" + QUERY_PARAMS;
	private static final String ISSUES_URI =
			"/v3/workspaces/%s/issues" + QUERY_PARAMS + "&status=open,fixed&project_id=%s";
	private static final String BRANCH_PARAM = "&branch=%s";

	private static final int NUM_RETRIES = 3;
	private static final String REQUEST_ERROR = "Error sending request to Veracode SCA API: ";
	private static final String NO_RESPONSE = "Could not obtain response from Veracode SCA API";
	private static final String BAD_STATUS = "Received status code %d from Veracode SCA API endpoint %s: %s";
	private static final String RETRY = "Retrying request. Retries remaining: %d";
	private static final String BAD_JSON = "Received malformed JSON from Veracode SCA API endpoint %s: %s";

	private VeracodeScaClient client;

	public void setClient(VeracodeScaClient client) {
		this.client = client;
	}

	public PagedResourcesWorkspace getWorkspaces(long page) throws VeracodeScaApiException {
		String url = String.format(WORKSPACES_URI, page, STANDARD_SIZE);
		String json = getRequest(url);
		try {
			return GSON.fromJson(json, PagedResourcesWorkspace.class);
		} catch (JsonSyntaxException e) {
			LOGGER.error(String.format(BAD_JSON, url, e.getMessage()));
			return new PagedResourcesWorkspace();
		}
	}

	public PagedResourcesProject getProjects(UUID workspaceId, long page)
			throws VeracodeScaApiException {
		String url = String.format(PROJECTS_URI, workspaceId, page, STANDARD_SIZE);
		String json = getRequest(url);
		try {
			return GSON.fromJson(json, PagedResourcesProject.class);
		} catch (JsonSyntaxException e) {
			LOGGER.error(String.format(BAD_JSON, url, e.getMessage()));
			return new PagedResourcesProject();
		}
	}

	public PagedResourcesIssueSummary getIssues(UUID workspaceId, UUID projectId, String branch,
			long page) throws VeracodeScaApiException {
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

	public PagedResourcesIssueSummary getIssue(UUID workspaceId, UUID projectId)
			throws VeracodeScaApiException {
		String url = String.format(ISSUES_URI, workspaceId, PAGE_ZERO, SIZE_ONE, projectId);
		String json = getRequest(url);
		try {
			return GSON.fromJson(json, PagedResourcesIssueSummary.class);
		} catch (JsonSyntaxException e) {
			LOGGER.error(String.format(BAD_JSON, url, e.getMessage()));
			return new PagedResourcesIssueSummary();
		}
	}

	private String getRequest(String path) throws VeracodeScaApiException {
		HttpResponse<String> response = null;
		final String url = client.getApiUrl() + path;
		int retries = NUM_RETRIES;
		while (retries > 0) {
			try {
				final String authorizationHeader = HmacRequestSigner
						.getVeracodeAuthorizationHeader(client.getApiId(), client.getApiSecretKey(),
								new URL(url), GET);
				response = Unirest.get(url).header("Authorization", authorizationHeader).asString();
			} catch (IllegalArgumentException | InvalidKeyException | NoSuchAlgorithmException
					| MalformedURLException e) {
				LOGGER.error(REQUEST_ERROR + e.getMessage());
			}
			retries--;

			// Handle response
			if (response == null) {
				throw new VeracodeScaApiException(NO_RESPONSE);
			} else if (response.isSuccess()) {
				return response.getBody();
			} else if (response.getStatus() / 100 == 5) {
				// If there is a 5xx status, retry
				if (retries > 0) {
					LOGGER.warn(String.format(BAD_STATUS, response.getStatus(), url,
							response.getBody()));
					LOGGER.info(String.format(RETRY, retries));
				}
			} else {
				// If there is any other unsuccessful status, exit
				break;
			}
		}
		throw new VeracodeScaApiException(
				String.format(BAD_STATUS, response.getStatus(), url, response.getBody()));

	}
}
