package com.tracelink.prodsec.plugin.bsimm.model.survey;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import com.tracelink.prodsec.plugin.bsimm.BSIMMPlugin;

/**
 * The Top level survey entity.
 * 
 * Has 1 to many with the survey's {@linkplain MeasureEntity}
 * 
 * @author csmith
 *
 */
@Entity
@Table(schema = BSIMMPlugin.SCHEMA, name = "survey")
public class SurveyEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "survey_id")
	private long id;

	@Column(name = "survey_name")
	private String surveyName;

	@OneToMany(fetch = FetchType.EAGER, mappedBy = "owningSurvey")
	@OrderBy(value = "measure_id")
	private List<MeasureEntity> measures;

	public long getId() {
		return this.id;
	}

	public void setSurveyName(String surveyName) {
		this.surveyName = surveyName;
	}

	public String getSurveyName() {
		return this.surveyName;
	}

	public void setMeasures(List<MeasureEntity> measures) {
		this.measures = measures;
	}

	public List<MeasureEntity> getMeasures() {
		return this.measures;
	}
}
