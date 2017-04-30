package com.encens.khipus.service.employees;

import com.encens.khipus.model.employees.Sector;

import javax.ejb.Local;
import java.util.List;

/**
 * @author
 * @version 1.1.8
 */
@Local
public interface SectorService {

    List<Sector> getAllSector();
}
