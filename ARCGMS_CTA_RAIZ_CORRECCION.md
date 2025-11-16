# Corrección de `cta_raiz` en tabla `arcgms`

**Fecha de análisis:** 2025-11-15
**Archivo analizado:** `tmp/arcgms.csv`
**Total de registros:** 875
**Registros con error:** 23
**Porcentaje de error:** 2.63%

---

## 1. Regla Correcta para `cta_raiz`

La columna `cta_raiz` debe almacenar la cuenta raíz (nivel 2) a la que pertenece cada cuenta.

### Estructura Correcta:
```
cta_raiz = [2 primeros dígitos de 'cuenta'] + "00000000"
```

### Ejemplos:

| cuenta (10 dígitos) | cta_raiz CORRECTA | Explicación |
|---------------------|-------------------|-------------|
| `1110040101` | `1100000000` | Primeros 2 dígitos = `11` → `1100000000` |
| `4150050300` | `4100000000` | Primeros 2 dígitos = `41` → `4100000000` |
| `5210010210` | `5200000000` | Primeros 2 dígitos = `52` → `5200000000` |
| `5560080200` | `5500000000` | Primeros 2 dígitos = `55` → `5500000000` |

### ❌ Errores Comunes Encontrados:
1. **Uso de cuenta nivel 4-5 como raíz** (ej: `cta_raiz = 5210000000` cuando debería ser `5200000000`)
2. **Uso de cuenta padre inmediata** (ej: `cta_raiz = 1110040100` cuando debería ser `1100000000`)
3. **Nivel 2 incorrecto** (ej: `cta_raiz = 4000000000` cuando debería ser `4200000000`)

---

## 2. Listado de Registros Incorrectos

### 2.1 Errores en Cuentas de ACTIVOS (1XXXXXXXXX)

| Cuenta | Descripción | cta_raiz ACTUAL ❌ | cta_raiz CORRECTA ✅ | Nivel |
|--------|-------------|-------------------|---------------------|-------|
| `1110040101` | BANCO UNION CTA. CTE MN  N. 1-52298813 | `1110040100` | `1100000000` | 6 |
| `1110040105` | BANCO MERCANTIL SANTA CRUZ CTA CTE MN N. 4011117301 | `1110040100` | `1100000000` | 6 |

**Problema:** Estas cuentas usan la cuenta padre (`1110040100`) como raíz en lugar de la cuenta nivel 2 (`11` → `1100000000`).

---

### 2.2 Errores en Cuentas de INGRESOS (4XXXXXXXXX)

| Cuenta | Descripción | cta_raiz ACTUAL ❌ | cta_raiz CORRECTA ✅ | Nivel |
|--------|-------------|-------------------|---------------------|-------|
| `4150050300` | INGRESO POR SERVICIO DE PESAJE EN BALANZA | `4150050000` | `4100000000` | 5 |
| `4150050400` | INGRESO POR SERVICIO DE MAQUILADO | `4150050000` | `4100000000` | 5 |
| `4200000000` | INGRESOS EXTRAORDINARIOS | `4000000000` | `4200000000` | 2 |

**Problemas:**
- Cuentas `4150XXXXXX`: Usan cuenta nivel 4 (`4150050000`) en lugar de nivel 2 (`4100000000`)
- Cuenta `4200000000`: Es nivel 2 pero tiene `4000000000` (nivel 1) como raíz

---

### 2.3 Errores en Cuentas de COSTOS/EGRESOS (5XXXXXXXXX)

| Cuenta | Descripción | cta_raiz ACTUAL ❌ | cta_raiz CORRECTA ✅ | Nivel |
|--------|-------------|-------------------|---------------------|-------|
| `5200000000` | COSTO DE VENTAS | `5000000000` | `5200000000` | 2 |
| `5210010210` | FLETES Y TRANSPORTES DE PRODUCTOS TERMINADOS ULEXITA | `5210010200` | `5200000000` | 6 |
| `5210010220` | FLETES Y TRANSPORTES DE PRODUCTOS TERMINADOS FORMULADO DE YESO AGRICOLA | `5210010200` | `5200000000` | 6 |
| `5210010230` | FLETES Y TRANSPORTES DE PRODUCTOS TERMINADOS ROCA FOSFORICA | `5210010200` | `5200000000` | 6 |
| `5210010240` | FLETES Y TRANSPORTES DE PRODUCTOS TERMINADOS YESO AGRICOLA | `5210010200` | `5200000000` | 6 |
| `5210060000` | DESCUENTO SOBRE SERVICIOS | `5210000000` | `5200000000` | 4 |
| `5210060100` | GASTOS DE ESTADIA EN FRONTERA | `5210050000` | `5200000000` | 5 |
| `5210060200` | DESCUENTO SOBRE SERVICIOS | `5210060000` | `5200000000` | 5 |
| `5350170000` | HERRAMIENTAS A CORTO PLAZO | `5350000000` | `5300000000` | 4 |
| `5350170100` | HERRAMIENTAS A CORTO PLAZO | `5350000000` | `5300000000` | 5 |
| `5560080200` | PONTONES TRACTOCAMION | `5560080000` | `5500000000` | 5 |
| `5570030200` | IMPUESTO MUNICIPAL A LA PROPIEDAD DE BIENES INMUEBLES | `5570030000` | `5500000000` | 5 |
| `5580010000` | GESTIONES EXPLORACION DE NUEVAS VETAS | `5580000000` | `5500000000` | 4 |
| `5580010100` | VIATICOS EXPLORACION | `5580010000` | `5500000000` | 5 |
| `5580010200` | SOCIALIZACION COMUNARIOS | `5580010000` | `5500000000` | 5 |
| `5580010300` | PEAJES EXPLORACIONES | `5580010000` | `5500000000` | 5 |
| `5580010400` | HOSPEDAJE EXPLORACIONES | `5580010000` | `5500000000` | 5 |
| `5580010500` | COMBUSTIBLE EN EXPLORACIONES | `5580010000` | `5500000000` | 5 |

**Problemas:**
- Cuenta `5200000000`: Es nivel 2 pero tiene `5000000000` (nivel 1) como raíz
- Cuentas `521XXXXXXX`: Usan cuentas nivel 3-5 en lugar de `5200000000`
- Cuentas `535XXXXXXX`: Usan `5350000000` en lugar de `5300000000`
- Cuentas `556XXXXXXX`: Usan `5560080000` en lugar de `5500000000`
- Cuentas `557XXXXXXX`: Usan `5570030000` en lugar de `5500000000`
- Cuentas `558XXXXXXX`: Usan cuentas nivel 3-4 en lugar de `5500000000`

---

## 3. Agrupación por Cuenta Raíz Correcta

### Resumen de correcciones necesarias:

| cta_raiz CORRECTA | Cantidad de registros a corregir | Rango de cuentas |
|-------------------|----------------------------------|------------------|
| `1100000000` | 2 | `111XXXXXXX` (Bancos) |
| `4100000000` | 2 | `415XXXXXXX` (Otros ingresos) |
| `4200000000` | 1 | `420XXXXXXX` (Ingresos extraordinarios) |
| `5200000000` | 8 | `520XXXXXXX`, `521XXXXXXX` (Costos) |
| `5300000000` | 2 | `535XXXXXXX` (Proyectos) |
| `5500000000` | 8 | `556XXXXXXX`, `557XXXXXXX`, `558XXXXXXX` (Gastos) |

**Total:** 23 registros

---

## 4. Consultas SQL de Corrección

### 4.1 Corrección Individual (Opción Segura)

```sql
-- ============================================
-- ACTIVOS (11XXXXXXXX)
-- ============================================
UPDATE arcgms SET cta_raiz = '1100000000' WHERE cuenta = '1110040101';
UPDATE arcgms SET cta_raiz = '1100000000' WHERE cuenta = '1110040105';

-- ============================================
-- INGRESOS (41XXXXXXXX)
-- ============================================
UPDATE arcgms SET cta_raiz = '4100000000' WHERE cuenta = '4150050300';
UPDATE arcgms SET cta_raiz = '4100000000' WHERE cuenta = '4150050400';

-- ============================================
-- INGRESOS EXTRAORDINARIOS (42XXXXXXXX)
-- ============================================
UPDATE arcgms SET cta_raiz = '4200000000' WHERE cuenta = '4200000000';

-- ============================================
-- COSTOS (52XXXXXXXX)
-- ============================================
UPDATE arcgms SET cta_raiz = '5200000000' WHERE cuenta = '5200000000';
UPDATE arcgms SET cta_raiz = '5200000000' WHERE cuenta = '5210010210';
UPDATE arcgms SET cta_raiz = '5200000000' WHERE cuenta = '5210010220';
UPDATE arcgms SET cta_raiz = '5200000000' WHERE cuenta = '5210010230';
UPDATE arcgms SET cta_raiz = '5200000000' WHERE cuenta = '5210010240';
UPDATE arcgms SET cta_raiz = '5200000000' WHERE cuenta = '5210060000';
UPDATE arcgms SET cta_raiz = '5200000000' WHERE cuenta = '5210060100';
UPDATE arcgms SET cta_raiz = '5200000000' WHERE cuenta = '5210060200';

-- ============================================
-- PROYECTOS (53XXXXXXXX)
-- ============================================
UPDATE arcgms SET cta_raiz = '5300000000' WHERE cuenta = '5350170000';
UPDATE arcgms SET cta_raiz = '5300000000' WHERE cuenta = '5350170100';

-- ============================================
-- GASTOS (55XXXXXXXX)
-- ============================================
UPDATE arcgms SET cta_raiz = '5500000000' WHERE cuenta = '5560080200';
UPDATE arcgms SET cta_raiz = '5500000000' WHERE cuenta = '5570030200';
UPDATE arcgms SET cta_raiz = '5500000000' WHERE cuenta = '5580010000';
UPDATE arcgms SET cta_raiz = '5500000000' WHERE cuenta = '5580010100';
UPDATE arcgms SET cta_raiz = '5500000000' WHERE cuenta = '5580010200';
UPDATE arcgms SET cta_raiz = '5500000000' WHERE cuenta = '5580010300';
UPDATE arcgms SET cta_raiz = '5500000000' WHERE cuenta = '5580010400';
UPDATE arcgms SET cta_raiz = '5500000000' WHERE cuenta = '5580010500';
```

---

### 4.2 Corrección por Lotes (Opción Optimizada)

```sql
-- Corrección por lotes usando IN
UPDATE arcgms
SET cta_raiz = '1100000000'
WHERE cuenta IN ('1110040101', '1110040105');

UPDATE arcgms
SET cta_raiz = '4100000000'
WHERE cuenta IN ('4150050300', '4150050400');

UPDATE arcgms
SET cta_raiz = '4200000000'
WHERE cuenta = '4200000000';

UPDATE arcgms
SET cta_raiz = '5200000000'
WHERE cuenta IN (
    '5200000000', '5210010210', '5210010220', '5210010230',
    '5210010240', '5210060000', '5210060100', '5210060200'
);

UPDATE arcgms
SET cta_raiz = '5300000000'
WHERE cuenta IN ('5350170000', '5350170100');

UPDATE arcgms
SET cta_raiz = '5500000000'
WHERE cuenta IN (
    '5560080200', '5570030200', '5580010000', '5580010100',
    '5580010200', '5580010300', '5580010400', '5580010500'
);
```

---

### 4.3 Corrección Global con Fórmula (Opción Automática)

⚠️ **ADVERTENCIA:** Esta consulta afectará **TODAS** las cuentas en la tabla. Usar con precaución.

```sql
-- Corrección automática usando SUBSTRING/CONCAT
UPDATE arcgms
SET cta_raiz = CONCAT(SUBSTRING(cuenta, 1, 2), '00000000')
WHERE cta_raiz != CONCAT(SUBSTRING(cuenta, 1, 2), '00000000')
  AND cta_raiz != '';
```

---

### 4.4 Validación Post-Corrección

Después de ejecutar las correcciones, ejecutar esta consulta para verificar que no queden errores:

```sql
-- Verificar que no hayan registros con cta_raiz incorrecta
SELECT
    cuenta,
    descri,
    cta_raiz AS cta_raiz_actual,
    CONCAT(SUBSTRING(cuenta, 1, 2), '00000000') AS cta_raiz_correcta,
    cn_nivel
FROM arcgms
WHERE cta_raiz != CONCAT(SUBSTRING(cuenta, 1, 2), '00000000')
  AND cta_raiz != ''
ORDER BY cuenta;
```

**Resultado esperado:** 0 filas (sin errores)

---

## 5. Impacto y Consideraciones

### 5.1 Tablas y Sistemas Afectados

La columna `cta_raiz` es utilizada en:
- **Dashboard de Finanzas** (`FinanceDashboardServlet.java`)
  - Query: JOIN con `ca.cta_niv3` para agrupar cuentas
  - Impacto: Los gráficos y reportes de Ingresos/Egresos agrupan por cuenta raíz

- **Reportes Contables**
  - Balances agrupados por cuenta nivel 2
  - Estados financieros consolidados

### 5.2 Recomendaciones

1. **Ejecutar en entorno de desarrollo primero**
2. **Hacer backup de la tabla antes de corregir:**
   ```sql
   CREATE TABLE arcgms_backup AS SELECT * FROM arcgms;
   ```
3. **Ejecutar la validación post-corrección**
4. **Verificar que los dashboards de finanzas muestren datos correctos**

### 5.3 Prevención de Errores Futuros

Considerar agregar un trigger o constraint:

```sql
-- Trigger para validar cta_raiz al insertar/actualizar
DELIMITER $$
CREATE TRIGGER arcgms_validate_cta_raiz
BEFORE INSERT ON arcgms
FOR EACH ROW
BEGIN
    DECLARE raiz_correcta VARCHAR(10);
    SET raiz_correcta = CONCAT(SUBSTRING(NEW.cuenta, 1, 2), '00000000');

    IF NEW.cta_raiz != raiz_correcta AND NEW.cta_raiz != '' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'cta_raiz debe ser los primeros 2 dígitos + 8 ceros';
    END IF;
END$$
DELIMITER ;
```

---

## 6. Archivos Generados

- **`tmp/arcgms.csv`** - Datos originales exportados
- **`tmp/arcgms_errores.csv`** - Listado de 23 registros con error
- **`tmp/analyze_arcgms.py`** - Script Python de análisis
- **`ARCGMS_CTA_RAIZ_CORRECCION.md`** - Este documento

---

**Documento generado:** 2025-11-15
**Autor:** Claude Code
**Versión:** 1.0
