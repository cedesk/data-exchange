CREATE SCHEMA `cedesk_repo` DEFAULT CHARACTER SET utf8;
CREATE USER 'cedesk'@'%' IDENTIFIED BY 'cedesk';
GRANT ALL PRIVILEGES ON 'cedesk_repo'. * TO 'cedesk'@'%';