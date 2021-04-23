package com.tracelink.prodsec.synapse.auth.service;

import com.tracelink.prodsec.synapse.auth.model.OidcUserDetails;
import com.tracelink.prodsec.synapse.auth.model.PrivilegeModel;
import com.tracelink.prodsec.synapse.auth.model.RoleModel;
import com.tracelink.prodsec.synapse.auth.model.UserModel;
import com.tracelink.prodsec.synapse.auth.repository.UserRepository;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken.TokenType;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class OidcAuthServiceTest {

	@MockBean
	private UserRepository userRepository;

	@Mock
	OidcUserRequest oidcUserRequest;

	private OidcAuthService oidcAuthService;
	private final String sub = UUID.randomUUID().toString();
	private final String email = "jdoe@example.com";
	private OidcIdToken idToken;
	private Map<String, Object> claims;

	@Before
	public void setup() {
		oidcAuthService = new OidcAuthService(userRepository);
		ClientRegistration clientRegistration = ClientRegistration.withRegistrationId("oidc")
				.clientId("ssoServer")
				.redirectUriTemplate("{baseUrl}/login/oauth2/code/{registrationId}")
				.authorizationUri("https://example.com/auth")
				.tokenUri("https://example.com/token")
				.userInfoUri("https://example.com/userinfo")
				.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE).build();

		OAuth2AccessToken accessToken = new OAuth2AccessToken(TokenType.BEARER, "1234567890ABCDEF",
				Instant.now().minusSeconds(10), Instant.now().plusSeconds(10));

		claims = new HashMap<>();
		claims.put("sub", sub);
		claims.put("email", email);
		idToken = new OidcIdToken("1234567890ABCDEF", Instant.now().minusSeconds(10),
				Instant.now().plusSeconds(10), claims);

		BDDMockito.when(oidcUserRequest.getClientRegistration()).thenReturn(clientRegistration);
		BDDMockito.when(oidcUserRequest.getAccessToken()).thenReturn(accessToken);
		BDDMockito.when(oidcUserRequest.getIdToken()).thenReturn(idToken);
	}

	@Test
	public void testLoadUser() {
		OidcUser oidcUser = oidcAuthService.loadUser(oidcUserRequest);

		ArgumentCaptor<UserModel> userCaptor = ArgumentCaptor.forClass(UserModel.class);
		BDDMockito.verify(userRepository, Mockito.times(1)).saveAndFlush(userCaptor.capture());

		Assert.assertEquals(sub, userCaptor.getValue().getSsoId());
		Assert.assertEquals(email, userCaptor.getValue().getUsername());
		Assert.assertTrue(userCaptor.getValue().isSsoUser());
		Assert.assertTrue(userCaptor.getValue().getRoles().isEmpty());
		Assert.assertTrue(userCaptor.getValue().isEnabled());
		Assert.assertNull(userCaptor.getValue().getPassword());

		Assert.assertTrue(oidcUser instanceof OidcUserDetails);
		Assert.assertEquals(email, oidcUser.getName());
		Assert.assertTrue(oidcUser.getAuthorities().isEmpty());
		Assert.assertEquals(claims, oidcUser.getClaims());
		Assert.assertEquals(idToken, oidcUser.getIdToken());
		Assert.assertNull(oidcUser.getUserInfo());
		Assert.assertEquals(claims, oidcUser.getAttributes());

	}

	@Test
	public void testLoadUserExistingSsoUser() {
		UserModel user = new UserModel();
		user.setUsername(email);
		user.setSsoId(sub);
		BDDMockito.when(userRepository.findByUsername(email)).thenReturn(user);
		OidcUser oidcUser = oidcAuthService.loadUser(oidcUserRequest);

		BDDMockito.verify(userRepository, Mockito.times(0)).saveAndFlush(user);

		Assert.assertTrue(oidcUser instanceof OidcUserDetails);
		Assert.assertEquals(email, oidcUser.getName());
		Assert.assertTrue(oidcUser.getAuthorities().isEmpty());
	}

	@Test
	public void testLoadUserMissingEmail() {
		claims.remove("email");
		claims.put("username", "jdoe");
		idToken = new OidcIdToken("1234567890ABCDEF", Instant.now().minusSeconds(10),
				Instant.now().plusSeconds(10), claims);
		BDDMockito.when(oidcUserRequest.getIdToken()).thenReturn(idToken);

		try {
			oidcAuthService.loadUser(oidcUserRequest);
			Assert.fail("Exception should have been thrown");
		} catch (OAuth2AuthenticationException e) {
			Assert.assertTrue(
					e.getMessage().contains("User info must contain an email attribute to login."));
		}
	}

	@Test
	public void testLoadUserExistingLocalUserCollision() {
		UserModel user = new UserModel();
		user.setUsername(email);
		BDDMockito.when(userRepository.findByUsername(email)).thenReturn(user);
		try {
			oidcAuthService.loadUser(oidcUserRequest);
			Assert.fail("Exception should have been thrown");
		} catch (OAuth2AuthenticationException e) {
			Assert.assertTrue(e.getMessage()
					.contains("A local user with the username \"" + email + "\" already exists."));
		}

		BDDMockito.verify(userRepository, Mockito.times(0)).saveAndFlush(user);
	}

	@Test
	public void testLoadUserExistingSsoUserUpdateEmail() {
		UserModel user = new UserModel();
		user.setUsername("oldemail@example.com");
		user.setSsoId(sub);
		BDDMockito.when(userRepository.findByUsername(email)).thenReturn(null);
		BDDMockito.when(userRepository.findBySsoId(sub)).thenReturn(user);
		OidcUser oidcUser = oidcAuthService.loadUser(oidcUserRequest);

		BDDMockito.verify(userRepository, Mockito.times(1)).saveAndFlush(user);
		Assert.assertEquals(email, user.getUsername());
	}

	@Test
	public void testLoadUserExistingSsoUserWithRoles() {
		UserModel user = new UserModel();
		user.setUsername("oldemail@example.com");
		user.setSsoId(sub);

		PrivilegeModel privilege = new PrivilegeModel();
		privilege.setName("SpecialPrivilege");

		RoleModel role = new RoleModel();
		role.setRoleName("SpecialRole");
		role.setPrivileges(Collections.singleton(privilege));
		user.setRoles(Collections.singleton(role));

		BDDMockito.when(userRepository.findByUsername(email)).thenReturn(null);
		BDDMockito.when(userRepository.findBySsoId(sub)).thenReturn(user);
		OidcUser oidcUser = oidcAuthService.loadUser(oidcUserRequest);

		BDDMockito.verify(userRepository, Mockito.times(1)).saveAndFlush(user);
		Assert.assertEquals(email, user.getUsername());

		Assert.assertEquals(1, oidcUser.getAuthorities().size());
		Assert.assertEquals(privilege.getName(),
				oidcUser.getAuthorities().iterator().next().getAuthority());
	}
}
