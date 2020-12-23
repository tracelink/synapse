package com.tracelink.prodsec.plugin.jira.exception;

/**
 * Exception thrown when there are no Jira phrases configured in the
 * database.
 *
 * @author bhoran
 */
public class JiraPhrasesException extends RuntimeException {

	private static final long serialVersionUID = -8874094340309364469L;

	/**
	 * Creates an exception with the given message.
	 *
	 * @param message string indicating reason for exception thrown
	 */
	public JiraPhrasesException(String message) {
		super(message);
	}
}
