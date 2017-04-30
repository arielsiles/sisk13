--no olvidar porner el no_trans en unico
--select sigte_trans();


DELIMITER $$
USE `khipus`$$
DROP FUNCTION IF EXISTS `sigte_trans`$$
CREATE DEFINER=`root`@`localhost` FUNCTION `sigte_trans`() RETURNS VARCHAR(10)
BEGIN
	DECLARE act_trans VARCHAR(10);
	SET act_trans = 0;
	SELECT IFNULL(MAX(no_trans),0) INTO act_trans FROM khipus.inv_vales;
  RETURN act_trans+1;
    END$$

DELIMITER ;
