/** 13.06.2019 **/
alter table periodocostoindirecto add column contab int(1) after procesado;
update periodocostoindirecto p set p.`contab` = 1;

update periodocostoindirecto p set p.`contab` = 0 
where p.`mes` = 5 and p.`idgestion` = 6;

update periodocostoindirecto p set p.`procesado` = 1;
update periodocostoindirecto p set p.`procesado` = 0 
where p.`mes` = 5 and p.`idgestion` = 6;