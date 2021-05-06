package com.tracelink.prodsec.synapse.scheduler.model;

import java.time.Duration;
import java.util.Date;

import org.springframework.scheduling.TriggerContext;

public class JobDto implements TriggerContext {
	private String pluginName;

	private String jobName;

	private Date lastStartTime;

	private Date nextStartTime;

	private Date lastEndTime;

	private boolean active;

	public JobDto(String pluginName, String jobName) {
		this.pluginName = pluginName;
		this.jobName = jobName;
	}

	public String getPluginName() {
		return pluginName;
	}

	public String getJobName() {
		return jobName;
	}

	public Date getLastStartTime() {
		return lastStartTime;
	}

	public void setLastStartTime(Date lastStartTime) {
		this.lastStartTime = lastStartTime;
	}

	public Date getNextStartTime() {
		return nextStartTime;
	}

	public void setNextStartTime(Date nextStartTime) {
		this.nextStartTime = nextStartTime;
	}

	public Date getLastEndTime() {
		return lastEndTime;
	}

	public void setLastEndTime(Date lastEndTime) {
		this.lastEndTime = lastEndTime;
	}

	public long getDurationMs() {
		if (getLastStartTime() == null || getLastEndTime() == null) {
			return -1;
		}
		return Duration.between(getLastStartTime().toInstant(), getLastEndTime().toInstant()).toMillis();
	}

	public String getDurationString() {
		long totalMs = getDurationMs();
		if (totalMs == -1L) {
			return "N/A";
		}

		long minutes = totalMs / (1000 * 60) % 60;
		long seconds = (totalMs / 1000) % 60;
		long millis = totalMs % 1000;

		return String.format("%d:%02d.%02d", minutes, seconds, millis);
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	@Override
	public Date lastScheduledExecutionTime() {
		return nextStartTime;
	}

	@Override
	public Date lastActualExecutionTime() {
		return lastStartTime;
	}

	@Override
	public Date lastCompletionTime() {
		return lastEndTime;
	}

}