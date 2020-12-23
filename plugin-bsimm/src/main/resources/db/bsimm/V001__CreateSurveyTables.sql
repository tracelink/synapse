CREATE TABLE survey (
	survey_id BIGSERIAL NOT NULL,
	survey_name text NOT NULL,
	PRIMARY KEY (survey_id)
);

CREATE TABLE measure (
	measure_id BIGSERIAL NOT NULL,
	function text NOT NULL,
	practice text NOT NULL,
	level INT NOT NULL,
	bsimm_measure_id varchar(255) NOT NULL,
	bsimm_measure_title text NOT NULL,
	bsimm_measure_detail text NOT NULL,
	owning_survey BIGINT NOT NULL,
	PRIMARY KEY (measure_id),
	CONSTRAINT fk_measure_survey FOREIGN KEY (owning_survey) REFERENCES survey (survey_id)
);

CREATE TABLE survey_response (
	survey_response_id BIGSERIAL NOT NULL,
	date_filed DATE NOT NULL,
	author varchar(255) NOT NULL,
	original_survey BIGSERIAL NOT NULL,
	survey_target BIGSERIAL NOT NULL,
	PRIMARY KEY (survey_response_id),
	CONSTRAINT fk_survey_response_product FOREIGN KEY (survey_target) REFERENCES synapsecore.productline (productline_id),
	CONSTRAINT fk_survey_response_survey FOREIGN KEY (original_survey) REFERENCES survey (survey_id)
);

CREATE TABLE measure_response (
	measure_response_id BIGSERIAL NOT NULL,
	status varchar(255) NOT NULL,
	responsible varchar(255) NOT NULL,
	response text NOT NULL,
	related_measure BIGSERIAL NOT NULL,
	survey_response_id BIGSERIAL,
	PRIMARY KEY (measure_response_id),
	CONSTRAINT fk_related_measure_measure FOREIGN KEY (related_measure) REFERENCES measure (measure_id),
	CONSTRAINT fk_measure_response_survey_response FOREIGN KEY (survey_response_id) REFERENCES survey_response (survey_response_id)
);

CREATE TABLE survey_comparison (
	survey_compare_id BIGSERIAL NOT NULL,
	original_survey BIGSERIAL NOT NULL,
	comparison_name text NOT NULL,
	function_name text NOT NULL,
	practice_name text NOT NULL,
	practice_score DECIMAL NOT NULL,
	PRIMARY KEY (survey_compare_id),
	CONSTRAINT fk_comparison_original_survey FOREIGN KEY (original_survey) REFERENCES survey (survey_id)
);