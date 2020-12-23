package com.tracelink.prodsec.synapse.mvc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.tracelink.prodsec.synapse.sidebar.service.SidebarService;

/**
 * Configuration file to add the {@link SynapseMvcHandler} to the interceptors
 * list
 * 
 * @author csmith
 *
 */
@Configuration
public class SynapseWebMvcConfig implements WebMvcConfigurer {

	@Autowired
	private SidebarService sidebarService;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(new SynapseMvcHandler(sidebarService));
	}
}
