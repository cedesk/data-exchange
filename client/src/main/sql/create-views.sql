## --------------------------------------------------------

CREATE OR REPLACE VIEW `modelnode` AS
    SELECT `systemmodel`.`id`   AS `id`,
           `systemmodel`.`name` AS `node_name`,
           `systemmodel`.`id`   AS `sys_id`,
           `systemmodel`.`name` AS `sys_name`,
           1                    AS `lvl`
    FROM `systemmodel`
    UNION
    SELECT `su`.`id`   AS `id`,
           `su`.`name` AS `node_name`,
           `sy`.`id`   AS `sys_id`,
           `sy`.`name` AS `sys_name`,
           2           AS `lvl`
    FROM (`subsystemmodel` `su`
             JOIN `systemmodel` `sy` ON ((`su`.`parent_id` = `sy`.`id`)))
    UNION
    SELECT `e1`.`id`   AS `id`,
           `e1`.`name` AS `node_name`,
           `sy`.`id`   AS `sys_id`,
           `sy`.`name` AS `sys_name`,
           3           AS `lvl`
    FROM ((`elementmodel` `e1`
        JOIN `systemmodel` `su1` ON ((`e1`.`parent_id` = `su1`.`id`)))
             JOIN `systemmodel` `sy` ON ((`su1`.`parent_id` = `sy`.`id`)))
    UNION
    SELECT `i`.`id`    AS `id`,
           `i`.`name`  AS `node_name`,
           `sy`.`id`   AS `sys_id`,
           `sy`.`name` AS `sys_name`,
           4           AS `lvl`
    FROM (((`instrumentmodel` `i`
        JOIN `elementmodel` `e2` ON ((`i`.`parent_id` = `e2`.`id`)))
        JOIN `systemmodel` `su2` ON ((`e2`.`parent_id` = `su2`.`id`)))
             JOIN `systemmodel` `sy` ON ((`su2`.`parent_id` = `sy`.`id`)))
;

# this ignores parameters which were associated to nodes that were deleted
CREATE OR REPLACE VIEW `parameter_changes` AS
SELECT `pa`.`id`                                       AS `param_id`,
       `pa`.`description`                              AS `description`,
       `pa`.`exportField`                              AS `exportField`,
       `pa`.`importField`                              AS `importField`,
       `pa`.`isExported`                               AS `isExported`,
       `pa`.`isReferenceValueOverridden`               AS `isReferenceValueOverridden`,
       `pa`.`lastModification`                         AS `lastModification`,
       `pa`.`name`                                     AS `name`,
       `pa`.`nature`                                   AS `nature`,
       `pa`.`overrideValue`                            AS `overrideValue`,
       `pa`.`uuid`                                     AS `uuid`,
       `pa`.`value`                                    AS `value`,
       `pa`.`valueSource`                              AS `valueSource`,
       `pa`.`calculation_id`                           AS `calculation_id`,
       `pa`.`exportModel_id`                           AS `exportModel_id`,
       `pa`.`importModel_id`                           AS `importModel_id`,
       `pa`.`parent_id`                                AS `parent_id`,
       `pa`.`unit_id`                                  AS `unit_id`,
       `pa`.`valueLink_id`                             AS `valueLink_id`,
       `ri`.`id`                                       AS `rev_id`,
       `ri`.`timestamp`                                AS `timestamp`,
       LOWER(`ri`.`username`)                          AS `username`,
       FROM_UNIXTIME((`ri`.`timestamp` / 1000))        AS `timestamp_h`,
       FROM_UNIXTIME((`pa`.`lastModification` / 1000)) AS `lastModification_h`,
       (CASE `pa`.`REVTYPE`
            WHEN 0 THEN 'create'
            WHEN 1 THEN 'update'
            WHEN 2 THEN 'delete'
           END)                                        AS `change_type`,
       (CASE `pa`.`valueSource`
            WHEN 0 THEN 'manual'
            WHEN 1 THEN 'link'
            WHEN 2 THEN 'calc'
            WHEN 3 THEN 'import'
           END)                                        AS `value_source`,
       `mn`.`id`                                       AS `node_id`,
       `mn`.`node_name`                                AS `node_name`,
       `mn`.`sys_id`                                   AS `sys_id`,
       `mn`.`sys_name`                                 AS `sys_name`,
       `mn`.`lvl`                                      AS `lvl`
FROM (((`revchanges` `rc`
    JOIN `revinfo` `ri` ON ((`ri`.`id` = `rc`.`REV`)))
    JOIN `parametermodel_aud` `pa` ON ((`ri`.`id` = `pa`.`REV`)))
         JOIN `modelnode` `mn` ON ((`pa`.`parent_id` = `mn`.`id`)))
WHERE (`rc`.`ENTITYNAME` = 'ru.skoltech.cedl.dataexchange.entity.ParameterModel')
ORDER BY `mn`.`sys_id`, `mn`.`id`, `pa`.`id`, `ri`.`timestamp`;


CREATE OR REPLACE VIEW `overall_study_statistics` AS
SELECT study.id                     AS study_id,
       study.`name`                 AS study_name,
       sys_name,
       sys_id,
       MIN(timestamp_h)             AS first_change,
       MAX(timestamp_h)             AS last_change,
       COUNT(1)                     AS changes,
       COUNT(DISTINCT param_id)     AS parameters,
       COUNT(DISTINCT node_id)      AS nodes,
       COUNT(DISTINCT username)     AS users,
       GROUP_CONCAT(DISTINCT username
                    ORDER BY `parameter_changes`.`username` ASC
                    SEPARATOR ', ') AS usernames
FROM parameter_changes
         JOIN
     study ON study.systemModel_id = parameter_changes.sys_id
GROUP BY sys_id, sys_name, study_id, study_name;
