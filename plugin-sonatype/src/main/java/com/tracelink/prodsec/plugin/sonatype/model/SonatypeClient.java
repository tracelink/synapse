package com.tracelink.prodsec.plugin.sonatype.model;

import com.tracelink.prodsec.plugin.sonatype.SonatypePlugin;
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
 * The Database entity for the Sonatype API client.
 *
 * @author mcool
 */
@Entity
@Table(schema = SonatypePlugin.SCHEMA, name = "sonatype_clients")
public class SonatypeClient {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "client_id")
	private long id;

	@Column(name = "api_url")
	private String apiUrl;

	@Column(name = "username")
	private String user;

	@Column(name = "authentication")
	@Convert(converter = SonatypeClientConverter.class)
	private String auth;

	public long getId() {
		return id;
	}

	public String getApiUrl() {
		return apiUrl;
	}

	public void setApiUrl(String apiUrl) {
		this.apiUrl = apiUrl;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getAuth() {
		return auth;
	}

	public void setAuth(String auth) {
		this.auth = auth;
	}

	/**
	 * Encrypted attribute converter class for Sonatype clients.
	 */
	static class SonatypeClientConverter extends StringEncryptedAttributeConverter {

		SonatypeClientConverter(@Lazy DataEncryptionService dataEncryptionService) {
			super(dataEncryptionService);
		}
	}
}
