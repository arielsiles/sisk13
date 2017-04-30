DELIMITER $$
USE `khipus`$$
DROP FUNCTION IF EXISTS `next_tmpenc`$$
CREATE DEFINER=`root`@`localhost` FUNCTION `next_tmpenc`() RETURNS VARCHAR(10)
BEGIN
	DECLARE act_trans VARCHAR(10);
	SET act_trans = 0;
	SELECT IFNULL(MAX(no_trans),0) INTO act_trans FROM khipus.sf_tmpenc;
  RETURN act_trans+1;
    END$$

DELIMITER ;