package com.tracelink.prodsec.plugin.sonatype.model;

import com.tracelink.prodsec.plugin.sonatype.SonatypePlugin;
import com.tracelink.prodsec.synapse.products.model.ProjectModel;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.Table;

/**
 * The Database entity for the Sonatype application.
 *
 * @author mcool
 */
@Entity
@Table(schema = SonatypePlugin.SCHEMA, name = "sonatype_apps")
public class SonatypeApp {
    @Id
    @Column(name = "app_id")
    private String id;

    @Column(name = "name")
    private String name;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "synapse_project")
    private ProjectModel synapseProject;

    @OneToMany(mappedBy = "app", fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SUBSELECT)
    @OrderBy("recorded_date desc")
    private List<SonatypeMetrics> metrics;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ProjectModel getSynapseProject() {
        return synapseProject;
    }

    public void setSynapseProject(ProjectModel synapseProject) {
        this.synapseProject = synapseProject;
    }

    public List<SonatypeMetrics> getMetrics() {
        return metrics;
    }

    public void setMetrics(List<SonatypeMetrics> metrics) {
        this.metrics = metrics;
    }

    public List<SonatypeMetrics> getMostRecentMetrics() {
        return metrics.stream().filter(m -> m.getRecordedDate().compareTo(LocalDate.now()) == 0)
                .collect(Collectors.toList());
    }

    public boolean isVulnerable() {
        return !getMostRecentMetrics().isEmpty();
    }
}
