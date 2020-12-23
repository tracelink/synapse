package com.tracelink.prodsec.synapse.encryption.converter;

import com.tracelink.prodsec.synapse.encryption.service.DataEncryptionService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class StringEncryptedAttributeConverterTest {

	@MockBean
	private DataEncryptionService dataEncryptionService;

	@Test
	public void testConvertEntityAttributeToString() {
		StringEncryptedAttributeConverter converter = new StringEncryptedAttributeConverter(
				dataEncryptionService);
		Assert.assertEquals("foo", converter.convertEntityAttributeToString("foo"));
	}

	@Test
	public void testConvertStringToEntityAttribute() {
		StringEncryptedAttributeConverter converter = new StringEncryptedAttributeConverter(
				dataEncryptionService);
		Assert.assertEquals("foo", converter.convertStringToEntityAttribute("foo"));
	}

	@Test
	public void testConvertToDatabaseColumn() {
		StringEncryptedAttributeConverter converter = new StringEncryptedAttributeConverter(
				dataEncryptionService);

		BDDMockito.when(dataEncryptionService.encryptString("foo", converter.getClass()))
				.thenReturn("bar");

		Assert.assertEquals("bar", converter.convertToDatabaseColumn("foo"));
	}

	@Test
	public void testConvertToEntityAttribute() {
		StringEncryptedAttributeConverter converter = new StringEncryptedAttributeConverter(
				dataEncryptionService);

		BDDMockito.when(dataEncryptionService.decryptString("bar", converter.getClass()))
				.thenReturn("foo");

		Assert.assertEquals("foo", converter.convertToEntityAttribute("bar"));
	}
}
