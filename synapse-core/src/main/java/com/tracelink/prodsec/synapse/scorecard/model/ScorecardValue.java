package com.tracelink.prodsec.synapse.scorecard.model;

/**
 * The ScorecardValue handles the output data for a {@link ScorecardRow} and
 * {@link ScorecardColumn}
 * 
 * @author csmith
 *
 */
public class ScorecardValue {

	public static final ScorecardValue BLANK = new ScorecardValue("", TrafficLight.NONE);

	/**
	 * Traffic Light helps define the output coloring in the Scorecard
	 * 
	 * @author csmith
	 *
	 */
	public enum TrafficLight {
		RED("red"), YELLOW("yellow"), GREEN("green"), NONE("none");
		private final String color;

		TrafficLight(String color) {
			this.color = color;
		}

		public String getColor() {
			return this.color;
		}
	}

	private final TrafficLight color;
	private final String value;

	public ScorecardValue(String value, TrafficLight color) {
		this.value = value;
		this.color = color;
	}

	public TrafficLight getColor() {
		return this.color;
	}

	public String getValue() {
		return this.value;
	}
}
