package com.encens.khipus.action.finances;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.finances.UserCashBoxState;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 * Data model for User
 *
 * @author:
 */
@Name("userCashBoxDataModel")
@Scope(ScopeType.PAGE)
public class UserCashBoxDataModel extends QueryDataModel<Long, User> {

    private User criteria;

    private static final String[] RESTRICTIONS = {
            "lower(user.employee.firstName) like concat('%', concat(lower(#{userCashBoxDataModel.criteria.employee.firstName}), '%'))",
            "lower(user.employee.lastName) like concat('%', concat(lower(#{userCashBoxDataModel.criteria.employee.lastName}), '%'))"};

    @Override
    public String getEjbql() {

        //TODO this query needs to be checked out.
        /*return "select distinct user from User user where not exists (select u.user from UserCashBox " +
                "u where u.user = user and u.state = #{state})";*/
        return "select distinct user from User user inner join user.roles role inner join role.accessRights accessRight " +
                "where accessRight.function.code ='CASHBOX' " +
                "and not exists (select u.user from UserCashBox u where u.user = user and u.state = #{state})";

    }

    @Factory(value = "state", scope = ScopeType.EVENT)
    public UserCashBoxState getUserCashBoxState() {
        return UserCashBoxState.ACTIVE;
    }

    @Create
    public void defaultSort() {
        sortProperty = "user.username";
        if (criteria != null) {
            initEmployee();
        } else {
            criteria = new User();
            initEmployee();
        }
    }

    private void initEmployee() {
        if (getCriteria().getEmployee() == null) {
            getCriteria().setEmployee(new Employee());
        }
    }

    @Override
    public User getCriteria() {
        return criteria;
    }

    @Override
    public void setCriteria(User criteria) {
        this.criteria = criteria;
    }


    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

}
