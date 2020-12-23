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
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
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
	private static final String QUERY_PARAMS = "?page=%s&size=50";
	private static final String WORKSPACES_URI = "/v3/workspaces" + QUERY_PARAMS;
	private static final String PROJECTS_URI = "/v3/workspaces/%s/projects" + QUERY_PARAMS;
	private static final String ISSUES_URI =
			"/v3/workspaces/%s/issues" + QUERY_PARAMS + "&status=open,fixed";

	private VeracodeScaClient client;

	public void setClient(VeracodeScaClient client) {
		this.client = client;
	}

	public PagedResourcesWorkspace getWorkspaces(long page)
			throws VeracodeScaApiException, JsonSyntaxException {
		String json = getRequest(String.format(WORKSPACES_URI, page));
		return GSON.fromJson(json, PagedResourcesWorkspace.class);
	}

	public PagedResourcesProject getProjects(String workspaceId, long page)
			throws VeracodeScaApiException, JsonSyntaxException {
		String json = getRequest(String.format(PROJECTS_URI, workspaceId, page));
		return GSON.fromJson(json, PagedResourcesProject.class);
	}

	public PagedResourcesIssueSummary getIssues(String workspaceId, long page)
			throws VeracodeScaApiException, JsonSyntaxException {
		String json = getRequest(String.format(ISSUES_URI, workspaceId, page));
		return GSON.fromJson(json, PagedResourcesIssueSummary.class);
	}

	private String getRequest(String path) throws VeracodeScaApiException {
		HttpResponse<String> json = null;
		try {
			final String url = client.getApiUrl() + path;
			final String authorizationHeader = HmacRequestSigner
					.getVeracodeAuthorizationHeader(client.getApiId(), client.getApiSecretKey(),
							new URL(url), GET);
			json = Unirest.get(url).header("Authorization", authorizationHeader).asString();
		} catch (InvalidKeyException | NoSuchAlgorithmException | MalformedURLException e) {
			LOGGER.error("Error sending request to Veracode SCA API: " + e.getMessage());
		}
		if (json != null && json.isSuccess()) {
			return json.getBody();
		} else {
			throw new VeracodeScaApiException("Could not obtain response from Veracode SCA API");
		}
	}
}
