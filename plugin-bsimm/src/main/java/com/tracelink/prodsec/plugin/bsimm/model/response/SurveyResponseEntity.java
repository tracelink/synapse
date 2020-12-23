package com.tracelink.prodsec.plugin.bsimm.model.response;

import com.tracelink.prodsec.plugin.bsimm.BSIMMPlugin;
import com.tracelink.prodsec.plugin.bsimm.model.survey.MeasureEntity;
import com.tracelink.prodsec.plugin.bsimm.model.survey.SurveyEntity;
import com.tracelink.prodsec.synapse.products.model.ProductLineModel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

/**
 * Holder of Measure Responses object
 * <p>
 * Has 1 to 1 with the {@linkplain SurveyEntity} object
 * <p>
 * Has 1 to 1 with the {@linkplain ProductLineModel} this is in response to
 * <p>
 * Has 1 to many with the {@linkplain MeasureResponseEntity}s for this survey
 * response
 *
 * @author csmith
 */
@Entity
@Table(schema = BSIMMPlugin.SCHEMA, name = "survey_response")
public class SurveyResponseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "survey_response_id")
	private long id;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "date_filed")
	private Date dateFiled;

	@Column(name = "author")
	private String author;

	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "original_survey")
	@Fetch(value = FetchMode.SELECT)
	private SurveyEntity originalSurvey;

	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "survey_target")
	private ProductLineModel surveyTarget;

	@OneToMany(fetch = FetchType.EAGER, mappedBy = "surveyResponse")
	@Fetch(value = FetchMode.SUBSELECT)
	@OrderBy(value = "measure_response_id")
	private List<MeasureResponseEntity> measureResponses;

	private static final SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy");

	public long getId() {
		return id;
	}

	public Date getDateFiled() {
		return dateFiled;
	}

	public String getDateString() {
		return sdf.format(dateFiled);
	}

	public void setDateFiled(Date dateFiled) {
		this.dateFiled = dateFiled;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public SurveyEntity getOriginalSurvey() {
		return originalSurvey;
	}

	public void setOriginalSurvey(SurveyEntity originalSurvey) {
		this.originalSurvey = originalSurvey;
	}

	public ProductLineModel getSurveyTarget() {
		return surveyTarget;
	}

	public void setSurveyTarget(ProductLineModel surveyTarget) {
		this.surveyTarget = surveyTarget;
	}

	public void setMeasureResponses(List<MeasureResponseEntity> measures) {
		this.measureResponses = measures;
	}

	public List<MeasureResponseEntity> getMeasureResponses() {
		return measureResponses;
	}

	public String getTitle() {
		return getSurveyTarget().getName() + " - " + getDateString() + " - " + getOriginalSurvey()
				.getSurveyName();
	}

	public double getResponseScore() {
		Map<String, Map<String, List<Double>>> practiceMap = new LinkedHashMap<>();
		for (MeasureResponseEntity mre : measureResponses) {
			MeasureEntity measure = mre.getRelatedMeasure();
			String practice = measure.getPracticeName();

			Map<String, List<Double>> levelMap = practiceMap.getOrDefault(practice,
					new LinkedHashMap<>());
			String level = String.valueOf(measure.getLevel());
			List<Double> scores = levelMap.getOrDefault(level, new ArrayList<>());
			scores.add(mre.getScore());
			levelMap.put(level, scores);
			practiceMap.put(practice, levelMap);
		}
		return practiceMap.values().stream().map(stringListMap -> stringListMap.values().stream()
				.map(doubles -> doubles.stream().collect(Collectors.averagingDouble(v -> v)))
				.mapToDouble(v -> v).sum()).collect(Collectors.averagingDouble(v -> v));
	}

}
