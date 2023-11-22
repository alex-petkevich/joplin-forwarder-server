CREATE TABLE settings
(
    `id`        INT PRIMARY KEY AUTOINCREMENT,
    `user_id`   INT NOT NULL,
    `name`      VARCHAR(255),
    `value`     VARCHAR(255),
    `created_at`  INT,
    `last_modified_at`  INT
);