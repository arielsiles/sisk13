# Pruebas del motor de asistencia

El modulo no tiene tests automatizados, asi que estas dos clases son la red del
constructor de sesiones (E2.1). Se compilan contra el `khipus_jar` ya construido.

    ant compile

    JH="/c/Program Files/Java/jdk1.8.0_311"
    CP="D:/Intellij/sisk13/build/exploded-archives/khipus.ear/khipus_jar"
    OUT=/tmp/att && mkdir -p $OUT

    # 1) Los criterios de aceptacion del SPEC, con casos armados a mano
    "$JH/bin/javac" -cp "$CP" -d $OUT scripts/rh/attendance/TestSessions.java
    "$JH/bin/java"  -cp "$CP;$OUT" TestSessions

    # 2) Contra las marcaciones reales de la base
    "$JH/bin/javac" -cp "$CP" -d $OUT scripts/rh/attendance/TestRealMarks.java
    "$JH/bin/java"  -cp "$CP;$OUT;D:/Intellij/sisk13/lib/mysql-connector-java-5.0.8-bin.jar" TestRealMarks

## Por que las dos

`TestSessions` prueba lo que el SPEC pide y pasa siempre; no habria detectado
ninguno de los defectos reales. `TestRealMarks` no afirma nada: mide. Los tres
problemas que aparecieron -las marcas repetidas, el indicador que se contradice
en el mismo minuto, y el emparejamiento cruzando dias- salieron de mirar sus
numeros, no de un caso escrito a mano.

Referencia de lo que deberia dar sobre julio 2026: alrededor de 1.400 sesiones,
mas del 85% completas, promedio cercano a las 10 horas y casi ninguna sesion
anormalmente larga. Si eso se desvia mucho, algo se rompio.
