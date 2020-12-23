package com.tracelink.prodsec.plugin.jira.exception;

/**
 * Exception thrown when there are no SLA configurations in the
 * database.
 *
 * @author bhoran
 */
public class JiraAllowedSlaException extends RuntimeException {

	private static final long serialVersionUID = -8919703065402862834L;

	/**
	 * Creates an exception with the given message.
	 *
	 * @param message string indicating reason for exception thrown
	 */
	public JiraAllowedSlaException(String message) {
		super(message);
	}
}
