package com.tracelink.prodsec.plugin.sonatype.repository;

import com.tracelink.prodsec.plugin.sonatype.model.SonatypeApp;
import com.tracelink.prodsec.synapse.products.model.ProjectModel;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for DB integration with the {@link SonatypeApp}.
 *
 * @author mcool
 */
@Repository
public interface SonatypeAppRepository extends JpaRepository<SonatypeApp, String> {
    /**
     * Gets all {@link SonatypeApp} entities where the Synapse {@link
     * ProjectModel} is not null
     *
     * @return list of mapped Sonatype apps
     */
    List<SonatypeApp> findAllBySynapseProjectNotNull();

    /**
     * Gets all {@link SonatypeApp} entities where the Synapse {@link
     * ProjectModel} is null
     *
     * @return list of unmapped Sonatype apps
     */
    List<SonatypeApp> findAllBySynapseProjectIsNull();

    /**
     * Gets the {@link SonatypeApp} for the given Synapse {@link ProjectModel}.
     * This does a search by the join column automatically.
     *
     * @param synapseProject the Synapse Project to search by
     * @return the Sonatype app mapped to the Synapse Project, or null
     */
    SonatypeApp findBySynapseProject(ProjectModel synapseProject);

    /**
     * Gets the {@link SonatypeApp} with the given name.
     *
     * @param name the name to search by
     * @return the Sonatype app with the given name, or null
     */
    SonatypeApp findByName(String name);
}
