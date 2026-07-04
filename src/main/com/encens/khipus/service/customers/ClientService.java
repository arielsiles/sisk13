package com.encens.khipus.service.customers;

import com.encens.khipus.model.contacts.City;
import com.encens.khipus.model.contacts.Department;
import com.encens.khipus.model.customers.Client;
import com.encens.khipus.model.customers.ClientContact;
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

    /**
     * Busca la ciudad por nombre dentro del departamento; si no existe la crea
     * en el catalogo. Devuelve null si el nombre o el departamento son vacios.
     */
    City findOrCreateCity(String name, Department department);

    /** Ciudades del departamento cuyo nombre empieza con el prefijo (autocompletar). */
    List<City> suggestCities(Department department, String prefix);

    /** Busca la ciudad exacta (por nombre+departamento). null si no existe. NO crea. */
    City findCity(String name, Department department);

    /** Persiste (nuevo) o actualiza el contacto y hace flush inmediato. */
    ClientContact saveContact(ClientContact contact);

    /** Elimina el contacto de la BD de inmediato. */
    void deleteContact(ClientContact contact);

}
