-- create educ_nc_spring_19 database as user 'postgres'
-- CREATE DATABASE educ_nc_spring_19;
-- you must connect to 'educ_nc_spring_19' DB first and after it execute next statements

-- create user
CREATE USER stud_spreading_service WITH PASSWORD 'stud_spreading_service';
-- create schema
CREATE SCHEMA IF NOT EXISTS AUTHORIZATION stud_spreading_service;

-- BEGIN CREATE TABLES

-- BEGIN CREATE TABLE stud_spreading_service.cauldron
CREATE TABLE stud_spreading_service.cauldron (
    id uuid NOT NULL,
    name character varying(255) COLLATE pg_catalog."default",
    description character varying(255) COLLATE pg_catalog."default",
    created_date timestamp with time zone,
    created_by_user_id uuid,
    updated_date timestamp with time zone,
    updated_by_user_id uuid,
    CONSTRAINT cauldron_pkey PRIMARY KEY (id)
) WITH (
    OIDS = FALSE
) TABLESPACE pg_default;

ALTER TABLE stud_spreading_service.cauldron OWNER to stud_spreading_service;
-- END CREATE TABLE stud_spreading_service.cauldron

-- BEGIN CREATE TABLE stud_spreading_service.cauldron_mentor
CREATE TABLE stud_spreading_service.cauldron_mentor (
    cauldron_id uuid NOT NULL,
    mentor_id uuid NOT NULL,
    CONSTRAINT fkhum2ti216la6y5uqmilf9wa9b FOREIGN KEY (cauldron_id)
        REFERENCES stud_spreading_service.cauldron (id) MATCH SIMPLE
        ON UPDATE CASCADE
        ON DELETE CASCADE
) WITH (
    OIDS = FALSE
) TABLESPACE pg_default;

ALTER TABLE stud_spreading_service.cauldron_mentor OWNER to stud_spreading_service;
-- END CREATE TABLE stud_spreading_service.cauldron_mentor

-- BEGIN CREATE TABLE stud_spreading_service.stage
CREATE TABLE stud_spreading_service.stage (
    id uuid NOT NULL,
    name character varying(255) COLLATE pg_catalog."default",
    description character varying(255) COLLATE pg_catalog."default",
    deadline timestamp with time zone,
    created_date timestamp with time zone,
    created_by_user_id uuid,
    updated_date timestamp with time zone,
    updated_by_user_id uuid,
    CONSTRAINT stage_pkey PRIMARY KEY (id)
) WITH (
    OIDS = FALSE
) TABLESPACE pg_default;

ALTER TABLE stud_spreading_service.stage OWNER to stud_spreading_service;
-- END CREATE TABLE stud_spreading_service.stage

-- BEGIN CREATE TABLE stud_spreading_service.spr_group
CREATE TABLE stud_spreading_service.spr_group (
    id uuid NOT NULL,
    name character varying(255) COLLATE pg_catalog."default",
    mentor_id uuid,
    backup_id uuid,
    stage_id uuid NOT NULL,
    created_date timestamp with time zone,
    created_by_user_id uuid,
    updated_date timestamp with time zone,
    updated_by_user_id uuid,
    CONSTRAINT spr_group_pkey PRIMARY KEY (id),
    CONSTRAINT fklhgtah7khf966hqoil7iplucy FOREIGN KEY (stage_id)
        REFERENCES stud_spreading_service.stage (id) MATCH SIMPLE
        ON UPDATE CASCADE
        ON DELETE NO ACTION
) WITH (
    OIDS = FALSE
) TABLESPACE pg_default;

ALTER TABLE stud_spreading_service.spr_group OWNER to stud_spreading_service;
-- END CREATE TABLE stud_spreading_service.spr_group

-- BEGIN CREATE TABLE stud_spreading_service.group_student
CREATE TABLE stud_spreading_service.group_student (
    group_id uuid NOT NULL,
    student_id uuid NOT NULL,
    CONSTRAINT fki3d7cedchue50kj46ihgr8cwg FOREIGN KEY (group_id)
        REFERENCES stud_spreading_service.spr_group (id) MATCH SIMPLE
        ON UPDATE CASCADE
        ON DELETE CASCADE
) WITH (
    OIDS = FALSE
) TABLESPACE pg_default;

ALTER TABLE stud_spreading_service.group_student OWNER to stud_spreading_service;
-- END CREATE TABLE stud_spreading_service.group_student

-- BEGIN CREATE TABLE stud_spreading_service.pool
CREATE TABLE stud_spreading_service.pool (
    id uuid NOT NULL,
    CONSTRAINT pool_pkey PRIMARY KEY (id)
) WITH (
    OIDS = FALSE
) TABLESPACE pg_default;

ALTER TABLE stud_spreading_service.pool OWNER to stud_spreading_service;
-- END CREATE TABLE stud_spreading_service.pool

-- BEGIN CREATE TABLE stud_spreading_service.pool_student
CREATE TABLE stud_spreading_service.pool_student (
    pool_id uuid NOT NULL,
    student_id uuid NOT NULL,
    CONSTRAINT fkv0qajsym99p13j7ot1ng8470 FOREIGN KEY (pool_id)
        REFERENCES stud_spreading_service.pool (id) MATCH SIMPLE
        ON UPDATE CASCADE
        ON DELETE CASCADE
) WITH (
    OIDS = FALSE
) TABLESPACE pg_default;

ALTER TABLE stud_spreading_service.pool_student OWNER to stud_spreading_service;
-- END CREATE TABLE stud_spreading_service.pool_student

-- END CREATE TABLES