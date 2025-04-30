package com.encens.khipus.action.reports;

import com.encens.khipus.service.production.CollectMaterialService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Name("dashboardReportAction")
@Scope(ScopeType.PAGE)
public class DashboardReportAction implements Serializable {

    @In
    CollectMaterialService collectMaterialService;

    public String getMateriaPrimaChartData() {
        try {
            List<Object[]> rawData = collectMaterialService.findCollectMaterial();
            List<Map<String, Object>> list = new ArrayList<>();

            System.out.println("rawData: " + rawData.size());

            for (Object[] row : rawData) {
                Map<String, Object> map = new HashMap<>();
                map.put("name", row[0] != null ? row[0].toString() : "");
                map.put("peso", row[1] != null ? Math.round(((Number) row[1]).doubleValue() / 1000 * 100.0) / 100.0 : 0.0); // Se convierte a toneladas
                list.add(map);
            }

            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(list);
        } catch (Exception e) {
            e.printStackTrace();
            return "[]"; // retornar un JSON vacío si ocurre error
        }
    }

    public String getCollectMaterialByProducerChartData() {
        try {
            List<Object[]> rawData = collectMaterialService.findCollectMaterialByProducer();
            List<Map<String, Object>> list = new ArrayList<>();

            System.out.println("rawData: " + rawData.size());

            for (Object[] row : rawData) {
                Map<String, Object> map = new HashMap<>();
                map.put("name", row[0] != null ? row[0].toString() : "");
                map.put("peso", row[1] != null ? Math.round(((Number) row[1]).doubleValue() / 1000 * 100.0) / 100.0 : 0.0); // Se convierte a toneladas
                list.add(map);
            }

            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(list);
        } catch (Exception e) {
            e.printStackTrace();
            return "[]"; // retornar un JSON vacío si ocurre error
        }
    }

    public String getCollectMaterialByZoneChartData() {
        try {
            List<Object[]> rawData = collectMaterialService.findCollectMaterialByZone();
            List<Map<String, Object>> list = new ArrayList<>();

            System.out.println("rawData: " + rawData.size());

            for (Object[] row : rawData) {
                Map<String, Object> map = new HashMap<>();
                map.put("name", row[0] != null ? row[0].toString() : "");
                map.put("peso", row[1] != null ? Math.round(((Number) row[1]).doubleValue() / 1000 * 100.0) / 100.0 : 0.0); // Se convierte a toneladas
                list.add(map);
            }

            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(list);
        } catch (Exception e) {
            e.printStackTrace();
            return "[]"; // retornar un JSON vacío si ocurre error
        }
    }


    /** Para Ejemplos: **/

    public String getEmployeeChartData() {
        try {
            List<Empleado> employees = new ArrayList<>();
            employees.add(new Empleado("Ariel Siles", 1000.0));
            employees.add(new Empleado("Juan Siles", 2000.0));
            employees.add(new Empleado("Robert Rojas", 3000.0));

            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(employees);

        } catch (Exception e) {
            e.printStackTrace();
            return "[]"; // En caso de error, devuelve array vacío
        }
    }

    private class Empleado {
        private String name;
        private Double salary;

        public Empleado(String name, Double salary) {
            this.name = name;
            this.salary = salary;
        }

        public String getName() {
            return name;
        }

        public Double getSalary() {
            return salary;
        }
    }

}
