package com.tracelink.prodsec.synapse.web;

import com.tracelink.prodsec.synapse.auth.model.UserModel;
import com.tracelink.prodsec.synapse.auth.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for public-facing Synapse pages such as login and register.
 *
 * @author csmith, mcool
 */
@Controller
public class PublicController {

	private final AuthService authService;

	private final ClientRegistrationRepository clientRegistrationRepository;

	public PublicController(@Autowired AuthService authService,
			@Autowired(required = false) ClientRegistrationRepository clientRegistrationRepository) {
		this.authService = authService;
		this.clientRegistrationRepository = clientRegistrationRepository;
	}

	@GetMapping("/login")
	public String login() {
		return clientRegistrationRepository == null
				|| clientRegistrationRepository.findByRegistrationId("oidc") == null ? "auth/login"
				: "auth/login-sso";
	}

	@GetMapping("/register")
	public String register() {
		return "auth/register";
	}

	@PostMapping("/register")
	public String registerUser(@RequestParam String username, @RequestParam String password,
			@RequestParam String confirmPassword, RedirectAttributes redirectAttributes) {
		UserModel user = authService.findByUsername(username);
		if (user != null) {
			redirectAttributes.addFlashAttribute("failure", "User already exists");
			return "redirect:/register";
		}
		if (!password.equals(confirmPassword)) {
			redirectAttributes.addFlashAttribute("failure", "Passwords don't match");
			return "redirect:/register";
		}

		user = authService.createNewUser(username, password);
		redirectAttributes.addFlashAttribute("success",
				"User " + user.getUsername() + " created Successfully");
		return "redirect:/login";
	}
}
