package com.tracelink.prodsec.lib.veracode.api;

import com.tracelink.prodsec.lib.veracode.api.rest.VeracodeRestApiClient;
import com.tracelink.prodsec.lib.veracode.api.rest.VeracodeRestApiException;
import com.tracelink.prodsec.lib.veracode.api.rest.model.ApplicationScan.ScanTypeEnum;
import com.tracelink.prodsec.lib.veracode.api.rest.model.PagedResourceOfApplication;
import com.tracelink.prodsec.lib.veracode.api.rest.model.SummaryReport;
import com.tracelink.prodsec.lib.veracode.api.xml.VeracodeXmlApiClient;
import com.tracelink.prodsec.lib.veracode.api.xml.VeracodeXmlApiException;
import com.tracelink.prodsec.lib.veracode.api.xml.data.applist.Applist;
import com.tracelink.prodsec.lib.veracode.api.xml.data.buildlist.Buildlist;
import com.tracelink.prodsec.lib.veracode.api.xml.data.detailedreport.Detailedreport;

public class VeracodeApiClient {
	private final VeracodeRestApiClient restApi;
	private final VeracodeXmlApiClient xmlApi;
	
	public VeracodeApiClient(String apiBaseUrl, String key, String secret){
		restApi = new VeracodeRestApiClient(apiBaseUrl, key, secret);
		xmlApi = new VeracodeXmlApiClient(key, secret);
	}
	
	public Detailedreport getXMLDetailedReport(String buildId) throws VeracodeXmlApiException {
		return xmlApi.getDetailedReport(buildId);
	}
	
	public Applist getXMLApplications() throws VeracodeXmlApiException {
		return xmlApi.getApplications();
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
}
