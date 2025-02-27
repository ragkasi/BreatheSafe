CREATE DATABASE locker_db;
CREATE USER 'locker_user'@'%' IDENTIFIED BY 'password';
GRANT ALL PRIVILEGES ON locker_db.* TO 'locker_user'@'%';
