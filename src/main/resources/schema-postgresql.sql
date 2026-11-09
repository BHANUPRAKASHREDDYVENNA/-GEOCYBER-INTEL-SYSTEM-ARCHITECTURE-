-- GEOCYBER INTEL - PostgreSQL Database Schema
-- Advanced Geopolitical Cyber Threat Intelligence and APT Attribution Platform

CREATE TABLE IF NOT EXISTS apt_actors (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    country VARCHAR(10) NOT NULL,
    country_name VARCHAR(100),
    aliases TEXT,
    motivation VARCHAR(50),
    target_sectors TEXT,
    target_countries TEXT,
    observed_ttps TEXT,
    observed_malware TEXT,
    confidence_score DOUBLE PRECISION DEFAULT 0.85,
    first_seen VARCHAR(20),
    last_seen VARCHAR(20),
    active BOOLEAN DEFAULT TRUE,
    description TEXT,
    provenance TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS gdelt_events (
    id BIGSERIAL PRIMARY KEY,
    global_event_id VARCHAR(50) NOT NULL UNIQUE,
    event_date DATE NOT NULL,
    actor1_code VARCHAR(50),
    actor1_country VARCHAR(10),
    actor2_code VARCHAR(50),
    actor2_country VARCHAR(10),
    event_code VARCHAR(20),
    event_description VARCHAR(255),
    goldstein_scale DOUBLE PRECISION,
    avg_tone DOUBLE PRECISION,
    num_mentions INT,
    action_geo_country VARCHAR(10),
    action_geo_lat DOUBLE PRECISION,
    action_geo_long DOUBLE PRECISION,
    source_url TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS mitre_techniques (
    id BIGSERIAL PRIMARY KEY,
    technique_id VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    tactic VARCHAR(100) NOT NULL,
    description TEXT,
    platforms VARCHAR(255),
    detection TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS threat_reports (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    publisher VARCHAR(100) NOT NULL,
    publication_date DATE,
    source_url TEXT,
    sha256_hash VARCHAR(64),
    summary TEXT,
    full_content TEXT,
    extracted_actors TEXT,
    extracted_malware TEXT,
    extracted_ttps TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS indicators_of_compromise (
    id BIGSERIAL PRIMARY KEY,
    ioc_type VARCHAR(20) NOT NULL,
    raw_value VARCHAR(255) NOT NULL,
    normalized_value VARCHAR(255) NOT NULL,
    defanged_value VARCHAR(255) NOT NULL,
    reputation_score DOUBLE PRECISION DEFAULT 0.5,
    confidence_level VARCHAR(20) DEFAULT 'MEDIUM',
    threat_feed VARCHAR(100),
    associated_actors TEXT,
    associated_campaigns TEXT,
    first_seen TIMESTAMP,
    last_seen TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS cyber_campaigns (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL UNIQUE,
    attributed_actor_name VARCHAR(100),
    start_date DATE,
    end_date DATE,
    target_nations TEXT,
    target_sectors TEXT,
    geopolitical_objective TEXT,
    observed_ttps TEXT,
    observed_malware TEXT,
    observed_iocs TEXT,
    gdelt_correlation_score DOUBLE PRECISION DEFAULT 0.0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS attribution_cases (
    id BIGSERIAL PRIMARY KEY,
    case_number VARCHAR(50) NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    lead_analyst VARCHAR(100),
    status VARCHAR(30) DEFAULT 'OPEN',
    incident_date DATE,
    incident_target_country VARCHAR(10),
    incident_target_sector VARCHAR(100),
    observed_ttps TEXT,
    observed_iocs TEXT,
    geopolitical_context TEXT,
    attributed_actor VARCHAR(100),
    calibrated_confidence DOUBLE PRECISION,
    is_unknown_actor BOOLEAN DEFAULT FALSE,
    notes TEXT,
    evidence_breakdown TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100),
    email VARCHAR(150),
    role VARCHAR(50) NOT NULL,
    enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGSERIAL PRIMARY KEY,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    principal VARCHAR(100) NOT NULL,
    action VARCHAR(100) NOT NULL,
    resource VARCHAR(150),
    details TEXT,
    ip_address VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS benchmark_records (
    id BIGSERIAL PRIMARY KEY,
    run_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    model_name VARCHAR(100) NOT NULL,
    split_strategy VARCHAR(100) NOT NULL,
    accuracy DOUBLE PRECISION,
    precision_score DOUBLE PRECISION,
    recall_score DOUBLE PRECISION,
    macro_f1 DOUBLE PRECISION,
    weighted_f1 DOUBLE PRECISION,
    top1_accuracy DOUBLE PRECISION,
    top3_accuracy DOUBLE PRECISION,
    brier_score DOUBLE PRECISION,
    expected_calibration_error DOUBLE PRECISION,
    ablation_notes VARCHAR(255)
);

CREATE INDEX IF NOT EXISTS idx_apt_name ON apt_actors(name);
CREATE INDEX IF NOT EXISTS idx_apt_country ON apt_actors(country);
CREATE INDEX IF NOT EXISTS idx_gdelt_date ON gdelt_events(event_date);
CREATE INDEX IF NOT EXISTS idx_gdelt_actors ON gdelt_events(actor1_country, actor2_country);
CREATE INDEX IF NOT EXISTS idx_ioc_norm ON indicators_of_compromise(normalized_value);
CREATE INDEX IF NOT EXISTS idx_ioc_type ON indicators_of_compromise(ioc_type);
CREATE INDEX IF NOT EXISTS idx_cases_actor ON attribution_cases(attributed_actor);
