CREATE TABLE record_info (
	id serial PRIMARY KEY,
	guid char(36) NOT null references record(guid) on delete cascade,
	creation_date timestamp NOT NULL DEFAULT now(),
	description TEXT
);
