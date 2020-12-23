package com.tracelink.prodsec.synapse.encryption.converter;

import com.tracelink.prodsec.synapse.encryption.service.KeyEncryptionService;
import com.tracelink.prodsec.synapse.encryption.utils.EncryptionUtils;
import java.security.Key;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class DataEncryptionKeyConverterTest {

	@MockBean
	private KeyEncryptionService keyEncryptionService;

	@Test
	public void testConvertToDatabaseColumn() {
		DataEncryptionKeyConverter converter = new DataEncryptionKeyConverter(
				keyEncryptionService);

		Key key = EncryptionUtils.generateKey();
		BDDMockito.when(keyEncryptionService.encryptKey(key)).thenReturn("bar");

		Assert.assertEquals("bar", converter.convertToDatabaseColumn(key));
	}

	@Test
	public void testConvertToEntityAttribute() {
		DataEncryptionKeyConverter converter = new DataEncryptionKeyConverter(
				keyEncryptionService);

		Key key = EncryptionUtils.generateKey();
		BDDMockito.when(keyEncryptionService.decryptKey("bar")).thenReturn(key);

		Assert.assertEquals(key, converter.convertToEntityAttribute("bar"));
	}
}
