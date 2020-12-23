package com.tracelink.prodsec.plugin.sonatype.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tracelink.prodsec.plugin.sonatype.exception.SonatypeClientException;
import com.tracelink.prodsec.plugin.sonatype.model.SonatypeApp;
import com.tracelink.prodsec.plugin.sonatype.model.SonatypeClient;
import com.tracelink.prodsec.plugin.sonatype.repository.SonatypeClientRepository;
import com.tracelink.prodsec.plugin.sonatype.util.ThreatLevel;
import com.tracelink.prodsec.plugin.sonatype.util.client.ApplicationViolation;
import com.tracelink.prodsec.plugin.sonatype.util.client.ApplicationViolations;
import com.tracelink.prodsec.plugin.sonatype.util.client.Applications;
import com.tracelink.prodsec.plugin.sonatype.util.client.Policies;
import com.tracelink.prodsec.plugin.sonatype.util.client.Policy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import kong.unirest.Unirest;

/**
 * Service to store and retrieve data about the Sonatype API client from the
 * {@link SonatypeClientRepository}. Also handles logic to fetch data from the
 * Sonatype Nexus IQ server.
 */
@Service
public class SonatypeClientService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SonatypeClientService.class);
    private final SonatypeAppService appService;
    private final SonatypeMetricsService metricsService;
    private final SonatypeClientRepository clientRepository;

    public SonatypeClientService(@Autowired SonatypeAppService appService,
                                 @Autowired SonatypeMetricsService metricsService,
                                 @Autowired SonatypeClientRepository clientRepository) {
        this.appService = appService;
        this.metricsService = metricsService;
        this.clientRepository = clientRepository;
    }

    /**
     * Determines whether a connection can be established with the Sonatype
     * Nexus IQ server, using the configured API client settings.
     *
     * @return true if data can be fetched, false if no API client is configured
     * or if API client URL, user or auth are incorrect
     */
    public boolean testConnection() {
        SonatypeClient client;
        try {
            client = getClient();
            getAllApplications(client);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * Fetches current violations data from the Sonatype Nexus IQ server. Stores
     * Sonatype apps and metrics in the database.
     */
    public void fetchData() {
        SonatypeClient client;
        try {
            client = getClient();
        } catch (SonatypeClientException e) {
            LOGGER.error(e.getMessage());
            return;
        }

        Applications applications;
        Policies policies;
        List<String> policyIds;
        List<ApplicationViolation> applicationViolations;
        try {
            applications = getAllApplications(client);
            policies = getPolicies(client);
            policyIds = policies.getPolicies().stream().map(Policy::getId).collect(Collectors.toList());
            applicationViolations = getViolations(client, policyIds).getApplicationViolations();
        } catch (Exception e) {
            LOGGER.error("Cannot fetch data from Sonatype server. Error is: " + e.getMessage());
            return;
        }

        // Get SonatypeApps from the database, or create them if they do not exist
        Map<String, SonatypeApp> appMap = new HashMap<>();
        applications.getApplications()
                .forEach(a -> appMap.put(a.getId(), appService.getAppForId(a.getId(), a.getName())));

        // Filter policy violations to only include those with the stage id "build"
        for (ApplicationViolation applicationViolation : applicationViolations) {
            applicationViolation.setPolicyViolations(applicationViolation.getPolicyViolations().stream()
                    .filter(v -> v.getStageId().equals("build")).collect(Collectors.toList()));
        }

        // Store metrics for each application
        for (ApplicationViolation applicationViolation : applicationViolations) {
            Map<ThreatLevel, Integer> threatLevels = new HashMap<>();
            applicationViolation.getPolicyViolations().forEach(v -> {
                ThreatLevel level = ThreatLevel.forLevel(v.getThreatLevel());
                threatLevels.put(level, threatLevels.getOrDefault(level, 0) + 1);
            });
            metricsService.storeMetrics(appMap.get(applicationViolation.getApplication().getId()), threatLevels);
        }
    }

    /**
     * Gets the configured Sonatype API client from the database.
     *
     * @return the configured API client
     * @throws SonatypeClientException if no API client is configured
     */
    public SonatypeClient getClient() throws SonatypeClientException {
        List<SonatypeClient> clients = clientRepository.findAll();
        if (clients.isEmpty()) {
            throw new SonatypeClientException("No Sonatype client configured.");
        }
        return clients.get(0);
    }

    /**
     * Sets the values of the Sonatype API client in the database. If no API
     * client is currently configured, it will create a new entity. Otherwise,
     * it will update the existing entity. Only sets the values if the given URL
     * is properly formed.
     *
     * @param apiUrl URL for the Nexus IQ server API
     * @param user   username for the Nexus IQ server
     * @param auth   authentication for the Nexus IQ server
     * @return true if the API client is set, false otherwise
     */
    public boolean setClient(String apiUrl, String user, String auth) {
        try {
            URL url = new URL(apiUrl);
            apiUrl = url.getProtocol() + "://" + url.getAuthority();
        } catch (MalformedURLException e) {
            return false;
        }

        List<SonatypeClient> clients = clientRepository.findAll();
        SonatypeClient client;
        if (clients.isEmpty()) {
            client = new SonatypeClient();
        } else {
            client = clients.get(0);
        }
        client.setApiUrl(apiUrl);
        client.setUser(user);
        client.setAuth(auth);
        clientRepository.saveAndFlush(client);
        return true;
    }

    private Applications getAllApplications(SonatypeClient client) throws Exception {
        String url = buildRequestUrl(client.getApiUrl(), "api", "v2", "applications");

        AtomicReference<Applications> applications = new AtomicReference<>();
        AtomicReference<Exception> exception = new AtomicReference<>();
        Unirest.get(url).basicAuth(client.getUser(), client.getAuth()).thenConsume(r -> {
            try {
                ObjectMapper om = new ObjectMapper();
                applications.set(om.readValue(r.getContent(), Applications.class));
            } catch (Exception e) {
                exception.set(e);
            }
        });

        Exception e;
        if ((e = exception.get()) != null) {
            throw e;
        }
        return applications.get();
    }

    private Policies getPolicies(SonatypeClient client) throws Exception {
        String url = buildRequestUrl(client.getApiUrl(), "api", "v2", "policies");

        AtomicReference<Policies> policies = new AtomicReference<>();
        AtomicReference<Exception> exception = new AtomicReference<>();
        Unirest.get(url).basicAuth(client.getUser(), client.getAuth()).thenConsume(r -> {
            try {
                ObjectMapper om = new ObjectMapper();
                policies.set(om.readValue(r.getContent(), Policies.class));
            } catch (Exception e) {
                exception.set(e);
            }
        });

        Exception e;
        if ((e = exception.get()) != null) {
            throw e;
        }
        return policies.get();
    }

    private ApplicationViolations getViolations(SonatypeClient client, Collection<String> policyIds) throws Exception {
        String url = buildRequestUrl(client.getApiUrl(), "api", "v2", "policyViolations");

        AtomicReference<ApplicationViolations> appVios = new AtomicReference<>();
        AtomicReference<Exception> exception = new AtomicReference<>();
        Unirest.get(url).queryString("p", policyIds).basicAuth(client.getUser(), client.getAuth()).thenConsume(r -> {
            try {
                ObjectMapper om = new ObjectMapper();
                appVios.set(om.readValue(r.getContent(), ApplicationViolations.class));
            } catch (Exception e) {
                exception.set(e);
            }
        });

        Exception e;
        if ((e = exception.get()) != null) {
            throw e;
        }
        return appVios.get();
    }

    private String buildRequestUrl(String baseUrl, String... pathElements) {
        StringBuilder sb = new StringBuilder(baseUrl);
        for (String elem : pathElements) {
            sb.append("/").append(elem);
        }
        return sb.toString();
    }
}
