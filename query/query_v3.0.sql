/** 11.04.2019 Para Modulo de produccion 2 **/
DELETE FROM costosindirectos WHERE idcostosindirectos = 0;

ALTER TABLE costosindirectos ADD FOREIGN KEY (idperiodocostoindirecto) REFERENCES periodocostoindirecto(idperiodocostoindirecto);


ALTER TABLE ingredienteproduccion ADD FOREIGN KEY (idcomposicionproducto) REFERENCES composicionproducto(idcomposicionproducto);

-- ALTER TABLE metaproductoproduccion ADD FOREIGN KEY (cod_art) REFERENCES inv_articulos(cod_art); error
-- alter table metaproductoproduccion add constraint FK_INVARTICULOMETAPRODUCTO FOREIGN KEY (cod_art, no_cia) REFERENCES inv_articulos(cod_art, no_cia); error
-- alter table productoprocesado add foreign key (idproductoprocesado) references metaproductoproduccion(idmetaproductoproduccion); error

