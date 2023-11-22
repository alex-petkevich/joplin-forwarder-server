CREATE TABLE mails
(
    `id`        INT PRIMARY KEY AUTO_INCREMENT,
    `user_id`   INT NOT NULL,
    `rule_id`   INT NOT NULL,  -- from RULES table
    `text`      TEXT,
    `subject`   VARCHAR(255),
    `sender`    VARCHAR(255),
    `recipient` VARCHAR(255),
    `attachments` VARCHAR(255),
    `message_id`    VARCHAR(255),   -- message id from email headers (Message-ID if exists)
    `converted`  TINYINT,                  -- is email already stored in Joplin db
    `received`  TINYINT,           -- mail date
    `added_at`  TINYINT           -- saved date in the table
);