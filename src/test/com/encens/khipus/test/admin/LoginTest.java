package com.encens.khipus.test.admin;

import org.jboss.seam.mock.SeamTest;
import org.jboss.seam.web.Session;
import org.testng.annotations.Test;

/**
 * Login test
 *
 * @author
 * @version 1.0
 */
public class LoginTest extends SeamTest {

    @Test
    public void testLogin() throws Exception {


        /*new FacesRequest() {
            @Override
            protected void invokeApplication() {
                assert !isSessionInvalid();
                assert getValue("#{identity.loggedIn}").equals(false);
            }

        }.run();
        */

        new FacesRequest() {
            @Override
            protected void updateModelValues() throws Exception {
                assert !isSessionInvalid();
                setValue("#{credentials.username}", "ariel");
                setValue("#{credentials.password}", "ariel");
                setValue("#{authenticator.companyLogin}", "efestia");
            }

            @Override
            protected void invokeApplication() {
                invokeAction("#{identity.login}");
            }

            @Override
            protected void renderResponse() {
                assert getValue("#{credentials.username}").equals("ariel");
                assert !isLongRunningConversation();
                assert getValue("#{identity.loggedIn}").equals(true);
            }

            @Override
            protected void afterRequest() {
                assert isInvokeApplicationComplete();
            }

        }.run();

        new FacesRequest() {

            @Override
            protected void invokeApplication() {
                assert !isSessionInvalid();
                assert getValue("#{identity.loggedIn}").equals(true);
            }

        }.run();

        new FacesRequest() {

            @Override
            protected void invokeApplication() {
                assert !isLongRunningConversation();
                assert !isSessionInvalid();
                invokeAction("#{identity.logout}");
                assert Session.instance().isInvalid();
            }

            @Override
            protected void renderResponse() {
                assert getValue("#{identity.loggedIn}").equals(false);
                assert Session.instance().isInvalid();
            }

        }.run();

    }


}
