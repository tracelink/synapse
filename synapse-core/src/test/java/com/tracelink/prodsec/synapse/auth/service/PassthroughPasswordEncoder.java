package com.tracelink.prodsec.synapse.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;

public class PassthroughPasswordEncoder implements PasswordEncoder {
	@Override
	public String encode(CharSequence rawPassword) {
		return rawPassword.toString();
	}

	@Override
	public boolean matches(CharSequence rawPassword, String encodedPassword) {
		return rawPassword.toString().equals(encodedPassword);
	}
}
