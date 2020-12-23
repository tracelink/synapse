package com.tracelink.prodsec.synapse.scheduler.model;

import com.tracelink.prodsec.synapse.auth.SynapseAdminAuthDictionary;
import java.time.Duration;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * Jobs Model represents how the last run jobs are stored
 *
 * @author bhoran
 */
@Entity
@Table(name = "jobs", schema = SynapseAdminAuthDictionary.DEFAULT_SCHEMA)
public class JobsModel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "job_id")
	private long id;

	@Column(name = "job_name")
	private String pluginJobName;

	@Column(name = "start")
	private LocalDateTime start;

	@Column(name = "finish")
	private LocalDateTime finish;

	@Transient
	private LocalDateTime upcoming;

	public long getId() {
		return this.id;
	}

	public String getPluginJobName() {
		return this.pluginJobName;
	}

	public void setPluginJobName(String pluginJobName) {
		this.pluginJobName = pluginJobName;
	}

	public LocalDateTime getStart() {
		return start;
	}

	public void setStart(LocalDateTime start) {
		this.start = start;
	}

	public LocalDateTime getFinish() {
		return finish;
	}

	public void setFinish(LocalDateTime finish) {
		this.finish = finish;
	}

	public LocalDateTime getUpcoming() {
		return upcoming;
	}

	public void setUpcoming(LocalDateTime upcoming) {
		this.upcoming = upcoming;
	}

	public boolean getActive() {
		if (start == null || finish == null) {
			return true;
		}
		return finish.isBefore(start);
	}

	public String getRuntime() {
		if (start == null || finish == null) {
			return "N/A";
		}
		/* Get the total duration in ms and divide to get parts of time unit
		 * for formatting (Duration.toUnitPart() in Java 9, not 8) */
		long totalMs = Duration.between(start, finish).toMillis();
		long hours = totalMs / (1000 * 60 * 60);
		long minutes = totalMs / (1000 * 60) % 60;
		long seconds = (totalMs / 1000) % 60;
		long millis = totalMs % 1000;

		return String.format("%d:%02d:%02d.%02d", hours, minutes, seconds, millis);
	}
}


