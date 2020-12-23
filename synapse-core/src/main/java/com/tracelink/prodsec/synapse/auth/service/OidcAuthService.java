package com.tracelink.prodsec.synapse.auth.service;

import com.tracelink.prodsec.synapse.auth.model.OidcUserDetails;
import com.tracelink.prodsec.synapse.auth.model.PrivilegeModel;
import com.tracelink.prodsec.synapse.auth.model.RoleModel;
import com.tracelink.prodsec.synapse.auth.model.UserModel;
import com.tracelink.prodsec.synapse.auth.repository.UserRepository;
import java.util.Collection;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

/**
 * Service to store and retrieve users in the database, if they login via OpenID Connect. When
 * users login for the first time, this service will create a new {@link UserModel} in the
 * database to keep track of user roles and privileges.
 * <p>
 * Note that this service does not attempt to link an SSO user to a local user if they have the
 * same username. If there is a username collision, the SSO user cannot login.
 *
 * @author mcool
 */
@Service
public class OidcAuthService extends OidcUserService {

	private static final Logger LOGGER = LoggerFactory.getLogger(OidcAuthService.class);

	private final UserRepository userRepository;

	/**
	 * Constructor for this service that configures a {@link UserRepository} for interaction with
	 * the user database.
	 *
	 * @param userRepository to store and retrieve {@link UserModel}s.
	 */
	public OidcAuthService(@Autowired UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public OidcUser loadUser(OidcUserRequest userRequest)
			throws OAuth2AuthenticationException {
		OidcUser oidcUser = super.loadUser(userRequest);

		// Check that the user info contains an email
		String username = oidcUser.getEmail();
		if (username == null) {
			OAuth2Error oAuth2Error = new OAuth2Error("missing_user_email_attribute");
			throw new OAuth2AuthenticationException(oAuth2Error,
					"User info must contain an email attribute to login.");
		}

		UserModel user;
		// Check if there is a user in the DB with the same username
		UserModel localUser = userRepository.findByUsername(username);
		if (localUser != null) {
			// Check if the local user has the same SSO id
			if (localUser.getSsoId() != null && localUser.getSsoId()
					.equals(oidcUser.getSubject())) {
				user = localUser;
			} else {
				// There is a username collision, prevent login
				LOGGER.warn("User with username \"" + username
						+ "\" attempted to login via SSO, but already has a local user account.");
				OAuth2Error oauth2Error = new OAuth2Error("username_collision");
				throw new OAuth2AuthenticationException(oauth2Error,
						"A local user with the username \"" + username
								+ "\" already exists. Please login with the provided form instead of SSO.");
			}
		} else {
			user = userRepository.findBySsoId(oidcUser.getSubject());
		}

		// This is the first time this user is logging in via SSO
		if (user == null) {
			user = new UserModel();
			user.setSsoId(oidcUser.getSubject());
			user.setUsername(username);
			user.setEnabled(true);
			userRepository.saveAndFlush(user);
		}

		// Update local username if the user's email has been updated in the SSO
		if (!user.getUsername().equals(username)) {
			user.setUsername(username);
			userRepository.saveAndFlush(user);
		}

		// Get database roles and privileges
		Collection<GrantedAuthority> authorities = user.getRoles().stream()
				.map(RoleModel::getPrivileges).flatMap(Collection::stream)
				.map(PrivilegeModel::getName).map(SimpleGrantedAuthority::new)
				.collect(Collectors.toSet());

		return new OidcUserDetails(oidcUser, authorities);
	}
}
