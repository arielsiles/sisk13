/** 29.01.2020 **/
alter table arcgms add column est varchar(3) null after cta_niv3;

-- FLech
-- Prestamos Amortizables CIS M.N.
update arcgms a set a.`est` = 'VIG' where a.`cuenta` = '1310510600';
update arcgms a set a.`est` = 'VEN' where a.`cuenta` = '1320510600';
update arcgms a set a.`est` = 'EJE' where a.`cuenta` = '1330510600';
-- Prestamos Amortizables M.E. BID
update arcgms a set a.`est` = 'VIG' where a.`cuenta` = '1310520600';
update arcgms a set a.`est` = 'VEN' where a.`cuenta` = '1320520600';
update arcgms a set a.`est` = 'EJE' where a.`cuenta` = '1330520600';


