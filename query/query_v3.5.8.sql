/** 20.07.2019 **/
insert into gensecuencia values(19, 'SAVINGS_ACCOUNT_NUMBER', 1000000100, 1);
update secuencia set valor = (select MAX(g.`idgensecuencia`)+1 from gensecuencia g) where tabla='gensecuencia';

/** 22.07.2019 **/
/* ya esta actualizado en prod
insert into gensecuencia values(152, 'ILVA', 'SAN BENITO I', '551', 0, 1);
insert into gensecuencia values(153, 'ILVA', 'SAN BENITO II', '552', 0, 1);
update secuencia set valor = (select MAX(z.`idzonaproductiva`)+1 from zonaproductiva z) where tabla='ZONAPRODUCTIVA';
update productormateriaprima p set p.`idzonaproductiva` = 152
where p.`idproductormateriaprima` in (524,94,102,526,539,570);
update productormateriaprima p set p.`idzonaproductiva` = 153
where p.`idproductormateriaprima` in (105,107,108,109,111,112,113,91);
*/