package com.encens.khipus.service.employees;

import com.encens.khipus.model.customers.DocumentType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;

/**
 * User: Ariel
 * Date: 22-03-2010
 * Time: 09:07:35 PM
 */
@Stateless
@Name("documentTypeService")
@AutoCreate
public class DocumentTypeServiceBean implements DocumentTypeService {

    @In("#{entityManager}")
    private EntityManager em;

    public DocumentType getDocumentTypeDefault(Long id) {
        DocumentType result = null;
        try {
            result = (DocumentType) em.createNamedQuery("DocumentType.findDocumentTypeDefault").setParameter("id", id).getSingleResult();
        } catch (Exception e) {
        }
        return result;
    }
}