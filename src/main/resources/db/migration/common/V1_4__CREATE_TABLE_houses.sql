-- noinspection SqlNoDataSourceInspectionForFile

CREATE TABLE IF NOT EXISTS houses
(
    id          BIGSERIAL       PRIMARY KEY,
    name TEXT NOT NULL,
    deleted     BOOLEAN DEFAULT FALSE NOT NULL,
    CONSTRAINT house_name_idempotent UNIQUE (name)
);

CREATE INDEX idx_house_reference ON houses (name);


CREATE TABLE IF NOT EXISTS members
(
    id               BIGSERIAL       PRIMARY KEY,
    display_name     TEXT            NOT NULL,
    reference_name   TEXT            NOT NULL,
    house_id         INT,
    deleted          BOOLEAN         DEFAULT FALSE NOT NULL,

    CONSTRAINT member_name_ref_idempotent UNIQUE (display_name, reference_name, house_id),
);

CREATE INDEX idx_house_id ON members (house_id);
