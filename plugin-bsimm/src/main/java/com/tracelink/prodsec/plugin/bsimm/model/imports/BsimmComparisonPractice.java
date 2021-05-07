package com.tracelink.prodsec.plugin.bsimm.model.imports;

/**
 * Comparison for a BSIMM practice, containing a name and value.
 *
 * @author csmith
 */
public class BsimmComparisonPractice {
	private String practiceName;
	private Double value;

	public String getPracticeName() {
		return practiceName;
	}

	public void setPracticeName(String practiceName) {
		this.practiceName = practiceName;
	}

	public Double getValue() {
		return value;
	}

	public void setValue(Double value) {
		this.value = value;
	}

}
