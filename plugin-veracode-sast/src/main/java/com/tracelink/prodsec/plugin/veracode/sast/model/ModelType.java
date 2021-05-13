package com.tracelink.prodsec.plugin.veracode.sast.model;

import javax.persistence.AttributeConverter;

/**
 * Enum for the types of Veracode SAST scans.
 */
public enum ModelType {
	APP("Application"), SBX("Sandbox"),
	;
	private final String typeName;

	ModelType(String typeName) {
		this.typeName = typeName;
	}

	@Override
	public String toString() {
		return this.typeName;
	}

	/**
	 * Gets the model type associated with the given name
	 *
	 * @param typeName the name to get the model type for
	 * @return the model type, or null if no model type matches the given name
	 */
	public static ModelType ofModelType(String typeName) {
		for (ModelType t : ModelType.values()) {
			if (t.typeName.equals(typeName)) {
				return t;
			}
		}
		return null;
	}

	/**
	 * Converter to store and retrieve the {@link ModelType} enum in the database.
	 */
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
