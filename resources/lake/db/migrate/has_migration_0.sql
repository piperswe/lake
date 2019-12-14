-- Migration 0 has been applied if this query returns any rows

SELECT
    schema_name
FROM
    information_schema.schemata
WHERE
    schema_name = 'migrations';