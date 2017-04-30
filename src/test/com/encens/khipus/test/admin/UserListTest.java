package com.encens.khipus.test.admin;

import org.jboss.seam.mock.SeamTest;
import org.testng.annotations.Test;

/**
 * User list integration test
 *
 * @author
 * @version 1.0
 */
public class UserListTest extends SeamTest {

    @Test
    public void testList() throws Exception {
        new FacesRequest() {
            @Override
            protected void invokeApplication() throws Exception {
                setValue("#{credentials.username}", "root");
                setValue("#{credentials.password}", "ariel");
                setValue("#{authenticator.companyLogin}", "efestia");
                invokeAction("#{identity.login}");
            }

        }.run();

        new NonFacesRequest("/admin/userList.xhtml") {
            @Override
            protected void renderResponse() throws Exception {
                Integer results = (Integer) getValue("#{userDataModel.rowCount}");
                assert results > 0;
            }
        }.run();
    }

    @Test
    public void testSearch() throws Exception {
        new FacesRequest("/admin/userList.xhtml") {

            @Override
            protected void updateModelValues() throws Exception {
                setValue("#{userDataModel.criteria.employee.firstName}", "Ariel Randy");
                setValue("#{userDataModel.criteria.employee.lastName}", "Siles Encinas");
            }

            @Override
            protected void invokeApplication() throws Exception {
                invokeAction("#{userDataModel.search}");
            }


            @Override
            protected void renderResponse() throws Exception {
                Integer results = (Integer) getValue("#{userDataModel.rowCount}");
                assert results == 1;
            }

        }.run();
    }


}
