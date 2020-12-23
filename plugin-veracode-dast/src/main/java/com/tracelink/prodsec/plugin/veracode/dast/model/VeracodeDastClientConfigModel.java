package com.tracelink.prodsec.plugin.veracode.dast.model;

import com.tracelink.prodsec.plugin.veracode.dast.VeracodeDastPlugin;
import com.tracelink.prodsec.synapse.encryption.converter.StringEncryptedAttributeConverter;
import com.tracelink.prodsec.synapse.encryption.service.DataEncryptionService;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import org.springframework.context.annotation.Lazy;

/**
 * The Client Config stores API authentication information to interact with
 * Veracode
 *
 * @author csmith
 */
@Entity
@Table(schema = VeracodeDastPlugin.SCHEMA, name = "veracode_dast_client_config")
public class VeracodeDastClientConfigModel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "client_id")
	private long id;

	@Column(name = "api_id")
	private String apiId;

	@Column(name = "api_key")
	@Convert(converter = VeracodeDastClientConfigConverter.class)
	private String apiKey;

	public long getId() {
		return id;
	}

	public String getApiId() {
		return apiId;
	}

	public void setApiId(String apiId) {
		this.apiId = apiId;
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	/**
	 * Encrypted attribute converter class for Veracode DAST client configurations.
	 */
	static class VeracodeDastClientConfigConverter extends StringEncryptedAttributeConverter {

		public VeracodeDastClientConfigConverter(
				@Lazy DataEncryptionService dataEncryptionService) {
			super(dataEncryptionService);
		}
	}

}
