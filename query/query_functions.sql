-- SET GLOBAL log_bin_trust_function_creators = 1;
-- 1. 
delimiter $$
create function `getNextSeq`(sSeqName varchar(50)) returns varchar(10) charset utf8
begin
    declare nLast_val varchar(10);
 
    set nLast_val =  (select seq_val
                          from _sequence
                          where seq_name = sSeqName);
    if nLast_val is null then
        set nLast_val = 1;
        insert into _sequence (seq_name,seq_val)
        values (sSeqName,nLast_Val);
    else
        set nLast_val = nLast_val + 1;
        update _sequence set seq_val = nLast_val
        where seq_name = sSeqName;
    end if;
    
    return nLast_val;
end$$
delimiter ;

-- 2.
delimiter $$
create function `newId_sf_tmpdet`() returns int(11)
begin
    declare nLast_val int;
    set nLast_val =  (select valor
                          from secuencia
                          where tabla = 'sf_tmpdet');
        set nLast_val = nLast_val + 1;
        update secuencia set valor = nLast_val
        where tabla = 'sf_tmpdet';
    return nLast_val;
end$$
delimiter ;

-- 3.
delimiter $$
create function `newId_sf_tmpenc`() returns int(11)
begin
    declare nLast_val int;
    set nLast_val =  (select valor
                          from secuencia
                          where tabla = 'sf_tmpenc');
        set nLast_val = nLast_val + 1;
        update secuencia set valor = nLast_val
        where tabla = 'sf_tmpenc';
    return nLast_val;
end$$
delimiter ;

-- 4.
delimiter $$
create function `next_tmpenc`() returns varchar(10) charset utf8
begin
    declare act_trans varchar(10);
    set act_trans = 0;
    select IFNULL(MAX(no_trans),0) into act_trans from khipus.sf_tmpenc;
  return act_trans+1;
    end
$$
delimiter ;


-- 5.
delimiter $$
create function `sigte_trans`() returns varchar(10) charset utf8
begin
    declare act_trans varchar(10);
    set act_trans = 0;
    select IFNULL(MAX(no_trans),0) into act_trans from khipus.inv_vales;
  return act_trans+1;
    end
$$
delimiter ;

-- 6.
delimiter $$
create function `newId_inv_inventario_detalle`() returns int(11)
begin
    declare nLast_val int;
    set nLast_val =  (select valor
                          from secuencia
                          where tabla = 'inv_inventario_detalle');
        set nLast_val = nLast_val + 1;
        update secuencia set valor = nLast_val
        where tabla = 'inv_inventario_detalle';
    return nLast_val;
end $$
delimiter ;

