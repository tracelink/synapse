package com.tracelink.prodsec.synapse.auth.controller;

import com.tracelink.prodsec.synapse.auth.SynapseAdminAuthDictionary;
import com.tracelink.prodsec.synapse.auth.model.RoleModel;
import com.tracelink.prodsec.synapse.auth.model.UserModel;
import com.tracelink.prodsec.synapse.auth.service.AuthService;
import com.tracelink.prodsec.synapse.mvc.SynapseModelAndView;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for all User Management including showing current user
 * permissions, editing, and deleting users
 *
 * @author csmith
 */
@Controller
@PreAuthorize("hasAuthority('" + SynapseAdminAuthDictionary.ADMIN_PRIV + "')")
public class UserMgmtController {

	@Autowired
	private AuthService authService;

	@Autowired
	private ConversionService conversionService;

	@GetMapping("/usermgmt")
	public SynapseModelAndView usermgmt(Principal currentUser) {
		SynapseModelAndView mv = new SynapseModelAndView("auth/usermgmt");
		mv.setStatus(HttpStatus.OK);
		mv.addObject("users", authService.findAllUsers());
		return mv;
	}

	@GetMapping("/usermgmt/edit/{id}")
	public SynapseModelAndView editUser(@PathVariable long id,
			RedirectAttributes redirectAttributes, Principal authenticatedUser) {
		SynapseModelAndView modelAndView = new SynapseModelAndView("auth/useredit");
		UserModel user = authService.findById(id);

		if (user == null) {
			redirectAttributes
					.addFlashAttribute(SynapseModelAndView.FAILURE_FLASH, "Cannot find user.");
			modelAndView.setViewName("redirect:/usermgmt");
		} else if (authService.isCurrentUser(user, authenticatedUser)) {
			redirectAttributes.addFlashAttribute(SynapseModelAndView.FAILURE_FLASH,
					"You cannot edit your own information.");
			modelAndView.setViewName("redirect:/usermgmt");
		} else {
			modelAndView.addObject("user", user);
			modelAndView.addObject("roles", authService.findAllRoles());
		}
		return modelAndView;
	}

	@PostMapping("/usermgmt/edit")
	public String saveUser(@RequestParam int id,
			@RequestParam(required = false, defaultValue = "false") boolean enabled,
			@RequestParam Map<String, String> parameters, RedirectAttributes redirect,
			Principal authenticatedUser) {
		UserModel user = authService.findById(id);
		// Make sure user exists
		if (user == null) {
			redirect.addFlashAttribute(SynapseModelAndView.FAILURE_FLASH, "Cannot find user.");
		}
		// Make sure user is not trying to edit themselves
		else if (authService.isCurrentUser(user, authenticatedUser)) {
			redirect.addFlashAttribute(SynapseModelAndView.FAILURE_FLASH,
					"Cannot edit own information.");
		} else {
			List<RoleModel> roles = new ArrayList<>();
			for (String param : parameters.keySet()) {
				RoleModel desiredRole = authService.findRoleByName(param);
				if (desiredRole == null) {
					// this is fine
					continue;
				}
				Boolean desiredState = conversionService
						.convert(parameters.get(param), Boolean.class);

				// Update roles if the user should have the role
				if (!user.getRoles().contains(desiredRole) && desiredState != null
						&& desiredState) {
					roles.add(desiredRole);
				}
			}

			user.setRoles(roles);

			// Update enabled
			user.setEnabled(enabled);

			// Save user in database
			authService.saveUser(user);

			redirect.addFlashAttribute(SynapseModelAndView.SUCCESS_FLASH,
					"User information for " + user.getUsername() + " saved successfully.");
		}
		return "redirect:/usermgmt";
	}

	@PostMapping("/usermgmt/delete")
	public String deleteUser(@RequestParam int id, RedirectAttributes redirectAttributes,
			Principal authenticatedUser) {
		UserModel user = authService.findById(id);

		if (user == null) {
			redirectAttributes
					.addFlashAttribute(SynapseModelAndView.FAILURE_FLASH, "Cannot find user.");
		} else if (authService.isCurrentUser(user, authenticatedUser)) {
			redirectAttributes.addFlashAttribute(SynapseModelAndView.FAILURE_FLASH,
					"Cannot delete own account.");
		} else {
			user.getRoles().clear();
			authService.saveUser(user);
			authService.deleteUser(user);

			redirectAttributes.addFlashAttribute(SynapseModelAndView.SUCCESS_FLASH,
					"User " + user.getUsername() + " deleted successfully");
		}
		return "redirect:/usermgmt";
	}
}
