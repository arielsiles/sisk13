package com.encens.khipus.action.employees;

import com.encens.khipus.service.employees.VacationPlanningService;
import com.encens.khipus.util.employees.VacationPlanningBulkResult;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.log.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Generacion en lote de los planes de vacacion.
 * <p/>
 * Vive aparte de VacationPlanningAction y en scope PAGE a proposito: la lista de planes declara
 * end-conversation, asi que una conversacion no sobrevive entre analizar y confirmar. El scope
 * de pagina si sobrevive al postback, que es todo lo que hace falta.
 * <p/>
 * De la vista previa se guardan solo los numeros y los motivos de exclusion, no los planes: al
 * confirmar se vuelve a analizar y se persiste. Asi no viajan cientos de entidades en el estado
 * de la vista, y lo que se crea es lo que la base dice en ese momento.
 *
 * @author
 * @version 6.1.0
 */
@Name("vacationPlanningBulkAction")
@Scope(ScopeType.PAGE)
public class VacationPlanningBulkAction implements Serializable {

    @Logger
    private Log log;

    @In
    private FacesMessages facesMessages;

    @In
    private VacationPlanningService vacationPlanningService;

    @In(create = true)
    private VacationPlanningDataModel vacationPlanningDataModel;

    private boolean analyzed = false;
    private int toCreate = 0;
    private int alreadyPlanned = 0;
    private List<String> excluded = new ArrayList<String>();

    @Restrict("#{s:hasPermission('VACATIONPLANNING','CREATE')}")
    public void analyze() {
        VacationPlanningBulkResult result = vacationPlanningService.analyzeBulkGeneration();
        toCreate = result.getNewPlanningCount();
        alreadyPlanned = result.getAlreadyPlanned();
        excluded = result.getExcluded();
        analyzed = true;
    }

    @Restrict("#{s:hasPermission('VACATIONPLANNING','CREATE')}")
    public void confirm() {
        try {
            int created = vacationPlanningService.confirmBulkGeneration(
                    vacationPlanningService.analyzeBulkGeneration());
            if (created == 0) {
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                        "VacationPlanning.bulk.error.nothingToCreate");
            } else {
                facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                        "VacationPlanning.bulk.info.created", created);
            }
            /* El paginador cachea el conteo: sin esto la lista sigue creyendo que hay cero
               planes y muestra "no se encontraron resultados" debajo de las filas. */
            vacationPlanningDataModel.update();
            clear();
        } catch (Exception e) {
            log.error("No se pudieron generar los planes de vacacion", e);
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "VacationPlanning.bulk.error.failed");
        }
    }

    public void clear() {
        analyzed = false;
        toCreate = 0;
        alreadyPlanned = 0;
        excluded = new ArrayList<String>();
    }

    public boolean isAnalyzed() {
        return analyzed;
    }

    public int getToCreate() {
        return toCreate;
    }

    public int getAlreadyPlanned() {
        return alreadyPlanned;
    }

    public List<String> getExcluded() {
        return excluded;
    }

    public int getExcludedCount() {
        return excluded.size();
    }
}
