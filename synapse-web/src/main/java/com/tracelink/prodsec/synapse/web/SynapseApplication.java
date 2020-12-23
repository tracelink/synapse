package com.tracelink.prodsec.synapse.web;

import org.springframework.boot.Banner.Mode;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.tracelink.prodsec.synapse")
@EntityScan(basePackages = "com.tracelink.prodsec.synapse")
@EnableJpaRepositories(basePackages = "com.tracelink.prodsec.synapse")
public class SynapseApplication {

	public static void main(String... args) {
		SpringApplication app = new SpringApplication(SynapseApplication.class);
		app.setBannerMode(Mode.OFF);
		app.run(args);
	}
}
