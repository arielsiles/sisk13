/** 01.07.2019 **/
ALTER TABLE planillafiscalporcategoria ADD COLUMN afplab_individual DECIMAL(13,2) AFTER retencionafp;
ALTER TABLE planillafiscalporcategoria ADD COLUMN afplab_riesocomun DECIMAL(13,2) AFTER afplab_individual;
ALTER TABLE planillafiscalporcategoria ADD COLUMN afplab_solidario DECIMAL(13,2) AFTER afplab_riesocomun;
ALTER TABLE planillafiscalporcategoria ADD COLUMN afplab_comision DECIMAL(13,2) AFTER afplab_solidario;

UPDATE planillafiscalporcategoria SET afplab_individual = 0;