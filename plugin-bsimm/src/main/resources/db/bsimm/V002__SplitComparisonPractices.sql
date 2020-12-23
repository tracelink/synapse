/* Create a new temp table that is a copy of the comparison table with no constraints */
CREATE TABLE survey_comparison_temp (
	survey_compare_id BIGSERIAL NOT NULL,
	original_survey BIGSERIAL NOT NULL,
	comparison_name text NOT NULL,
	function_name text NOT NULL,
	practice_name text NOT NULL,
	practice_score DECIMAL NOT NULL
);

/* Copy all existing data into a new temp */
INSERT INTO survey_comparison_temp SELECT * FROM survey_comparison;

/* Remove unneeded columns from existing table */
ALTER TABLE survey_comparison DROP COLUMN function_name;
ALTER TABLE survey_comparison DROP COLUMN practice_name;
ALTER TABLE survey_comparison DROP COLUMN practice_score;

/* Delete duplicate rows in existing table */
WITH cte AS (
  SELECT survey_compare_id, original_survey, comparison_name, ROW_NUMBER() OVER (
    PARTITION BY original_survey, comparison_name
    ORDER BY survey_compare_id, original_survey, comparison_name
  ) row_num
  FROM survey_comparison
)
DELETE FROM survey_comparison WHERE survey_compare_id IN (
  SELECT survey_compare_id FROM cte WHERE row_num <> 1
);

/* Create the new practice table */
CREATE TABLE survey_comparison_practice (
	survey_compare_practice_id BIGSERIAL NOT NULL,
	comparison_id BIGSERIAL NOT NULL,
	function_name text NOT NULL,
	practice_name text NOT NULL,
	practice_score DECIMAL NOT NULL,
	PRIMARY KEY (survey_compare_practice_id),
	CONSTRAINT fk_practice_comparison FOREIGN KEY (comparison_id) REFERENCES survey_comparison (survey_compare_id)
);

/* Update temp table to have compare ids all match what is in the existing table because we just deleted dupes */
UPDATE
  survey_comparison_temp AS dest
SET
  survey_compare_id = 
  (
    SELECT
      survey_compare_id
    FROM
      survey_comparison AS src
    WHERE dest.original_survey = src.original_survey
    AND dest.comparison_name = src.comparison_name
  );

/* Now insert the corrected temp values into the practice table since the references are correct in the comparison column now */
INSERT INTO survey_comparison_practice (comparison_id, function_name, practice_name, practice_score) SELECT survey_compare_id, function_name, practice_name, practice_score FROM survey_comparison_temp;

/* Delete the temp table */
DROP TABLE survey_comparison_temp;
    