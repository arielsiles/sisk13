package com.encens.khipus.action.admin;

import com.encens.khipus.action.billing.BillControllerAction;
import com.encens.khipus.action.billing.SyncControllerAction;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.customers.BranchOffice;
import com.encens.khipus.model.rest.PointOfSaleTypeCode;
import com.encens.khipus.model.rest.RegistroPosResponsePOJO;
import com.encens.khipus.util.Constants;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.international.StatusMessage;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by IntelliJ IDEA.
 * User: admin
 *
 * @version 2.8
 */
@Name("branchOfficeAction")
@Scope(ScopeType.CONVERSATION)
public class BranchOfficeAction extends GenericAction<BranchOffice> {

    private List<PointOfSaleTypeCode> pointOfSaleTypeCodeList = new ArrayList<PointOfSaleTypeCode>();
    private PointOfSaleTypeCode pointOfSaleTypeCode;

    @In(create = true)
    private SyncControllerAction syncControllerAction;
    @In(create = true)
    private BillControllerAction billControllerAction;

    @Factory(value = "branchOffice", scope = ScopeType.STATELESS)
    public BranchOffice initBusinessUnit() {
        return getInstance();
    }

    @Create
    public void init() {
        setPointOfSaleTypeCodeList(syncControllerAction.getPointOfSaleTypes());
    }

    @Override
    @Begin(ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    public String select(BranchOffice instance) {
        String outCome = super.select(instance);
        return outCome;
    }

    @Override
    @End
    public String create() {
        System.out.println("----> Creating branch office - POS Type: " + getPointOfSaleTypeCode().getCodigoClasificador() + " - POS Description: " + getPointOfSaleTypeCode().getDescripcion());

        try {

            BranchOffice branchOffice = getInstance();

            RegistroPosResponsePOJO registroPosResponsePOJO = billControllerAction.registerPos(
                    branchOffice.getOfficeCode(),
                    branchOffice.getPosCode(),
                    getPointOfSaleTypeCode().getCodigoClasificador(),
                    branchOffice.getName(), branchOffice.getName());

            if ( registroPosResponsePOJO == null ){
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "No es posible en este momento, intentelo mas tarde (1).");
                return Outcome.REDISPLAY;
            }

            String descripcionPos = MessageFormat.format(Constants.DESCRIPCION, branchOffice.getOfficeCode(), registroPosResponsePOJO.getCodigoPuntoVenta());
            String nombrePos      = MessageFormat.format(Constants.NOMBRE_PUNTOVENTA, registroPosResponsePOJO.getCodigoPuntoVenta());

            branchOffice.setPosCode(registroPosResponsePOJO.getCodigoPuntoVenta()); //Codigo del nuevo Punto de Venta
            branchOffice.setPosName(nombrePos);
            branchOffice.setSectorDocumentCode(Constants.DOC_SECTOR);
            branchOffice.setPointOfSaleType(getPointOfSaleTypeCode().getCodigoClasificador());
            branchOffice.setDescription(descripcionPos);
            branchOffice.setActivity(Constants.ACTIVIDAD_ECONOMICA_SUC2);
            branchOffice.setCompanyName(Constants.NOMBRE_EMPRESA_CAISC);
            branchOffice.setBranchName(MessageFormat.format(Constants.SUCURSAL, branchOffice.getOfficeCode()));
            branchOffice.setPlace(Constants.LUGAR);
            branchOffice.setPhone(Constants.TELEFONO);
            branchOffice.setAddress(Constants.DIRECCION_SUC2);

            genericService.create(getInstance());
            addCreatedMessage();
            return Outcome.SUCCESS;

        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            return Outcome.REDISPLAY;
        }
    }


    public List<PointOfSaleTypeCode> getPointOfSaleTypeCodeList() {
        return pointOfSaleTypeCodeList;
    }

    public void setPointOfSaleTypeCodeList(List<PointOfSaleTypeCode> pointOfSaleTypeCodeList) {
        this.pointOfSaleTypeCodeList = pointOfSaleTypeCodeList;
    }

    public PointOfSaleTypeCode getPointOfSaleTypeCode() {
        return pointOfSaleTypeCode;
    }

    public void setPointOfSaleTypeCode(PointOfSaleTypeCode pointOfSaleTypeCode) {
        this.pointOfSaleTypeCode = pointOfSaleTypeCode;
    }
}