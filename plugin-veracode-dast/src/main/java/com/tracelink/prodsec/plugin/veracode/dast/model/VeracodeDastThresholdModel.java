package com.tracelink.prodsec.plugin.veracode.dast.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.tracelink.prodsec.plugin.veracode.dast.VeracodeDastPlugin;
import com.tracelink.prodsec.synapse.scorecard.model.ScorecardValue.TrafficLight;

/**
 * The Threshold holds information about the cutoff point for a score to turn
 * red, yellow, or green on the Synapse Scorecard
 * 
 * @author csmith
 *
 */
@Entity
@Table(schema = VeracodeDastPlugin.SCHEMA, name = "veracode_dast_thresholds")
public class VeracodeDastThresholdModel {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "thresholds_id")
	private long id;

	/**
	 * Represents the threshold between a green and yellow {@link TrafficLight}. Any
	 * risk score less than or equal to this value will be green. Any risk score
	 * greater than this value, but less than or equal to yellowRed will be yellow.
	 */
	@Column(name = "green_yellow")
	private int greenYellow;

	/**
	 * Represents the threshold between a yellow and red {@link TrafficLight}. Any
	 * risk score less than or equal to this value, but greater than greenYellow
	 * will be yellow. Any risk score greater than this value will be red.
	 */
	@Column(name = "yellow_red")
	private int yellowRed;

	public long getId() {
		return id;
	}

	public long getGreenYellow() {
		return greenYellow;
	}

	public void setGreenYellow(int greenYellow) {
		this.greenYellow = greenYellow;
	}

	public long getYellowRed() {
		return yellowRed;
	}

	public void setYellowRed(int yellowRed) {
		this.yellowRed = yellowRed;
	}

}
