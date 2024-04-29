ALTER TABLE rules_conditions ADD `condition` TINYINT(1) AFTER rule_id; -- (1 - AND, 0 - OR)
