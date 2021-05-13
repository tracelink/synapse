package com.tracelink.prodsec.synapse.web.dev;

import com.tracelink.prodsec.synapse.products.service.ProductsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;

/**
 * Sets up product lines and projects to populate data for testing during development.
 */
@Component
@Profile("dev")
public class DevelopmentSetup {

	private static final Logger LOGGER = LoggerFactory.getLogger(DevelopmentSetup.class);

	private final Environment environment;
	private final ProductsService productsService;

	public DevelopmentSetup(@Autowired Environment environment, @Autowired ProductsService productsService) {
		this.environment = environment;
		this.productsService = productsService;
	}

	private static final String PRIMARY_PRODUCT = "Primary Product";
	private static final String SECONDARY_PRODUCT = "Secondary Product";
	private static final String CORPORATE_PRODUCT = "Corporate Product";
	private static final String MOBILE_PRODUCT = "Mobile Product";

	/**
	 * Set up product lines and projects for development testing after Synapse has started up.
	 *
	 * @param event the refresh event signaling that Synapse is up
	 */
	@EventListener
	public void onApplicationEvent(ContextRefreshedEvent event) {
		// being extra-sure that this is called correctly
		if (!environment.acceptsProfiles(Profiles.of("dev"))) {
			return;
		}
		new Thread(() -> {
			LOGGER.info("Beginning Dev Setup");
			try {
				addProductLines();
				addProjects();
			} catch (Exception e) {
				LOGGER.info("Dev Setup Failed", e);
			}
			LOGGER.info("Dev Setup Complete");
		}).start();
	}

	private void addProductLines() throws Exception {
		productsService.createProductLine(PRIMARY_PRODUCT);
		productsService.createProductLine(SECONDARY_PRODUCT);
		productsService.createProductLine(CORPORATE_PRODUCT);
		productsService.createProductLine(MOBILE_PRODUCT);
	}

	private void addProjects() throws Exception {
		productsService.createProject("Primary UI", PRIMARY_PRODUCT);
		productsService.createProject("Business Logic", PRIMARY_PRODUCT);
		productsService.createProject("Database", PRIMARY_PRODUCT);
		productsService.createProject("Gateway", PRIMARY_PRODUCT);
		productsService.createProject("Notifications", PRIMARY_PRODUCT);
		productsService.createProject("API", PRIMARY_PRODUCT);
		productsService.createProject("Message Router", PRIMARY_PRODUCT);

		productsService.createProject("Secondary UI", SECONDARY_PRODUCT);
		productsService.createProject("Logging", SECONDARY_PRODUCT);
		productsService.createProject("Authentication Service", SECONDARY_PRODUCT);
		productsService.createProject("Authorization Service", SECONDARY_PRODUCT);
		productsService.createProject("Data Backup Service", SECONDARY_PRODUCT);
		productsService.createProject("Shared Utils", SECONDARY_PRODUCT);
		productsService.createProject("Privacy Settings", SECONDARY_PRODUCT);
		productsService.createProject("Performance", SECONDARY_PRODUCT);

		productsService.createProject("Corporate UI", CORPORATE_PRODUCT);
		productsService.createProject("User Management", CORPORATE_PRODUCT);
		productsService.createProject("Sales Service", CORPORATE_PRODUCT);
		productsService.createProject("Product Demos", CORPORATE_PRODUCT);
		productsService.createProject("Customer Portal", CORPORATE_PRODUCT);
		productsService.createProject("Help Center", CORPORATE_PRODUCT);
		productsService.createProject("Billing", CORPORATE_PRODUCT);
		productsService.createProject("Terms of Service", CORPORATE_PRODUCT);

		productsService.createProject("Primary Mobile App", MOBILE_PRODUCT);
	}
}
