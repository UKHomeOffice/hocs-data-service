-- noinspection SqlNoDataSourceInspectionForFile

CREATE TABLE IF NOT EXISTS team_email
(
  id           BIGSERIAL PRIMARY KEY,
  name         TEXT NOT NULL,
  display_name TEXT NOT NULL,
  email        TEXT NOT NULL
);

CREATE INDEX idx_teamname_id
  ON team_email (name);
