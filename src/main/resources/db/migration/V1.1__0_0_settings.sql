CREATE TABLE settings
(
    `id`        INTEGER PRIMARY KEY AUTOINCREMENT,
    `user_id`   INTEGER NOT NULL,
    `name`      TEXT,
    `value`     TEXT,
    `created_at`  INTEGER,
    `last_modified_at`  INTEGER
);