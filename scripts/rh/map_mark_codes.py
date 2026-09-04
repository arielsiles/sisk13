# -*- coding: utf-8 -*-
"""Cruza el export de marcaciones del biometrico contra `empleado` por nombre y genera
el UPDATE de `codigomarcacion`. No escribe en la base: solo emite SQL."""
import difflib, io, os, re, sys, subprocess, unicodedata, zipfile
import xml.etree.ElementTree as ET

NS = '{http://schemas.openxmlformats.org/spreadsheetml/2006/main}'
MYSQL = r'C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe'


def read_sheet_rows(path):
    z = zipfile.ZipFile(path)
    shared = [''.join(t.text or '' for t in si.iter(NS + 't'))
              for si in ET.fromstring(z.read('xl/sharedStrings.xml')).findall(NS + 'si')] \
        if 'xl/sharedStrings.xml' in z.namelist() else []

    def colidx(ref):
        s = ''.join(c for c in ref if c.isalpha())
        n = 0
        for c in s:
            n = n * 26 + (ord(c) - 64)
        return n - 1

    for name in [n for n in z.namelist() if n.startswith('xl/worksheets/sheet')]:
        for r in ET.fromstring(z.read(name)).iter(NS + 'row'):
            cells, auto = {}, 0
            for c in r.findall(NS + 'c'):
                ref = c.get('r')
                idx = colidx(ref) if ref else auto
                auto = idx + 1
                v = c.find(NS + 'v')
                val = (shared[int(v.text)] if c.get('t') == 's' else (v.text or '')) if v is not None else ''
                cells[idx] = val
            yield [cells.get(i, '') for i in range(max(cells) + 1)] if cells else []


def norm(text):
    """Solo letras, sin tildes, en mayusculas: el dispositivo pega los nombres sin espacios."""
    text = unicodedata.normalize('NFKD', text)
    text = ''.join(c for c in text if not unicodedata.combining(c))
    text = text.replace(u'\u00d1', 'N').replace(u'\u00f1', 'N')
    return re.sub(r'[^A-Za-z]', '', text).upper()


def device_pairs(path):
    """{codigo del dispositivo: nombre tal como lo tiene el dispositivo}"""
    pairs, id_col, name_col = {}, None, None
    for row in read_sheet_rows(path):
        if id_col is None:
            for i, cell in enumerate(row):
                head = norm(cell)
                if head.startswith('IDDELEMPLEADO'):
                    id_col = i
                elif head == 'NOMBRES':
                    name_col = i
            continue
        if id_col >= len(row) or name_col >= len(row):
            continue
        code, name = row[id_col].strip(), row[name_col].strip()
        if code and name:
            pairs.setdefault(code, name)
    return pairs


def db_employees(db, user, password):
    sql = ("SELECT e.idempleado, p.nombres, p.apellidopaterno, p.apellidomaterno,"
           " e.codigomarcacion, e.fechasalida"
           " FROM empleado e JOIN persona p ON p.idpersona = e.idempleado")
    env = dict(os.environ, MYSQL_PWD=password)
    out = subprocess.check_output(
        [MYSQL, '-h127.0.0.1', '-u' + user, db, '-N', '-B',
         '--default-character-set=utf8mb4', '-e', sql], env=env)
    rows = []
    for line in out.decode('utf-8').splitlines():
        f = line.split('\t')
        if len(f) >= 6:
            rows.append({'id': f[0], 'nombres': f[1], 'paterno': f[2], 'materno': f[3],
                         'codigo': f[4], 'salida': None if f[5] == 'NULL' else f[5]})
    return rows


def candidate_keys(emp):
    """El dispositivo pega el nombre sin espacios y lo corta. Ademas quien lo registro no
    siempre uso el primer nombre: hay gente cargada por el segundo (PABLO FELIPE TUSCO
    figura como "Felipetusco"). Se arman todas las variantes plausibles."""
    nombres = [norm(t) for t in emp['nombres'].split() if norm(t)]
    apellidos = [norm(t) for t in (emp['paterno'] + ' ' + emp['materno']).split() if norm(t)]
    if not nombres and not apellidos:
        return []
    variantes = [nombres] + [[t] for t in nombres]
    keys = set()
    for n in variantes:
        keys.add(''.join(n + apellidos))
        keys.add(''.join(apellidos + n))
    return [k for k in keys if len(k) >= 6]


def similarity(device, key):
    """Compara solo hasta donde alcanza el mas corto: el nombre del dispositivo viene cortado."""
    n = min(len(device), len(key))
    if n < 6:
        return 0.0
    return difflib.SequenceMatcher(None, device[:n], key[:n]).ratio()


MIN_RATIO = 0.86
MIN_MARGIN = 0.04


def match(pairs, employees):
    matched, ambiguous, unmatched = [], [], []
    used = {}
    for code in sorted(pairs, key=lambda c: int(c) if c.isdigit() else 0):
        device = norm(pairs[code])
        if len(device) < 6:
            unmatched.append((code, pairs[code], 'nombre demasiado corto para cruzar'))
            continue
        scored = []
        for emp in employees:
            best = max([similarity(device, k) for k in candidate_keys(emp)] or [0.0])
            if best >= MIN_RATIO:
                scored.append((best, 0 if emp['salida'] is None else 1, emp))
        scored.sort(key=lambda t: (-t[0], t[1]))
        if not scored:
            unmatched.append((code, pairs[code], 'ningun empleado se parece'))
        elif len(scored) == 1 or scored[0][0] - scored[1][0] >= MIN_MARGIN:
            emp = scored[0][2]
            used.setdefault(emp['id'], []).append(code)
            matched.append((code, pairs[code], emp, scored[0][0]))
        else:
            ambiguous.append((code, pairs[code], [t[2] for t in scored if t[0] >= scored[0][0] - MIN_MARGIN]))
    collisions = dict((i, c) for i, c in used.items() if len(c) > 1)
    return matched, ambiguous, unmatched, collisions


def main():
    path = sys.argv[1] if len(sys.argv) > 1 else 'tmp/rh/Reporte_Marcaciones_export.xlsx'
    db = sys.argv[2] if len(sys.argv) > 2 else 'khipus'
    user = sys.argv[3] if len(sys.argv) > 3 else 'ilva'
    password = sys.argv[4] if len(sys.argv) > 4 else os.environ.get('MYSQL_PWD', '')

    pairs = device_pairs(path)
    employees = db_employees(db, user, password)
    matched, ambiguous, unmatched, collisions = match(pairs, employees)

    out = io.StringIO()
    w = out.write
    w(u'-- Actualizacion de `codigomarcacion` cruzando por nombre contra el export del biometrico.\n')
    w(u'-- Archivo: %s | empleados en base: %d | codigos en el archivo: %d\n'
      % (os.path.basename(path), len(employees), len(pairs)))
    w(u'-- Cruzados %d, ambiguos %d, sin cruzar %d.\n\n' % (len(matched), len(ambiguous), len(unmatched)))

    changed = [t for t in matched if t[2]['codigo'] != t[0]]
    w(u'-- %d empleado(s) cambian de codigo; %d ya lo tenian correcto.\n\n'
      % (len(changed), len(matched) - len(changed)))
    for code, device_name, emp, ratio in changed:
        w(u'-- %s %s %s | archivo "%s" | %s -> %s%s\n'
          % (emp['nombres'].strip(), emp['paterno'].strip(), emp['materno'].strip(),
             device_name, emp['codigo'], code,
             u'' if ratio >= 0.999 else u'   <-- parecido %.2f, VERIFICAR' % ratio))
        w(u"UPDATE empleado SET codigomarcacion = '%s' WHERE idempleado = %s;\n" % (code, emp['id']))

    if collisions:
        w(u'\n-- ATENCION: un mismo empleado quedo con mas de un codigo, revisar a mano:\n')
        for emp_id, codes in collisions.items():
            w(u'--   idempleado %s -> %s\n' % (emp_id, ', '.join(codes)))
    if ambiguous:
        w(u'\n-- Ambiguos (el nombre del dispositivo cruza con mas de un empleado):\n')
        for code, device_name, hits in ambiguous:
            w(u'--   %s "%s" -> %s\n' % (code, device_name,
                                         '; '.join('%s %s %s (id %s)' % (h['nombres'].strip(), h['paterno'].strip(),
                                                                         h['materno'].strip(), h['id']) for h in hits)))
    if unmatched:
        w(u'\n-- Sin cruzar (no hay empleado con ese nombre; hay que crearlo o corregir el nombre):\n')
        for code, device_name, why in unmatched:
            w(u'--   %s "%s"  (%s)\n' % (code, device_name, why))

    target = os.environ.get('SQL_OUT')
    if target:
        io.open(target, 'w', encoding='utf-8').write(out.getvalue())
        sys.stderr.write('SQL escrito en %s\n' % target)
    else:
        sys.stdout.write(out.getvalue())


if __name__ == '__main__':
    main()
