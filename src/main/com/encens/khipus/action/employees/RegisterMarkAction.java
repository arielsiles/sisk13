package com.encens.khipus.action.employees;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Scope;
import com.encens.khipus.model.employees.RHMark;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 13/09/13
 * Time: 21:05
 * To change this template use File | Settings | File Templates.
 */
@Name("registerMarkActionAction")
@Scope(ScopeType.CONVERSATION)
public class RegisterMarkAction {

    @In
    @Out
    private RHMark rhMark;

    @Out
    private List<RHMark> markList;

    public RHMark getRhMark() {
        return rhMark;
    }

    public void setRhMark(RHMark rhMark) {
        this.rhMark = rhMark;
    }

    public List<RHMark> getMarkList() {
        return markList;
    }

    public void setMarkList(List<RHMark> markList) {
        this.markList = markList;
    }
}
