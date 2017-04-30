package com.encens.khipus.test.common;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.admin.Company;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Manager;
import org.jboss.seam.mock.SeamTest;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import java.util.List;

/**
 * This test class checks if concurrent insertions on a entity configured with a table generator
 * strategy could cause duplicated key exceptions when the allocation size is greater than one.
 * <p/>
 * Role entity is used as use case for this test.
 *
 * @author
 * @version 1.0
 */
public class TableGeneratorTest extends SeamTest {

    /* Number of emulated concurrent users */
    private static final int CONCURRENT_USERS_NUMBER = 15;

    /**
     * Test of creation of concurrent roles
     *
     * @throws Exception if something goes wrong
     */
    @Test(invocationCount = CONCURRENT_USERS_NUMBER, threadPoolSize = CONCURRENT_USERS_NUMBER)
    public void testCreateConcurrentRoles() throws Exception {


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
        final String cid = new FacesRequest("/admin/role.xhtml") {

            @Override
            protected void invokeApplication() throws Exception {
                Manager.instance().beginConversation();
                assert isLongRunningConversation();
            }

            protected void renderResponse() throws Exception {
                assert getValue("#{userAction.op}").equals(GenericAction.OP_CREATE);
            }

        }.run();


        /* Submitting the new role  */
        new FacesRequest("/admin/user.xhtml", cid) {
            @Override
            protected void updateModelValues() throws Exception {
                Contexts.getSessionContext().set("currentCompany", new Company(1L, "efestia"));
                setValue("#{role.name}", "Role: " + Manager.instance().getCurrentConversationId());
            }

            @Override
            protected void invokeApplication() throws Exception {
                assert Outcome.SUCCESS.equals(invokeAction("#{roleAction.create}"));

            }

        }.run();

    }

    /**
     * Check if the created roles -1 is equal to CONCURRENT_USERS_NUMBER.
     * -1 because there are a default role created within import-test.sql
     *
     * @throws Exception if something fails
     */
    @Test(dependsOnMethods = {"testCreateConcurrentRoles"})
    public void testRolesCreated() throws Exception {
        new FacesRequest() {
            @Override
            protected void invokeApplication() throws Exception {
                List roles = (List) getValue("#{roleList.resultList}");
                assert roles.size() - 1 == CONCURRENT_USERS_NUMBER;
            }
        }.run();
    }

    /**
     * This the super implementation causes a ConcurrentModificationException, so because for this test
     * does not matter if the session is closed, we disable this invocation
     */
    @AfterMethod
    @Override
    public void end() {
        //do nothing;
    }

}
