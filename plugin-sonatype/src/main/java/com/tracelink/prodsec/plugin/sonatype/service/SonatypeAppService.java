package com.tracelink.prodsec.plugin.sonatype.service;

import com.tracelink.prodsec.plugin.sonatype.model.SonatypeApp;
import com.tracelink.prodsec.plugin.sonatype.model.SonatypeMetrics;
import com.tracelink.prodsec.plugin.sonatype.repository.SonatypeAppRepository;
import com.tracelink.prodsec.synapse.products.model.ProductLineModel;
import com.tracelink.prodsec.synapse.products.model.ProjectModel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service to store and retrieve data about Sonatype apps from the {@link
 * SonatypeAppRepository}.
 */
@Service
public class SonatypeAppService {
    private final SonatypeAppRepository appRepository;

    public SonatypeAppService(@Autowired SonatypeAppRepository appRepository) {
        this.appRepository = appRepository;
    }

    /* * * * * * * * * * * *
     * Repository methods
     * * * * * * * * * * * */

    /**
     * Gets the {@link SonatypeApp} with the given id, or if it does not exist,
     * creates a {@link SonatypeApp} with the given id and name.
     *
     * @param id   the id to search by
     * @param name the name to assign to the new app, if no app exists with the
     *             given {@param id}
     * @return existing or new Sonatype app with the given id
     */
    SonatypeApp getAppForId(String id, String name) {
        Optional<SonatypeApp> optionalApp = appRepository.findById(id);
        SonatypeApp app;
        if (optionalApp.isPresent()) {
            app = optionalApp.get();
        } else {
            app = new SonatypeApp();
            app.setId(id);
            app.setName(name);
            appRepository.saveAndFlush(app);
        }
        return app;
    }

    /**
     * Gets all {@link SonatypeApp}s that are mapped to a Synapse {@link
     * ProjectModel}.
     *
     * @return list of mapped Sonatype apps
     */
    public List<SonatypeApp> getMappedApps() {
        return appRepository.findAllBySynapseProjectNotNull();
    }

    /**
     * Gets all {@link SonatypeApp}s that are not mapped to a Synapse {@link
     * ProjectModel}.
     *
     * @return list of unmapped Sonatype apps
     */
    public List<SonatypeApp> getUnmappedApps() {
        return appRepository.findAllBySynapseProjectIsNull();
    }

    /* * * * * * * * * * *
     * Scorecard methods
     * * * * * * * * * * */

    /**
     * Gets the most recent {@link SonatypeMetrics} for the {@link SonatypeApp}
     * mapped to each Synapse {@link ProjectModel} in the given Synapse {@link
     * ProductLineModel}. List will be null if none of the projects in the given
     * product line are mapped.
     *
     * @param synapseProductLine Synapse product line to gather metrics for
     * @return list of Sonatype metrics associated with the given product line
     */
    public List<SonatypeMetrics> getMostRecentMetricsForProductLine(ProductLineModel synapseProductLine) {
        List<List<SonatypeMetrics>> metricsLists = synapseProductLine.getProjects().stream()
                .map(this::getMostRecentMetricsForProject).filter(Objects::nonNull).collect(Collectors.toList());
        if (metricsLists.isEmpty()) {
            return null;
        }
        return metricsLists.stream().filter(l -> !l.isEmpty()).map(l -> l.get(0)).collect(Collectors.toList());
    }

    /**
     * Gets the most recent {@link SonatypeMetrics} for the {@link SonatypeApp}
     * mapped to the given Synapse {@link ProjectModel}. List will be either a
     * singleton list, an empty list (if there are no metrics) or null (if the
     * {@link ProjectModel} is not mapped.
     *
     * @param synapseProject Synapse project to gather metrics for
     * @return list of Sonatype metrics associated with the given project
     */
    public List<SonatypeMetrics> getMostRecentMetricsForProject(ProjectModel synapseProject) {
        SonatypeApp app = appRepository.findBySynapseProject(synapseProject);
        if (app == null) {
            return null;
        }
        List<SonatypeMetrics> metrics = app.getMostRecentMetrics();
        if (metrics.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.singletonList(metrics.get(0));
    }

    /* * * * * * * * * *
     * Mapping methods
     * * * * * * * * * */

    /**
     * Creates a mapping between the given {@link ProjectModel} and the {@link
     * SonatypeApp} associated with the given app name. If the app does not
     * exist, no mapping is created.
     *
     * @param project the Synapse project to be mapped
     * @param appName the name of the Sonatype app to be mapped
     */
    public void createMapping(ProjectModel project, String appName) {

        SonatypeApp app = appRepository.findByName(appName);
        if (project != null && app != null) {
            app.setSynapseProject(project);
            appRepository.saveAndFlush(app);
        }
    }

    /**
     * Deletes the mapping for the {@link SonatypeApp} associated with the
     * given app name. If the app does not exist, does nothing.
     *
     * @param appName the name of the Sonatype app to be unmapped
     */
    public void deleteMapping(String appName) {
        SonatypeApp app = appRepository.findByName(appName);
        if (app != null) {
            app.setSynapseProject(null);
            appRepository.saveAndFlush(app);
        }
    }
}
