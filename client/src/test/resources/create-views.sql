/*--------------------------------------------------------*/

CREATE VIEW modelnode AS
  SELECT
    id,
    name AS node_name,
    id   AS sys_id,
    name AS sys_name
  FROM
    systemmodel
  UNION SELECT
          su.id,
          su.name AS node_name,
          sy.id   AS sys_id,
          sy.name AS sys_name
        FROM
          (subsystemmodel su
            JOIN systemmodel sy ON su.parent_id = sy.id)
  UNION SELECT
          e1.id,
          e1.name AS node_name,
          sy.id   AS sys_id,
          sy.name AS sys_name
        FROM
          (elementmodel e1
            JOIN systemmodel su1 ON e1.parent_id = su1.id
            JOIN systemmodel sy ON su1.parent_id = sy.id)
  UNION SELECT
          i.id,
          i.name  AS node_name,
          sy.id   AS sys_id,
          sy.name AS sys_name
        FROM
          (instrumentmodel i
            JOIN elementmodel e2 ON i.parent_id = e2.id
            JOIN systemmodel su2 ON e2.parent_id = su2.id
            JOIN systemmodel sy ON su2.parent_id = sy.id);

/*this ignores parameters which were associated to nodes that were deleted*/
CREATE VIEW parameter_changes AS
  SELECT
    /*all parameter attributes*/
    pa.id                              AS param_id,
    pa.description,
    pa.exportField,
    pa.importField,
    pa.isExported,
    pa.isReferenceValueOverridden,
    pa.lastModification,
    pa.name,
    pa.nature,
    pa.overrideValue,
    pa.uuid,
    pa.value,
    pa.valueSource,
    pa.calculation_id,
    pa.exportModel_id,
    pa.importModel_id,
    pa.parent_id,
    pa.unit_id,
    pa.valueLink_id,
    /*revision attributes*/
    ri.id                              AS rev_id,
    ri."TIMESTAMP",
    ri.username,
    /*attributes human readable*/
    TIMESTAMP("TIMESTAMP" / 1000)      AS timestamp_h,
    TIMESTAMP(lastModification / 1000) AS lastModification_h,
    CASE pa.valueSource
    WHEN 0
      THEN 'manual'
    WHEN 1
      THEN 'link'
    WHEN 2
      THEN 'calc'
    WHEN 3
      THEN 'import' END                AS value_source,
    /*belonging model node information*/
    mn.id                              AS node_id,
    mn.node_name,
    mn.sys_id,
    mn.sys_name
  FROM
    revchanges rc
    JOIN
    REVINFO ri ON ri.id = rc.REV
    JOIN
    parametermodel_aud pa ON ri.id = pa.REV
    JOIN
    modelnode mn ON pa.parent_id = mn.id
  WHERE
    rc.ENTITYNAME = 'ru.skoltech.cedl.dataexchange.structure.model.ParameterModel'
  ORDER BY sys_id, node_id, param_id, "TIMESTAMP";


CREATE VIEW overall_study_statistics AS
  SELECT
    study.id                                       AS study_id,
    study.name                                     AS study_name,
    sys_name,
    sys_id,
    MIN(timestamp_h)                               AS first_change,
    MAX(timestamp_h)                               AS last_change,
    COUNT(1)                                       AS changes,
    COUNT(DISTINCT param_id)                       AS parameters,
    COUNT(DISTINCT node_id)                        AS nodes,
    COUNT(DISTINCT username)                       AS users,
    GROUP_CONCAT(DISTINCT username SEPARATOR ', ') AS usernames
  FROM
    parameter_changes
    JOIN
    study ON study.systemModel_id = parameter_changes.sys_id
  GROUP BY sys_id, sys_name, study_id, study_name;
