CREATE
USER cqrs WITH PASSWORD 'cqrs';
CREATE
DATABASE cqrs
  WITH 
  OWNER = cqrs
  ENCODING = 'UTF8'
  LC_COLLATE = 'en_US.utf8'
  LC_CTYPE = 'en_US.utf8'
  TABLESPACE = pg_default
  CONNECTION LIMIT = -1;
CREATE
EXTENSION IF NOT EXISTS "uuid-ossp";
SET
statement_timeout = 120000;