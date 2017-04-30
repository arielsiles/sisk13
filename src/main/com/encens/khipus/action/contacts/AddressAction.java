package com.encens.khipus.action.contacts;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.contacts.Address;
import com.encens.khipus.model.contacts.District;
import com.encens.khipus.model.contacts.Street;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

/**
 * Created by IntelliJ IDEA.
 * User: macmac
 * Date: 11-ene-2009
 * Time: 22:33:20
 * To change this template use File | Settings | File Templates.
 */

@Name("addressAction")
@Scope(ScopeType.CONVERSATION)
public class AddressAction extends GenericAction<Address> {


    private District district;
    private boolean getDistrictOnce = true;
    private Street addressStreet;


    @Factory(value = "address", scope = ScopeType.STATELESS)
    public Address initAddress() {
        return getInstance();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "name";
    }

    public District getDistrict() {
        if (isManaged() && getDistrictOnce) {
            district = getInstance().getStreet().getDistrict();
            getDistrictOnce = false;
        }
        return district;
    }

    public void setDistrict(District district) {
        this.district = district;
    }

    public Street getAddressStreet() {
        return addressStreet;
    }

    public void setAddressStreet(Street addressStreet) {
        System.out.println("entro **setAddressStreet** " + addressStreet.getName() + " : " + addressStreet.getId());

        //getInstance().setAddress(addressBusiness);
        Address addressS = new Address();
        addressS.setStreet(addressStreet);
        System.out.println("entro **setAddressBusiness** n " + addressS.getStreet().getName() + " number: " + addressS.getStreet().getId());
        //getInstance().setAddress(addressS);

        this.addressStreet = addressStreet;
    }
}
