DROP SCHEMA IF EXISTS `cedesk_prod`;
CREATE SCHEMA `cedesk_prod`
  DEFAULT CHARACTER SET utf8;

CREATE USER 'cedesk'@'%'
  IDENTIFIED BY 'cedesk';
GRANT ALL PRIVILEGES ON cedesk_prod.* TO 'cedesk'@'%';
