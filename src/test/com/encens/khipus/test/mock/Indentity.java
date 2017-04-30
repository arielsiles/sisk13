package com.encens.khipus.test.mock;

import org.jboss.seam.security.Identity;

/**
 * Mocks seam identity.
 * This mock identity class allows to avoid security checking in all the tests are are going to do in the application.
 *
 * @author
 * @version 1.0.18
 */
/*@Name("org.jboss.seam.security.identity")
@Install(precedence = Install.MOCK)*/
public class Indentity extends Identity {

    @Override
    public boolean isLoggedIn() {
        return true;
    }

    @Override
    public boolean hasRole(String role) {
        return true;
    }

    @Override
    public boolean hasPermission(String name, String action, Object... arg) {
        return true;
    }
}
