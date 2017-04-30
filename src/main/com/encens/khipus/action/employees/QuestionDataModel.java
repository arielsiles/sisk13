package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.employees.Question;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.List;

/**
 * Encens Team
 *
 * @author
 * @version : QuestionDataModel, 26-10-2009 07:48:51 PM
 */
@Name("questionDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('POLLFORMQUESTION','VIEW')}")
public class QuestionDataModel extends QueryDataModel<Long, Question> {

    private static final String[] RESTRICTIONS = {"question.section.pollForm = #{pollForm}"};


    @Create
    public void init() {
        sortProperty = "question.sequence, question.section.sequence";
    }

    @Override
    public String getEjbql() {
        return "select question from Question question";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
