package com.encens.khipus.util;

import com.encens.khipus.service.employees.RHMarkServiceBean;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * Created by IntelliJ IDEA.
 * User:
 */
public class Updater {
    @PersistenceContext(unitName = "khipus")
    public static EntityManager em;

    public static void main(String args[]) {

        RHMarkServiceBean rhMarkServiceBean = new RHMarkServiceBean();
        upateFlag(rhMarkServiceBean);

    }

    public static void upateAFPFlag(RHMarkServiceBean rhMarkServiceBean) {
//        List<Employee> employeeList = rhMarkServiceBean.findEmployeesByIdRange(new Long(60000), new Long(200000));
//        List<Integer> ciList = new ArrayList<Integer>();
//        ciList.add(8);
//        for (Employee employee : employeeList) {
//            if (ciList.contains(employee.getIdNumber())) {
//                employee.setAfpFlag(1);
//                em.merge(employee);
//            } else {
//                employee.setAfpFlag(0);
//                em.merge(employee);
//            }
//        }
    }

    public static void upateFlag(RHMarkServiceBean rhMarkServiceBean) {
//        List<Integer> ciList = new ArrayList<Integer>();
//        ciList.add(475757);
//        for (Integer ci : ciList) {
//            List<Employee> employeeList = rhMarkServiceBean.findEmployeesByCI(ci.longValue());
//            for (Employee employee : employeeList) {
//                employee.setControlFlag(1);
//                em.merge(employee);
//            }
//        }
    }

    public static void upateRetentionFlag(RHMarkServiceBean rhMarkServiceBean) {
//        List<Employee> employeeList = rhMarkServiceBean.findEmployeesByIdRange(new Long(60000), new Long(200000));
//        List<Integer> ciList = new ArrayList<Integer>();
//        ciList.add(8);
//        for (Employee employee : employeeList) {
//            if (ciList.contains(employee.getIdNumber())) {
//                employee.setRetentionFlag(1);
//                em.merge(employee);
//            } else {
//                employee.setRetentionFlag(0);
//                em.merge(employee);
//            }
//        }
    }
}