CREATE TABLE rules_actions
(
    `id`        INT PRIMARY KEY AUTO_INCREMENT,
    `rule_id`   INT NOT NULL,
    `action` VARCHAR(255), -- (mark as read, delete, move to another folder)
    `action_target` VARCHAR(255), -- (folder name if move on step above)
    `created_at`  TIMESTAMP,
    `last_modified_at`  TIMESTAMP
);

INSERT INTO rules_actions (rule_id, action, action_target, created_at) SELECT id, final_action, final_action_target, created_at FROM rules;
ALTER TABLE rules DROP COLUMN final_action;
ALTER TABLE rules DROP COLUMN final_action_target;