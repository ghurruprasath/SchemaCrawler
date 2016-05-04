SELECT /*+ PARALLEL(AUTO) */
  NULL AS INDEX_CATALOG,
  INDEXES.OWNER AS INDEX_SCHEMA,
  INDEXES.INDEX_NAME AS INDEX_NAME,
  INDEXES.TABLE_NAME AS TABLE_NAME,
  CASE WHEN INDEXES.UNIQUENESS = 'UNIQUE' THEN 'YES' ELSE 'NO' END AS IS_UNIQUE,
  DBMS_METADATA.GET_DDL('INDEX', INDEXES.INDEX_NAME, INDEXES.OWNER) AS INDEX_DEFINITION
FROM
  ALL_INDEXES INDEXES
WHERE
  INDEXES.OWNER NOT IN 
    ('ANONYMOUS', 'APEX_PUBLIC_USER', 'APPQOSSYS', 'BI', 'CTXSYS', 'DBSNMP', 'DIP', 
    'EXFSYS', 'FLOWS_30000', 'FLOWS_FILES', 'HR', 'IX', 'LBACSYS', 
    'MDDATA', 'MDSYS', 'MGMT_VIEW', 'OE', 'OLAPSYS', 'ORACLE_OCM', 
    'ORDPLUGINS', 'ORDSYS', 'OUTLN', 'OWBSYS', 'PM', 'SCOTT', 'SH', 
    'SI_INFORMTN_SCHEMA', 'SPATIAL_CSW_ADMIN_USR', 'SPATIAL_WFS_ADMIN_USR', 
    'SYS', 'SYSMAN', 'SYSTEM', 'TSMSYS', 'WKPROXY', 'WKSYS', 'WK_TEST', 
    'WMSYS', 'XDB', 'XS$NULL', 'RDSADMIN')  
  AND NOT REGEXP_LIKE(INDEXES.OWNER, '^APEX_[0-9]{6}$')
  AND NOT REGEXP_LIKE(INDEXES.OWNER, '^FLOWS_[0-9]{5,6}$')
  AND REGEXP_LIKE(INDEXES.OWNER, '${schemas}')
  AND INDEXES.TABLE_NAME NOT LIKE 'BIN$%'
  AND INDEXES.INDEX_NAME NOT LIKE 'BIN$%'
ORDER BY
  INDEX_SCHEMA,
  TABLE_NAME,
  INDEX_NAME
