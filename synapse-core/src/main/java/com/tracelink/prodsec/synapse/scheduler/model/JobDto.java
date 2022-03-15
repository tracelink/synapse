package com.tracelink.prodsec.synapse.scheduler.model;

import java.time.Duration;
import java.util.Date;
import java.util.concurrent.ScheduledFuture;

import org.springframework.scheduling.TriggerContext;

/**
 * Class to manage the state of a job including its next execution time.
 * Implements the {@linkplain TriggerContext} to help manage the job state
 *
 * @author csmith
 */
public class JobDto implements TriggerContext {

	private final String pluginName;

	private final String jobName;

	private Date lastStartTime;

	private Date nextStartTime;

	private Date lastEndTime;

	private boolean active;

	private ScheduledFuture<?> future;

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

	/**
	 * Get the duration of the last job run in milliseconds, or -1 if the job hasn't
	 * run or completed yet
	 *
	 * @return the milliseconds of duration, or -1 if the job hasn't run or
	 * completed
	 */
	public long getDurationMs() {
		if (getLastStartTime() == null || getLastEndTime() == null) {
			return -1;
		}
		return Duration.between(getLastStartTime().toInstant(), getLastEndTime().toInstant())
				.toMillis();
	}

	/**
	 * Report the duration of the last job run.
	 *
	 * @return a friendly string describing the duration of the last run in
	 * minutes:seconds.milliseconds, or 'N/A' if the job hasn't run
	 */
	public String getDurationString() {
		long totalMs = getDurationMs();
		if (totalMs < 0L) {
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

	public void setFuture(ScheduledFuture<?> future) {
		this.future = future;
	}
	
	public ScheduledFuture<?> getFuture(){
		return this.future;
	}

}
