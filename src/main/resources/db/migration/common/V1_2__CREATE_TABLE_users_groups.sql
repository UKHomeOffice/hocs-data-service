-- noinspection SqlNoDataSourceInspectionForFile

CREATE TABLE IF NOT EXISTS users
(
    id               BIGSERIAL      PRIMARY KEY,
    first_name       TEXT           NOT NULL,
    last_name        TEXT           NOT NULL,
    user_name        TEXT           NOT NULL,
    email            TEXT           NOT NULL,
    department       TEXT           NOT NULL,
    deleted          BOOLEAN        DEFAULT FALSE NOT NULL,

    CONSTRAINT user_name_idempotent UNIQUE (user_name)
);

CREATE INDEX idx_users_user_name ON users (user_name);
CREATE INDEX idx_users_department ON users (department);

CREATE TABLE IF NOT EXISTS groups
(
    id               BIGSERIAL       PRIMARY KEY,
    display_name     TEXT            NOT NULL,
    reference_name   TEXT            NOT NULL,
    parent_group_id  INT,
    deleted          BOOLEAN         DEFAULT FALSE NOT NULL,

    CONSTRAINT group_name_ref_idempotent UNIQUE (display_name, reference_name),
    CONSTRAINT fk_parent_group_id FOREIGN KEY (parent_group_id) REFERENCES groups(id)
);

CREATE INDEX idx_reference_name ON groups (reference_name);
CREATE INDEX idx_parent_group_id ON groups (parent_group_id);

CREATE TABLE IF NOT EXISTS users_groups
(
    user_id     INT     NOT NULL,
    group_id    INT     NOT NULL,

    PRIMARY KEY(user_id, group_id),
    CONSTRAINT fk_user_id FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_group_id FOREIGN KEY (group_id) REFERENCES groups(id)
);