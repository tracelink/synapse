package com.tracelink.prodsec.plugin.veracode.sast.model;

import com.tracelink.prodsec.plugin.veracode.sast.VeracodeSastPlugin;
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
@Table(schema = VeracodeSastPlugin.SCHEMA, name = "veracode_sast_client_config")
public class VeracodeSastClientConfigModel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "client_id")
	private long id;

	@Column(name = "api_id")
	private String apiId;

	@Column(name = "api_key")
	@Convert(converter = VeracodeSastClientConfigConverter.class)
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
	 * Encrypted attribute converter class for Veracode SAST client configurations.
	 */
	static class VeracodeSastClientConfigConverter extends StringEncryptedAttributeConverter {

		public VeracodeSastClientConfigConverter(
				@Lazy DataEncryptionService dataEncryptionService) {
			super(dataEncryptionService);
		}
	}

}
