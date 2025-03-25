package com.encens.khipus.service;

import com.encens.khipus.action.customers.reports.PrintBillReportAction;
import com.encens.khipus.model.customers.CustomerOrder;
import org.jboss.aspects.asynch.Asynchronous;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;

@Stateless
@Name("reportService")
@AutoCreate
public class ReportServiceBean implements ReportService{

    @In(create = true, value = "printBillReportAction")
    private PrintBillReportAction printBillReportAction;

    @Asynchronous
    public void generateReportAsync(CustomerOrder customerOrder) {
        try {
            Thread.sleep(500); // Pequeño retardo para asegurar que los datos se guardan
            printBillReportAction.generateReport();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}