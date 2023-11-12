CREATE TABLE IF NOT EXISTS `roles`
(
	`id`   INTEGER PRIMARY KEY AUTOINCREMENT,
	`name` TEXT
);

insert into roles (id, name) values (1, 'ROLE_ADMIN');
insert into roles (id, name) values (2, 'ROLE_MODERATOR');
insert into roles (id, name) values (3, 'ROLE_USER');

CREATE TABLE IF NOT EXISTS `users`
(
	`id`       INTEGER PRIMARY KEY AUTOINCREMENT,
	`username` TEXT,
	`firstname` TEXT,
	`lastname` TEXT,
	`email`    TEXT,
	`password` TEXT,
	`image` TEXT,
	`lang` TEXT,
	`activation_key` TEXT,
	'active'   INTEGER default 0,
	'created_at'  INTEGER,
	'last_modified_at'  INTEGER
);

CREATE TABLE IF NOT EXISTS user_roles
(
	user_id INTEGER NOT NULL,
	role_id INTEGER NOT NULL
);

insert into users (id, username, email, password, active, created_at) values (1, 'admin', 'admin@local', '$2a$10$p9TRp2W3W4Hf4L6FrBXmAeMgJXKODbtCng97kG4GTuDuR6lTemsLy', 1, datetime('now')); -- adminadminadmin

insert into user_roles values (1, 1);

