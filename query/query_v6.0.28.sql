-- 18.02.2026

-- Auditoría en impuestoproductor
ALTER TABLE impuestoproductor ADD COLUMN created_at  DATETIME     NULL;
ALTER TABLE impuestoproductor ADD COLUMN created_by  VARCHAR(100) NULL;
ALTER TABLE impuestoproductor ADD COLUMN updated_at  DATETIME     NULL;
ALTER TABLE impuestoproductor ADD COLUMN updated_by  VARCHAR(100) NULL;

delete from impuestoproductor
where idimpuestoproductor in (
353, 953, 529, 22, 398, 355, 465, 828, 653, 399
);


alter table impuestoproductor add foreign key (idproductormateriaprima) references productormateriaprima (idproductormateriaprima);