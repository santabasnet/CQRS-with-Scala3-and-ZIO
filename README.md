# CQRS-with-Scala3-and-ZIO
---
The Command Query Responsibility Segregation (CQRS) design separates database operations into distinct models: the Command (Write) model for handling write operations and the Query (Read) model for managing read operations. This approach improves performance by enabling scalability, enhances security by restricting write access to designated databases, and is particularly beneficial for microservices architectures.

This work demonstrate a complete example of CQRS implementation with Scala, ZIO, Doobie and PosgreSQL. The docker-compose file is defined to load PostgreSQL db. For the project build, "mill" is used here.
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

Some Scala macros are defined for SQL queries insert/update generations. Initially it clears all the tables in the db, because it is a console application. 
``` Scala
def clearAll = (for {
    _ <- deleteFromEntityRecord
    _ <- deleteFromQueue
    _ <- deleteFromAttributeValues
    _ <- deleteFromAttributes
    _ <- deleteFromEntities
  } yield ()).transact(doobieTransactor.transactor)
```
After that, it inserts all the demo data prepared in the scope of DemoData object.
```Scala
def insertAll = for {
    _ <- insertEntities
    _ <- insertAttributes
    _ <- insertAttributeValues(DemoData.shivAttributeValues)
    _ <- insertQueueItem(DemoData.shivAttributeValues)
    _ <- insertAttributeValues(DemoData.parbatiAttributeValues)
    _ <- insertQueueItem(DemoData.parbatiAttributeValues)
  } yield ()
```
When the items are inserted in the Queue, the background process invokes to cruch it and transfers to Read models. The list of attributes and their values are transformed to Map of data labels and associated values.

#### Input from Write Model: 
```CSV
4578de57-3143-4138-a39a-ff06cf7da85a,37271618-b2d9-4a4c-b6e6-3f81e738af8a,7df7a406-aa11-4135-9f4b-ae2130afb08c,"""Shiv Kumar Bantawa""",2025-03-23 23:48:05.638498,2025-03-23 23:48:05.638543
0ed9ce5e-5fd6-4cf1-b24e-08b77435108f,37271618-b2d9-4a4c-b6e6-3f81e738af8a,b52faa52-b148-4c78-8cb8-671917c251af,"""Male""",2025-03-23 23:48:05.639142,2025-03-23 23:48:05.639162
cb1580d3-aa07-451b-9634-4fe256db7054,37271618-b2d9-4a4c-b6e6-3f81e738af8a,83a91215-5210-4d56-9f8c-cf3a21a0f856,"""Kathmandu""",2025-03-23 23:48:05.639215,2025-03-23 23:48:05.639229
5a6faa45-75b2-40e3-9c93-72b908eea0f2,37271618-b2d9-4a4c-b6e6-3f81e738af8a,c3d16165-edd7-48e9-ba5f-c62a1259ff49,"""Kalanki, Ward No. 39, Kathmandu, Nepal""",2025-03-23 23:48:05.639255,2025-03-23 23:48:05.639276
323b2d2d-6818-40fd-ba2a-53a1ab4e7175,37271618-b2d9-4a4c-b6e6-3f81e738af8a,0aebfa91-bf9b-4b40-8257-9ae6245172a5,"""shiv_bantawa@gmail.com""",2025-03-23 23:48:05.639318,2025-03-23 23:48:05.639333
33c59fb4-c5d9-4069-a795-a46dfb056992,37271618-b2d9-4a4c-b6e6-3f81e738af8a,7df7a406-aa11-4135-9f4b-ae2130afb08c,"""Parbati Kumar Shah""",2025-03-23 23:48:05.639403,2025-03-23 23:48:05.639417
38fc6806-744a-4aca-93d2-9f114afbe3bc,37271618-b2d9-4a4c-b6e6-3f81e738af8a,b52faa52-b148-4c78-8cb8-671917c251af,"""Female""",2025-03-23 23:48:05.639464,2025-03-23 23:48:05.639477
1a4068b4-7b96-4025-ae02-b7eeaa793098,37271618-b2d9-4a4c-b6e6-3f81e738af8a,83a91215-5210-4d56-9f8c-cf3a21a0f856,"""Kathmandu""",2025-03-23 23:48:05.639513,2025-03-23 23:48:05.639525
e133a418-560e-429d-ad00-74c38fe92997,37271618-b2d9-4a4c-b6e6-3f81e738af8a,c3d16165-edd7-48e9-ba5f-c62a1259ff49,"""Balkhu, Ward No. 40, Kathmandu, Nepal""",2025-03-23 23:48:05.639548,2025-03-23 23:48:05.639559
a9bbc52c-f51d-4b05-abcd-327568e8fb4e,37271618-b2d9-4a4c-b6e6-3f81e738af8a,0aebfa91-bf9b-4b40-8257-9ae6245172a5,"""parbati_shah@gmail.com""",2025-03-23 23:48:05.639596,2025-03-23 23:48:05.639619
```

#### Output to Read Model: 
```CSV
3279473f-afda-41cb-8b9f-634a5f99cdc6,Person,"{""City"": ""Kathmandu"", ""Name"": ""Shiv Kumar Bantawa"", ""Gender"": ""Male"", ""Email Address"": ""shiv_bantawa@gmail.com"", ""Contact Address"": ""Kalanki, Ward No. 39, Kathmandu, Nepal""}",2025-03-23 23:48:05.914545,2025-03-23 23:48:05.914564
32512259-b21d-47b5-b240-8883474c7d30,Person,"{""City"": ""Kathmandu"", ""Name"": ""Parbati Kumar Shah"", ""Gender"": ""Female"", ""Email Address"": ""parbati_shah@gmail.com"", ""Contact Address"": ""Balkhu, Ward No. 40, Kathmandu, Nepal""}",2025-03-23 23:48:05.942720,2025-03-23 23:48:05.942734
```

### Usage
```Scala
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

