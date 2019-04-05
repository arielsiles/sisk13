/** 05.04.2019 Modificacion en vales para transferencias **/
ALTER TABLE inv_vales ADD COLUMN dest VARCHAR(10) AFTER oper;
ALTER TABLE inv_vales ADD COLUMN orig VARCHAR(10) AFTER dest;