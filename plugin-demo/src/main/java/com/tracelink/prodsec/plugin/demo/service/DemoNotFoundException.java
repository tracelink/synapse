package com.tracelink.prodsec.plugin.demo.service;

/**
 * A special class for when a demo project can't be found
 * 
 * @author csmith
 *
 */
public class DemoNotFoundException extends Exception {
	private static final long serialVersionUID = 3201161570868064613L;

	public DemoNotFoundException(String message) {
		super(message);
	}
}
