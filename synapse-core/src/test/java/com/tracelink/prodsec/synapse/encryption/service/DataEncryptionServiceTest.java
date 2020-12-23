package com.tracelink.prodsec.synapse.encryption.service;

import ch.qos.logback.classic.Level;
import com.tracelink.prodsec.synapse.encryption.model.DataEncryptionKey;
import com.tracelink.prodsec.synapse.encryption.model.EncryptionType;
import com.tracelink.prodsec.synapse.encryption.repository.DataEncryptionKeyRepository;
import com.tracelink.prodsec.synapse.encryption.utils.EncryptionUtils;
import com.tracelink.prodsec.synapse.test.LoggerRule;
import java.security.Key;
import java.util.Optional;
import javax.crypto.spec.SecretKeySpec;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class DataEncryptionServiceTest {

	@Rule
	public final LoggerRule loggerRule = LoggerRule.forClass(DataEncryptionService.class);

	@MockBean
	private DataEncryptionKeyRepository dataEncryptionKeyRepository;

	@Test
	public void testEncryptDecryptString() {
		DataEncryptionService dataEncryptionService = new DataEncryptionService(
				EncryptionType.ENVIRONMENT, dataEncryptionKeyRepository);

		DataEncryptionKey dataEncryptionKey = new DataEncryptionKey();
		dataEncryptionKey.setCurrentKey(EncryptionUtils.generateKey());
		dataEncryptionKey.setConverterClassName(TestConverter.class.getName());
		BDDMockito.when(dataEncryptionKeyRepository
				.findByConverterClassName(TestConverter.class.getName())).thenReturn(
				Optional.of(dataEncryptionKey));

		String encrypted = dataEncryptionService.encryptString("foo", TestConverter.class);
		Assert.assertNotEquals("foo", encrypted);
		Assert.assertEquals("foo",
				dataEncryptionService.decryptString(encrypted, TestConverter.class));
	}

	@Test
	public void testEncryptDecryptStringNullAttribute() {
		DataEncryptionService dataEncryptionService = new DataEncryptionService(
				EncryptionType.ENVIRONMENT, dataEncryptionKeyRepository);

		Assert.assertNull(dataEncryptionService.encryptString(null, TestConverter.class));
		Assert.assertNull(dataEncryptionService.decryptString(null, TestConverter.class));
	}

	@Test
	public void testEncryptDecryptStringEncryptionTypeNone() {
		DataEncryptionService dataEncryptionService = new DataEncryptionService(
				EncryptionType.NONE, dataEncryptionKeyRepository);

		Assert.assertEquals("foo", dataEncryptionService.encryptString("foo", TestConverter.class));
		Assert.assertEquals("bar", dataEncryptionService.decryptString("bar", TestConverter.class));
	}

	@Test
	public void testEncryptDecryptStringDekNotFound() {
		loggerRule.setLevel(Level.ALL);
		DataEncryptionService dataEncryptionService = new DataEncryptionService(
				EncryptionType.ENVIRONMENT, dataEncryptionKeyRepository);

		BDDMockito.when(dataEncryptionKeyRepository
				.findByConverterClassName(TestConverter.class.getName()))
				.thenReturn(Optional.empty());

		Assert.assertEquals("foo", dataEncryptionService.encryptString("foo", TestConverter.class));
		Assert.assertEquals("foo", dataEncryptionService.decryptString("foo", TestConverter.class));

		Assert.assertEquals(2, loggerRule.getMessages().size());
		Assert.assertEquals("No data encryption key found for converter "
				+ TestConverter.class.getName(), loggerRule.getMessages().get(0));
		Assert.assertEquals("No data encryption key found for converter "
				+ TestConverter.class.getName(), loggerRule.getMessages().get(1));
	}

	@Test
	public void testEncryptDecryptStringNullCipher() {
		DataEncryptionService dataEncryptionService = new DataEncryptionService(
				EncryptionType.ENVIRONMENT, dataEncryptionKeyRepository);

		DataEncryptionKey dataEncryptionKey = new DataEncryptionKey();
		dataEncryptionKey.setConverterClassName(TestConverter.class.getName());
		dataEncryptionKey.setCurrentKey(new SecretKeySpec("qwertyuiop".getBytes(), "DES"));
		BDDMockito.when(dataEncryptionKeyRepository
				.findByConverterClassName(TestConverter.class.getName()))
				.thenReturn(Optional.of(dataEncryptionKey));

		Assert.assertEquals("foo", dataEncryptionService.encryptString("foo", TestConverter.class));
		Assert.assertEquals("foo", dataEncryptionService.decryptString("foo", TestConverter.class));

		Assert.assertEquals(3, loggerRule.getMessages().size());
		Assert.assertEquals("Cannot initialize cipher when trying to encrypt with converter "
				+ TestConverter.class.getName(), loggerRule.getMessages().get(0));
		Assert.assertEquals("Cannot initialize cipher when trying to decrypt with converter "
				+ TestConverter.class.getName(), loggerRule.getMessages().get(1));
		Assert.assertEquals("Cannot decrypt database column with converter "
				+ TestConverter.class.getName(), loggerRule.getMessages().get(2));
	}

	@Test
	public void testEncryptDecryptStringNullKey() {
		DataEncryptionService dataEncryptionService = new DataEncryptionService(
				EncryptionType.ENVIRONMENT, dataEncryptionKeyRepository);

		DataEncryptionKey dataEncryptionKey = new DataEncryptionKey();
		dataEncryptionKey.setConverterClassName(TestConverter.class.getName());
		dataEncryptionKey.setRotationInProgress(true);
		BDDMockito.when(dataEncryptionKeyRepository
				.findByConverterClassName(TestConverter.class.getName()))
				.thenReturn(Optional.of(dataEncryptionKey));

		Assert.assertEquals("foo", dataEncryptionService.encryptString("foo", TestConverter.class));
		Assert.assertEquals("foo", dataEncryptionService.decryptString("foo", TestConverter.class));

		Assert.assertEquals(3, loggerRule.getMessages().size());
		Assert.assertEquals("Skipping decryption with null key", loggerRule.getMessages().get(0));
		Assert.assertEquals(
				"Retrying decryption with current key for converter " + TestConverter.class
						.getName(), loggerRule.getMessages().get(1));
		Assert.assertEquals("Skipping decryption with null key", loggerRule.getMessages().get(2));
	}

	@Test
	public void testDecryptStringFailure() {
		DataEncryptionService dataEncryptionService = new DataEncryptionService(
				EncryptionType.ENVIRONMENT, dataEncryptionKeyRepository);

		DataEncryptionKey dataEncryptionKey = new DataEncryptionKey();
		dataEncryptionKey.setConverterClassName(TestConverter.class.getName());
		dataEncryptionKey.setCurrentKey(EncryptionUtils.generateKey());

		BDDMockito.when(dataEncryptionKeyRepository
				.findByConverterClassName(TestConverter.class.getName()))
				.thenReturn(Optional.of(dataEncryptionKey));

		String encrypted = dataEncryptionService.encryptString("foo", TestConverter.class);
		// Change current encryption key
		dataEncryptionKey.setCurrentKey(EncryptionUtils.generateKey());
		Assert.assertEquals(encrypted,
				dataEncryptionService.decryptString(encrypted, TestConverter.class));

		Assert.assertEquals(2, loggerRule.getMessages().size());
		Assert.assertEquals("Cannot decrypt database column with converter "
						+ TestConverter.class.getName() + " with current key",
				loggerRule.getMessages().get(0));
		Assert.assertEquals("Cannot decrypt database column with converter "
				+ TestConverter.class.getName(), loggerRule.getMessages().get(1));
	}

	@Test
	public void testDecryptStringPreviousKey() {
		DataEncryptionService dataEncryptionService = new DataEncryptionService(
				EncryptionType.ENVIRONMENT, dataEncryptionKeyRepository);

		Key key = EncryptionUtils.generateKey();

		DataEncryptionKey dataEncryptionKey = new DataEncryptionKey();
		dataEncryptionKey.setConverterClassName(TestConverter.class.getName());
		dataEncryptionKey.setCurrentKey(key);
		dataEncryptionKey.setPreviousKey(key);
		dataEncryptionKey.setRotationInProgress(true);

		BDDMockito.when(dataEncryptionKeyRepository
				.findByConverterClassName(TestConverter.class.getName()))
				.thenReturn(Optional.of(dataEncryptionKey));

		String encrypted = dataEncryptionService.encryptString("foo", TestConverter.class);
		Assert.assertNotEquals("foo", encrypted);
		Assert.assertEquals("foo",
				dataEncryptionService.decryptString(encrypted, TestConverter.class));

		Assert.assertTrue(loggerRule.getMessages().isEmpty());
	}

	@Test
	public void testDecryptStringPreviousKeyInvalid() {
		loggerRule.setLevel(Level.ALL);
		DataEncryptionService dataEncryptionService = new DataEncryptionService(
				EncryptionType.ENVIRONMENT, dataEncryptionKeyRepository);

		Key key = EncryptionUtils.generateKey();

		DataEncryptionKey dataEncryptionKey = new DataEncryptionKey();
		dataEncryptionKey.setConverterClassName(TestConverter.class.getName());
		dataEncryptionKey.setCurrentKey(key);
		dataEncryptionKey.setPreviousKey(EncryptionUtils.generateKey());
		dataEncryptionKey.setRotationInProgress(true);

		BDDMockito.when(dataEncryptionKeyRepository
				.findByConverterClassName(TestConverter.class.getName()))
				.thenReturn(Optional.of(dataEncryptionKey));

		String encrypted = dataEncryptionService.encryptString("foo", TestConverter.class);
		Assert.assertNotEquals("foo", encrypted);
		Assert.assertEquals("foo",
				dataEncryptionService.decryptString(encrypted, TestConverter.class));

		Assert.assertEquals(2, loggerRule.getMessages().size());
		Assert.assertEquals("Cannot decrypt database column with converter "
						+ TestConverter.class.getName() + " with previous key",
				loggerRule.getMessages().get(0));
		Assert.assertEquals(
				"Retrying decryption with current key for converter " + TestConverter.class
						.getName(), loggerRule.getMessages().get(1));
	}

	@Test
	public void testDecryptStringAlreadyDecrypted() {
		DataEncryptionService dataEncryptionService = new DataEncryptionService(
				EncryptionType.ENVIRONMENT, dataEncryptionKeyRepository);

		DataEncryptionKey dataEncryptionKey = new DataEncryptionKey();
		dataEncryptionKey.setConverterClassName(TestConverter.class.getName());
		dataEncryptionKey.setPreviousKey(EncryptionUtils.generateKey());
		dataEncryptionKey.setDisabled(true);
		BDDMockito.when(dataEncryptionKeyRepository
				.findByConverterClassName(TestConverter.class.getName()))
				.thenReturn(Optional.of(dataEncryptionKey));

		Assert.assertEquals("foo", dataEncryptionService.decryptString("foo", TestConverter.class));

		Assert.assertEquals(1, loggerRule.getMessages().size());
		Assert.assertEquals("Skipping decryption with null key", loggerRule.getMessages().get(0));
	}

	static class TestConverter {

	}
}
