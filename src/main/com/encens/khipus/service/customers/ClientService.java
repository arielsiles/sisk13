package com.encens.khipus.service.customers;

import com.encens.khipus.model.customers.Client;

import javax.ejb.Local;
import java.util.List;

/**
 * @author
 * @version $Id: ClientService.java $
 */
@Local
public interface ClientService {

    List<Client> getAllClients();

}
