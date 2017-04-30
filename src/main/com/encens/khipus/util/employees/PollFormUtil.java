package com.encens.khipus.util.employees;

import com.encens.khipus.model.employees.*;

/**
 * PollFormUtil
 *
 * @author
 * @version 2.24
 */
public final class PollFormUtil {
    private PollFormUtil() {
    }

    public static Boolean isVisible(FieldRestrictionType type) {
        return FieldRestrictionType.VISIBLENOTREQUIRED.equals(type) || FieldRestrictionType.VISIBLEREQUIRED.equals(type);
    }

    public static Boolean isRequired(FieldRestrictionType type) {
        return FieldRestrictionType.VISIBLEREQUIRED.equals(type);
    }

    public static Boolean isFacultyGrouppingType(PollFormGrouppingType pollFormGrouppingType) {
        return PollFormGrouppingType.FACULTY.equals(pollFormGrouppingType);
    }

    public static Boolean isCareerGrouppingType(PollFormGrouppingType pollFormGrouppingType) {
        return PollFormGrouppingType.CAREER.equals(pollFormGrouppingType);
    }

    public static Boolean isSubjectGrouppingType(PollFormGrouppingType pollFormGrouppingType) {
        return PollFormGrouppingType.SUBJECT.equals(pollFormGrouppingType);
    }

    public static void insertGrouppingValues(PollCopy pollCopy, Faculty faculty, Career career, Subject subject) {
        insertGrouppingValues(pollCopy.getPollForm(), pollCopy, faculty, career, subject);
    }

    public static void insertGrouppingValues(PollForm pollForm, PollCopy pollCopy, Faculty faculty, Career career, Subject subject) {
        if (isFacultyGrouppingType(pollForm.getPollFormGrouppingType())) {
            pollCopy.setFaculty(faculty);
            pollCopy.setCareer(null);
            pollCopy.setSubject(null);
        } else if (isCareerGrouppingType(pollForm.getPollFormGrouppingType())) {
            pollCopy.setFaculty(faculty);
            pollCopy.setCareer(career);
            pollCopy.setSubject(null);
        } else if (isSubjectGrouppingType(pollForm.getPollFormGrouppingType())) {
            pollCopy.setFaculty(faculty);
            pollCopy.setCareer(career);
            pollCopy.setSubject(subject);
        }
    }

}
