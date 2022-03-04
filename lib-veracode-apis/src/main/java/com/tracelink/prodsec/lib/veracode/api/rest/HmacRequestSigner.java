package com.tracelink.prodsec.lib.veracode.api.rest;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Locale;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

/**
 * Helper class to sign requests to the Veracode SCA API.
 *
 * @author mcool
 */
public final class HmacRequestSigner {

	// Included in the signature to inform Veracode of the signature version.
	private static final String VERACODE_REQUEST_VERSION_STRING = "vcode_request_version_1";

	// Expected format for the unencrypted data string.
	private static final String DATA_FORMAT = "id=%s&host=%s&url=%s&method=%s";

	// Expected format for the Authorization header.
	private static final String HEADER_FORMAT = "%s id=%s,ts=%s,nonce=%s,sig=%s";

	// Expect prefix to the Authorization header.
	private static final String VERACODE_HMAC_SHA_256 = "VERACODE-HMAC-SHA-256";

	// HMAC encryption algorithm.
	private static final String HMAC_SHA_256 = "HmacSHA256";

	// A cryptographically secure random number generator.
	private static final SecureRandom SECURE_RANDOM = new SecureRandom();

	// Private constructor.
	private HmacRequestSigner() {
		/*
		 * This is a utility class that should only be accessed through its
		 * static methods.
		 */
	}

	/**
	 * Entry point for HmacRequestSigner. Returns the value for the Authorization header for use
	 * with Veracode APIs when provided an API id, secret key, and target URL.
	 *
	 * @param id         An API id for authentication
	 * @param key        The secret key corresponding to the API id
	 * @param url        The URL of the called API, including query parameters
	 * @param httpMethod The HTTP verb to use when sending the request
	 * @return The value to be put in the Authorization header
	 * @throws InvalidKeyException      if there is an error computing the signature
	 * @throws NoSuchAlgorithmException if there is an issue with the signing algorithm
	 * @throws IllegalStateException    if the MAC is not properly initialized
	 */
	public static String getVeracodeAuthorizationHeader(final String id, final String key,
			final URL url, final String httpMethod)
			throws InvalidKeyException, NoSuchAlgorithmException, IllegalStateException {
		final String urlPath = (url.getQuery() == null) ? url.getPath()
				: url.getPath().concat("?").concat(url.getQuery());
		final String data = String.format(DATA_FORMAT, id, url.getHost(), urlPath, httpMethod);
		final String timestamp = String.valueOf(System.currentTimeMillis());
		final String nonce = DatatypeConverter.printHexBinary(generateRandomBytes(16))
				.toLowerCase(Locale.US);
		final String signature = getSignature(key, data, timestamp, nonce);
		return String.format(HEADER_FORMAT, VERACODE_HMAC_SHA_256, id, timestamp, nonce, signature);
	}

	/*
	 * Generate the signature expected by the Veracode platform by chaining
	 * encryption routines in the correct order.
	 */
	private static String getSignature(final String key, final String data, final String timestamp,
			final String nonce)
			throws InvalidKeyException, NoSuchAlgorithmException, IllegalStateException {
		final byte[] keyBytes = DatatypeConverter.parseHexBinary(key);
		final byte[] nonceBytes = DatatypeConverter.parseHexBinary(nonce);
		final byte[] encryptedNonce = hmacSha256(nonceBytes, keyBytes);
		final byte[] encryptedTimestamp = hmacSha256(timestamp, encryptedNonce);
		final byte[] signingKey = hmacSha256(VERACODE_REQUEST_VERSION_STRING, encryptedTimestamp);
		final byte[] signature = hmacSha256(data, signingKey);
		return DatatypeConverter.printHexBinary(signature).toLowerCase(Locale.US);
	}

	// Encrypt a string using the provided key.
	private static byte[] hmacSha256(final String data, final byte[] key)
			throws NoSuchAlgorithmException, InvalidKeyException, IllegalStateException {
		final Mac mac = Mac.getInstance(HMAC_SHA_256);
		mac.init(new SecretKeySpec(key, HMAC_SHA_256));
		return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
	}

	// Encrypt a byte array using the provided key.
	private static byte[] hmacSha256(final byte[] data, final byte[] key)
			throws NoSuchAlgorithmException, InvalidKeyException {
		final Mac mac = Mac.getInstance(HMAC_SHA_256);
		mac.init(new SecretKeySpec(key, HMAC_SHA_256));
		return mac.doFinal(data);
	}

	// Generate a random byte array for cryptographic use.
	private static byte[] generateRandomBytes(final int size) {
		final byte[] key = new byte[size];
		SECURE_RANDOM.nextBytes(key);
		return key;
	}

}
