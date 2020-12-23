package com.tracelink.prodsec.synapse.web.configuration;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

public class SynapseAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception) throws IOException, ServletException {
		String error = "Invalid Login";
		if (exception instanceof DisabledException) {
			error = "Account Disabled";
		} else if (exception instanceof OAuth2AuthenticationException) {
			error = exception.getMessage();
		}
		getRedirectStrategy().sendRedirect(request, response, "/login?error=" + error);
	}

}
