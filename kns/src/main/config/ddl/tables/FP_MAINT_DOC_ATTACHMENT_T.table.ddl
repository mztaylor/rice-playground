CREATE TABLE FP_MAINT_DOC_ATTACHMENT_T (
        FDOC_NBR                       VARCHAR2(14) NOT NULL,
        ATTACHMENT                     BLOB NOT NULL,
		FILE_NAME		       		   VARCHAR2(150),
		CONTENT_TYPE		       	   VARCHAR2(50),
        OBJ_ID                         VARCHAR2(36) DEFAULT SYS_GUID() NOT NULL,
        VER_NBR                        NUMBER(8) DEFAULT 1 NOT NULL, 
        CONSTRAINT FP_MAINT_DOC_ATTACHMENT_TP1 PRIMARY KEY (FDOC_NBR),
        CONSTRAINT FP_MAINT_DOC_ATTACHMENT_TC0 UNIQUE (OBJ_ID)
)
/