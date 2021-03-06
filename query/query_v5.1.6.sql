-- v5.1.6
-- 06.03.2021
alter table pedidos add column factesp int(1) after tipoventa;
update pedidos p set p.`factesp` = 0;