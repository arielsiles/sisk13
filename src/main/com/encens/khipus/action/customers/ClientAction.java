package com.encens.khipus.action.customers;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.contacts.City;
import com.encens.khipus.model.contacts.Department;
import com.encens.khipus.model.customers.Client;
import com.encens.khipus.model.customers.ClientContact;
import com.encens.khipus.model.customers.PaymentMethodSin;
import com.encens.khipus.model.finances.CashAccount;
import com.encens.khipus.service.customers.ClientService;
import com.encens.khipus.service.finances.CashAccountService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.international.StatusMessage;

/**
 * @author
 * @version 2.2
 */

@Name("clientAction")
@Scope(ScopeType.CONVERSATION)
public class ClientAction extends GenericAction<Client> {

    @In
    private ClientService clientService;
    @In
    private CashAccountService cashAccountService;

    private String clientName;
    private Boolean personFlag = Boolean.TRUE;
    private PaymentMethodSin paymentMethodSin;
    private CashAccount regularizeCashAccount;

    private boolean showNitExtension = false;

    /* Contacto en edicion (holder del modal de personas de contacto) */
    private ClientContact contact;
    private boolean contactIsNew;

    /* Modal "+ nueva ciudad": nombre tecleado y a quien se asigna (client|contact) */
    private String newCityName;
    private String cityTarget;

    @Factory(value = "client", scope = ScopeType.STATELESS)
    public Client initClient() {
        return getInstance();
    }

    public Client getClient() {
        return getInstance();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "fullName";
    }

    @Override
    @Begin(ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    public String select(Client instance) {
        String outCome = super.select(instance);
        setPaymentMethodSin(clientService.findPaymentMethodSin(instance.getPaymentMethodTypeCode()));

        if (instance.getRegularizeAccount() != null)
            setRegularizeCashAccount(cashAccountService.findByAccountCode(instance.getRegularizeAccount()));

        return outCome;
    }

    @End
    @Override
    public String create() {

        getInstance().setCommission(0.0);
        getInstance().setGuarantee(0.0);

        if (getInstance().getName() == null)
            getInstance().setName("");
        if (getInstance().getLastName() == null)
            getInstance().setLastName("");
        if (getInstance().getMaidenName() == null)
            getInstance().setMaidenName("");

        if (regularizeCashAccount != null)
            getInstance().setRegularizeAccount(regularizeCashAccount.getAccountCode());

        getInstance().setPaymentMethodTypeCode(this.paymentMethodSin.getCode());

        return super.create();

    }

    @End
    @Override
    public String update() {
        getInstance().setPaymentMethodTypeCode(this.paymentMethodSin.getCode());
        if (regularizeCashAccount != null)
            getInstance().setRegularizeAccount(regularizeCashAccount.getAccountCode());
        return super.update();
    }

    public void clearRegularizeAccount() {
        setRegularizeCashAccount(null);
        getInstance().setRegularizeAccount(null);
    }

    /* ==================== Personas de contacto ==================== */

    /**
     * Prepara un contacto nuevo para el modal (aun no se agrega a la lista;
     * se agrega recien al aceptar).
     */
    public void newContact() {
        contact = new ClientContact();
        contact.setClient(getInstance());
        contact.setActive(Boolean.TRUE);
        contact.setPrimaryContact(Boolean.FALSE);
        contactIsNew = true;
    }

    /**
     * Abre el modal apuntando a un contacto existente de la lista (edicion in situ).
     */
    public void editContact(ClientContact clientContact) {
        contact = clientContact;
        contactIsNew = false;
    }

    public String getContactModalTitle() {
        return messages.get(contactIsNew ? "ClientContact.new" : "ClientContact.edit");
    }

    /**
     * Confirma el contacto del modal. Si es nuevo lo agrega a la lista del cliente;
     * los cambios se persisten por cascade al guardar el cliente. Garantiza un solo
     * contacto principal.
     */
    public void acceptContact() {
        if (contact == null)
            return;

        if (contact.getClient() == null)
            contact.setClient(getInstance());

        if (Boolean.TRUE.equals(contact.getPrimaryContact())) {
            for (ClientContact other : getInstance().getContacts()) {
                if (other != contact)
                    other.setPrimaryContact(Boolean.FALSE);
            }
        }

        // Se persiste de inmediato (el cliente ya existe): al Aceptar queda guardado
        // aunque luego se cancele la edicion del cliente.
        ClientContact saved = clientService.saveContact(contact);
        if (!getInstance().getContacts().contains(saved))
            getInstance().getContacts().add(saved);

        contact = null;
    }

    /**
     * Quita un contacto de la lista y lo elimina de la BD de inmediato.
     */
    public void removeContact(ClientContact clientContact) {
        getInstance().getContacts().remove(clientContact);
        clientService.deleteContact(clientContact);
    }

    public ClientContact getContact() {
        return contact;
    }

    public void setContact(ClientContact contact) {
        this.contact = contact;
    }

    /* ==================== Ciudad (catalogo + alta rapida) ==================== */

    /** Al cambiar el departamento se limpia la ciudad (podria no pertenecer al nuevo). */
    public void clearClientCity() {
        getInstance().setCity(null);
    }

    public void clearContactCity() {
        if (contact != null)
            contact.setCity(null);
    }

    /** Al cambiar el pais se limpian departamento y ciudad (quedarian fuera de lista). */
    public void clearClientDepartment() {
        getInstance().setDepartment(null);
        getInstance().setCity(null);
    }

    public void clearContactDepartment() {
        if (contact != null) {
            contact.setDepartment(null);
            contact.setCity(null);
        }
    }

    /** Abre el modal de alta de ciudad para el destino indicado (client|contact). */
    public void openNewCity(String target) {
        this.cityTarget = target;
        this.newCityName = null;
    }

    private Department targetDepartment() {
        return "contact".equals(cityTarget)
                ? (contact != null ? contact.getDepartment() : null)
                : getInstance().getDepartment();
    }

    public String getNewCityDepartmentName() {
        Department dep = targetDepartment();
        return dep != null ? dep.getName() : "";
    }

    /**
     * Registra la ciudad nueva (find-or-create: si ya existe en el departamento
     * la reutiliza, evitando duplicados) y la selecciona en el destino.
     */
    public void acceptNewCity() {
        Department dep = targetDepartment();
        if (dep == null) {
            facesMessages.add(StatusMessage.Severity.WARN, messages.get("Client.city.needDepartment"));
            return;
        }
        City city = clientService.findOrCreateCity(newCityName, dep);
        if (city == null)
            return;
        if ("contact".equals(cityTarget))
            contact.setCity(city);
        else
            getInstance().setCity(city);
    }

    public String getNewCityName() {
        return newCityName;
    }

    public void setNewCityName(String newCityName) {
        this.newCityName = newCityName;
    }

    public void updateShowNitExtension() {
        showNitExtension = true;
    }

    public void changePersonFlag(){
        System.out.println("------------> changePersonFlag: " + this.personFlag);
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }


    public Boolean getPersonFlag() {
        return personFlag;
    }

    public void setPersonFlag(Boolean personFlag) {
        this.personFlag = personFlag;
    }

    public boolean isShowNitExtension() {
        return showNitExtension;
    }

    public void setShowNitExtension(boolean showNitExtension) {
        this.showNitExtension = showNitExtension;
    }

    public boolean hasDocumentTypeCI(){
        if (getInstance().getInvoiceDocumentType() == null)
            return false;
        Integer sinCode = getInstance().getInvoiceDocumentType().getSinCode();
        return sinCode != null && sinCode == 1;
    }

    public PaymentMethodSin getPaymentMethodSin() {
        return paymentMethodSin;
    }

    public void setPaymentMethodSin(PaymentMethodSin paymentMethodSin) {
        this.paymentMethodSin = paymentMethodSin;
    }

    public CashAccount getRegularizeCashAccount() {
        return regularizeCashAccount;
    }

    public void setRegularizeCashAccount(CashAccount regularizeCashAccount) {
        this.regularizeCashAccount = regularizeCashAccount;
    }
}
