-- 6
-- CCRelationalExpressionOperatorE
-- RelationalOperatorExchanger with ChainedSupplier with CheckConstraintSupplier and ChainedSupplier with CheckExpressionSupplier and ChainedSupplier with RelationalExpressionSupplier and RelationalExpressionOperatorSupplier - > replaced with <=

CREATE TABLE "STAFF" (
	"SALARY"	INT,
	"EMPNAME"	CHAR(20),
	"GRADE"	DECIMAL,
	"EMPNUM"	CHAR(3)	PRIMARY KEY	NOT NULL
)

CREATE TABLE "TEST12649" (
	"TNUM1"	NUMERIC(5, 0)	NOT NULL,
	"TNUM2"	NUMERIC(5, 0)	NOT NULL,
	"TCHAR"	CHAR(3)	CONSTRAINT "CND12649C"  REFERENCES "STAFF" ("EMPNUM"),
	CONSTRAINT "CND12649A" PRIMARY KEY ("TNUM1", "TNUM2"),
	CONSTRAINT "CND12649B" CHECK ("TNUM2" <= 0)
)

