package com.tracelink.prodsec.plugin.bsimm.model.imports;

import java.util.Collection;

/**
 * DTO to store level information for a BSIMM survey. Maintains a list of associated measures.
 *
 * @author csmith
 */
public class BsimmLevel {

	private int levelNum;

	private Collection<BsimmMeasure> measures;

	public int getLevelNum() {
		return levelNum;
	}

	public void setLevelNum(int levelNum) {
		this.levelNum = levelNum;
	}

	public Collection<BsimmMeasure> getMeasures() {
		return measures;
	}

	public void setMeasures(Collection<BsimmMeasure> measures) {
		this.measures = measures;
	}

}
