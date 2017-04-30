package com.encens.khipus.action.contacts;

import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.common.File;
import com.encens.khipus.model.contacts.FileFormat;
import com.encens.khipus.model.contacts.Template;
import com.encens.khipus.model.contacts.TemplateType;
import com.encens.khipus.util.FileUtil;
import com.encens.khipus.util.JSFUtil;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.international.StatusMessage;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

/**
 * Encens S.R.L.
 * Template action class
 *
 * @author
 * @version $Id: TemplateAction.java  24-feb-2010 19:17:32$
 */
@Name("templateAction")
@Scope(ScopeType.CONVERSATION)
public class TemplateAction extends GenericAction<Template> {

    @Factory(value = "template", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('TEMPLATE','VIEW')}")
    public Template initTemplate() {
        return getInstance();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "name";
    }

    @Override
    @End
    @Restrict("#{s:hasPermission('TEMPLATE','CREATE')}")
    public String create() {
        Template template = getInstance();
        if (!isValidTemplateFile(template)) {
            return Outcome.REDISPLAY;
        }
        return super.create();
    }

    @Override
    @End
    @Restrict("#{s:hasPermission('TEMPLATE','UPDATE')}")
    public String update() {
        Template template = getInstance();
        if (!isValidTemplateFile(template)) {
            return Outcome.REDISPLAY;
        }

        return super.update();
    }

    @Factory("templateType")
    public TemplateType[] getTemplateTypes() {
        return TemplateType.values();
    }

    @Factory("fileFormat")
    public FileFormat[] getFileFormats() {
        return FileFormat.values();
    }

    /**
     * Validate template file
     *
     * @param template template
     * @return true or false
     */
    private boolean isValidTemplateFile(Template template) {
        boolean isValid = true;

        File file = template.getFile();
        log.debug("File.........." + file);

        if (FileUtil.isEmpty(file)) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "Common.required", messages.get("Template"));
            isValid = false;
        } else if (!FileUtil.isValidFileFormat(file, template.getFileFormat())) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "Template.invalidFileFormat", template.getFileFormat().getExt());
            isValid = false;
        }
        return isValid;
    }

    /**
     * Download template file
     *
     * @param template
     */
    @Restrict("#{s:hasPermission('TEMPLATE','VIEW')}")
    public void download(Template template) {
        try {
            template = getService().findById(Template.class, template.getId());
        } catch (EntryNotFoundException e) {
            log.debug("Error in find template.. ", e);
        }

        File file = template.getFile();

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
}