package com.encens.khipus.service.employees;

import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.employees.SIPContributionRegime;
import com.encens.khipus.model.finances.Contract;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import java.util.ArrayList;
import java.util.List;

/**
 * @author
 * @version 6.0.129
 */
@Stateless
@Name("sipContributionRegimeService")
@AutoCreate
public class SIPContributionRegimeServiceBean extends GenericServiceBean implements SIPContributionRegimeService {

    @SuppressWarnings({"unchecked"})
    public SIPContributionRegime findDefault() {
        List<SIPContributionRegime> resultList = getEntityManager()
                .createNamedQuery("SIPContributionRegime.findDefault")
                .setParameter("byDefault", Boolean.TRUE)
                .setParameter("active", Boolean.TRUE)
                .getResultList();
        return resultList.isEmpty() ? null : resultList.get(0);
    }

    @SuppressWarnings({"unchecked"})
    public List<SIPContributionRegime> findActiveList() {
        List<SIPContributionRegime> resultList = getEntityManager()
                .createNamedQuery("SIPContributionRegime.findActive")
                .setParameter("active", Boolean.TRUE)
                .getResultList();
        return null == resultList ? new ArrayList<SIPContributionRegime>() : resultList;
    }

    public SIPContributionRegime resolveRegime(Contract contract, SIPContributionRegime defaultRegime) {
        if (null != contract && null != contract.getSipContributionRegime()) {
            return contract.getSipContributionRegime();
        }
        return defaultRegime;
    }

    public Long countByName(String name) {
        return (Long) getEntityManager().createNamedQuery("SIPContributionRegime.countByName")
                .setParameter("name", name).getSingleResult();
    }

    public Long countByNameButThis(String name, Long id) {
        return (Long) getEntityManager().createNamedQuery("SIPContributionRegime.countByNameButThis")
                .setParameter("name", name)
                .setParameter("id", id).getSingleResult();
    }

    public Long countContractsByRegime(SIPContributionRegime regime) {
        return (Long) getEntityManager().createNamedQuery("SIPContributionRegime.countContractsByRegime")
                .setParameter("sipContributionRegime", regime).getSingleResult();
    }
}
