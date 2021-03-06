package com.tracelink.prodsec.plugin.veracode.sca.model;

import com.tracelink.prodsec.plugin.veracode.sca.VeracodeScaPlugin;
import com.tracelink.prodsec.synapse.products.model.ProjectModel;
import com.tracelink.prodsec.synapse.scorecard.model.ScorecardValue.TrafficLight;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * The Database entity for the risk tolerance thresholds.
 * <p>
 * These threshold values are used to determine which {@link TrafficLight} to display for the
 * scorecard, given the risk score for the {@link VeracodeScaIssue} associated with a particular
 * {@link ProjectModel}. They are configurable so that risk tolerance can be adjusted over time.
 *
 * @author mcool
 */
@Entity
@Table(schema = VeracodeScaPlugin.SCHEMA, name = "veracode_sca_thresholds")
public class VeracodeScaThresholds {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "thresholds_id")
	private long id;

	/**
	 * Represents the threshold between a green and yellow {@link TrafficLight}. Any risk score less
	 * than or equal to this value will be green. Any risk score greater than this value, but less
	 * than or equal to yellowRed will be yellow.
	 */
	@Column(name = "green_yellow")
	private long greenYellow;

	/**
	 * Represents the threshold between a yellow and red {@link TrafficLight}. Any risk score less
	 * than or equal to this value, but greater than greenYellow will be yellow. Any risk score
	 * greater than this value will be red.
	 */
	@Column(name = "yellow_red")
	private long yellowRed;

	public long getId() {
		return id;
	}

	public long getGreenYellow() {
		return greenYellow;
	}

	public void setGreenYellow(long greenYellow) {
		this.greenYellow = greenYellow;
	}

	public long getYellowRed() {
		return yellowRed;
	}

	public void setYellowRed(long yellowRed) {
		this.yellowRed = yellowRed;
	}
}
