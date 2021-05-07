package com.tracelink.prodsec.synapse.mvc;

import com.tracelink.prodsec.synapse.sidebar.service.SidebarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration file to add the {@link SynapseMvcHandler} to the interceptors
 * list
 *
 * @author csmith
 */
@Configuration
public class SynapseWebMvcConfig implements WebMvcConfigurer {

	private final SidebarService sidebarService;

	public SynapseWebMvcConfig(@Autowired SidebarService sidebarService) {
		super();
		this.sidebarService = sidebarService;
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(new SynapseMvcHandler(sidebarService));
	}
}
