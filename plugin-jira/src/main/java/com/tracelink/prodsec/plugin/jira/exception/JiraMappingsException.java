package com.tracelink.prodsec.plugin.jira.exception;

/**
 * Exception thrown when there is an error creating or deleting a mapping
 * in the database.
 *
 * @author bhoran
 */
public class JiraMappingsException extends RuntimeException {

	private static final long serialVersionUID = -7704462989883185680L;

	/**
	 * Creates an exception with the given message.
	 *
	 * @param message string indicating reason for exception thrown
	 */
	public JiraMappingsException(String message) {
		super(message);
	}
}
