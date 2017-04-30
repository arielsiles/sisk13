package com.encens.khipus.service.common;

import com.encens.khipus.model.common.Text;
import com.encens.khipus.util.TextUtil;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;

/**
 * TextServiceBean
 *
 * @author
 * @version 1.2.3
 */
@Name("textService")
@Stateless
@AutoCreate
public class TextServiceBean implements TextService {


    @In(value = "#{entityManager}")
    private EntityManager em;


    /**
     * This method create,updated or delete the Text entity, but this operation
     * depends of Id value and Value Attribute. E.g.
     * If text.getId() == null, should create a Text entity
     * If text.getId() != null and TextUtil.isEmpty(text)==True, should delete a Text entity
     * If text.getId() != null and TextUtil.isEmpty(text)==False, should update a Text entity
     *
     * @param text
     */
    public Text handleText(Text text) throws PersistenceException {
        if (text != null) {
            if (text.getId() == null) {
                if (!TextUtil.isEmpty(text)) {
                    em.persist(text);
                } else {
                    text = null;
                }
            } else {
                if (TextUtil.isEmpty(text)) {
                    try {
                        em.remove(text);
                    } catch (Exception e) {
                    }
                    text = null;
                } else {
                    em.merge(text);
                }
            }
            em.flush();
        }
        return text;
    }
}
