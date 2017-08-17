DROP PROCEDURE IF EXISTS populate_audit;
DELIMITER //
CREATE PROCEDURE populate_audit (IN entityname CHAR(100), IN entity CHAR(100))
  BEGIN
    DECLARE tstmp LONG DEFAULT 1502883794633;
    DECLARE clmns CHAR(255);
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
      BEGIN
        DROP TABLE IF EXISTS temp1;
        DROP TABLE IF EXISTS temp2;
      END;

    SET clmns = (SELECT group_concat(column_name order by ordinal_position)
    FROM information_schema.columns
    WHERE table_schema = 'cedesk_repo' and table_name = entityname AND column_name NOT IN ('id', 'version', 'um_id'));

    IF clmns != '' THEN
      SET @delete_entity_aud = CONCAT('DELETE FROM ', entityname, '_aud');
      SET @create_temp2 = CONCAT('CREATE TABLE temp2 AS SELECT id, @rev := @rev + 1 REV, 1 REVTYPE, ', clmns, ' FROM ', entityname);
      SET @insert_entity_aud = CONCAT('INSERT INTO ', entityname, '_aud(id, REV, REVTYPE, ', clmns, ') SELECT * FROM temp2');
    ELSE
      SET @delete_entity_aud = CONCAT('DELETE FROM ', entityname, '_aud');
      SET @create_temp2 = CONCAT('CREATE TABLE temp2 AS SELECT id, @rev := @rev + 1 REV, 1 REVTYPE FROM ', entityname);
      SET @insert_entity_aud = CONCAT('INSERT INTO ', entityname, '_aud(id, REV, REVTYPE) SELECT * FROM temp2');
    END IF;

    CREATE TABLE temp1 AS SELECT REV FROM revchanges WHERE ENTITYNAME = entity;

    PREPARE stmt FROM @delete_entity_aud; EXECUTE stmt; DEALLOCATE PREPARE stmt;
    DELETE FROM revchanges WHERE REV in (SELECT REV FROM temp1);
    DELETE FROM revinfo WHERE id in (SELECT REV FROM temp1);

    DROP TABLE temp1;

    SET @rev = (SELECT MAX(ID) FROM revinfo);
    PREPARE stmt FROM @create_temp2; EXECUTE stmt; DEALLOCATE PREPARE stmt;

    INSERT INTO revinfo(id, timestamp, username, tag) SELECT REV, tstmp, 'admin', null FROM temp2;
    INSERT INTO revchanges(REV, ENTITYNAME) SELECT REV, entity FROM temp2;
    PREPARE stmt FROM @insert_entity_aud; EXECUTE stmt; DEALLOCATE PREPARE stmt;

    DROP TABLE temp2;
  END //
DELIMITER ;

CALL populate_audit('InstrumentModel', 'ru.skoltech.cedl.dataexchange.entity.model.InstrumentModel');
CALL populate_audit('ElementModel', 'ru.skoltech.cedl.dataexchange.entity.model.ElementModel');
CALL populate_audit('SubsystemModel', 'ru.skoltech.cedl.dataexchange.entity.model.SubSystemModel');
CALL populate_audit('Discipline', 'ru.skoltech.cedl.dataexchange.entity.user.Discipline');
CALL populate_audit('DisciplineSubsystem', 'ru.skoltech.cedl.dataexchange.entity.user.UserRoleManagement');
CALL populate_audit('User', 'ru.skoltech.cedl.dataexchange.entity.user.User');
CALL populate_audit('UserDiscipline', 'ru.skoltech.cedl.dataexchange.entity.user.UserDiscipline');
CALL populate_audit('Usermanagement', 'ru.skoltech.cedl.dataexchange.entity.user.UserManagement');
CALL populate_audit('UserRoleManagement', 'ru.skoltech.cedl.dataexchange.entity.user.UserRoleManagement');
CALL populate_audit('SystemModel', 'ru.skoltech.cedl.dataexchange.entity.model.SystemModel');
CALL populate_audit('StudySettings', 'ru.skoltech.cedl.dataexchange.entity.StudySettings');
CALL populate_audit('Study', 'ru.skoltech.cedl.dataexchange.entity.Study');

UPDATE hibernate_sequence SET next_val = (SELECT MAX(id) + 1 FROM revinfo);
