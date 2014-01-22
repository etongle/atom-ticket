/**
 * 创建数据库
 */
CREATE DATABASE mplat DEFAULT CHARSET=UTF8;

GRANT ALL PRIVILEGES ON mplat.* TO 'mplat'@'%' IDENTIFIED BY 'mplat';
GRANT ALL PRIVILEGES ON mplat.* TO 'mplat'@'localhost' IDENTIFIED BY 'mplat';

/**
 * V2.0.1
 */
DROP TABLE IF EXISTS adm_mutex_ticket;
CREATE TABLE adm_mutex_ticket (
    name VARCHAR(64) PRIMARY KEY,
    varsion BIGINT,
    step BIGINT,
    value BIGINT,
    minv BIGINT,
    maxv BIGINT,
    cycle VARCHAR(16)
) Engine=InnoDB DEFAULT CHARSET=UTF8;

INSERT INTO adm_mutex_ticket VALUES('USER-INFO-ID', 1, 10, 0, 1, 9999999999, 'TRUE');

/**
 * 1.0.1 upgrade to 2.0.1
 */
ALTER TABLE adm_mutex_ticket
DROP COLUMN stamp,
ADD COLUMN varsion  bigint(20) NULL DEFAULT 1 AFTER name,
ADD COLUMN step  bigint(20) NULL DEFAULT 10 AFTER varsion,
ADD COLUMN minv  bigint(20) NULL DEFAULT 1 AFTER value,
ADD COLUMN maxv  bigint(20) NULL DEFAULT 9999999999 AFTER minv,
ADD COLUMN cycle  varchar(16) NULL DEFAULT 'TRUE' AFTER maxv;

/**
 * V1.0.1
 */
DROP TABLE IF EXISTS adm_mutex_ticket;
CREATE TABLE adm_mutex_ticket (
    name VARCHAR(64) primary key,
    value BIGINT,
    stamp BIGINT
) Engine=InnoDB DEFAULT CHARSET=UTF8;

INSERT INTO adm_mutex_ticket VALUES('USER-INFO-ID', 0, 0);
