CREATE SCHEMA `cedesk_repo` DEFAULT CHARACTER SET utf8;
CREATE USER 'cedesk'@'%' IDENTIFIED BY 'cedesk';
GRANT ALL PRIVILEGES ON 'cedesk_repo'. * TO 'cedesk'@'%';


CREATE OR REPLACE VIEW `modelnode` AS
    SELECT
        id, `name` as node_name, id AS sys_id, `name` as sys_name
    FROM
        systemmodel
    UNION SELECT
        su.id, su.`name` as node_name, sy.id AS sys_id, sy.`name` as sys_name
    FROM
        (subsystemmodel su join systemmodel sy on su.parent_id = sy.id)
    UNION SELECT
        e1.id, e1.`name` as node_name, sy.id AS sys_id, sy.`name` as sys_name
    FROM
        (elementmodel e1
        join systemmodel su1 on e1.parent_id=su1.id
        join systemmodel sy on su1.parent_id = sy.id)
    UNION SELECT
        i.id, i.`name` as node_name, sy.id AS sys_id, sy.`name` as sys_name
    FROM
        (instrumentmodel i
        join elementmodel e2 on i.parent_id=e2.id
        join systemmodel su2 on e2.parent_id=su2.id
        join systemmodel sy on su2.parent_id=sy.id);

# this ignores parameters which were associated to nodes that were deleted
CREATE OR REPLACE VIEW `parameter_changes` AS
    SELECT
	# all parameter attributes
        pa.`id` AS param_id,
		pa.`description`,
		pa.`exportField`,
		pa.`importField`,
		pa.`isExported`,
		pa.`isReferenceValueOverridden`,
		pa.`lastModification`,
		pa.`name`,
		pa.`nature`,
		pa.`overrideValue`,
		pa.`uuid`,
		pa.`value`,
		pa.`valueSource`,
		pa.`calculation_id`,
		pa.`exportModel_id`,
		pa.`importModel_id`,
		pa.`parent_id`,
		pa.`unit_id`,
		pa.`valueLink_id`,
    # revision attributes
        ri.id AS rev_id,
        ri.`timestamp`,
        ri.`username`,
    # attributes human readable
        FROM_UNIXTIME(`timestamp` / 1000) AS `timestamp_h`,
        FROM_UNIXTIME(`lastModification` / 1000) AS `lastModification_h`,
		CASE pa.valueSource WHEN 0 THEN 'manual' WHEN 1 THEN 'link' WHEN 2 THEN 'calc' WHEN 3 THEN 'import' END as value_source,
    # belonging model node information
        mn.id as node_id,
        mn.node_name,
        mn.sys_id,
        mn.sys_name
    FROM
        revchanges rc
            JOIN
        revinfo ri ON ri.id = rc.REV
            JOIN
        parametermodel_aud pa ON ri.id = pa.REV
            JOIN
        modelnode mn ON pa.parent_id = mn.id
    WHERE
        rc.ENTITYNAME = 'ru.skoltech.cedl.dataexchange.structure.model.ParameterModel'
    ORDER BY sys_id , node_id , param_id, `timestamp`;


CREATE OR REPLACE VIEW `overall_study_statistics` AS
    SELECT
        study.id AS study_id,
        study.`name` AS study_name,
        sys_name,
        sys_id,
        MIN(timestamp_h) AS first_change,
        MAX(timestamp_h) AS last_change,
        COUNT(1) AS changes,
        COUNT(DISTINCT param_id) AS parameters,
        COUNT(DISTINCT node_id) AS nodes,
        COUNT(DISTINCT username) AS users,
        GROUP_CONCAT(DISTINCT username
            SEPARATOR ', ') AS usernames
    FROM
        parameter_changes
            JOIN
        study ON study.systemModel_id = parameter_changes.sys_id
    GROUP BY sys_id;
