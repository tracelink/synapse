package com.tracelink.prodsec.plugin.bsimm.model.imports;

public class BsimmMeasure {
	private String measureId;

	private String measureTitle;

	private String detailMessage;

	public String getMeasureId() {
		return measureId;
	}

	public void setMeasureId(String measureId) {
		this.measureId = measureId;
	}

	public String getMeasureTitle() {
		return measureTitle;
	}

	public void setMeasureTitle(String measureTitle) {
		this.measureTitle = measureTitle;
	}

	public String getDetailMessage() {
		return detailMessage;
	}

	public void setDetailMessage(String detailMessage) {
		this.detailMessage = detailMessage;
	}
}
