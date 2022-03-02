package com.tracelink.prodsec.synapse.web.configuration;

import com.tracelink.prodsec.synapse.auth.SynapseAdminAuthDictionary;
import com.tracelink.prodsec.synapse.auth.service.OidcAuthService;
import com.tracelink.prodsec.synapse.mvc.SynapsePublicRequestMatcherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * Spring configuration class to handle security settings for authorization and
 * authentication.
 *
 * @author csmith, mcool
 */
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Configuration
@Order
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	private final SynapsePublicRequestMatcherService matcherService;
	private final OidcAuthService oidcAuthService;
	private final ClientRegistrationRepository clientRegistrationRepository;

	public SecurityConfig(@Autowired SynapsePublicRequestMatcherService matcherService,
			@Autowired OidcAuthService oidcAuthService,
			@Autowired(required = false) ClientRegistrationRepository clientRegistrationRepository) {
		super();
		this.matcherService = matcherService;
		this.oidcAuthService = oidcAuthService;
		this.clientRegistrationRepository = clientRegistrationRepository;
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.csrf().ignoringAntMatchers("/console/**");
		http.headers().httpStrictTransportSecurity().disable();
		http.headers().frameOptions().disable();
		http.authorizeRequests()
				.antMatchers("/login", "/register").permitAll()
				.requestMatchers(matcherService).permitAll()
				.antMatchers("/console/**").hasAuthority(SynapseAdminAuthDictionary.ADMIN_PRIV)
				.anyRequest().authenticated()
			.and()
				.formLogin()
					.loginPage("/login")
					.failureHandler(synapseAuthFailureHandler())
					.usernameParameter("username")
					.passwordParameter("password")
					.defaultSuccessUrl("/", true)
			.and()
				.logout()
					.logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
					.logoutSuccessUrl("/login?logout=true")
				.deleteCookies("JSESSIONID")
					.invalidateHttpSession(true);

		if (clientRegistrationRepository != null && clientRegistrationRepository.findByRegistrationId("oidc") != null) {
			http.oauth2Login().loginPage("/login").failureHandler(synapseAuthFailureHandler())
					.defaultSuccessUrl("/", true).userInfoEndpoint().oidcUserService(oidcAuthService);
		}

	}

	@Override
	public void configure(WebSecurity web) throws Exception {
		web.ignoring().antMatchers("/styles/**", "/icons/**", "/images/**", "/scripts/**", "/webjars/**");
	}

	@Bean
	public AuthenticationFailureHandler synapseAuthFailureHandler() {
		return new SynapseAuthenticationFailureHandler();
	}
}
