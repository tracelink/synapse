package com.tracelink.prodsec.synapse.mvc;

import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Service;

/**
 * A service used to allow plugins and other controllers to self-register public
 * resources. By default, all requests require authentication, however, there
 * are cases where a plugin or otherwise might need to expose something
 * publicly. Registering a RequestMatcher here allows that resource to get
 * exposed.
 * <p>
 * Note that even in a controller registers a route here, the controller may
 * still use Pre and Post Authorization to require authentication or
 * authorization checks. These checks remain valid and used.
 *
 * @author csmith
 */
@Service
public class SynapsePublicRequestMatcherService implements RequestMatcher {

	private final List<RequestMatcher> matchers = new ArrayList<>();

	@Override
	public boolean matches(HttpServletRequest request) {
		return matchers.stream().anyMatch(m -> m.matches(request));
	}

	/**
	 * Add a matcher to this service to allow whatever it matches to be made public.
	 *
	 * @param matcher the matcher to be made public
	 */
	public void registerMatcher(RequestMatcher matcher) {
		this.matchers.add(matcher);
	}

}
