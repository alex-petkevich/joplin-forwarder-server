CREATE TABLE settings
(
    `id`        INT PRIMARY KEY AUTO_INCREMENT,
    `user_id`   INT NOT NULL,
    `name`      VARCHAR(255),
    `value`     VARCHAR(255),
    `created_at`  TIMESTAMP,
    `last_modified_at`  TIMESTAMP
);