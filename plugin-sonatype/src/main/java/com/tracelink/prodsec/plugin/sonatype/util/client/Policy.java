package com.tracelink.prodsec.plugin.sonatype.util.client;

public class Policy {

	private String id;
	private String name;
	private String ownerId;
	private String ownerType;
	private int threatLevel;
	private String policyType;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setOwnerId(String ownerId) {
		this.ownerId = ownerId;
	}

	public void setOwnerType(String ownerType) {
		this.ownerType = ownerType;
	}

	public void setThreatLevel(int threatLevel) {
		this.threatLevel = threatLevel;
	}

	public void setPolicyType(String policyType) {
		this.policyType = policyType;
	}

}
