package com.tracelink.prodsec.plugin.bsimm.model.survey;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.tracelink.prodsec.plugin.bsimm.BSIMMPlugin;

/**
 * Holds a comparison score for a response to a BSIMM survey version
 * 
 * This contains response scores for each comparison for each function's
 * practice.
 * 
 * Has many to one with the {@linkplain SurveyEntity} that this relates to
 * 
 * @author csmith
 *
 */
@Entity
@Table(schema = BSIMMPlugin.SCHEMA, name = "survey_comparison")
public class SurveyComparisonEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "survey_compare_id")
	private long id;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "original_survey")
	@Fetch(value = FetchMode.SELECT)
	private SurveyEntity originalSurvey;

	@Column(name = "comparison_name")
	private String comparisonName;

	@OneToMany(mappedBy = "comparison", fetch = FetchType.EAGER)
	private List<SurveyComparisonPracticeEntity> practices;

	public long getId() {
		return id;
	}

	public SurveyEntity getOriginalSurvey() {
		return originalSurvey;
	}

	public void setOriginalSurvey(SurveyEntity originalSurvey) {
		this.originalSurvey = originalSurvey;
	}

	public String getComparisonName() {
		return comparisonName;
	}

	public void setComparisonName(String comparisonName) {
		this.comparisonName = comparisonName;
	}

	public List<SurveyComparisonPracticeEntity> getPractices() {
		return this.practices;
	}

	public void setPractices(List<SurveyComparisonPracticeEntity> practices) {
		this.practices = practices;
	}

	public String getTitle() {
		return getComparisonName() + " - " + getOriginalSurvey().getSurveyName();
	}

}
