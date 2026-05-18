package com.encens.khipus.service.warehouse;

import com.encens.khipus.model.warehouse.DispatchState;
import com.encens.khipus.model.warehouse.WarehouseVoucherDispatch;
import com.encens.khipus.model.warehouse.WarehouseVoucherDispatchDetail;
import com.encens.khipus.service.finances.FinancesUserService;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import java.util.Date;

/**
 * Implementacion del servicio CRUD del Vale de Despacho (BORRADOR).
 */
@Stateless
@Name("dispatchVoucherService")
@AutoCreate
public class DispatchVoucherServiceBean implements DispatchVoucherService {

    @In(value = "#{entityManager}")
    private EntityManager em;

    @In
    private FinancesUserService financesUserService;

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public WarehouseVoucherDispatch saveDraft(WarehouseVoucherDispatch dispatch) {
        if (dispatch.getState() == null) {
            dispatch.setState(DispatchState.BORRADOR);
        }
        if (dispatch.getState() != DispatchState.BORRADOR) {
            throw new IllegalStateException(
                    "Solo se puede guardar un despacho en estado BORRADOR (estado actual: "
                            + dispatch.getState() + ")");
        }
        String userCode = financesUserService.getFinancesUserCode();
        Date now = new Date();
        dispatch.setCreatedBy(userCode);
        dispatch.setCreatedDate(now);
        dispatch.setUpdatedBy(userCode);
        dispatch.setUpdatedDate(now);

        // El @CompanyListener pone company; el campo no_cia se autollena en
        // los setters de Warehouse/CostCenter/Provider. Si llega null aqui,
        // significa que el form no se completo aun - dejamos persistir y
        // que la BD valide cuando corresponda en aprobacion.

        linkDetails(dispatch);
        em.persist(dispatch);
        em.flush();
        return dispatch;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public WarehouseVoucherDispatch updateDraft(WarehouseVoucherDispatch dispatch) {
        if (dispatch.getState() != DispatchState.BORRADOR) {
            throw new IllegalStateException(
                    "Solo se puede editar un despacho en estado BORRADOR (estado actual: "
                            + dispatch.getState() + ")");
        }
        dispatch.setUpdatedBy(financesUserService.getFinancesUserCode());
        dispatch.setUpdatedDate(new Date());

        linkDetails(dispatch);
        WarehouseVoucherDispatch merged = em.merge(dispatch);
        em.flush();
        return merged;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void deleteDraft(WarehouseVoucherDispatch dispatch) {
        if (dispatch.getState() != DispatchState.BORRADOR) {
            throw new IllegalStateException(
                    "Solo se puede eliminar un despacho en estado BORRADOR (estado actual: "
                            + dispatch.getState() + ")");
        }
        WarehouseVoucherDispatch managed = em.find(WarehouseVoucherDispatch.class, dispatch.getId());
        if (managed != null) {
            em.remove(managed);
            em.flush();
        }
    }

    @Override
    public WarehouseVoucherDispatch findById(Long id) {
        return em.find(WarehouseVoucherDispatch.class, id);
    }

    /**
     * Asegura que cada detalle tenga la referencia inversa al despacho.
     * Necesario porque JPA 1.0 no tiene mappedBy bidireccional auto-sync.
     */
    private void linkDetails(WarehouseVoucherDispatch dispatch) {
        if (dispatch.getDetails() == null) {
            return;
        }
        for (WarehouseVoucherDispatchDetail detail : dispatch.getDetails()) {
            detail.setDispatch(dispatch);
        }
    }
}
