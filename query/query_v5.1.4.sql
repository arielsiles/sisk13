-- v5.1.4
-- 23.01.2021
-- Para diferimiento
alter table credito add column montodif decimal(13,2) after importe;
alter table credito add column cuotadif decimal(13,2) after cuota;
