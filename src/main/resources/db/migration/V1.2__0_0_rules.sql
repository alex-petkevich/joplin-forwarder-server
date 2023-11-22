CREATE TABLE rules
(
    `id`        INT PRIMARY KEY AUTOINCREMENT,
    `user_id`   INT NOT NULL,
    `name`      VARCHAR(255),
    `type`     VARCHAR(255) NOT NULL, -- rule type (from/subject/attach/date... )
    `comparison_method` VARCHAR(255), -- (=,<>,contains)
    `comparison_text`   VARCHAR(255),
    `save_in`   TINYINT, -- (save as a new record in note,..)
    `final_action` VARCHAR(255), -- (mark as read, delete, move to another folder)
    `final_action_target` VARCHAR(255), -- (folder name if move on step above)
    `processed` SMALLINT,   -- times processed
    `created_at`  INT,
    `last_modified_at`  INT,
    `last_processed_at` INT
);