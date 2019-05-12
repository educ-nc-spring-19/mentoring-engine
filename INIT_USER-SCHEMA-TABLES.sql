-- create educ_nc_spring_19 database as user 'postgres'
-- CREATE DATABASE educ_nc_spring_19;
-- you must connect to 'educ_nc_spring_19' DB first and after it execute next statements

-- create user
CREATE USER mentoring_engine WITH PASSWORD 'mentoring_engine';
-- create schema
CREATE SCHEMA IF NOT EXISTS AUTHORIZATION mentoring_engine;

-- BEGIN CREATE TABLES

-- BEGIN CREATE TABLE mentoring_engine.cauldron
CREATE TABLE mentoring_engine.cauldron (
    id uuid PRIMARY KEY,
    name character varying(255) COLLATE pg_catalog."default",
    description character varying(255) COLLATE pg_catalog."default",
    created_date timestamp with time zone,
    created_by_user_id uuid,
    updated_date timestamp with time zone,
    updated_by_user_id uuid
) WITH (
    OIDS = FALSE
) TABLESPACE pg_default;

ALTER TABLE mentoring_engine.cauldron OWNER to mentoring_engine;
-- END CREATE TABLE mentoring_engine.cauldron

-- BEGIN CREATE TABLE mentoring_engine.cauldron_mentor
CREATE TABLE mentoring_engine.cauldron_mentor (
    cauldron_id uuid NOT NULL,
    mentor_id uuid NOT NULL UNIQUE,
    CONSTRAINT fk_cauldron_id FOREIGN KEY (cauldron_id)
        REFERENCES mentoring_engine.cauldron (id) MATCH SIMPLE
        ON UPDATE CASCADE
        ON DELETE CASCADE
) WITH (
    OIDS = FALSE
) TABLESPACE pg_default;

ALTER TABLE mentoring_engine.cauldron_mentor OWNER to mentoring_engine;
-- END CREATE TABLE mentoring_engine.cauldron_mentor

-- BEGIN CREATE TABLE mentoring_engine.cauldron_student
CREATE TABLE mentoring_engine.cauldron_student (
    cauldron_id uuid NOT NULL,
    student_id uuid UNIQUE NOT NULL,
    CONSTRAINT fk_cauldron_id FOREIGN KEY (cauldron_id)
        REFERENCES mentoring_engine.cauldron (id) MATCH SIMPLE
        ON UPDATE CASCADE
        ON DELETE CASCADE
) WITH (
    OIDS = FALSE
) TABLESPACE pg_default;

ALTER TABLE mentoring_engine.cauldron_student OWNER to mentoring_engine;
-- END CREATE TABLE mentoring_engine.cauldron_student

-- BEGIN CREATE TABLE mentoring_engine.stage
CREATE TABLE mentoring_engine.stage (
    id uuid PRIMARY KEY,
    name character varying(255) COLLATE pg_catalog."default",
    description character varying(255) COLLATE pg_catalog."default",
    stage_type character varying(255) COLLATE pg_catalog."default" UNIQUE NOT NULL,
    deadline timestamp with time zone,
    created_date timestamp with time zone,
    created_by_user_id uuid,
    updated_date timestamp with time zone,
    updated_by_user_id uuid
) WITH (
    OIDS = FALSE
) TABLESPACE pg_default;

ALTER TABLE mentoring_engine.stage OWNER to mentoring_engine;
-- END CREATE TABLE mentoring_engine.stage

-- BEGIN CREATE TABLE mentoring_engine.spr_group
CREATE TABLE mentoring_engine.spr_group (
    id uuid PRIMARY KEY,
    name character varying(255) COLLATE pg_catalog."default",
    mentor_id uuid,
    backup_id uuid,
    stage_id uuid NOT NULL,
    first_meeting_date timestamp with time zone,
    created_date timestamp with time zone,
    created_by_user_id uuid,
    updated_date timestamp with time zone,
    updated_by_user_id uuid,
    CONSTRAINT fk_stage_id FOREIGN KEY (stage_id)
        REFERENCES mentoring_engine.stage (id) MATCH SIMPLE
        ON UPDATE CASCADE
        ON DELETE NO ACTION
) WITH (
    OIDS = FALSE
) TABLESPACE pg_default;

ALTER TABLE mentoring_engine.spr_group OWNER to mentoring_engine;
-- END CREATE TABLE mentoring_engine.spr_group

-- BEGIN CREATE TABLE mentoring_engine.group_student
CREATE TABLE mentoring_engine.group_student (
    group_id uuid NOT NULL,
    student_id uuid UNIQUE NOT NULL,
    status character varying(255) COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT fk_group_id FOREIGN KEY (group_id)
        REFERENCES mentoring_engine.spr_group (id) MATCH SIMPLE
        ON UPDATE CASCADE
        ON DELETE CASCADE
) WITH (
    OIDS = FALSE
) TABLESPACE pg_default;

ALTER TABLE mentoring_engine.group_student OWNER to mentoring_engine;
-- END CREATE TABLE mentoring_engine.group_student

-- BEGIN CREATE TABLE mentoring_engine.group_meeting
CREATE TABLE mentoring_engine.group_meeting (
    group_id uuid NOT NULL,
    day character varying(255) COLLATE pg_catalog."default" NOT NULL,
    day_time time with time zone NOT NULL,
    CONSTRAINT group_meeting_group_id_day UNIQUE (group_id, day),
    CONSTRAINT fk_group_id FOREIGN KEY (group_id)
        REFERENCES mentoring_engine.spr_group (id) MATCH SIMPLE
        ON UPDATE CASCADE
        ON DELETE CASCADE
) WITH (
    OIDS = FALSE
) TABLESPACE pg_default;

ALTER TABLE mentoring_engine.group_meeting OWNER to mentoring_engine;
-- END CREATE TABLE mentoring_engine.group_meeting

-- BEGIN CREATE TABLE mentoring_engine.pool
CREATE TABLE mentoring_engine.pool (
    id uuid PRIMARY KEY,
    direction_id uuid UNIQUE NOT NULL,
    created_date timestamp with time zone,
    created_by_user_id uuid,
    updated_date timestamp with time zone,
    updated_by_user_id uuid
) WITH (
    OIDS = FALSE
) TABLESPACE pg_default;

ALTER TABLE mentoring_engine.pool OWNER to mentoring_engine;
-- END CREATE TABLE mentoring_engine.pool

-- BEGIN CREATE TABLE mentoring_engine.pool_student
CREATE TABLE mentoring_engine.pool_student (
    pool_id uuid NOT NULL,
    student_id uuid UNIQUE NOT NULL,
    CONSTRAINT fk_pool_id FOREIGN KEY (pool_id)
        REFERENCES mentoring_engine.pool (id) MATCH SIMPLE
        ON UPDATE CASCADE
        ON DELETE CASCADE
) WITH (
    OIDS = FALSE
) TABLESPACE pg_default;

ALTER TABLE mentoring_engine.pool_student OWNER to mentoring_engine;
-- END CREATE TABLE mentoring_engine.pool_student

-- BEGIN CREATE TABLE mentoring_engine.lecture
CREATE TABLE mentoring_engine.lecture (
    id uuid PRIMARY KEY,
    direction_id uuid UNIQUE NOT NULL,
    first_lecture timestamp with time zone,
    created_date timestamp with time zone,
    created_by_user_id uuid,
    updated_date timestamp with time zone,
    updated_by_user_id uuid
) WITH (
    OIDS = FALSE
) TABLESPACE pg_default;

ALTER TABLE mentoring_engine.lecture OWNER to mentoring_engine;
-- END CREATE TABLE mentoring_engine.lecture

-- BEGIN CREATE TABLE mentoring_engine.lecture_day
CREATE TABLE mentoring_engine.lecture_day (
    dir_lec_id uuid NOT NULL,
    day character varying(255) COLLATE pg_catalog."default" NOT NULL,
    day_time time with time zone NOT NULL,
    CONSTRAINT lecture_day_dir_lec_id_day UNIQUE (dir_lec_id, day),
    CONSTRAINT fk_dir_lec_id FOREIGN KEY (dir_lec_id)
        REFERENCES mentoring_engine.lecture (id) MATCH SIMPLE
        ON UPDATE CASCADE
        ON DELETE CASCADE
) WITH (
    OIDS = FALSE
) TABLESPACE pg_default;

ALTER TABLE mentoring_engine.lecture_day OWNER to mentoring_engine;
-- END CREATE TABLE mentoring_engine.lecture_day

-- END CREATE TABLES