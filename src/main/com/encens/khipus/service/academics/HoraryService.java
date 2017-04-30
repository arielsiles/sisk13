package com.encens.khipus.service.academics;

import com.encens.khipus.model.academics.Horary;

import javax.ejb.Local;

/**
 * User: Ariel
 * Date: 23-06-2010
 */
@Local
public interface HoraryService {
    public Horary getHoraryById(Long horaryId, Integer gestion, Integer period);

    void getHorarys();
}
