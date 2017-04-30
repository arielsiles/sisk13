package com.encens.khipus.service.employees;

import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.employees.RHMark;
import com.encens.khipus.util.DateUtils;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;
import org.omg.PortableServer.POAPackage.AdapterAlreadyExistsHelper;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TemporalType;
import java.util.*;

/**
 * RHMark service implementation class
 *
 * @author
 */

@Name("rhMarkService")
@Stateless
@AutoCreate
public class RHMarkServiceBean implements RHMarkService {
    @In("#{entityManager}")
    private EntityManager em;
    @Logger
    protected Log log;

    public List<RHMark> findRHMarkByEmployeeIdNumberByInitDateByEndDate(Employee employee, Date initDate, Date endDate) {
        try {
            Query query = em.createNamedQuery("RHMark.findRHMarkByMarkCodeByInitDateByEndDate")
                    .setParameter("markCode", employee.getMarkCode())
                    .setParameter("initDate", initDate)
                    .setParameter("endDate", endDate);
            return query.getResultList();
        } catch (NoResultException e) {
            return new ArrayList<RHMark>(0);
        }
    }

    public void registerMark(RHMark rhMark){
        try{
        em.createNativeQuery("insert into marcados.RHMARCADO (IDRHMARCADO,MARFECHA,MARIPPC,MARPERID,MARREFTARJETA,MARESTADO,SEDE,IDCOMPANIA,MAR_IN_OUT,MARHORA) \n" +
                "values (marcados.seq_idrhmarcado.NEXTVAL,:date,:ip,:narPerID,:narPerID,null,'COCHABAMBA','1','GENERAL',:date)")
                .setParameter("date",new Date())
                .setParameter("ip",rhMark.getMarIpPc())
                .setParameter("narPerID",rhMark.getMarRefCard())
                .executeUpdate();
        } catch (Exception e)
        {

        }
    }

    public List<RHMark> findRHMarkByInitDateByEndDate(Date initDate, Date endDate) {
        try {
            Query query = em.createNamedQuery("RHMark.findRHMarkByInitDateAndEndDate")
                    .setParameter("initDate", initDate)
                    .setParameter("endDate", endDate);
            return query.getResultList();
        } catch (NoResultException e) {
            return new ArrayList<RHMark>(0);
        }
    }

    @Override
    public boolean verificateIdPerson(int idPerson) {
        try{
            List<Object[]> result = em.createQuery("select p from Person p where p.idNumber = :idPersona")
              .setParameter("idPersona", idPerson).getResultList();
            if(result.size() > 0)
                return true;

        } catch (NoResultException r)
        {
            return false;
        }
        return true;
    }

    public Map<Date, List<Date>> getRHMarkDateTimeMapByDateRange(Employee employee, Date initDate, Date endDate) {
        Map<Date, List<Date>> result = new HashMap<Date, List<Date>>();
        try {
            List<Object[]> rhMarkDateList = em.createNamedQuery("RHMark.findRHMarkDateForPayrollGeneration")
                    .setParameter("markCode", employee.getMarkCode())
                    .setParameter("initDate", initDate)
                    .setParameter("endDate", endDate).getResultList();
            for (Object rhMarkDate[] : rhMarkDateList) {
                Date date = DateUtils.toCalendar((Date) rhMarkDate[0]).getTime();
                Date time = DateUtils.joinDateAndTime((Date) rhMarkDate[0], (Date) rhMarkDate[1]).getTime();
                if (!result.containsKey(date)) {
                    List<Date> rhMarkListTemp = new ArrayList<Date>();
                    rhMarkListTemp.add(time);
                    result.put(date, rhMarkListTemp);
                } else {
                    result.get(date).add(time);
                }
            }
            return result;
        } catch (Exception e) {
            return result;
        }
    }
}