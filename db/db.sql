BEGIN;

-- Create Database
CREATE DATABASE cqrs;

-- Write Models: Entity, Attribute and Value tables.
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

-- Queue Model.
CREATE TABLE queue
(
    id          UUID        PRIMARY KEY,
    title       TEXT        NOT NULL,
    attributes  JSONB       NOT NULL,
    status      VARCHAR(32) NOT NULL,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    modified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Read Model: Stores all models based data, collected with combined attributes.
CREATE TABLE entity_record
(
    id          UUID        PRIMARY KEY,
    label       VARCHAR(64) NOT NULL,
    attributes  JSONB       NOT NULL,
    created_at  TIMESTAMP   DEFAULT CURRENT_TIMESTAMP NOT NULL,
    modified_at TIMESTAMP   DEFAULT CURRENT_TIMESTAMP NOT NULL
);

END;