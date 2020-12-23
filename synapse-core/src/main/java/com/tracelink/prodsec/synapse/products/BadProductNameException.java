package com.tracelink.prodsec.synapse.products;

/**
 * Exception for when a Product name is illegal or already in use
 * 
 * @author csmith
 *
 */
public class BadProductNameException extends Exception {

	private static final long serialVersionUID = 5658660404898498957L;

	public BadProductNameException(String message) {
		super(message);
	}
}
