package com.encens.khipus.service.customers;

import com.encens.khipus.model.customers.Client;
import com.encens.khipus.model.customers.PaymentMethodSin;

import javax.ejb.Local;
import java.util.Date;
import java.util.List;

/**
 * @author
 * @version $Id: ClientService.java $
 */
@Local
public interface ClientService {

    List<Client> getAllClients();
    Double getBalanceClient(Date startDate, String cashAccountCode, Long clientId);
    PaymentMethodSin findPaymentMethodSin(Integer code);

    Client findClientByIdNumber(String idNumber);

}
