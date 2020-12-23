package com.tracelink.prodsec.plugin.sonatype.model;

import com.tracelink.prodsec.plugin.sonatype.SonatypePlugin;
import com.tracelink.prodsec.plugin.sonatype.util.ThreatLevel;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * The Database entity for the Sonatype violation metrics.
 *
 * @author mcool
 */
@Entity
@Table(schema = SonatypePlugin.SCHEMA, name = "sonatype_metrics")
public class SonatypeMetrics {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "metrics_id")
    private long id;

    @Column(name = "recorded_date")
    private LocalDate recordedDate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "app", nullable = false)
    private SonatypeApp app;

    @Column(name = "high_vios")
    private long highVios;

    @Column(name = "med_vios")
    private long medVios;

    @Column(name = "low_vios")
    private long lowVios;

    @Column(name = "info_vios")
    private long infoVios;

    public long getId() {
        return id;
    }

    public LocalDate getRecordedDate() {
        return recordedDate;
    }

    public void setRecordedDate(LocalDate recordedDate) {
        this.recordedDate = recordedDate;
    }

    public SonatypeApp getApp() {
        return app;
    }

    public void setApp(SonatypeApp app) {
        this.app = app;
    }

    public long getHighVios() {
        return highVios;
    }

    public void setHighVios(long highVios) {
        this.highVios = highVios;
    }

    public long getMedVios() {
        return medVios;
    }

    public void setMedVios(long medVios) {
        this.medVios = medVios;
    }

    public long getLowVios() {
        return lowVios;
    }

    public void setLowVios(long lowVios) {
        this.lowVios = lowVios;
    }

    public long getInfoVios() {
        return infoVios;
    }

    public void setInfoVios(long infoVios) {
        this.infoVios = infoVios;
    }

    public long getTotalVios() {
        return highVios + medVios + lowVios + infoVios;
    }

    /**
     * Calculates the risk score for this metrics entity using the threat
     * level weights for each violation severity.
     *
     * @return risk score for this metrics entity
     */
    public double getRiskScore() {
        return getHighVios() * ThreatLevel.HIGH.getWeight() + getMedVios() * ThreatLevel.MEDIUM.getWeight()
                + getLowVios() * ThreatLevel.LOW.getWeight();
    }
}
