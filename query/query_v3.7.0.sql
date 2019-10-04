/** 04.10.2019 **/
alter table tipocredito add column ipctavig varchar(20) after ictaeje;

-- Para F.Cisc
update tipocredito t set t.`ipctavig` = '5139930100' where t.`idtipocredito` = 1;
update tipocredito t set t.`ipctavig` = '5139930100' where t.`idtipocredito` = 3;
update tipocredito t set t.`ipctavig` = '5139930100' where t.`idtipocredito` = 4;