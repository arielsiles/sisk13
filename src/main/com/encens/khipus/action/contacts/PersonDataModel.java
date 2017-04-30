package com.encens.khipus.action.contacts;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.contacts.Person;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 * Encens Team
 *
 * @author
 * @version : PersonDataModel, 27-10-2009 03:02:04 PM
 */
@Name("personDataModel")
@Scope(ScopeType.PAGE)
public class PersonDataModel extends QueryDataModel<Long, Person> {
    private static final String[] RESTRICTIONS =
            {"lower(person.firstName) like concat('%', concat(lower(#{personDataModel.criteria.firstName}), '%'))",
                    "lower(person.lastName) like concat('%', concat(lower(#{personDataModel.criteria.lastName}), '%'))",
                    "lower(person.maidenName) like concat('%', concat(lower(#{personDataModel.criteria.maidenName}), '%'))",
                    "person.idNumber like concat(#{personDataModel.criteria.idNumber}, '%')"
            };
//                    "person.id>=#{personDataModel.rootPersonId}"//remove this restriction when employees duplicated are solved
//    private Long rootPersonId = new Long(60000);

    @Create
    public void init() {
        sortProperty = "person.lastName,person.maidenName,person.firstName";
    }

    @Override
    public String getEjbql() {
        return "select person from Person person";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
