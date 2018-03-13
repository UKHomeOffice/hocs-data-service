-- noinspection SqlNoDataSourceInspectionForFile

DROP INDEX idx_users_deleted;

ALTER TABLE users
    DROP COLUMN deleted;