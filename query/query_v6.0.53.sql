-- 01.04.2025
alter table sf_tmpenc add column open int after descri;
alter table sf_tmpenc add column close int after open;

update sf_tmpenc set open = 0 where open is null;
update sf_tmpenc set close = 0 where close is null;