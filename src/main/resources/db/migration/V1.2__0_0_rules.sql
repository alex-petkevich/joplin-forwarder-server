CREATE TABLE rules
(
    `id`        INTEGER PRIMARY KEY AUTOINCREMENT,
    `user_id`   INTEGER NOT NULL,
    `name`      TEXT,
    `type`     INTEGER NOT NULL, -- rule type (from/subject/attach/date... )
    `comparison_method` INTEGER, -- (=,<>,contains)
    `comparison_text`   TEXT,
    `save_in`   INTEGER, -- (save as a new record in note,..)
    `final_action` INTEGER, -- (mark as read, delete, move to another folder)
    `final_action_target` TEXT, -- (folder name if move on step above)
    `processed` INTEGER,
    `created_at`  INTEGER,
    `last_modified_at`  INTEGER,
    `last_processed_at` INTEGER
);