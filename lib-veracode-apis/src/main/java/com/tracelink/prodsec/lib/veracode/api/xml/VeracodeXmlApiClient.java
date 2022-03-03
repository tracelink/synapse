package com.tracelink.prodsec.lib.veracode.api.xml;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.tracelink.prodsec.lib.veracode.api.xml.data.buildlist.Buildlist;
import com.veracode.apiwrapper.wrappers.ResultsAPIWrapper;
import com.veracode.apiwrapper.wrappers.SandboxAPIWrapper;
import com.veracode.apiwrapper.wrappers.UploadAPIWrapper;


/**
 * A client implementation to wrap the Veracode API and produce reasonable
 * POJOs.
 *
 * @author csmith
 */
public class VeracodeXmlApiClient {

	private UploadAPIWrapper uploadWrapper = new UploadAPIWrapper();

	public VeracodeXmlApiClient(String apiID, String apiKey) {
		uploadWrapper.setUpApiCredentials(apiID, apiKey);
	}

	private <T> T translate(String input, Class<T> target)
			throws JAXBException, XMLStreamException {
		JAXBContext jaxb = JAXBContext.newInstance(target);

		XMLInputFactory xif = XMLInputFactory.newFactory();
		xif.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
		xif.setProperty(XMLInputFactory.SUPPORT_DTD, false);
		XMLStreamReader xsr = xif.createXMLStreamReader(new StringReader(input));

		Unmarshaller unmarshaller = jaxb.createUnmarshaller();
		Object result = unmarshaller.unmarshal(xsr);
		return target.cast(result);
	}


	/**
	 * get a list of builds for this application/sandbox combo
	 *
	 * @param appId the application id to search by
	 * @return a Build list object for this appid and sandboxid
	 * @throws VeracodeXmlApiException if the API had an issue or if the result
	 *                                 could not be parsed into a POJO
	 */
	public Buildlist getBuildList(String appId) throws VeracodeXmlApiException {
		Buildlist buildList;
		String message;
		try {
			message = this.uploadWrapper.getBuildList(appId);
			buildList = translate(message, Buildlist.class);
		} catch (JAXBException | IOException | XMLStreamException e) {
			throw new VeracodeXmlApiException("Could not get XML build list for appid " + appId, e);
		}
		return buildList;
	}
}
