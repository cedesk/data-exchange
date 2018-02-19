## --------------------------------------------------------

SELECT
  concat("RENAME TABLE ", LOWER(tr.table_name), " TO ", tr.table_name, "1, ", tr.table_name, "1 TO ", td.table_name,
         ";")
FROM
  information_schema.tables td
  JOIN
  information_schema.tables tr
    ON td.table_name LIKE tr.table_name
WHERE
  td.table_schema = 'cedesk_dev'
  AND td.table_type = 'BASE TABLE'
  AND tr.table_schema = 'cedesk_prod'
  AND tr.table_type = 'BASE TABLE';

## --------------------------------------------------------
