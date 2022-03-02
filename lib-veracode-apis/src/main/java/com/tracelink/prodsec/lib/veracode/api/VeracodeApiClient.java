package com.tracelink.prodsec.lib.veracode.api;

import com.tracelink.prodsec.lib.veracode.api.rest.VeracodeRestApiClient;
import com.tracelink.prodsec.lib.veracode.api.rest.VeracodeRestApiException;
import com.tracelink.prodsec.lib.veracode.api.rest.VeracodeRestPagedResourcesIterator;
import com.tracelink.prodsec.lib.veracode.api.rest.model.ApplicationScan.ScanTypeEnum;
import com.tracelink.prodsec.lib.veracode.api.rest.model.PagedResourceOfApplication;
import com.tracelink.prodsec.lib.veracode.api.rest.model.SummaryReport;
import com.tracelink.prodsec.lib.veracode.api.xml.VeracodeXmlApiClient;
import com.tracelink.prodsec.lib.veracode.api.xml.VeracodeXmlApiException;
import com.tracelink.prodsec.lib.veracode.api.xml.data.buildlist.BuildType;
import com.tracelink.prodsec.lib.veracode.api.xml.data.buildlist.Buildlist;

public class VeracodeApiClient {
	private final VeracodeRestApiClient restApi;
	private final VeracodeXmlApiClient xmlApi;
	
	public VeracodeApiClient(String apiBaseUrl, String key, String secret){
		restApi = new VeracodeRestApiClient(apiBaseUrl, key, secret);
		xmlApi = new VeracodeXmlApiClient(key, secret);
	}
	
	public Buildlist getXMLBuildList(String appId) throws VeracodeXmlApiException {
		return xmlApi.getBuildList(appId);
	}
	
	public PagedResourceOfApplication getRestApplications(ScanTypeEnum type, long page) throws VeracodeRestApiException {
		return restApi.getApplications(type, page);
	}
	
	public SummaryReport getRestSummaryReport(String appId, String buildId) throws VeracodeApiException {
		return restApi.getSummaryReport(appId, buildId);
	}
	
	public void testAccess(ScanTypeEnum scanType) throws VeracodeApiException {
		// At each api call, we can fail due to an access problem,
		// so we need to call each api
		VeracodeRestPagedResourcesIterator<PagedResourceOfApplication> appIterator = new VeracodeRestPagedResourcesIterator<>(
				page -> getRestApplications(scanType, page));
		if (appIterator.hasNext()) {
			String appId = String.valueOf(appIterator.next().getEmbedded().getApplications().get(0).getId());
			Buildlist builds = getXMLBuildList(appId);
			for (BuildType build : builds.getBuild()) {
				String buildId = String.valueOf(build.getBuildId());
				getRestSummaryReport(appId, buildId);
				// we've called all apis and can quit now
				return;
			}
		} else {
			throw new VeracodeApiException("Could not get applications with the REST API");
		}
	}
}
