package com.encens.khipus.service.finances;

import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.interceptor.FinancesUser;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.model.finances.FinanceUser;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;

/**
 * @author
 * @version 2.1.2
 */

@Stateless
@Name("financesUserService")
@FinancesUser
@AutoCreate
public class FinancesUserServiceBean extends GenericServiceBean implements FinancesUserService {
    @In(value = "#{listEntityManager}")
    private EntityManager khipusListEm;

    @In(value = "#{listEntityManager}")
    private EntityManager financesListEm;

    @In(required = false)
    private User currentUser;

    public String getFinancesUserCode() {
        if (null == currentUser.getFinancesUser() || !currentUser.getFinancesUser()) {
            throw new RuntimeException("Current user is not a finances user.");
        }

        return currentUser.getFinancesCode();
    }

    public void createFinanceUser(User khipusUser) {
        FinanceUser financeUser = new FinanceUser();

        financeUser.setId(khipusUser.getFinancesCode());
        financeUser.setName(khipusUser.getEmployee().getFullName());

        getEntityManager().persist(financeUser);
        getEntityManager().flush();
    }

    public Boolean isAvailableCode(String financeUserCode, Long userId) {

        if (null == userId) {
            //When create new user
            FinanceUser financeUser = getFinanceUser(financeUserCode);
            return null == financeUser;
        } else {
            //When update user
            User khipusUser = getKhipusUser(userId);
            if (null == khipusUser) {
                log.debug("The user was deleted by other user.");
                return null;
            }

            if (null != khipusUser.getFinancesCode() && !"".equals(khipusUser.getFinancesCode().trim())) {
                log.debug("The user is already enabled as finance user.");
                return null;
            }

            FinanceUser financeUser = getFinanceUser(financeUserCode);
            return null == financeUser;
        }
    }

    public Boolean isFinanceUser(Long userId) {
        User khipusUser = getKhipusUser(userId);
        if (null == khipusUser) {
            log.debug("The user was deleted by other user.");
            return null;
        }

        return null != khipusUser.getFinancesUser() && khipusUser.getFinancesUser();
    }

    private FinanceUser getFinanceUser(String financeUserCode) {
        FinanceUser financeUser = financesListEm.find(FinanceUser.class, financeUserCode);
        if (null == financeUser) {
            log.debug("Cannot find Finance user for id=" + financeUserCode);
        }

        return financeUser;
    }

    private User getKhipusUser(Long userId) {
        User khipusUser = khipusListEm.find(User.class, userId);
        if (null == khipusUser) {
            log.debug("Cannot find User for id=" + userId);
        }

        return khipusUser;
    }
}
