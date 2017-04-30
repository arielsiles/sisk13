/*
SQLyog Ultimate v11.11 (64 bit)
MySQL - 5.6.23-log : Database - khipus
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
/*Table structure for table `id_gen` */

DROP TABLE IF EXISTS `id_gen`;

CREATE TABLE `id_gen` (
  `GEN_NAME` varchar(30) NOT NULL,
  `GEN_VAL` bigint(50) DEFAULT NULL,
  PRIMARY KEY (`GEN_NAME`),
  UNIQUE KEY `GEN_NAME_UNIQUE` (`GEN_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Data for the table `id_gen` */

insert  into `id_gen`(`GEN_NAME`,`GEN_VAL`) values ('ArticulosPedido_Gen',1700),('Impresionfactura_Gen',83),('Movimiento_Gen',25),('PedidosCodigo_Gen',43),('Pedidos_Gen',44),('Persona_GEN',20),('Territoriotrabajo_Gen',5);

/* Function  structure for function  `getNextSeq` */

/*!50003 DROP FUNCTION IF EXISTS `getNextSeq` */;
DELIMITER $$

/*!50003 CREATE DEFINER=`root`@`localhost` FUNCTION `getNextSeq`(sSeqName varchar(50)) RETURNS varchar(10) CHARSET utf8
begin
    declare nLast_val VARCHAR(10);
 
    set nLast_val =  (select seq_val
                          from _sequence
                          where seq_name = sSeqName);
    if nLast_val is null then
        set nLast_val = 1;
        insert into _sequence (seq_name,seq_val)
        values (sSeqName,nLast_Val);
    else
        set nLast_val = nLast_val + 1;
        update _sequence set seq_val = nLast_val
        where seq_name = sSeqName;
    end if;
	
    return nLast_val;
end */$$
DELIMITER ;

/* Function  structure for function  `next_tmpenc` */

/*!50003 DROP FUNCTION IF EXISTS `next_tmpenc` */;
DELIMITER $$

/*!50003 CREATE DEFINER=`root`@`localhost` FUNCTION `next_tmpenc`() RETURNS varchar(10) CHARSET utf8
BEGIN
	DECLARE act_trans VARCHAR(10);
	SET act_trans = 0;
	SELECT IfNULL(MAX(no_trans),0) INTO act_trans FROM khipus.sf_tmpenc;
  RETURN act_trans+1;
    END */$$
DELIMITER ;

/* Function  structure for function  `sigte_trans` */

/*!50003 DROP FUNCTION IF EXISTS `sigte_trans` */;
DELIMITER $$

/*!50003 CREATE DEFINER=`root`@`localhost` FUNCTION `sigte_trans`() RETURNS varchar(10) CHARSET utf8
BEGIN
	DECLARE act_trans varchar(10);
	SET act_trans = 0;
	SELECT ifnull(MAX(no_trans),0) into act_trans FROM khipus.inv_vales;
  RETURN act_trans+1;
    END */$$
DELIMITER ;

/* Procedure structure for procedure `sp_setSeqVal` */

/*!50003 DROP PROCEDURE IF EXISTS  `sp_setSeqVal` */;

DELIMITER $$

/*!50003 CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_setSeqVal`(sSeqName varchar(50), nVal int unsigned)
begin
    if (select count(*) from _sequence where seq_name = sSeqName) = 0 then
        insert into _sequence (seq_name,seq_val)
        values (sSeqName,nVal);
    else
        update _sequence set seq_val = nVal
        where seq_name = sSeqName;
    end if;
end */$$
DELIMITER ;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
