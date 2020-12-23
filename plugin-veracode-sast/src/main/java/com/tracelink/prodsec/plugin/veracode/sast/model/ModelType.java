package com.tracelink.prodsec.plugin.veracode.sast.model;

import javax.persistence.AttributeConverter;

public enum ModelType {
	APP("Application"), SBX("Sandbox"),;
	private final String typeName;

	ModelType(String typeName) {
		this.typeName = typeName;
	}

	@Override
	public String toString() {
		return this.typeName;
	}

	public static ModelType ofModelType(String typeName) {
		for (ModelType t : ModelType.values()) {
			if (t.typeName.equals(typeName)) {
				return t;
			}
		}
		return null;
	}

	public static class ModelTypeConverter implements AttributeConverter<ModelType, String> {

		@Override
		public String convertToDatabaseColumn(ModelType attribute) {
			return attribute.toString();
		}

		@Override
		public ModelType convertToEntityAttribute(String dbData) {
			return ModelType.ofModelType(dbData);
		}

	}
}
