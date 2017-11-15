-- noinspection SqlNoDataSourceInspectionForFile

CREATE TABLE IF NOT EXISTS topic_groups
(
    id          BIGSERIAL       PRIMARY KEY,
    name        TEXT            NOT NULL,
    case_type   TEXT            NOT NULL,

    CONSTRAINT topic_group_name_idempotent UNIQUE (name, case_type)
);

CREATE INDEX idx_topic_case_type ON topic_groups (case_type);

CREATE TABLE IF NOT EXISTS topic
(
    id               BIGSERIAL       PRIMARY KEY,
    name             TEXT            NOT NULL,
    owning_unit      TEXT            NOT NULL,
    owning_team      TEXT,
    parent_topic_id  INT,

    CONSTRAINT topic_name_ref_idempotent UNIQUE (name, parent_topic_id),
    CONSTRAINT fk_parent_topic_id FOREIGN KEY (parent_topic_id) REFERENCES topic_groups(id)
);

CREATE INDEX idx_parent_topic_id ON topic(parent_topic_id);