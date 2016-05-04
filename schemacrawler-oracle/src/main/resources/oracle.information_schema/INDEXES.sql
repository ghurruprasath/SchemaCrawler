SELECT /*+ PARALLEL(AUTO) */
  NULL AS TABLE_CAT,
  INDEXES.OWNER AS TABLE_SCHEM,
  INDEXES.TABLE_NAME,
  DECODE (INDEXES.UNIQUENESS, 'UNIQUE', 0, 1) AS NON_UNIQUE,
  NULL AS INDEX_QUALIFIER,
  INDEXES.INDEX_NAME,
  1 AS TYPE,
  INDEX_COLUMNS.COLUMN_POSITION AS ORDINAL_POSITION,
  INDEX_COLUMNS.COLUMN_NAME,
  CASE WHEN DESCEND = 'ASC' THEN 'A' ELSE 'D' END AS ASC_OR_DESC,
  INDEXES.DISTINCT_KEYS AS CARDINALITY,
  INDEXES.LEAF_BLOCKS AS PAGES,
  NULL AS FILTER_CONDITION
FROM 
  ALL_INDEXES INDEXES
  INNER JOIN ALL_IND_COLUMNS INDEX_COLUMNS
  ON 
    INDEXES.INDEX_NAME = INDEX_COLUMNS.INDEX_NAME
    AND INDEXES.TABLE_OWNER = INDEX_COLUMNS.TABLE_OWNER
    AND INDEXES.TABLE_NAME = INDEX_COLUMNS.TABLE_NAME
    AND INDEXES.OWNER = INDEX_COLUMNS.INDEX_OWNER
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
ORDER BY 
  TABLE_SCHEM,
  TABLE_NAME,
  INDEX_NAME,
  NON_UNIQUE, 
  TYPE,  
  ORDINAL_POSITION
