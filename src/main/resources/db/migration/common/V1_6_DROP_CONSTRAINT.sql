ALTER TABLE topic DROP CONSTRAINT topic_name_ref_idempotent;

ALTER TABLE topic ADD CONSTRAINT topic_name_ref_idempotent UNIQUE (name, parent_topic_id, owning_team, owning_unit);