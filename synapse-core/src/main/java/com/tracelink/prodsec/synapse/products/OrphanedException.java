package com.tracelink.prodsec.synapse.products;

/**
 * Exception when an operation would leave an object without a required parent
 * 
 * @author csmith
 *
 */
public class OrphanedException extends Exception {

	private static final long serialVersionUID = 6202353944271289018L;

	public OrphanedException(String message) {
		super(message);
	}
}
