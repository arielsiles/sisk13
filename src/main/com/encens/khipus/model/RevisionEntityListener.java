package com.encens.khipus.model;

import com.encens.khipus.model.admin.User;
import com.encens.khipus.model.common.RevisionEntityInfo;
import com.encens.khipus.service.common.SequenceGeneratorService;
import org.hibernate.envers.RevisionListener;
import org.jboss.seam.Component;

/**
 * RevisionEntityListener
 *
 * @author
 * @version 2.24
 */
public class RevisionEntityListener implements RevisionListener {
    @Override
    public void newRevision(Object entity) {
        if (entity instanceof RevisionEntityInfo) {
            ((RevisionEntityInfo) entity).setStoredBy((User) Component.getInstance("currentUser"));
            ((RevisionEntityInfo) entity).setRevisionNumber(((SequenceGeneratorService) Component.getInstance("sequenceGeneratorService")).nextValue(RevisionEntityInfo.class.getSimpleName()));
        }
    }
}
