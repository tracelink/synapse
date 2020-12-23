package com.tracelink.prodsec.synapse.encryption.service;

import ch.qos.logback.classic.Level;
import com.tracelink.prodsec.synapse.encryption.model.EncryptionType;
import com.tracelink.prodsec.synapse.encryption.utils.EncryptionUtils;
import com.tracelink.prodsec.synapse.test.LoggerRule;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.security.Key;
import java.security.UnrecoverableKeyException;
import java.util.Arrays;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.ReflectionUtils;

@RunWith(SpringRunner.class)
public class KeyEncryptionServiceTest {

	@Rule
	public final LoggerRule loggerRule = LoggerRule.forClass(KeyEncryptionService.class);

	private static final String CURRENT_KEY = "currentKey";
	private static final String PREVIOUS_KEY = "previousKey";
	private static String keyStorePath;
	private static String newKeyStorePath;

	@BeforeClass
	public static void init() throws Exception {
		URL keyStoreUrl = KeyEncryptionServiceTest.class.getClassLoader()
				.getResource("encryption/keystore.p12");
		if (keyStoreUrl != null) {
			keyStorePath = Paths.get(keyStoreUrl.toURI()).toAbsolutePath().toString();
		}

		URL newKeyStoreUrl = KeyEncryptionServiceTest.class.getClassLoader()
				.getResource("encryption/new-keystore.p12");
		if (newKeyStoreUrl != null) {
			newKeyStorePath = Paths.get(newKeyStoreUrl.toURI()).toAbsolutePath().toString();
		}
	}

	@Test
	public void testInitEncryptionTypeNone() throws Exception {
		KeyEncryptionService keyEncryptionService = new KeyEncryptionService(EncryptionType.NONE,
				null, null, null, null, null, null);
		keyEncryptionService.init();

		Assert.assertNull(getField(CURRENT_KEY, keyEncryptionService));
		Assert.assertNull(getField(PREVIOUS_KEY, keyEncryptionService));

		Key key = EncryptionUtils.generateKey();
		Assert.assertNull(keyEncryptionService.encryptKey(key));
		Assert.assertNull(keyEncryptionService.decryptKey("foo"));
	}

	@Test
	public void testInitEncryptionTypeEnvironment() throws Exception {
		KeyEncryptionService keyEncryptionService = new KeyEncryptionService(
				EncryptionType.ENVIRONMENT, keyStorePath, "password", null, null, null, null);
		keyEncryptionService.init();

		Key currentKey = (Key) getField(CURRENT_KEY, keyEncryptionService);
		Assert.assertNotNull(currentKey);
		Assert.assertEquals("AES", currentKey.getAlgorithm());
		Assert.assertEquals(32, currentKey.getEncoded().length);

		Assert.assertNull(getField(PREVIOUS_KEY, keyEncryptionService));

		Assert.assertFalse(keyEncryptionService.keyRotationInProgress());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInitEncryptionTypeEnvironmentBlankKeyStorePath() throws Exception {
		KeyEncryptionService keyEncryptionService = new KeyEncryptionService(
				EncryptionType.ENVIRONMENT, " ", "password",
				null, null, null, null);
		keyEncryptionService.init();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInitEncryptionTypeEnvironmentBlankKeyStorePassword() throws Exception {
		KeyEncryptionService keyEncryptionService = new KeyEncryptionService(
				EncryptionType.ENVIRONMENT, "foo", null, null, null, null, null);
		keyEncryptionService.init();
	}

	@Test(expected = IOException.class)
	public void testInitEncryptionTypeEnvironmentBadPassword() throws Exception {
		KeyEncryptionService keyEncryptionService = new KeyEncryptionService(
				EncryptionType.ENVIRONMENT, keyStorePath, "foo", null, null, null, null);
		keyEncryptionService.init();
	}

	@Test(expected = UnrecoverableKeyException.class)
	public void testInitEncryptionTypeEnvironmentBadAlias() throws Exception {
		KeyEncryptionService keyEncryptionService = new KeyEncryptionService(
				EncryptionType.ENVIRONMENT, newKeyStorePath, "password", null, null, null, null);
		keyEncryptionService.init();
	}

	@Test
	public void testInitEncryptionTypeEnvironmentWithPreviousKey() throws Exception {
		KeyEncryptionService keyEncryptionService = new KeyEncryptionService(
				EncryptionType.ENVIRONMENT, newKeyStorePath, "password", "synapse", keyStorePath,
				"password", null);
		keyEncryptionService.init();

		Key currentKey = (Key) getField(CURRENT_KEY, keyEncryptionService);
		Assert.assertNotNull(currentKey);
		Assert.assertEquals("AES", currentKey.getAlgorithm());
		Assert.assertEquals(32, currentKey.getEncoded().length);

		Key previousKey = (Key) getField(PREVIOUS_KEY, keyEncryptionService);
		Assert.assertNotNull(previousKey);
		Assert.assertEquals("AES", previousKey.getAlgorithm());
		Assert.assertEquals(32, previousKey.getEncoded().length);

		Assert.assertTrue(keyEncryptionService.keyRotationInProgress());
		keyEncryptionService.finishKeyRotation();
		Assert.assertFalse(keyEncryptionService.keyRotationInProgress());
	}

	@Test
	public void testEncryptDecryptKey() throws Exception {
		KeyEncryptionService keyEncryptionService = new KeyEncryptionService(
				EncryptionType.ENVIRONMENT, keyStorePath, "password", null, null, null, null);
		keyEncryptionService.init();

		Key key = EncryptionUtils.generateKey();
		String encrypted = keyEncryptionService.encryptKey(key);
		Assert.assertFalse(Arrays.equals(key.getEncoded(), encrypted.getBytes()));

		Key decrypted = keyEncryptionService.decryptKey(encrypted);
		Assert.assertArrayEquals(key.getEncoded(), decrypted.getEncoded());
		Assert.assertEquals(key.getAlgorithm(), decrypted.getAlgorithm());
		Assert.assertEquals(key.getFormat(), decrypted.getFormat());
	}

	@Test
	public void testEncryptDecryptKeyNull() throws Exception {
		KeyEncryptionService keyEncryptionService = new KeyEncryptionService(
				EncryptionType.ENVIRONMENT, keyStorePath, "password", null, null, null, null);
		keyEncryptionService.init();

		Assert.assertNull(keyEncryptionService.encryptKey(null));
		Assert.assertNull(keyEncryptionService.decryptKey(null));
	}

	@Test
	public void testEncryptDecryptKeyNullCipher() throws Exception {
		KeyEncryptionService keyEncryptionService = new KeyEncryptionService(
				EncryptionType.ENVIRONMENT, keyStorePath, "password", null, null, null, null);
		keyEncryptionService.init();
		setField(CURRENT_KEY, keyEncryptionService, null);

		Key key = EncryptionUtils.generateKey();
		Assert.assertEquals(new String(key.getEncoded(), StandardCharsets.UTF_8),
				keyEncryptionService.encryptKey(key));

		Assert.assertNull(keyEncryptionService.decryptKey("foo"));

		Assert.assertEquals(3, loggerRule.getMessages().size());
		Assert.assertEquals(
				"Cannot initialize cipher when trying to wrap data encryption key with current key",
				loggerRule.getMessages().get(0));
		Assert.assertEquals(
				"Cannot initialize cipher when trying to unwrap data encryption key with current key",
				loggerRule.getMessages().get(1));
		Assert.assertEquals("Cannot unwrap data encryption key", loggerRule.getMessages().get(2));
	}

	@Test
	public void testDecryptKeyInvalidKey() throws Exception {
		KeyEncryptionService keyEncryptionService = new KeyEncryptionService(
				EncryptionType.ENVIRONMENT, keyStorePath, "password", null, null, null, null);
		keyEncryptionService.init();

		Key key = EncryptionUtils.generateKey();
		String encryptedKey = keyEncryptionService.encryptKey(key);

		setField(CURRENT_KEY, keyEncryptionService, EncryptionUtils.generateKey());
		Assert.assertNull(keyEncryptionService.decryptKey(encryptedKey));

		Assert.assertEquals(2, loggerRule.getMessages().size());
		Assert.assertEquals(
				"Cannot unwrap data encryption key with current key",
				loggerRule.getMessages().get(0));
		Assert.assertEquals("Cannot unwrap data encryption key", loggerRule.getMessages().get(1));
	}

	@Test
	public void testDecryptKeyInvalidKeyDuringRotation() throws Exception {
		loggerRule.setLevel(Level.ALL);
		KeyEncryptionService keyEncryptionService = new KeyEncryptionService(
				EncryptionType.ENVIRONMENT, keyStorePath, "password", null, null, null, null);
		keyEncryptionService.init();

		Key key = EncryptionUtils.generateKey();
		String encrypted = keyEncryptionService.encryptKey(key);
		setField(PREVIOUS_KEY, keyEncryptionService, EncryptionUtils.generateKey());
		setField("keyRotationInProgress", keyEncryptionService, true);

		Key decrypted = keyEncryptionService.decryptKey(encrypted);
		Assert.assertArrayEquals(key.getEncoded(), decrypted.getEncoded());
		Assert.assertEquals(key.getAlgorithm(), decrypted.getAlgorithm());
		Assert.assertEquals(key.getFormat(), decrypted.getFormat());

		Assert.assertFalse(loggerRule.getMessages().isEmpty());
		Assert.assertTrue(loggerRule.getMessages().get(0)
				.contains("Cannot unwrap data encryption key with previous key"));
		Assert.assertTrue(loggerRule.getMessages().get(1)
				.contains("Retrying decryption with current key"));
	}

	@Test
	public void testDecryptKeyPreviousKey() throws Exception {
		KeyEncryptionService keyEncryptionService = new KeyEncryptionService(
				EncryptionType.ENVIRONMENT, keyStorePath, "password", null, null, null, null);
		keyEncryptionService.init();

		Key key = EncryptionUtils.generateKey();
		String encrypted = keyEncryptionService.encryptKey(key);
		setField(PREVIOUS_KEY, keyEncryptionService, getField(CURRENT_KEY, keyEncryptionService));
		setField("keyRotationInProgress", keyEncryptionService, true);

		Key decrypted = keyEncryptionService.decryptKey(encrypted);
		Assert.assertArrayEquals(key.getEncoded(), decrypted.getEncoded());
		Assert.assertEquals(key.getAlgorithm(), decrypted.getAlgorithm());
		Assert.assertEquals(key.getFormat(), decrypted.getFormat());

		Assert.assertTrue(loggerRule.getMessages().isEmpty());
	}

	private static Object getField(String name, Object target) {
		Field field = ReflectionUtils
				.findField(KeyEncryptionService.class, name);
		if (field == null) {
			Assert.fail("Cannot find " + name + " field");
		}
		ReflectionUtils.makeAccessible(field);
		return ReflectionUtils.getField(field, target);
	}

	private static void setField(String name, Object target, Object value) {
		Field field = ReflectionUtils
				.findField(KeyEncryptionService.class, name);
		if (field == null) {
			Assert.fail("Cannot find " + name + " field");
		}
		ReflectionUtils.makeAccessible(field);
		ReflectionUtils.setField(field, target, value);
	}
}
