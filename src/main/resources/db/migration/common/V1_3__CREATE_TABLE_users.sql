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
CREATE INDEX idx_users_deleted ON users (deleted);

CREATE TABLE IF NOT EXISTS users_teams
(
    user_id     INT     NOT NULL,
    team_id    INT     NOT NULL,

    PRIMARY KEY(user_id, team_id),
    CONSTRAINT fk_user_id FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_team_id FOREIGN KEY (team_id) REFERENCES teams(id)
);