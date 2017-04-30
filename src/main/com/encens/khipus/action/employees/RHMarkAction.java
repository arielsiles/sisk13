package com.encens.khipus.action.employees;

import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.employees.Mark;
import com.encens.khipus.model.employees.RHMark;
import com.encens.khipus.service.employees.RHMarkService;
import com.encens.khipus.util.Constants;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.util.Reflections;


import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Actions for RHMark
 *
 * @author
 */

@Name("rHMarkAction")
@Scope(ScopeType.CONVERSATION)
public class RHMarkAction extends GenericAction<RHMark> {

    @In("#{entityManager}")
    private EntityManager em;

    private Date dateRegister;
    private Object displayPropertyValueMarak;

    @Factory(value = "rHMark", scope = ScopeType.STATELESS)
    //@Restrict("#{s:hasPermission('RHMARK','VIEW')}")
    public RHMark initRHMark() {
        return getInstance();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "marDate";
    }

    @Override
    @End(beforeRedirect=true)
    public String create() {
        try {
            RHMark rhMark = getInstance();

            List<Object[]> result = em.createQuery("select p.markCode,p.firstName, p.lastName , p.maidenName from Employee p where p.markCode = :markCode")
                    .setParameter("markCode", rhMark.getMarPerId().toString()).getResultList();

            if(result.size()==0)
            {
                addNoFoundCIMessage(rhMark);
                rhMark = createInstance();
                return Outcome.REDISPLAY;
            }
            rhMark.setCompany(new Company(Constants.defaultCompanyId, Constants.defaultCompanyName));
            rhMark.setSeat("1");
            rhMark.setMarRefCard((String)result.get(0)[0]);
            rhMark.setMarIpPc("10.0.0.200");//ip por defecto del servidor
            rhMark.setControl(1);
            rhMark.setMarState("ACTIVO");
            rhMark.setMarTime(rhMark.getStartMarDate());
            getService().create(rhMark);
            addCreateRegisterMessage(rhMark,(String)result.get(0)[1] +" "+(String)result.get(0)[2]+" "+(String)result.get(0)[3]);
            rhMark = createInstance();
            return Outcome.SUCCESS;
        } catch (NoResultException e) {
            return Outcome.REDISPLAY;
        } catch (EntryDuplicatedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            return Outcome.REDISPLAY;
        }
    }

    private void clearForm(RHMark rhMark)
    {
        rhMark.setDescription("");
        rhMark.setMarPerId(0);
    }

    protected void addNoFoundCIMessage(RHMark rhMark) {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "Common.message.idPerson", rhMark.getMarPerId().toString());
    }

    protected void addCreateRegisterMessage(RHMark rhMark,String name) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        //Date myDate = fmt.parse(rhMark.getMarTime());
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "Common.message.register", name, dateFormat.format(rhMark.getMarTime().getTime()));
    }

    public Date getDateRegister() {
        Date date = new Date();
        if (null == dateRegister) {
            dateRegister = new Date();
            return dateRegister;
        }
        return dateRegister;
    }

    public void setDateRegister(Date dateRegister) {
        this.dateRegister = dateRegister;
    }

    public Object getDisplayPropertyValueMarak() {
        Object entity = getInstance();
        if (entity != null && getDisplayNameProperty() != null) {
            Method entityDisplayPropertyGetter = Reflections.getGetterMethod(entity.getClass(), getDisplayNameProperty());
            try {
                Object value = Reflections.invoke(entityDisplayPropertyGetter, entity);
                if (value != null) {
                    return value;
                } else {
                    return getDisplayNameMessage();
                }
            } catch (Exception e) {
                throw new RuntimeException("Error trying to recover the value of the entity for displayNameProperty");
            }
        } else {
            return getDisplayNameMessage();
        }
    }

}