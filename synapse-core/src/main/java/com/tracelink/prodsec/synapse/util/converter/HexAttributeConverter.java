package com.tracelink.prodsec.synapse.util.converter;

import java.nio.charset.StandardCharsets;
import javax.persistence.AttributeConverter;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

/**
 * An attribute converter to handle Hex encoding of database columns.
 *
 * @author mcool
 */
public class HexAttributeConverter implements AttributeConverter<String, String> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String convertToDatabaseColumn(String attribute) {
		if (attribute == null) {
			return null;
		}
		return Hex.encodeHexString(attribute.getBytes());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String convertToEntityAttribute(String dbData) {
		if (dbData == null) {
			return null;
		}
		try {
			return new String(Hex.decodeHex(dbData), StandardCharsets.UTF_8);
		} catch (DecoderException e) {
			return "";
		}
	}
}
