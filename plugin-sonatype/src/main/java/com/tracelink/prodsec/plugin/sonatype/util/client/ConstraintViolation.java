package com.tracelink.prodsec.plugin.sonatype.util.client;

import java.util.List;

public class ConstraintViolation {

	private String constraintId;
	private String constraintName;
	private List<Reason> reasons = null;

	public void setConstraintId(String constraintId) {
		this.constraintId = constraintId;
	}

	public void setConstraintName(String constraintName) {
		this.constraintName = constraintName;
	}

	public void setReasons(List<Reason> reasons) {
		this.reasons = reasons;
	}
}
