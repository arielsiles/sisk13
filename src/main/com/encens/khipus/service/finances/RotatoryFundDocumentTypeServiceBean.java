package com.encens.khipus.service.finances;

import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.finances.RotatoryFundDocumentType;
import com.encens.khipus.service.common.SequenceGeneratorService;
import com.encens.khipus.util.Constants;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;

import static javax.ejb.TransactionAttributeType.REQUIRES_NEW;

/**
 * @author
 * @version 2.40
 */
@Stateless
@Name("rotatoryFundDocumentTypeService")
@AutoCreate
public class RotatoryFundDocumentTypeServiceBean extends GenericServiceBean implements RotatoryFundDocumentTypeService {

    @In
    private SequenceGeneratorService sequenceGeneratorService;

    public RotatoryFundDocumentType load(RotatoryFundDocumentType documentType) throws EntryNotFoundException {
        RotatoryFundDocumentType result = null;
        if (documentType != null) {
            result = (RotatoryFundDocumentType) getEntityManager().createNamedQuery("RotatoryFundDocumentType.findById")
                    .setParameter("rotatoryFundDocumentTypeId", documentType.getId())
                    .getSingleResult();
        }
        if (result == null) {
            throw new EntryNotFoundException("Entity(" + RotatoryFundDocumentType.class.getName() + ") not found");
        }
        return result;
    }

    @TransactionAttribute(REQUIRES_NEW)
    public void createDocumentType(RotatoryFundDocumentType rotatoryFundDocumentType) throws EntryDuplicatedException {
        rotatoryFundDocumentType.setCode(sequenceGeneratorService.nextValue(Constants.ROTATORYFUND_DOCUMENTYPE_CODE_SEQUENCE));
        super.create(rotatoryFundDocumentType);
    }
}
