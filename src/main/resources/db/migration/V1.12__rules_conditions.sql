CREATE TABLE rules_conditions
(
    `id`        INT PRIMARY KEY AUTO_INCREMENT,
    `rule_id`   INT NOT NULL,
    `type`     VARCHAR(255) NOT NULL, -- rule type (from/subject/attach/date... )
    `comparison_method` VARCHAR(255), -- (=,<>,contains)
    `comparison_text`   VARCHAR(255),
    `created_at`  TIMESTAMP,
    `last_modified_at`  TIMESTAMP
);

INSERT INTO rules_conditions (rule_id, type, comparison_method, comparison_text, created_at) SELECT id, type, comparison_method, comparison_text, created_at FROM rules;
ALTER TABLE rules DROP COLUMN type;
ALTER TABLE rules DROP COLUMN comparison_method;
ALTER TABLE rules DROP COLUMN comparison_text;