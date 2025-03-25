package com.encens.khipus.service;

import com.encens.khipus.model.customers.CustomerOrder;

import javax.ejb.Local;

@Local
public interface ReportService {
    void generateReportAsync(CustomerOrder customerOrder);
}
