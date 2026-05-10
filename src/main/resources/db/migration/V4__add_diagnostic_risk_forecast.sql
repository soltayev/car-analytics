ALTER TABLE diagnostic_reports
ADD COLUMN risk_forecast VARCHAR(2000) NOT NULL
DEFAULT 'No progression forecast was generated for this diagnostic report.';

ALTER TABLE diagnostic_reports
ALTER COLUMN risk_forecast DROP DEFAULT;
