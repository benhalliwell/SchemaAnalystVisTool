-- 43
-- UCColumnA
-- Added UNIQUE to column name in table Residence

CREATE TABLE "Residence" (
	"name"	VARCHAR(50)	PRIMARY KEY	UNIQUE	NOT NULL,
	"capacity"	INT	NOT NULL,
	CHECK ("capacity" > 1),
	CHECK ("capacity" <= 10)
)

CREATE TABLE "Student" (
	"id"	INT	PRIMARY KEY,
	"firstName"	VARCHAR(50),
	"lastName"	VARCHAR(50),
	"residence"	VARCHAR(50)	 REFERENCES "Residence" ("name"),
	CHECK ("id" >= 0)
)

