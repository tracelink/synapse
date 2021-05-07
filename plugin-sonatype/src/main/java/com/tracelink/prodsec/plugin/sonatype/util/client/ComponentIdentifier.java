package com.tracelink.prodsec.plugin.sonatype.util.client;

/**
 * DTO to store ComponentIdentifier info from the Sonatype Nexus IQ API.
 */
public class ComponentIdentifier {

	private String format;
	private Coordinates coordinates;

	public void setFormat(String format) {
		this.format = format;
	}

	public void setCoordinates(Coordinates coordinates) {
		this.coordinates = coordinates;
	}
}
