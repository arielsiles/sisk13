package com.encens.khipus.test.admin;

import com.encens.khipus.action.admin.UserAction;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.model.employees.Employee;
import org.jboss.seam.core.Manager;
import org.jboss.seam.mock.SeamTest;
import org.testng.annotations.Test;

import javax.faces.context.FacesContext;
import java.util.List;

/**
 * User action integration test
 *
 * @author
 * @version 1.0
 */
public class UserActionTest extends SeamTest {

    private User createdUser;


    @Test
    public void testCreate() throws Exception {
        new FacesRequest() {
            @Override
            protected void invokeApplication() throws Exception {
                setValue("#{credentials.username}", "ariel");
                setValue("#{credentials.password}", "ariel");
                setValue("#{authenticator.companyLogin}", "efestia");
                invokeAction("#{identity.login}");
            }
        }.run();

        /* Open the create view and recover the conversationId*/
        String cid = new FacesRequest("/admin/user.xhtml") {

            @Override
            protected void invokeApplication() throws Exception {
                Manager.instance().beginConversation();
                assert isLongRunningConversation();

            }

            protected void renderResponse() throws Exception {
                assert getValue("#{userAction.op}").equals(GenericAction.OP_CREATE);
                assert isLongRunningConversation();
            }

            @Override
            protected void afterRequest() {
                assert isInvokeApplicationComplete();
            }
        }.run();

        /** Validate a null username and an invalid email */
        new FacesRequest("/admin/user.xhtml", cid) {
            @Override
            protected void processValidations() throws Exception {
                validateValue("#{user.username}", null);
                validateValue("#{user.email}", "invalidemail");
                assert isValidationFailure();
            }


            @Override
            protected void renderResponse() throws Exception {
                assert FacesContext.getCurrentInstance().getMessages().hasNext();
                assert isLongRunningConversation();
            }

        }.run();

        /* Pressing save trying to create the user */
        new FacesRequest("/admin/user.xhtml", cid) {
            @Override
            protected void updateModelValues() throws Exception {
                //employee is null, we should expect a validation error
                setValue("#{user.username}", "ariel");
                setValue("#{user.password}", "ariel");
                setValue("#{user.confirmPassword}", "ariel");
                setValue("#{user.email}", "f@f.com");

            }

            @Override
            protected void invokeApplication() throws Exception {
                assert invokeAction("#{userAction.create}") == null;
            }

            @Override
            protected void renderResponse() throws Exception {
                assert FacesContext.getCurrentInstance().getMessages().hasNext();
                assert isLongRunningConversation();
            }

            @Override
            protected void afterRequest() {
                assert isInvokeApplicationComplete();
            }
        }.run();


        /** Let's create the user */
        new FacesRequest("/admin/user.xhtml", cid) {


            @Override
            protected void updateModelValues() throws Exception {
                assert isLongRunningConversation();


                Employee employee = new Employee();
                employee.setId(2L);
                employee.setLastName("Perez");
                employee.setFirstName("Juan");
                setValue("#{user.employee}", employee);


                setValue("#{user.username}", "javier2007");
                List roles = (List) getValue("#{roleList.resultList}");
                setValue("#{user.roles}", roles);

            }

            @Override
            protected void invokeApplication() throws Exception {
                assert Outcome.SUCCESS.equals(invokeAction("#{userAction.create}"));
                createdUser = (User) getValue("#{user}");
                assert !isLongRunningConversation();
                assert createdUser != null;
                assert ((List) getValue("#{user.roles}")).size() == 1;


            }

            @Override
            protected void afterRequest() {
                assert isInvokeApplicationComplete();
            }
        }.run();


    }


    @Test(dependsOnMethods = {"testCreate"})
    public void testUpdate() throws Exception {
        new FacesRequest() {
            @Override
            protected void invokeApplication() throws Exception {
                setValue("#{credentials.username}", "ariel");
                setValue("#{credentials.password}", "ariel");
                setValue("#{authenticator.companyLogin}", "efestia");
                invokeAction("#{identity.login}");
            }

        }.run();

        String cid = new FacesRequest("/admin/user.xhtml") {
            @Override
            protected void invokeApplication() throws Exception {
                UserAction userAction = (UserAction) getValue("#{userAction}");
                assert userAction.select(createdUser).equals(Outcome.SUCCESS);
                assert isLongRunningConversation();

            }

            @Override
            protected void renderResponse() throws Exception {
                User user = (User) getValue("#{user}");
                assert user.getUsername().equals("javier2007");
                assert user.getEmployee().getFirstName().equals("Juan");
                assert user.getEmployee().getLastName().equals("Perez");
                assert isLongRunningConversation();
            }

            @Override
            protected void afterRequest() {
                assert isInvokeApplicationComplete();
            }
        }.run();

        new FacesRequest("/admin/user.xhtml", cid) {

            @Override
            protected void updateModelValues() throws Exception {
                setValue("#{user.username}", "juan123");
                setValue("#{user.email}", "email@lastforever.com");
            }

            @Override
            protected void invokeApplication() throws Exception {
                assert invokeAction("#{userAction.update}").equals(Outcome.SUCCESS);
            }


            @Override
            protected void renderResponse() throws Exception {
                assert getValue("#{user.username}").equals("juan123");
                assert getValue("#{user.email}").equals("email@lastforever.com");
                assert !isLongRunningConversation();
            }

            @Override
            protected void afterRequest() {
                assert isInvokeApplicationComplete();
            }
        }.run();


    }


    @Test(dependsOnMethods = {"testUpdate"})
    public void testDelete() throws Exception {

        new FacesRequest() {
            @Override
            protected void invokeApplication() throws Exception {
                setValue("#{credentials.username}", "ariel");
                setValue("#{credentials.password}", "ariel");
                setValue("#{authenticator.companyLogin}", "efestia");
                invokeAction("#{identity.login}");
            }

        }.run();

        String cid = new FacesRequest("/admin/user.xhtml") {
            @Override
            protected void invokeApplication() throws Exception {
                UserAction userAction = (UserAction) getValue("#{userAction}");
                assert userAction.select(createdUser).equals(Outcome.SUCCESS);
                assert isLongRunningConversation();
            }

            @Override
            protected void renderResponse() throws Exception {
                User user = (User) getValue("#{user}");
                assert user != null;
                assert user.getUsername().equals("juan123");
                assert user.getEmployee().getFirstName().equals("Juan");
                assert user.getEmployee().getLastName().equals("Perez");
                assert isLongRunningConversation();
            }

            @Override
            protected void afterRequest() {
                assert isInvokeApplicationComplete();

            }
        }.run();

        new FacesRequest("/admin/user.xhtml", cid) {
            @Override
            protected void invokeApplication() throws Exception {
                assert invokeAction("#{userAction.delete}").equals(Outcome.SUCCESS);
            }

            @Override
            protected void renderResponse() throws Exception {
                assert !isLongRunningConversation();
            }

            @Override
            protected void afterRequest() {
                assert isInvokeApplicationComplete();
            }
        }.run();
    }


}
