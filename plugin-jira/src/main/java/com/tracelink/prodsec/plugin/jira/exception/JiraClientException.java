package com.tracelink.prodsec.plugin.jira.exception;

/**
 * Exception thrown when there is no Jira API client configured in the
 * database.
 *
 * @author bhoran
 */
public class JiraClientException extends RuntimeException {

	private static final long serialVersionUID = 7188844615832718166L;

	/**
	 * Creates an exception with the given message.
	 *
	 * @param message string indicating reason for exception thrown
	 */
	public JiraClientException(String message) {
		super(message);
	}
}
