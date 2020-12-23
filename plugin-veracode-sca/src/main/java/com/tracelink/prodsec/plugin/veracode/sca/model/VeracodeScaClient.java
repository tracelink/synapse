package com.tracelink.prodsec.plugin.veracode.sca.model;

import com.tracelink.prodsec.plugin.veracode.sca.VeracodeScaPlugin;
import com.tracelink.prodsec.synapse.encryption.converter.StringEncryptedAttributeConverter;
import com.tracelink.prodsec.synapse.encryption.service.DataEncryptionService;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import org.springframework.context.annotation.Lazy;

/**
 * The Database entity for the Veracode SCA API client.
 * <p>
 * Used to fetch data from the Veracode SCA server.
 *
 * @author mcool
 */
@Entity
@Table(schema = VeracodeScaPlugin.SCHEMA, name = "veracode_sca_clients")
public class VeracodeScaClient {

	private static final String VERACODE_API_URL = "https://api.veracode.com/srcclr";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "client_id")
	private long id;

	@Transient
	private String apiUrl = VERACODE_API_URL;

	@Column(name = "api_id")
	private String apiId;

	@Column(name = "api_secret_key")
	@Convert(converter = VeracodeScaClientConverter.class)
	private String apiSecretKey;

	public long getId() {
		return id;
	}

	public String getApiUrl() {
		return apiUrl;
	}

	public void setApiUrl(String apiUrl) {
		this.apiUrl = apiUrl;
	}

	public String getApiId() {
		return apiId;
	}

	public void setApiId(String apiId) {
		this.apiId = apiId;
	}

	public String getApiSecretKey() {
		return apiSecretKey;
	}

	public void setApiSecretKey(String apiSecretKey) {
		this.apiSecretKey = apiSecretKey;
	}

	/**
	 * Encrypted attribute converter class for Veracode SCA clients.
	 */
	static class VeracodeScaClientConverter extends StringEncryptedAttributeConverter {

		public VeracodeScaClientConverter(@Lazy DataEncryptionService dataEncryptionService) {
			super(dataEncryptionService);
		}
	}
}
