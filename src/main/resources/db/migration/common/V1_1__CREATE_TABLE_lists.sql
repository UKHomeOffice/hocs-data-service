-- noinspection SqlNoDataSourceInspectionForFile

CREATE TABLE IF NOT EXISTS lists
(
    id          BIGSERIAL       PRIMARY KEY,
    name        TEXT            NOT NULL,
    deleted     BOOLEAN         DEFAULT FALSE NOT NULL,

    CONSTRAINT list_name_idempotent UNIQUE (name)
);

CREATE INDEX idx_list_reference ON lists (name);
CREATE INDEX idx_list_reference_deleted ON lists (deleted);

CREATE TABLE IF NOT EXISTS entities
(
    id               BIGSERIAL      PRIMARY KEY,
    text             TEXT           NOT NULL,
    value            TEXT           NOT NULL,
    list_id          INT,
    deleted          BOOLEAN        DEFAULT FALSE NOT NULL,

    CONSTRAINT entity_name_ref_idempotent UNIQUE (value, text, list_id),
    CONSTRAINT fk_list_id FOREIGN KEY (list_id) REFERENCES lists(id),
);

CREATE INDEX idx_list_id ON entities (list_id);
CREATE INDEX idx_list_id_deleted ON entities (deleted);