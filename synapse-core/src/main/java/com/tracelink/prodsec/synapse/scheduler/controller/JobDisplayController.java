package com.tracelink.prodsec.synapse.scheduler.controller;

import com.tracelink.prodsec.synapse.auth.SynapseAdminAuthDictionary;
import com.tracelink.prodsec.synapse.mvc.SynapseModelAndView;
import com.tracelink.prodsec.synapse.scheduler.service.JobsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller for displaying plugin job information, including last run
 * and current activity status
 *
 * @author bhoran
 */
@Controller
@PreAuthorize("hasAuthority('" + SynapseAdminAuthDictionary.ADMIN_PRIV + "')")
public class JobDisplayController {

	@Autowired
	private JobsService jobsService;

	@GetMapping("/jobs")
	public SynapseModelAndView jobs() {
		SynapseModelAndView mv = new SynapseModelAndView("jobs");
		mv.addObject("jobs", jobsService.getAllJobs());
		return mv;
	}
}
