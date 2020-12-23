package com.tracelink.prodsec.plugin.jira.model;

import com.tracelink.prodsec.plugin.jira.JiraPlugin;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * The Database entity for Scrum Metrics.
 *
 * @author bhoran
 */

@Entity
@Table(schema = JiraPlugin.SCHEMA, name = "jira_scrum_metrics")
public class JiraScrumMetric {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "scrum_id")
	private long id;

	@Column(name = "recorded_date")
	private LocalDate recordedDate;

	@Column(name = "todo")
	private long todo;

	@Column(name = "prog")
	private long prog;

	@Column(name = "block")
	private long block;

	@Column(name = "done")
	private long done;

	public long getId() {
		return id;
	}

	public LocalDate getRecordedDate() {
		return recordedDate;
	}

	public void setRecordedDate(LocalDate recordedDate) {
		this.recordedDate = recordedDate;
	}

	public long getTodo() {
		return todo;
	}

	public void setTodo(long todo) {
		this.todo = todo;
	}

	public long getProg() {
		return prog;
	}

	public void setProg(long prog) {
		this.prog = prog;
	}

	public long getBlock() {
		return block;
	}

	public void setBlock(long block) {
		this.block = block;
	}

	public long getDone() {
		return done;
	}

	public void setDone(long done) {
		this.done = done;
	}

	public long getUnres() {
		return todo + prog + block;
	}

	public long getTotal() {
		return todo + prog + block + done;
	}
}
