package com.tracelink.prodsec.plugin.jira.model;

import javax.persistence.AttributeConverter;

/**
 * JiraPhraseDataFormat is an enumerated type containing static values, representing different data
 * formats within the plug-in. A data format is a value used to identify which service should use
 * a certain JQL Phrase when parsing data pulled from the Jira Server.
 */
public enum JiraPhraseDataFormat {
	SCRUM("Scrum"),
	VULN("Vulnerabilities");

	private final String displayName;

	JiraPhraseDataFormat(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}

	/**
	 * Gets the {@link JiraPhraseDataFormat} associated with the given name, or null if there is no
	 * such enum value.
	 *
	 * @param dataFormatName the name to compare against the display name
	 * @return the phrase data format
	 */
	public static JiraPhraseDataFormat ofFormat(String dataFormatName) {
		for (JiraPhraseDataFormat format : JiraPhraseDataFormat.values()) {
			if (format.getDisplayName().equals(dataFormatName)) {
				return format;
			}
		}
		return null;
	}

	/**
	 * Converter for the {@link JiraPhraseDataFormat} to store the enum in the database.
	 */
	public static class JiraPhraseDataFormatConverter implements
			AttributeConverter<JiraPhraseDataFormat, String> {

		@Override
		public String convertToDatabaseColumn(JiraPhraseDataFormat jiraPhraseDataFormat) {
			return jiraPhraseDataFormat.getDisplayName();
		}

		@Override
		public JiraPhraseDataFormat convertToEntityAttribute(String s) {
			return JiraPhraseDataFormat.ofFormat(s);
		}
	}
}
