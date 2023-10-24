ALTER TABLE mails DROP `processed`;

ALTER TABLE mails ADD `processed_id` TEXT;
