package com.tracelink.prodsec.synapse.auth.controller;

import com.tracelink.prodsec.synapse.auth.model.UserModel;
import com.tracelink.prodsec.synapse.auth.service.AuthService;
import com.tracelink.prodsec.synapse.mvc.SynapseModelAndView;
import java.security.Principal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for the profile editor. Allows changing passwords
 *
 * @author csmith
 */
@Controller
@PreAuthorize("isAuthenticated()")
public class ProfileController {

	private final AuthService authService;
	private final PasswordEncoder passwordEncoder;

	public ProfileController(@Autowired AuthService authService,
			@Autowired PasswordEncoder passwordEncoder) {
		this.authService = authService;
		this.passwordEncoder = passwordEncoder;
	}

	@GetMapping("/profile")
	public SynapseModelAndView profile(Principal authenticatedUser) {
		SynapseModelAndView modelAndView = new SynapseModelAndView("auth/profile");
		UserModel user = authService.findByUsername(authenticatedUser.getName());
		modelAndView.addObject("user_name", user.getUsername());
		modelAndView.addObject("user_role", user.getRolesAsString());
		modelAndView.addObject("local_user", user.getSsoId() == null);
		return modelAndView;
	}

	@PostMapping("/profile")
	public String changePassword(RedirectAttributes redirectAttributes,
			@RequestParam String currentPassword, @RequestParam String newPassword,
			@RequestParam String confirmPassword, Principal authenticatedUser) {
		UserModel user = authService.findByUsername(authenticatedUser.getName());
		if (user.getSsoId() != null) {
			redirectAttributes.addFlashAttribute(SynapseModelAndView.FAILURE_FLASH,
					"You cannot update your password if you authenticate using SSO.");
		} else if (!newPassword.equals(confirmPassword)) {
			redirectAttributes.addFlashAttribute(SynapseModelAndView.FAILURE_FLASH,
					"Your provided passwords don't match");
		} else if (!authService.passwordMatches(user, currentPassword)) {
			redirectAttributes.addFlashAttribute(SynapseModelAndView.FAILURE_FLASH,
					"Your current password is invalid");
		} else if (authService.passwordMatches(user, newPassword)) {
			redirectAttributes.addFlashAttribute(SynapseModelAndView.FAILURE_FLASH,
					"You cannot update your password to your old password");
		} else {
			user.setPassword(passwordEncoder.encode(newPassword));
			authService.saveUser(user);
			redirectAttributes.addFlashAttribute(SynapseModelAndView.SUCCESS_FLASH,
					"Your password has been updated successfully.");
		}

		return "redirect:/profile";
	}
}
