package com.tracelink.prodsec.synapse.products;

/**
 * Exception when a product is asked for, but doesn't exist
 * 
 * @author csmith
 *
 */
public class ProductsNotFoundException extends Exception {

	private static final long serialVersionUID = -1915410919782324773L;

	public ProductsNotFoundException(String message) {
		super(message);
	}

}
