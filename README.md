# CQRS-with-Scala3-and-ZIO
---
The Command Query Responsibility Segregation (CQRS) design separates database operations into distinct models: the Command (Write) model for handling write operations and the Query (Read) model for managing read operations. This approach improves performance by enabling scalability, enhances security by restricting write access to designated databases, and is particularly beneficial for microservices architectures.

This work demonstrate a complete example of CQRS implementation with Scala, ZIO, Doobie and PosgreSQL. 
### DataModel
```SQL

BEGIN;
-- Create Database
CREATE DATABASE cqrs;

-- Write(Command) Models: Entity, Attribute and Value tables.
CREATE TABLE entities
(
    id          UUID PRIMARY KEY,
    label       VARCHAR(255)                        NOT NULL,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    modified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);
CREATE TABLE attributes
(
    id          UUID PRIMARY KEY,
    label       VARCHAR(255)                        NOT NULL,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    modified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);
CREATE TABLE attribute_values
(
    id           UUID PRIMARY KEY,
    entity_id    UUID REFERENCES entities (id) ON DELETE RESTRICT,
    attribute_id UUID REFERENCES attributes (id) ON DELETE RESTRICT,
    value        JSONB,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    modified_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Queue Model, used for reconcile purpose in eventual consistency.
CREATE TABLE queue
(
    id          UUID        PRIMARY KEY,
    title       TEXT        NOT NULL,
    attributes  JSONB       NOT NULL,
    status      VARCHAR(32) NOT NULL,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    modified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Read(Query) Model: Stores all models based data, collected with combined attributes.
CREATE TABLE entity_record
(
    id          UUID        PRIMARY KEY,
    label       VARCHAR(64) NOT NULL,
    attributes  JSONB       NOT NULL,
    created_at  TIMESTAMP   DEFAULT CURRENT_TIMESTAMP NOT NULL,
    modified_at TIMESTAMP   DEFAULT CURRENT_TIMESTAMP NOT NULL
);
END;
```
### Usage
```Scala
import scalatags.Text.all._
import zio._
import com.iict.services._
import com.iict.model.DoobieTransactor

object Main extends ZIOAppDefault {

  def run = for {
    transactor <- DoobieService.doobieTransactor
    dataService <- EntityService.service(transactor)
    _ = println("\nClearing the data tables ...")
    _ <- dataService.clearAll

    _ = println("\nPerform Insert Operation on Demo Data ...")
    _ <- dataService.insertAll

    queuedJob <- EntityQueuedJob.service(transactor)
    _ <- queuedJob.runActivities

  } yield ()
}
```

### Compile and Run
- mill cqrs.compile
- mill cqrs.run

