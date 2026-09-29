-- Rebobina la secuencia IMP al mayor no_doc realmente usado, por compañía; solo si quedó adelantada.
UPDATE _sequence s
  JOIN (SELECT idcompania, MAX(CAST(no_doc AS UNSIGNED)) max_doc
          FROM sf_tmpenc
         WHERE tipo_doc = 'IMP'
         GROUP BY idcompania) t ON t.idcompania = s.idcompania
   SET s.seq_val = t.max_doc
 WHERE s.seq_name = 'IMP'
   AND s.seq_val > t.max_doc;
