# -*- coding: utf-8 -*-
"""Genera las actualizaciones de datos maestros desde la planilla oficial.

Cruza la hoja 'V.1-PLLA GRAL COMPLETA- JULIO' contra `empleado` y emite el SQL que
actualiza CI, fecha de nacimiento, fecha de ingreso, sueldo basico y regimen de aportes.
No escribe en la base: solo emite SQL.

La planilla es el dato oficial y gana ante la base, salvo donde el usuario indico otra cosa.
"""
import datetime
import io
import os
import re
import subprocess
import sys
import unicodedata
import xml.etree.ElementTree as ET
import zipfile

NS = '{http://schemas.openxmlformats.org/spreadsheetml/2006/main}'
MYSQL = r'C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe'

# Columnas de la hoja general (0-indexadas). Q = NUEVO SUELDO BASICO = 16.
COL_CI, COL_PAT, COL_MAT, COL_NOM = 3, 5, 6, 7
COL_NACIMIENTO, COL_INGRESO, COL_SUELDO, COL_AFP = 9, 12, 16, 29

# El CI de la planilla esta mal en estos casos y el usuario indico el correcto.
CI_CORREGIDO = {'COLQUEJORAVICTOR': '9465721'}

# Empleados que hay que ubicar por id: o la planilla los nombra distinto que la base, o el
# CI cae en un par duplicado y hay que quedarse con el que tiene contrato.
POR_ID = {
    'DERECKJOELGALVEZMOLINA': 33,   # en la base figura como DERICK y con otro CI
    'JOSELUISGALVEZGOMEZ': 31,      # duplicado con el id 4: manda el que tiene contrato
}

# Pares duplicados: (id que sobrevive, id a marcar). Se marca el segundo apellido con
# DUPLICA en lugar de borrar, porque borrar exige revisar las referencias de otros modulos.
DUPLICADOS = [
    (163, 165, 'IVAN CARLOS MAMANI CHOQUE'),
    (191, 201, 'JUAN LUIS COCA COLQUE'),
    (228, 227, 'ELIANA TRUJILLO'),
    (31, 4, 'JOSE LUIS GALVEZ GOMEZ'),
    (35, 32, 'LUIS GALVEZ MOLINA'),
]

# CI compartido por personas distintas. No son duplicados: solo se reportan.
CI_COMPARTIDO = [
    ('7951447', 5, 'FRANZ LUIS CHANEZ HUALLATA', 13, 'JORGE ERICK ALMANZA QUIROZ'),
    ('6550221', 23, 'BENJAMIN PELAEZ FLORES', 24, 'NAHUEL EINAR FERNANDEZ VARGAS'),
    ('9304252', 156, 'ELMER BUSTAMANTE MEJIA', 177, 'ELVIS OCTAVIO BUSTAMANTE MEJIA'),
]


def norm(text):
    text = unicodedata.normalize('NFKD', text)
    text = ''.join(c for c in text if not unicodedata.combining(c))
    text = text.replace(u'\u00d1', 'N').replace(u'\u00f1', 'N')
    return re.sub(r'[^A-Za-z]', '', text).upper()


def read_rows(path, sheet):
    z = zipfile.ZipFile(path)
    shared = [''.join(t.text or '' for t in si.iter(NS + 't'))
              for si in ET.fromstring(z.read('xl/sharedStrings.xml')).findall(NS + 'si')]

    def colidx(ref):
        s = ''.join(c for c in ref if c.isalpha())
        n = 0
        for c in s:
            n = n * 26 + (ord(c) - 64)
        return n - 1

    rows = []
    for r in ET.fromstring(z.read(sheet)).iter(NS + 'row'):
        cells, auto = {}, 0
        for c in r.findall(NS + 'c'):
            ref = c.get('r')
            idx = colidx(ref) if ref else auto
            auto = idx + 1
            v = c.find(NS + 'v')
            val = (shared[int(v.text)] if c.get('t') == 's' else (v.text or '')) if v is not None else ''
            cells[idx] = val
        rows.append([cells.get(i, '') for i in range(max(cells) + 1)] if cells else [])
    return rows


def excel_date(value):
    """Las fechas vienen como serial de Excel."""
    try:
        return (datetime.date(1899, 12, 30) + datetime.timedelta(days=int(float(value)))).isoformat()
    except (ValueError, TypeError):
        return None


def money(value):
    try:
        return '%.2f' % float(value)
    except (ValueError, TypeError):
        return None


def db_employees(db, user, password):
    sql = ("select e.idempleado, en.noidentificacion, p.nombres, p.apellidopaterno,"
           " p.apellidomaterno, p.fechanacimiento,"
           " (select c.idcontrato from contrato c where c.idempleado = e.idempleado limit 1)"
           " from empleado e"
           " join entidad en on en.identidad = e.idempleado"
           " join persona p on p.idpersona = e.idempleado")
    env = dict(os.environ, MYSQL_PWD=password)
    out = subprocess.check_output(
        [MYSQL, '-h127.0.0.1', '-u' + user, db, '-N', '-B',
         '--default-character-set=utf8mb4', '-e', sql], env=env).decode('utf-8')
    rows = []
    for line in out.strip().split('\n'):
        f = [c.strip() for c in line.split('\t')]
        if len(f) >= 7:
            rows.append({
                'id': int(f[0]),
                'ci': re.sub(r'[^0-9]', '', f[1]),
                'ciraw': f[1],
                'nombre': (f[2] + ' ' + f[3] + ' ' + f[4]).strip(),
                'clave': norm(f[2] + f[3] + f[4]),
                'nacimiento': None if f[5] in ('NULL', '') else f[5],
                'contrato': None if f[6] in ('NULL', '') else int(f[6]),
            })
    return rows


def main():
    path = sys.argv[1] if len(sys.argv) > 1 else 'tmp/rh/V1_PLANILLA GENERAL_07-2026_08-08-2026.xlsx'
    db = sys.argv[2] if len(sys.argv) > 2 else 'terdemol'
    user = sys.argv[3] if len(sys.argv) > 3 else 'adm'
    password = sys.argv[4] if len(sys.argv) > 4 else os.environ.get('MYSQL_PWD', '')

    filas = [x for x in read_rows(path, 'xl/worksheets/sheet1.xml')
             if len(x) > COL_NOM and x[0].strip().isdigit() and x[COL_CI].strip()]
    empleados = db_employees(db, user, password)
    por_ci = {}
    por_clave = {}
    por_id = {}
    for e in empleados:
        por_ci.setdefault(e['ci'], []).append(e)
        por_clave.setdefault(e['clave'], []).append(e)
        por_id[e['id']] = e

    cruzados, sin_cruzar = [], []
    for x in filas:
        nombre = (x[COL_PAT] + ' ' + x[COL_MAT] + ' ' + x[COL_NOM]).strip()
        clave = norm(x[COL_NOM] + x[COL_PAT] + x[COL_MAT])
        ci = CI_CORREGIDO.get(norm(x[COL_PAT] + x[COL_MAT] + x[COL_NOM]),
                              re.sub(r'[^0-9]', '', x[COL_CI]))

        empleado = None
        if clave in POR_ID:
            empleado = por_id.get(POR_ID[clave])
        elif ci in por_ci and len(por_ci[ci]) == 1:
            empleado = por_ci[ci][0]
        elif clave in por_clave and len(por_clave[clave]) == 1:
            empleado = por_clave[clave][0]
        elif ci in por_ci:
            # varios comparten ese CI: se desempata por nombre
            for e in por_ci[ci]:
                if e['clave'] == clave:
                    empleado = e

        if None is empleado:
            sin_cruzar.append((x[COL_CI].strip(), nombre))
            continue

        cruzados.append({
            'e': empleado,
            'nombre': nombre,
            'ci': ci,
            'nacimiento': excel_date(x[COL_NACIMIENTO]),
            'ingreso': excel_date(x[COL_INGRESO]),
            'sueldo': money(x[COL_SUELDO]) if len(x) > COL_SUELDO else None,
            'afp': (len(x) > COL_AFP and x[COL_AFP].strip() not in ('', '0')),
        })

    emitir(cruzados, sin_cruzar)


def emitir(cruzados, sin_cruzar):
    w = sys.stdout.write
    con_afp = [c for c in cruzados if c['afp']]
    w('-- Cruzados %d de la planilla | con AFP %d | sin AFP %d | sin cruzar %d\n\n'
      % (len(cruzados), len(con_afp), len(cruzados) - len(con_afp), len(sin_cruzar)))

    w('-- CI ---------------------------------------------------------------------\n')
    for c in sorted(cruzados, key=lambda c: c['nombre']):
        if c['ci'] and c['ci'] != c['e']['ci']:
            w('-- %s: %s -> %s\n' % (c['nombre'], c['e']['ciraw'] or '(vacio)', c['ci']))
            w("UPDATE entidad SET noidentificacion = '%s' WHERE identidad = %d;\n" % (c['ci'], c['e']['id']))
    w('\n-- Fecha de nacimiento ----------------------------------------------------\n')
    for c in sorted(cruzados, key=lambda c: c['nombre']):
        if c['nacimiento'] and c['nacimiento'] != c['e']['nacimiento']:
            w("UPDATE persona SET fechanacimiento = '%s' WHERE idpersona = %d;  -- %s\n"
              % (c['nacimiento'], c['e']['id'], c['nombre']))
    w('\n-- Fecha de ingreso y sueldo basico, en el contrato -----------------------\n')
    for c in sorted(cruzados, key=lambda c: c['nombre']):
        if None is c['e']['contrato']:
            continue
        campos = []
        if c['ingreso']:
            campos.append("fechainicio = '%s'" % c['ingreso'])
        if c['sueldo']:
            campos.append("haberbasicolaboral = %s" % c['sueldo'])
        if campos:
            w("UPDATE contrato SET %s WHERE idcontrato = %d;  -- %s\n"
              % (', '.join(campos), c['e']['contrato'], c['nombre']))
    w('\n-- Regimen de aportes -----------------------------------------------------\n')
    conafp = [c for c in cruzados if c['afp'] and c['e']['contrato']]
    sinafp = [c for c in cruzados if not c['afp'] and c['e']['contrato']]
    w('-- Con AFP (%d): aportan trabajador y empleador.\n' % len(conafp))
    w("UPDATE contrato SET idregimenaportesip = (SELECT idregimenaportesip FROM regimenaportesip WHERE pordefecto = 1)\n")
    w(" WHERE idcontrato IN (%s);\n" % ', '.join(str(c['e']['contrato']) for c in conafp))
    w('-- Sin AFP (%d): no aporta nadie.\n' % len(sinafp))
    w("UPDATE contrato SET idregimenaportesip = (SELECT idregimenaportesip FROM regimenaportesip WHERE nombre = 'Sin AFP')\n")
    w(" WHERE idcontrato IN (%s);\n" % ', '.join(str(c['e']['contrato']) for c in sinafp))

    w('\n\n-- Duplicados: se marca el segundo apellido del que NO tiene contrato -------\n')
    w('-- No se borran: eliminarlos exige revisar antes las referencias de otros modulos\n')
    w('-- -asientos, ordenes de compra, vales, usuarios- y reasignarlas al que sobrevive.\n')
    for sobrevive, marcar, nombre in DUPLICADOS:
        w('-- %s: manda el id %d, se marca el id %d\n' % (nombre, sobrevive, marcar))
        w("UPDATE persona SET apellidomaterno = CONCAT(IFNULL(apellidomaterno, ''), ' DUPLICA')\n")
        w(" WHERE idpersona = %d AND apellidomaterno NOT LIKE '%%DUPLICA';\n" % marcar)

    w('\n\n-- ===========================================================================\n')
    w('-- PENDIENTES - no se tocan, quedan para decidir\n')
    w('-- ===========================================================================\n')

    if sin_cruzar:
        w('--\n-- 1) En la planilla oficial pero NO existen en el sistema. Hay que darlos de alta:\n')
        for ci, nombre in sin_cruzar:
            w('--    CI %-14s %s\n' % (ci, nombre))

    w('--\n-- 2) Un mismo CI en dos personas DISTINTAS. No son duplicados: el CI esta mal\n')
    w('--    cargado en alguno de los dos. Ambos tienen contrato.\n')
    for ci, id1, n1, id2, n2 in CI_COMPARTIDO:
        w('--    CI %-10s id %-4d %-32s | id %-4d %s\n' % (ci, id1, n1, id2, n2))
    w('--    ALMANZA QUIROZ y ELVIS OCTAVIO BUSTAMANTE si estan en la planilla y su CI se\n')
    w('--    corrige mas arriba; los otros cuatro quedan compartiendo el CI.\n')

    w('--\n-- 3) `empleado.flagafp` y `contrato.activofonpension` quedan como estan. Se\n')
    w('--    verifico que NINGUN calculo los lee: el aporte al SIP se resuelve solo por el\n')
    w('--    regimen del contrato. Revisarlos y quitarlos si no cumplen otra funcion.\n')

    w('--\n-- 4) Los marcados con DUPLICA siguen existiendo. Para eliminarlos hay que revisar\n')
    w('--    sus referencias en los demas modulos y reasignarlas al que sobrevive.\n')


if __name__ == '__main__':
    main()
