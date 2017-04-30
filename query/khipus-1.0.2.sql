--permitir crear un sesion de acopio en una misma fecha y en gabs diferentes
alter table SESIONACOPIO drop constraint uq_sesionacopiofecha;
DROP INDEX eos.UQ_SESIONACOPIOFECHA;
commit;
--total pesado por gab
alter table PLANILLAPAGOMATERIAPRIMA add TOTALPESADOXGAB NUMBER(16,2) default 0.0;
--COMMIT
