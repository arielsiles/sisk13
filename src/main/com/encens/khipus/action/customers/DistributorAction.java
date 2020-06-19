package com.encens.khipus.action.customers;

import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.contacts.Country;
import com.encens.khipus.model.contacts.Department;
import com.encens.khipus.model.contacts.Extension;
import com.encens.khipus.model.contacts.Person;
import com.encens.khipus.model.customers.Distributor;
import com.encens.khipus.service.customers.ExtensionService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;

import java.util.List;

/**
 * Actions for Credit
 *
 * @author:
 */

@Name("distributorAction")
@Scope(ScopeType.CONVERSATION)
public class DistributorAction extends GenericAction<Distributor> {

    @In
    private ExtensionService extensionService;

    public List<Extension> extensionList;
    private boolean showExtension = false;

    private Country country;
    private Department department;
    private Person person;

    @Factory(value = "distributor")
    public Distributor initDistributor() {
        return getInstance();
    }


    /*@Override
    @Begin(ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    public String select(Distributor instance) {

        String outCome = super.select(instance);
        //setPerson(getInstance().getPerson());
        assignPerson(instance.getPerson());

        updateShowExtension();
        updateShowExtensionPerson();
        return outCome;
    }*/

    @Override
    @End
    public String create() {
        try {

            if (this.person != null) {
                addCreatedMessage();
            }

            /*if (this.person == null){

                Person person = new Person();
                person.setFirstName(getInstance().getFirstName());
                person.setLastName(getInstance().getLastName());
                person.setMaidenName(getInstance().getMaidenName());

                person.setDocumentType(getInstance().getDocumentType());
                person.setIdNumber(getInstance().getIdNumber());
                person.setExtensionSite(getInstance().getExtensionSite());

                person.setBirthDay(getInstance().getBirthDay());
                person.setSalutation(getInstance().getSalutation());
                person.setCellphone(getInstance().getCellphone());
                person.setHomeAddress(getInstance().getHomeAddress());
                person.setProfession(getInstance().getProfession());
                person.setMaritalStatus(getInstance().getMaritalStatus());
                person.setGender(getInstance().getGender());

                getService().create(person);
                getInstance().setPerson(person);
                partnerService.createPartner(getInstance());
                addCreatedMessage();
            }*/


            return Outcome.SUCCESS;
        } catch (Exception e) {
            addDuplicatedMessage();
            return Outcome.REDISPLAY;
        }
    }

    /*@Override
    @End
    public String update() {

        getInstance().setPerson(this.person);
        this.person.setGender(getInstance().getGender());
        this.person.setMaritalStatus(getInstance().getMaritalStatus());
        this.person.setSalutation(getInstance().getSalutation());
        this.person.setIdNumber(getInstance().getIdNumber());
        return super.update();

    }
*/
    public boolean isShowExtension() {
        return showExtension;
    }

    public void updateShowExtension() {
        extensionList = extensionService.findExtensionsByDocumentType(getInstance().getDocumentType());
        showExtension = extensionList != null && !extensionList.isEmpty();
        if (!showExtension) {
            getInstance().setExtensionSite(null);
        }
    }


    public void assignPerson(Person person) {
        if (person != null) {
            try {
                person = getService().findById(Person.class, person.getId());
            } catch (EntryNotFoundException e) {
                entryNotFoundLog();
            }
        }

        setPerson(person);
        updateShowExtensionPerson();
        getInstance().setFirstName(person.getFirstName());
        getInstance().setLastName(person.getLastName());
        getInstance().setMaidenName(person.getMaidenName());
        getInstance().setGender(person.getGender());
        getInstance().setSalutation(person.getSalutation());
        getInstance().setProfession(person.getProfession());
        getInstance().setMaritalStatus(person.getMaritalStatus());

        getInstance().setIdNumber(person.getIdNumber());
        getInstance().setDocumentType(person.getDocumentType());
        getInstance().setExtensionSite(person.getExtensionSite());

        System.out.println("DocumentType: " + getPerson().getDocumentType().getName());
        System.out.println("Extension: " + getPerson().getExtensionSite().getExtension());

    }

    public void updateShowExtensionPerson() {
        extensionList = extensionService.findExtensionsByDocumentType(getPerson().getDocumentType());
        showExtension = extensionList != null && !extensionList.isEmpty();
        if (!showExtension) {
            getPerson().setExtensionSite(null);
        }
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }
}
