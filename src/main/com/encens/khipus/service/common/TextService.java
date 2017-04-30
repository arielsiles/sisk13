package com.encens.khipus.service.common;

import com.encens.khipus.model.common.Text;

import javax.ejb.Local;

/**
 * TextService
 *
 * @author
 * @version 1.2.3
 */
@Local
public interface TextService {


    /**
     * This method create,updated or delete the Text entity, but this operation
     * depends of Id value and Value Attribute. E.g.
     * If text.getId() == null, should create a Text entity
     * If text.getId() != null and TextUtil.isEmpty(text)==True, should delete a Text entity
     * If text.getId() != null and TextUtil.isEmpty(text)==False, should update a Text entity
     *
     * @param text
     */
    Text handleText(Text text);
}
