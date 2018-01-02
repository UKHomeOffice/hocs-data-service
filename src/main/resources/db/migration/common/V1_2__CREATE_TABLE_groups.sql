-- noinspection SqlNoDataSourceInspectionForFile

CREATE TABLE IF NOT EXISTS units
(
    id               BIGSERIAL       PRIMARY KEY,
    display_name     TEXT            NOT NULL,
    reference_name   TEXT            NOT NULL,
    deleted          BOOLEAN         DEFAULT FALSE NOT NULL,

    CONSTRAINT unit_name_ref_idempotent UNIQUE (display_name, reference_name)
);

CREATE INDEX idx_units_reference_name ON units (reference_name);
CREATE INDEX idx_units_deleted ON units (deleted);

CREATE TABLE IF NOT EXISTS teams
(
    id               BIGSERIAL       PRIMARY KEY,
    display_name     TEXT            NOT NULL,
    reference_name   TEXT            NOT NULL,
    unit_id          INT,
    deleted          BOOLEAN         DEFAULT FALSE NOT NULL,

    CONSTRAINT team_name_ref_idempotent UNIQUE (display_name, reference_name),
    CONSTRAINT fk_teams_unit_id FOREIGN KEY (unit_id) REFERENCES units(id)
);

CREATE INDEX idx_team_reference_name ON teams(reference_name);
CREATE INDEX idx_team_unit_id ON teams(unit_id);
CREATE INDEX idx_team_deleted ON teams (deleted);