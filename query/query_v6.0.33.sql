-- 14.08.2024
select *
from arcgms a
where a.cn_nivel in (1, 2, 3, 4)
;

update arcgms set activa = 'S' where activa = 'N';

update arcgms a set a.ind_mov = 'N'
where a.cn_nivel in (1, 2, 3, 4)
;