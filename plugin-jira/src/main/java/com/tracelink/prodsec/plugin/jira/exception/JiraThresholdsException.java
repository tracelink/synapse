package com.tracelink.prodsec.plugin.jira.exception;

/**
 * Exception thrown when there are no violation tolerance thresholds configured
 * in the database.
 *
 * @author bhoran
 */
public class JiraThresholdsException extends RuntimeException {

	private static final long serialVersionUID = 5185228180002111817L;

	/**
	 * Creates an exception with the given message.
	 *
	 * @param message string indicating reason for exception thrown
	 */
	public JiraThresholdsException(String message) {
		super(message);
	}
}
