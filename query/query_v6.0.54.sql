-- 23.04.2025
select * from arcgms a where a.cta_niv3 = '';

update arcgms a set cta_niv3 = null
where a.cta_niv3 = '';