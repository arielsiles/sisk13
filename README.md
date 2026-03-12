# SISK13 / KHIPUS

Sistema Integrado Contable construido sobre Java EE / JBoss Seam 2.2.

Cubre contabilidad, presupuestos, tesoreria, produccion (acopio de leche), compras, almacenes, activos fijos, recursos humanos y mas.

## Documentacion

- [Arquitectura del proyecto](docs/architecture.md)
- [Historial de cambios](CHANGELOG.md)
- [Analisis: Planilla de acopio de leche](docs/analisis-planilla-acopio-leche.md)

## Build

Requiere: JDK 6+, Apache Ant, JBoss AS 5.x

```bash
# Build y deploy (desarrollo)
ant explode

# Generar EAR
ant archive

# Limpiar
ant clean

# Produccion
ant -f build-prod.xml archive

# Tests
ant test
```

Configuracion de JBoss en `build.properties`.
