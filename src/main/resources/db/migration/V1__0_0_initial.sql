CREATE TABLE IF NOT EXISTS `roles`
(
	`id`   INT PRIMARY KEY AUTO_INCREMENT,
	`name` VARCHAR(255)
);

insert into roles (id, name) values (1, 'ROLE_ADMIN');
insert into roles (id, name) values (2, 'ROLE_MODERATOR');
insert into roles (id, name) values (3, 'ROLE_USER');

CREATE TABLE IF NOT EXISTS `users`
(
    `id`       INT PRIMARY KEY AUTO_INCREMENT,
    `username` VARCHAR(255),
    `firstname` VARCHAR(255),
    `lastname` VARCHAR(255),
    `email`    VARCHAR(255),
    `password` VARCHAR(30),
    `image` VARCHAR(255),
    `lang` VARCHAR(10),
    `activation_key` VARCHAR(255),
    `active`   INT default 0,
    `created_at`  INT,
    `last_modified_at`  INT
);

CREATE TABLE IF NOT EXISTS user_roles
(
	user_id INT NOT NULL,
	role_id INT NOT NULL
);

insert into users (id, username, email, password, active, created_at) values (1, 'admin', 'admin@local', '$2a$10$p9TRp2W3W4Hf4L6FrBXmAeMgJXKODbtCng97kG4GTuDuR6lTemsLy', 1, datetime('now')); -- adminadminadmin

insert into user_roles values (1, 1);

