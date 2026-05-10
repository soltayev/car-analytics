CREATE TABLE vehicles (
    id BIGSERIAL PRIMARY KEY,
    vin VARCHAR(255) NOT NULL,
    brand VARCHAR(255) NOT NULL,
    model VARCHAR(255) NOT NULL,
    production_year INTEGER NOT NULL,
    engine_type VARCHAR(255) NOT NULL
);

CREATE TABLE diagnostic_sessions (
    id BIGSERIAL PRIMARY KEY,
    vehicle_id BIGINT NOT NULL REFERENCES vehicles (id),
    connection_type VARCHAR(255) NOT NULL,
    adapter_name VARCHAR(255) NOT NULL,
    adapter_identifier VARCHAR(255) NOT NULL,
    protocol VARCHAR(255) NOT NULL,
    started_at TIMESTAMP NOT NULL,
    overall_status VARCHAR(255) NOT NULL
);

CREATE TABLE raw_obd_frames (
    id BIGSERIAL PRIMARY KEY,
    session_id BIGINT NOT NULL REFERENCES diagnostic_sessions (id),
    mode VARCHAR(20) NOT NULL,
    pid VARCHAR(20),
    raw_response VARCHAR(2000) NOT NULL,
    decoded_label VARCHAR(255) NOT NULL,
    manufacturer_specific BOOLEAN NOT NULL,
    frame_timestamp TIMESTAMP NOT NULL
);

CREATE TABLE vehicle_info_items (
    id BIGSERIAL PRIMARY KEY,
    session_id BIGINT NOT NULL REFERENCES diagnostic_sessions (id),
    info_key VARCHAR(255) NOT NULL,
    info_value VARCHAR(1000) NOT NULL,
    source_mode VARCHAR(20) NOT NULL
);

CREATE TABLE obd_readings (
    id BIGSERIAL PRIMARY KEY,
    session_id BIGINT NOT NULL REFERENCES diagnostic_sessions (id),
    parameter_name VARCHAR(255) NOT NULL,
    pid_code VARCHAR(20),
    source_mode VARCHAR(20) NOT NULL,
    freeze_frame BOOLEAN NOT NULL,
    manufacturer_specific BOOLEAN NOT NULL,
    description VARCHAR(255),
    parameter_value NUMERIC(10, 2) NOT NULL,
    unit VARCHAR(255) NOT NULL
);

CREATE TABLE fault_codes (
    id BIGSERIAL PRIMARY KEY,
    session_id BIGINT NOT NULL REFERENCES diagnostic_sessions (id),
    code VARCHAR(255) NOT NULL,
    description VARCHAR(1000) NOT NULL,
    fault_code_type VARCHAR(255) NOT NULL,
    source_mode VARCHAR(20) NOT NULL,
    manufacturer_specific BOOLEAN NOT NULL,
    severity VARCHAR(255) NOT NULL
);

CREATE TABLE recommendations (
    id BIGSERIAL PRIMARY KEY,
    session_id BIGINT NOT NULL REFERENCES diagnostic_sessions (id),
    type VARCHAR(255) NOT NULL,
    message VARCHAR(2000) NOT NULL,
    action_label VARCHAR(255),
    reference_code VARCHAR(50)
);

CREATE TABLE diagnostic_reports (
    id BIGSERIAL PRIMARY KEY,
    session_id BIGINT NOT NULL UNIQUE REFERENCES diagnostic_sessions (id),
    health_score INTEGER NOT NULL,
    urgency VARCHAR(255) NOT NULL,
    drivable BOOLEAN NOT NULL,
    tow_recommended BOOLEAN NOT NULL,
    primary_issue VARCHAR(1000) NOT NULL,
    summary VARCHAR(2000) NOT NULL,
    generated_at TIMESTAMP NOT NULL
);

CREATE TABLE diagnostic_report_actions (
    report_id BIGINT NOT NULL REFERENCES diagnostic_reports (id),
    action_text VARCHAR(500) NOT NULL
);

CREATE TABLE fault_code_dictionary (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(2000) NOT NULL,
    system_name VARCHAR(120) NOT NULL,
    subsystem VARCHAR(120) NOT NULL,
    manufacturer_specific BOOLEAN NOT NULL,
    default_severity VARCHAR(255) NOT NULL,
    possible_causes VARCHAR(2000) NOT NULL,
    recommended_actions VARCHAR(2000) NOT NULL,
    drivable_allowed BOOLEAN NOT NULL
);

CREATE TABLE service_centers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    city VARCHAR(255) NOT NULL,
    address VARCHAR(255) NOT NULL,
    phone VARCHAR(255) NOT NULL,
    specialization VARCHAR(255) NOT NULL,
    emergency_support BOOLEAN NOT NULL,
    rating NUMERIC(3, 2) NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL
);

CREATE TABLE spare_parts (
    id BIGSERIAL PRIMARY KEY,
    part_number VARCHAR(255) NOT NULL,
    part_name VARCHAR(255) NOT NULL,
    manufacturer VARCHAR(255) NOT NULL,
    subsystem VARCHAR(255) NOT NULL,
    compatible_brand VARCHAR(255),
    compatible_model VARCHAR(255),
    fault_code VARCHAR(255),
    price NUMERIC(12, 2) NOT NULL,
    currency VARCHAR(255) NOT NULL,
    stock_status VARCHAR(255) NOT NULL
);

CREATE TABLE repair_guides (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    subsystem VARCHAR(255) NOT NULL,
    fault_code VARCHAR(255),
    description VARCHAR(2000) NOT NULL,
    difficulty VARCHAR(255) NOT NULL,
    estimated_minutes INTEGER NOT NULL,
    safety_notes VARCHAR(1000) NOT NULL
);

CREATE INDEX idx_diagnostic_sessions_vehicle_id ON diagnostic_sessions (vehicle_id);
CREATE INDEX idx_raw_obd_frames_session_id ON raw_obd_frames (session_id);
CREATE INDEX idx_vehicle_info_items_session_id ON vehicle_info_items (session_id);
CREATE INDEX idx_obd_readings_session_id ON obd_readings (session_id);
CREATE INDEX idx_fault_codes_session_id ON fault_codes (session_id);
CREATE INDEX idx_recommendations_session_id ON recommendations (session_id);
CREATE INDEX idx_diagnostic_report_actions_report_id ON diagnostic_report_actions (report_id);
CREATE INDEX idx_spare_parts_fault_code ON spare_parts (fault_code);
CREATE INDEX idx_repair_guides_fault_code ON repair_guides (fault_code);
