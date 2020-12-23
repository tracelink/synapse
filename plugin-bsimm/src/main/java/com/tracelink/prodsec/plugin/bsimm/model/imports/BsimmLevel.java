package com.tracelink.prodsec.plugin.bsimm.model.imports;

import java.util.Collection;

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
