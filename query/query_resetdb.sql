/** 30-08-2023 **/

DROP TABLE `movimiento0`;

DELETE FROM rhmarcado;
DELETE FROM rh_marcado;
DELETE FROM reportecontrol;

UPDATE movimiento m SET m.`id_tmpdet` = NULL;
UPDATE movimiento m SET m.`IDPEDIDOS` = NULL, m.`IDVENTADIRECTA` = NULL;
UPDATE inv_vales i SET i.`idpedidostr` = NULL;
UPDATE documentocompra d SET d.`idtmpenc` = NULL;
UPDATE inv_vales i SET i.`idtmpenc` = NULL;

DELETE FROM sf_tmpdet;
DELETE FROM pago;
DELETE FROM articulos_pedido;
DELETE FROM ventadirecta;
DELETE FROM pedidos;
DELETE FROM sf_tmpenc;

DELETE FROM inv_movdet;
DELETE FROM inv_mov;
DELETE FROM inv_vales;
DELETE FROM inv_invmes;
DELETE FROM auxinv;
DELETE FROM inv_inicio;

DELETE FROM `cxp_lcompras`;
DELETE FROM `documentocompra`;
DELETE FROM `documentocontable`;

DELETE FROM `com_detoc`;
DELETE FROM `com_encoc`;
DELETE FROM `movimiento`;
DELETE FROM `movimientosueldo`;

DELETE FROM inv_inventario;
DELETE FROM `inv_inventario_detalle`;

DELETE FROM `inv_subgrupos`;

DELETE FROM `ventaarticulo`;
DELETE FROM `ventacliente`;
DELETE FROM `promocion`;
DELETE FROM `personacliente`;

DELETE FROM pr_insumo;
DELETE FROM pr_producto;
DELETE FROM pr_produccion;
DELETE FROM pr_plan;
DELETE FROM pr_material;
DELETE FROM costosindirectos;
DELETE FROM periodocostoindirecto;

DELETE FROM `inv_articulos`;
DELETE FROM `inv_periodo`;

DELETE FROM `af_hdepre`;
DELETE FROM `af_activos`;
DELETE FROM  af_movs;
DELETE FROM `af_pago`;
DELETE FROM `com_af_detoc`;

DELETE FROM planillafiscal;
DELETE FROM planillafiscalporcategoria;
DELETE FROM planillatributaria;
DELETE FROM planillatributariaporcategoria;
DELETE FROM planillaadministrativos;
DELETE FROM planillagenerada;
DELETE FROM gestionplanilla;
DELETE FROM ciclogeneracionplanilla;
DELETE FROM `planillaaguinaldo`;

DELETE FROM `bandahorariacontrato`;
DELETE FROM `bandahoraria`;
DELETE FROM `bonoconseguido`;
DELETE FROM estadobandahoraria;
DELETE FROM `estadomarcado`;
DELETE FROM `fechaespecial`;

DELETE FROM materialproduccion;
DELETE FROM ordenmaterial;

DELETE FROM registroacopio;
DELETE FROM planillaacopio;
DELETE FROM acopiomateriaprima;
DELETE FROM sesionacopio;

DELETE FROM registropagomateriaprima;
DELETE FROM planillapagomateriaprima;
DELETE FROM descuentproductmateriaprima;
DELETE FROM productormateriaprima;
DELETE FROM descuentoproductor;
DELETE FROM `impuestoproductor`;
DELETE FROM `movimientosalarioproductor`;
DELETE FROM movimientosalariogab;


DELETE FROM `ingredienteproduccion`;
DELETE FROM `composicionproducto`;
DELETE FROM `insumoproduccion`;
DELETE FROM `productoprocesado`;
DELETE FROM `metaproductoproduccion`;
DELETE FROM `productobase`;
DELETE FROM `productoreprocesado`;
DELETE FROM `productosimple`;
DELETE FROM `productosimpleprocesado`;
DELETE FROM `metaproductoproduccion`;
DELETE FROM `ordenproduccion`;
DELETE FROM `planificacionproduccion`;

DELETE FROM `transaccioncredito`;
DELETE FROM `credito`;
DELETE FROM `socio`;

DELETE FROM cuentabancaria;
DELETE FROM distribuidor;

DELETE FROM empleado WHERE idempleado <> 1;
DELETE FROM persona WHERE idpersona <> 1;
DELETE FROM entidad WHERE identidad NOT IN (1, 470 );
DELETE FROM contratopuesto;
DELETE FROM contrato;
DELETE FROM usuariounidadnegocio WHERE idusuario <> 1;
DELETE FROM usuarios WHERE no_usr <> 'ADM';
DELETE FROM usuariorol WHERE idusuario <> 1;
DELETE FROM `cajausuario` WHERE idusuario <> 1;
DELETE FROM `usuario` WHERE idusuario <> 1;

DELETE FROM articulo_por_proveedor;
DELETE FROM `sf_entidades`;
DELETE FROM `cxp_proveedores`;

DELETE FROM inv_inicio;
DELETE FROM `historialarticuloprov`;
DELETE FROM `historialcaja`;
DELETE FROM `sf_confdet`;
DELETE FROM `sf_confenc`;

DELETE FROM sueldo;
DELETE FROM cargo;
DELETE FROM puesto;


-- REFERENCES
ALTER TABLE contrato ADD FOREIGN KEY (idempleado) REFERENCES empleado (idempleado);
ALTER TABLE contrato ADD PRIMARY KEY (idcontrato);
ALTER TABLE contratopuesto ADD FOREIGN KEY (idcontrato) REFERENCES contrato (idcontrato);
ALTER TABLE descuentoempleado ADD FOREIGN KEY (idempleado) REFERENCES empleado (idempleado);
ALTER TABLE detplanilladocentelaboral ADD FOREIGN KEY (idempleado) REFERENCES empleado (idempleado);
ALTER TABLE detplanilladocentelaboral ADD PRIMARY KEY (iddetplanilladocentelaboral);
ALTER TABLE `movimientosueldo` ADD PRIMARY KEY (idmovimientosueldo);
ALTER TABLE movimientosueldo ADD FOREIGN KEY (idempleado) REFERENCES empleado (idempleado);
ALTER TABLE `institucion` ADD FOREIGN KEY (idinstitucion) REFERENCES entidad (identidad);

-- ALTER TABLE usuariorol ADD FOREIGN KEY (idrol) REFERENCES rol (idrol);
-- ALTER TABLE usuariorol ADD FOREIGN KEY (idusuario) REFERENCES usuario (idusuario);
