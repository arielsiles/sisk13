/** 29.01.2020 **/
alter table arcgms add column est varchar(3) null after cta_niv3;

-- ACTUALIZAR PLAN DE CUENTAS PARA REPORTE ESTADO DE CARTERA
-- FLech
-- Prestamos Amortizables CIS M.N.
update arcgms a set a.`est` = 'VIG' where a.`cuenta` = '1310510600';
update arcgms a set a.`est` = 'VEN' where a.`cuenta` = '1320510600';
update arcgms a set a.`est` = 'EJE' where a.`cuenta` = '1330510600';
-- Prestamos Amortizables M.E. BID
update arcgms a set a.`est` = 'VIG' where a.`cuenta` = '1310520600';
update arcgms a set a.`est` = 'VEN' where a.`cuenta` = '1320520600';
update arcgms a set a.`est` = 'EJE' where a.`cuenta` = '1330520600';

-- FCisc
-- Prestamos Amortizables CIS M.N.
update arcgms a set a.`est` = 'VIG' where a.`cuenta` = '1310510100';
update arcgms a set a.`est` = 'VEN' where a.`cuenta` = '1320510100';
update arcgms a set a.`est` = 'EJE' where a.`cuenta` = '1330510100';
-- Prestamos Amortizables CIS M.E.
update arcgms a set a.`est` = 'VIG' where a.`cuenta` = '1310520100';
update arcgms a set a.`est` = 'VEN' where a.`cuenta` = '1320520100';
update arcgms a set a.`est` = 'EJE' where a.`cuenta` = '1330520100';
-- Prestamos Amortizables FDC M.E.
update arcgms a set a.`est` = 'VIG' where a.`cuenta` = '1310520400';
update arcgms a set a.`est` = 'VEN' where a.`cuenta` = '1320520400';
update arcgms a set a.`est` = 'EJE' where a.`cuenta` = '1330520400';
