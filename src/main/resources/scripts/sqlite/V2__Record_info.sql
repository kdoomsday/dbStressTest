CREATE TABLE record_info (
	id INTEGER PRIMARY KEY AUTOINCREMENT,
	guid char(36) NOT NULL,
	creation_date TEXT NOT NULL DEFAULT (DATETIME('now')),
	description TEXT,
	FOREIGN KEY (guid) REFERENCES record(guid)
);
