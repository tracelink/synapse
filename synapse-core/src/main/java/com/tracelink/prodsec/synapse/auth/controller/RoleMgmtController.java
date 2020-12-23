package com.tracelink.prodsec.synapse.auth.controller;

import com.tracelink.prodsec.synapse.auth.SynapseAdminAuthDictionary;
import com.tracelink.prodsec.synapse.auth.model.PrivilegeModel;
import com.tracelink.prodsec.synapse.auth.model.RoleModel;
import com.tracelink.prodsec.synapse.auth.service.AuthService;
import com.tracelink.prodsec.synapse.mvc.SynapseModelAndView;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
 * Controller for all Role Management including showing current role privileges,
 * editing and deleting roles, and creating new roles
 *
 * @author csmith
 */
@Controller
@PreAuthorize("hasAuthority('" + SynapseAdminAuthDictionary.ADMIN_PRIV + "')")
public class RoleMgmtController {

	@Autowired
	private AuthService authService;

	@Autowired
	private ConversionService conversionService;

	@GetMapping("/rolemgmt")
	public SynapseModelAndView rolemgmt() {
		SynapseModelAndView mv = new SynapseModelAndView("auth/rolemgmt");
		mv.setStatus(HttpStatus.OK);
		mv.addObject("roles", authService.findAllRoles());
		return mv;
	}

	@GetMapping("/rolemgmt/edit/{id}")
	public SynapseModelAndView editRole(@PathVariable long id,
			RedirectAttributes redirectAttributes) {
		SynapseModelAndView modelAndView = new SynapseModelAndView("auth/roleedit");
		RoleModel role = authService.findRoleById(id);

		if (role == null) {
			redirectAttributes
					.addFlashAttribute(SynapseModelAndView.FAILURE_FLASH, "Cannot find role.");
			modelAndView.setViewName("redirect:/rolemgmt");
		} else if (authService.isBuiltInRole(role)) {
			redirectAttributes.addFlashAttribute(SynapseModelAndView.FAILURE_FLASH,
					"Cannot edit a built in role");
			modelAndView.setViewName("redirect:/rolemgmt");
		} else {
			List<PrivilegeModel> privs = authService.findAllPrivileges().stream()
					.filter(p -> !p.getName().equals(SynapseAdminAuthDictionary.ADMIN_PRIV))
					.collect(Collectors.toList());

			modelAndView.addObject("role", role);
			modelAndView.addObject("privileges", privs);
		}
		return modelAndView;
	}

	@PostMapping("/rolemgmt/edit")
	public String saveRole(@RequestParam int id, @RequestParam Map<String, String> parameters,
			RedirectAttributes redirectAttributes) {
		RoleModel role = authService.findRoleById(id);
		// Make sure role exists
		if (role == null) {
			redirectAttributes
					.addFlashAttribute(SynapseModelAndView.FAILURE_FLASH, "Cannot find role.");
		} else if (authService.isBuiltInRole(role)) {
			redirectAttributes.addFlashAttribute(SynapseModelAndView.FAILURE_FLASH,
					"Cannot edit a built in role");
		} else {
			List<PrivilegeModel> privs = new ArrayList<>();
			for (String param : parameters.keySet()) {
				PrivilegeModel desiredPriv = authService.findPrivilegeByName(param);
				if (desiredPriv == null) {
					// this is fine
					continue;
				}
				Boolean desiredState = conversionService
						.convert(parameters.get(param), Boolean.class);
				// Update roles if the user doesn't have the role but should
				if (desiredState != null && desiredState) {
					privs.add(desiredPriv);
				}
			}

			role.setPrivileges(privs);

			authService.updateRole(role);

			redirectAttributes.addFlashAttribute(SynapseModelAndView.SUCCESS_FLASH,
					"Role information for " + role.getRoleName() + " saved successfully.");
		}
		return "redirect:/rolemgmt";
	}

	@PostMapping("/rolemgmt/delete")
	public String deleteRole(@RequestParam int id, RedirectAttributes redirectAttributes) {
		RoleModel role = authService.findRoleById(id);

		if (role == null) {
			redirectAttributes
					.addFlashAttribute(SynapseModelAndView.FAILURE_FLASH, "Cannot delete role.");
		} else if (authService.isBuiltInRole(role)) {
			redirectAttributes.addFlashAttribute(SynapseModelAndView.FAILURE_FLASH,
					"Cannot delete a built in role");
		} else {
			authService.deleteRole(role);
			redirectAttributes.addFlashAttribute(SynapseModelAndView.SUCCESS_FLASH,
					"Role " + role.getRoleName() + " deleted successfully");
		}
		return "redirect:/rolemgmt";
	}

	@PostMapping("/rolemgmt/create")
	public String createRole(@RequestParam String roleName, RedirectAttributes redirectAttributes) {
		String redirect = "redirect:/rolemgmt";
		if (roleName == null || roleName.length() == 0) {
			redirectAttributes
					.addFlashAttribute(SynapseModelAndView.FAILURE_FLASH, "Role must have a name.");
		} else if (authService.findRoleByName(roleName) != null) {
			redirectAttributes
					.addFlashAttribute(SynapseModelAndView.FAILURE_FLASH, "Role already exists.");
		} else {
			RoleModel newRole = new RoleModel();
			newRole.setRoleName(roleName);
			newRole = authService.updateRole(newRole);

			redirect = "redirect:/rolemgmt/edit/" + newRole.getId();
		}
		return redirect;
	}
}
