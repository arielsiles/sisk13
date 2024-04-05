package com.encens.khipus.action.production;

import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.common.File;
import com.encens.khipus.model.production.LaboratoryResult;
import com.encens.khipus.model.production.ZoneInspection;
import com.encens.khipus.util.JSFUtil;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;


@Name("laboratoryResultAction")
@Scope(ScopeType.CONVERSATION)
public class LaboratoryResultAction extends GenericAction<LaboratoryResult> {

    private File file = new File();

    @In(create = true)
    private ZoneInspectionAction zoneInspectionAction;

    @Factory(value = "laboratoryResult", scope = ScopeType.STATELESS)
    public LaboratoryResult initLaboratoryResult() {
        return getInstance();
    }

    @Override
    @Begin(nested = true, flushMode = FlushModeType.MANUAL)
    public String select(LaboratoryResult laboratoryResult) {
        setOp(OP_UPDATE);
        setInstance(laboratoryResult);
        return Outcome.SUCCESS;
    }

    @Override
    @Begin(join = true, flushMode = FlushModeType.MANUAL)
    public String create() {

        System.out.println("New File: " + file);

        if (null != file.getValue()) {
            file.setName(file.getName());
            file.setContentType(file.getContentType());
            file.setSize(file.getSize());
            getInstance().setFile(file);
        }

        try {
            ZoneInspection zoneInspection = zoneInspectionAction.getInstance();
            getInstance().setZoneInspection(zoneInspection);
            getService().create(getInstance());
            addCreatedMessage();
            return Outcome.SUCCESS;
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            return Outcome.FAIL;
        }
    }

    @End(beforeRedirect = true)
    public String update() {

        System.out.println("Update File: " + file);

        if (null != file.getValue()) {
            file.setName(file.getName());
            file.setContentType(file.getContentType());
            file.setSize(file.getSize());
            getInstance().setFile(file);
        }
        return super.update();
    }


    /**
     * Download file
     */
    public void download() {
        File file = getInstance().getFile();

        HttpServletResponse response = JSFUtil.getHttpServletResponse();
        response.setContentType(file.getContentType());
        response.addHeader("Content-disposition", "attachment; filename=\"" + file.getName() + "\"");
        try {
            ServletOutputStream os = response.getOutputStream();
            os.write(file.getValue());
            os.flush();
            os.close();
            JSFUtil.getFacesContext().responseComplete();
        } catch (Exception e) {
            log.error("\nFailure : " + e.toString() + "\n");
        }
    }


    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
}
