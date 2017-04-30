package com.encens.khipus.service.fixedassets;

import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.fixedassets.Model;
import com.encens.khipus.model.fixedassets.Trademark;
import com.encens.khipus.util.ValidatorUtil;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import java.util.List;

/**
 * @author
 * @version 2.25
 */
@Stateless
@Name("trademarkSynchronizeService")
@AutoCreate
public class TrademarkSynchronizeServiceBean extends GenericServiceBean implements TrademarkSynchronizeService {
    @In(value = "#{listEntityManager}")
    private EntityManager eventEm;


    @SuppressWarnings(value = "unchecked")
    public Trademark synchronizeTrademark(Trademark trademark, String trademarkName) {
        if (ValidatorUtil.isBlankOrNull(trademarkName)) {
            return null;
        }

        if (null != trademark && null != getTrademarkFromDatabase(trademark.getId())) {
            getEntityManager().refresh(trademark);
            return trademark;
        }

        List<Trademark> result = eventEm
                .createNamedQuery("Trademark.findByName")
                .setParameter("name", trademarkName)
                .getResultList();

        if (null != result && !result.isEmpty()) {
            Trademark dbTrademark = result.get(0);
            return getEntityManager().find(Trademark.class, dbTrademark.getId());
        }

        Trademark newInstance = new Trademark();
        newInstance.setName(trademarkName);

        getEntityManager().persist(newInstance);
        getEntityManager().flush();
        getEntityManager().refresh(newInstance);

        return newInstance;
    }

    @SuppressWarnings(value = "unchecked")
    public Model synchronizeModel(Model model, String modelName) {
        if (ValidatorUtil.isBlankOrNull(modelName)) {
            return null;
        }

        if (null != model && null != getModelFromDatabase(model.getId())) {
            getEntityManager().refresh(model);
            return model;
        }

        List<Model> result = eventEm
                .createNamedQuery("Model.findByName")
                .setParameter("name", modelName)
                .getResultList();

        if (null != result && !result.isEmpty()) {
            Model dbModel = result.get(0);
            return getEntityManager().find(Model.class, dbModel.getId());
        }

        Model newInstance = new Model();
        newInstance.setName(modelName);

        getEntityManager().persist(newInstance);
        getEntityManager().flush();
        getEntityManager().refresh(newInstance);

        return newInstance;
    }

    private Trademark getTrademarkFromDatabase(Long id) {
        return eventEm.find(Trademark.class, id);
    }

    private Model getModelFromDatabase(Long id) {
        return eventEm.find(Model.class, id);
    }
}
