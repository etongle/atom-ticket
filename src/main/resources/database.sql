/**
 * 创建数据库
 */
CREATE DATABASE mplat DEFAULT CHARSET=UTF8;

GRANT ALL PRIVILEGES ON mplat.* TO 'mplat'@'%' IDENTIFIED BY 'mplat';
GRANT ALL PRIVILEGES ON mplat.* TO 'mplat'@'localhost' IDENTIFIED BY 'mplat';

drop table if exists adm_mutex_ticket;
CREATE TABLE adm_mutex_ticket (
    name varchar(64) primary key,
    value bigint,
    stamp bigint
)Engine=InnoDB DEFAULT CHARSET=UTF8;

INSERT INTO adm_mutex_ticket VALUES('USER-INFO-ID', 0, 0);
