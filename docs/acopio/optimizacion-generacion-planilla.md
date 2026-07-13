# Optimización de la generación de planilla de acopio

**Estado:** EN PAUSA — avance parcial confirmado y mergeado a `dev_ilva`.
**Rama de trabajo:** `perf/acopio-generacion-planilla`.
**Última actualización:** 2026-07-13.

---

## 1. Problema

La **generación de planilla de acopio** de una quincena tarda:

- **Producción:** ~9-10 minutos (antes de este trabajo).
- **Local (desarrollo):** 35-40 segundos.

Dato **crítico** para el diagnóstico: **local trabaja con los MISMOS datos que producción**
(la base de local es una copia de la de prod). Misma data, mismas queries, mismo motor.

Entorno:
- App: JBoss AS 5.1 + Hibernate 3.3 (JPA 1.0), MySQL.
- Prod: Intel i7-2700K (2011), 16 GB RAM, SSD **SATA económico** (CT240BX500).
- Local: Intel i7-6820HQ, 32 GB RAM, SSD rápido.
- Datasource prod: `jdbc:mysql://10.0.0.100:3306/khipus` (DB y app en la misma máquina).

---

## 2. Qué se hizo (HECHO, probado, mejoró — mantener)

Todos estos cambios están **confirmados como correctos y seguros**. Bajaron el tiempo de
**~10 min → 6:25**.

### 2.1 Batching JDBC de Hibernate
En `persistence-prod.xml` y `persistence-dev.xml`:
```xml
<property name="hibernate.jdbc.batch_size" value="50"/>
<property name="hibernate.order_inserts" value="true"/>
<property name="hibernate.order_updates" value="true"/>
```

### 2.2 rewriteBatchedStatements en el datasource
En `khipus-prod-ds.xml` (y `khipus-dev-ds.xml`, este último **gitignored** → solo local):
```
jdbc:mysql://.../khipus?rewriteBatchedStatements=true
```
Hace que el driver colapse los INSERT en lote en una sola sentencia multi-fila.

**Verificado funcionando:** en una generación se escribieron **21.954 filas con solo 395
INSERT** (~55 filas por sentencia). El batching entra bien.

### 2.3 (Lado servidor, aplicado por el usuario en prod)
```ini
innodb_flush_log_at_trx_commit = 2
```
Confirmado activo con `SHOW VARIABLES`. Elimina el fsync-por-commit.

---

## 3. Mediciones y hallazgos (SHOW GLOBAL STATUS de una generación)

| Métrica | Valor | Lectura |
|---|---|---|
| `innodb_flush_log_at_trx_commit` | **2** | fsync ya no es el cuello |
| `Com_commit` | 579 | NO hay "miles de commits" por `secuencia` |
| `Com_insert` | 395 | INSERTs colapsados (batching OK) |
| `Com_select` | 1854 | moderado |
| `Com_update` | 482 | moderado |
| `Handler_write` | 21.954 | filas escritas (batching ≈55 filas/stmt) |

**Total ≈ 2.700 statements** para toda la generación.

### Hipótesis DESCARTADAS (importante para no repetir)
- ❌ **"fsync storm" por el generador de IDs `secuencia`**: `Com_commit`=579, no miles.
  El TABLE generator NO hace un commit por fila en este Hibernate/JBoss.
- ❌ **Cantidad de round-trips**: ~2.700 statements en la misma máquina deberían ser
  segundos, no minutos.
- ❌ **"Prod tiene más data que local"**: FALSO, misma data.

### Conclusión del diagnóstico actual
Con **data y queries idénticas**, 35s local vs 6:25 prod (**11×**) solo puede explicarse por
el **entorno del servidor MySQL de producción** (no el código, no la data, no el hardware de
CPU que sería ~2×). Pocas queries pero individualmente lentas → **lectura a disco** porque los
datos **no están cacheados en RAM**.

---

## 4. PRÓXIMO PASO (retomar acá)

### Hipótesis líder: `innodb_buffer_pool_size` chico en prod
Si el buffer pool de prod quedó por defecto (128 MB, o 8 MB en MySQL 5.1), la base **no entra
en RAM** → cada query lee del SSD SATA económico (lecturas aleatorias lentas), mientras que en
local (buffer pool grande / disco rápido) todo se sirve desde memoria. Explica el 11× con misma
data.

### Medición decisiva — comparar config MySQL **local vs prod**
Correr en **ambas** y comparar:
```sql
SELECT VERSION();
SHOW VARIABLES WHERE Variable_name IN (
 'innodb_buffer_pool_size','innodb_buffer_pool_instances','innodb_log_file_size',
 'innodb_flush_method','key_buffer_size','tmp_table_size','max_heap_table_size',
 'query_cache_type','query_cache_size','table_open_cache');
```
Motor y tamaño de tablas calientes (en prod):
```sql
SELECT table_name, engine, table_rows,
       ROUND((data_length+index_length)/1024/1024) AS mb
FROM information_schema.tables
WHERE table_schema='khipus'
  AND table_name IN ('acopiomateriaprima','registroacopio','sesionacopio',
     'planillapagomateriaprima','registropagomateriaprima',
     'movimientosalarioproductor','aplicacion_descuento_productor')
ORDER BY mb DESC;
```

### Fix probable (si se confirma la hipótesis)
- Subir `innodb_buffer_pool_size` en prod (p. ej. 4-6 GB de los 16), reiniciar MySQL.
  **Cero código, cero riesgo, reversible.** Debería dejar prod casi como local.
- Si las tablas calientes son **MyISAM** → manda `key_buffer_size` (mismo razonamiento).
- Si `tmp_table_size`/`max_heap_table_size` chicos → temp tables en disco (subirlos).

### Nota sobre la medición de queries lentas (por si se necesita)
`SET GLOBAL long_query_time=0` **NO** afecta las conexiones ya abiertas del **pool de JBoss**
(min-pool=5). Por eso el `slow_log` salió vacío. Para capturar hay que forzar conexiones nuevas
(reiniciar JBoss / el pool) o usar `general_log`.

---

## 5. Opción de último recurso (NO hacer sin validar)

Si tras arreglar la config de MySQL el tiempo aún molesta, queda la **Opción B**: subir
`allocationSize` (reservar bloques de IDs) solo en las 4 entidades de acopio
(`RawMaterialPayRoll`, `RawMaterialPayRecord`, `RawMaterialProducerDiscount`,
`DiscountApplication`), que usan `GenerationType.TABLE` sobre la tabla `secuencia` con
`Constants.SEQUENCE_ALLOCATION_SIZE = 1`.

**Riesgo (por eso es último recurso):** en Hibernate 3.3 legacy (hi/lo) cambia la semántica del
`valor` almacenado y puede saltar los IDs; hay que validar el generador y el margen de la
columna antes. Con `flush_log=2` ya activo, su impacto es menor de lo que parecía.

---

## 6. Cómo retomar

```bash
git checkout perf/acopio-generacion-planilla   # (o dev_ilva, ya mergeado)
git log --oneline --grep="acopio.*batching"    # ver el commit del avance
```
Este documento (`docs/acopio/optimizacion-generacion-planilla.md`) tiene todo el contexto.
