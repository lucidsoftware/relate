
CREATE DATABASE relate_it_tests;
USE relate_it_tests;

#create a table with an auto incremented bigint id
CREATE TABLE pokedex (
	id BIGINT NOT NULL AUTO_INCREMENT,
	name VARCHAR(50) NOT NULL,
	description VARCHAR(200) NOT NULL,
	PRIMARY KEY (id),
	KEY name_key (name)
) ENGINE=InnoDB CHARSET=utf8 COLLATE=utf8_general_ci;

#create a table with nullable fields
CREATE TABLE pokemon (
	id BIGINT NOT NULL AUTO_INCREMENT,
	pokedex_id BIGINT NOT NULL,
	level INT NOT NULL,
	trainer_id BIGINT,
	PRIMARY KEY(id),
	KEY trainer_key (trainer_id)
) ENGINE=InnoDB CHARSET=utf8 COLLATE=utf8_general_ci;
