package com.tracelink.prodsec.plugin.bsimm.model.imports;

import java.util.Collection;

public class BsimmPractice {

	private String practiceName;

	private Collection<BsimmLevel> levels;

	public String getPracticeName() {
		return practiceName;
	}

	public void setPracticeName(String practiceName) {
		this.practiceName = practiceName;
	}

	public Collection<BsimmLevel> getLevels() {
		return levels;
	}

	public void setLevels(Collection<BsimmLevel> levels) {
		this.levels = levels;
	}

}
