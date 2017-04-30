package com.encens.khipus.action.employees;

import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.common.File;
import com.encens.khipus.model.contacts.Template;
import com.encens.khipus.model.finances.OrganizationalLevel;
import com.encens.khipus.model.finances.OrganizationalUnit;
import com.encens.khipus.service.employees.GenerateContractService;
import com.encens.khipus.service.employees.OrganizationalUnitService;
import com.encens.khipus.util.JSFUtil;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.util.Date;
import java.util.List;

/**
 * Encens S.R.L.
 * Action to generate Accidental asociation document
 *
 * @author
 * @version $Id: GenerateAccidentalAssociationAction.java  02-jun-2010 14:56:55$
 */
@Name("generateAccidentalAssociationAction")
@Scope(ScopeType.CONVERSATION)
@Restrict("#{s:hasPermission('ACCIDENTALASSOCIATION','VIEW')}")
public class GenerateAccidentalAssociationAction extends GenericAction {

    private BusinessUnit businessUnit;
    private OrganizationalLevel organizationalLevel;
    private OrganizationalUnit organizationalUnit;
    private Template template;
    private Date initDate;
    private Date endDate;

    @In
    private GenerateContractService generateContractService;
    @In
    private OrganizationalUnitService organizationalUnitService;

    public void generateDocument() {
        log.debug("Generate accidental association document.....");
        try {
            template = getService().findById(Template.class, template.getId());
        } catch (EntryNotFoundException e) {
            log.debug("Error in find template.. ", e);
        }
        File file = template.getFile();

        try {
            ByteArrayInputStream byteArrayInputStream = generateContractService.generateAccidentalAssociationDocument(template, businessUnit, organizationalUnit, initDate, endDate);
            if (byteArrayInputStream.available() > 0) {
                byte[] document = new byte[byteArrayInputStream.available()];
                byteArrayInputStream.read(document);
                byteArrayInputStream.close();
                download(document, file);
            }
        } catch (Exception e) {
            log.debug("Error in generate accidental association document..", e);
        }
    }

    public Template getTemplate() {
        return template;
    }

    public void setTemplate(Template template) {
        this.template = template;
    }

    public BusinessUnit getBusinessUnit() {
        return businessUnit;
    }

    public void setBusinessUnit(BusinessUnit businessUnit) {
        this.businessUnit = businessUnit;
    }

    public OrganizationalLevel getOrganizationalLevel() {
        return organizationalLevel;
    }

    public void setOrganizationalLevel(OrganizationalLevel organizationalLevel) {
        this.organizationalLevel = organizationalLevel;
    }

    public OrganizationalUnit getOrganizationalUnit() {
        return organizationalUnit;
    }

    public void setOrganizationalUnit(OrganizationalUnit organizationalUnit) {
        this.organizationalUnit = organizationalUnit;
    }

    public Date getInitDate() {
        return initDate;
    }

    public void setInitDate(Date initDate) {
        this.initDate = initDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    /**
     * Download rtf merged document
     *
     * @param mergedDocument contract document
     * @param templateFile   template file
     */
    public void download(byte[] mergedDocument, File templateFile) {

        HttpServletResponse response = JSFUtil.getHttpServletResponse();
        response.setContentType(templateFile.getContentType());
        response.addHeader("Content-disposition", "attachment; filename=\"" + templateFile.getName() + "\"");
        try {
            ServletOutputStream os = response.getOutputStream();
            os.write(mergedDocument);
            os.flush();
            os.close();
            JSFUtil.getFacesContext().responseComplete();
        } catch (Exception e) {
            log.error("\nFailure : " + e.toString() + "\n");
        }
    }

    public void refreshBusinessUnit() {
        setOrganizationalLevel(null);
        setOrganizationalUnit(null);
    }

    public void refreshOrganizationalLevel() {
        setOrganizationalUnit(null);
    }

    public List<OrganizationalUnit> getOrganizationalUnitList() {
        return organizationalUnitService.getOrganizationalUnitByBusinessUnitLevelName(getBusinessUnit(), getOrganizationalLevel());
    }
}