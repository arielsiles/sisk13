package com.encens.khipus.framework.ui;

import com.encens.khipus.util.ValidatorUtil;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.framework.Identifier;
import org.jboss.seam.ui.JpaEntityLoader;

import static org.jboss.seam.ScopeType.STATELESS;
import static org.jboss.seam.annotations.Install.APPLICATION;

/**
 * @author
 * @version 2.9
 */

@Name("org.jboss.seam.ui.entityLoader")
@Install(precedence = APPLICATION,
        value = true,
        classDependencies = {"javax.persistence.EntityManager"})
@Scope(STATELESS)
public class CustomJpaEntityLoader extends JpaEntityLoader {

    @Override
    @Transactional
    public Object get(String key) {
        if (ValidatorUtil.isBlankOrNull(key)) {
            return null;
        }

        return super.get(key);
    }

    public CustomJpaEntityLoader() {
        super();
    }

    @Override
    protected Identifier createIdentifier(Object entity) {
        return new CustomEntityIdentifier(entity, getPersistenceContext());
    }
}
