ALTER TABLE externalmodel DROP COLUMN version; 
ALTER TABLE parametermodel DROP COLUMN version; 

UPDATE study set lastModification = latestModelModification WHERE lastModification IS NULL;
UPDATE study_aud set lastModification = latestModelModification WHERE lastModification IS NULL;

ALTER TABLE study DROP COLUMN latestModelModification;
ALTER TABLE study_aud DROP COLUMN latestModelModification;

DELETE FROM  hibernate_sequence WHERE next_val = 51;