package com.tracelink.prodsec.synapse.util.converter;

import org.apache.commons.codec.binary.Hex;
import org.junit.Assert;
import org.junit.Test;

public class HexAttributeConverterTest {

	@Test
	public void testConvertToDatabaseColumn() {
		HexAttributeConverter converter = new HexAttributeConverter();
		String hexEncoded = converter.convertToDatabaseColumn("foo");
		Assert.assertEquals(Hex.encodeHexString("foo".getBytes()), hexEncoded);
	}

	@Test
	public void testConvertToDatabaseColumnNull() {
		HexAttributeConverter converter = new HexAttributeConverter();
		Assert.assertNull(converter.convertToDatabaseColumn(null));
	}

	@Test
	public void testConvertToEntityAttribute() {
		HexAttributeConverter converter = new HexAttributeConverter();
		String hexEncoded = Hex.encodeHexString("foo".getBytes());
		Assert.assertEquals("foo", converter.convertToEntityAttribute(hexEncoded));
	}

	@Test
	public void testConvertToEntityAttributeNull() {
		HexAttributeConverter converter = new HexAttributeConverter();
		Assert.assertNull(converter.convertToEntityAttribute(null));
	}
}
