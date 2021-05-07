package com.tracelink.prodsec.plugin.bsimm.model.response;

/**
 * Holds the scoring for each response status
 *
 * @author csmith
 */
public enum MeasureResponseStatus {
	NOT_STARTED("Not Started", 0), //
	IN_PROGRESS("In Progress", 0), //
	DECLINED("Declined", 1), //
	COMPLETE("Complete", 1);

	private final String statusText;
	private final int score;

	MeasureResponseStatus(String statusText, int score) {
		this.statusText = statusText;
		this.score = score;
	}

	public String getStatusText() {
		return this.statusText;
	}

	public int getScore() {
		return this.score;
	}

	public static MeasureResponseStatus getMeasureFor(String statusText) {
		for (MeasureResponseStatus ms : MeasureResponseStatus.values()) {
			if (ms.statusText.equals(statusText)) {
				return ms;
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return this.statusText;
	}
}
