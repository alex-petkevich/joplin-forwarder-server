CREATE TABLE mails
(
    `id`        INTEGER PRIMARY KEY AUTOINCREMENT,
    `user_id`   INTEGER NOT NULL,
    `rule_id`   INTEGER NOT NULL,  -- from RULES table
    `text`      TEXT,
    `subject`   TEXT,
    `sender`    TEXT,
    `recipient` TEXT,
    `attachments` TEXT,
    `message_id`    TEXT,   -- message id from email headers (Message-ID if exists)
    `converted`                    -- is email already stored in Joplin db
    `received`  INTEGER,           -- mail date
    `added_at`  INTEGER           -- saved date in the table
);