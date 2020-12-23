package com.tracelink.prodsec.plugin.sonatype.util.client;

public class Coordinates {

	private String artifactId;
	private String classifier;
	private String extension;
	private String groupId;
	private String version;
	private String name;
	private String qualifier;

	public void setArtifactId(String artifactId) {
		this.artifactId = artifactId;
	}

	public void setClassifier(String classifier) {
		this.classifier = classifier;
	}

	public void setExtension(String extension) {
		this.extension = extension;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setQualifier(String qualifier) {
		this.qualifier = qualifier;
	}
}
