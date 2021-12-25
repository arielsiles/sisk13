package com.encens.khipus.action.billing;

import com.encens.khipus.action.restful.Json;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.model.customers.EconomicActivity;
import com.encens.khipus.model.customers.MeasureUnitSIN;
import com.encens.khipus.model.customers.ProductsServices;
import com.encens.khipus.model.finances.CompanyConfiguration;
import com.encens.khipus.model.rest.*;
import com.encens.khipus.service.admin.UserService;
import com.encens.khipus.service.customers.SyncControllerService;
import com.encens.khipus.service.fixedassets.CompanyConfigurationService;
import com.encens.khipus.util.ServerResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by AS on 23/12/2021.
 */

@Name("syncControllerAction")
@Scope(ScopeType.PAGE)
public class SyncControllerAction {

    @In(create = true)
    private BillControllerAction billControllerAction;

    @In(required = false)
    private User currentUser;

    @In
    private UserService userService;

    @In
    private CompanyConfigurationService companyConfigurationService;

    @In
    private SyncControllerService syncControllerService;

    @In
    private FacesMessages facesMessages;


    public void syncData() throws IOException {
        syncActivities();
        syncProductsAndServices();
        syncMeasureUnits();
    }


    public void syncMeasureUnits()  throws IOException {
        User user = getUser(currentUser.getId());
        CompanyConfiguration companyConfiguration = getCompanyConfiguration();

        MeasureUnitsPOJO measureUnitsPOJO = new MeasureUnitsPOJO(user.getBranchOffice().getOfficeCode(), user.getBranchOffice().getPosCode());
        String jsonString = Json.prettyPrint(Json.toJson(measureUnitsPOJO));
        ServerResponse serverResponse = billControllerAction.doPostHttpConnection(companyConfiguration.getMeasureUnitsURL() , jsonString);

        if (serverResponse.getResponseJson() != null) {
            JsonNode jsonNodeResponse = Json.parse(serverResponse.getResponseJson());
            MeasureUnitsResponsePOJO measureUnitsResponsePOJO = Json.fromJson(jsonNodeResponse, MeasureUnitsResponsePOJO.class);

            System.out.println("+++++++++ SYNC MEASURE UNITS ++++++++");
            List<MeasureUnitSIN> measureUnitSINList = new ArrayList<MeasureUnitSIN>();

            for (MeasureUnitsCodesPOJO measureUnit : measureUnitsResponsePOJO.getListaCodigos()){
                MeasureUnitSIN measureUnitSIN = new MeasureUnitSIN();
                measureUnitSIN.setCode(measureUnit.getCodigoClasificador());
                measureUnitSIN.setDescription(measureUnit.getDescripcion());

                measureUnitSINList.add(measureUnitSIN);
            }
            syncControllerService.syncMeasureUnits(measureUnitSINList);
        }

    }

    public void syncActivities() throws IOException {

        User user = getUser(currentUser.getId());
        CompanyConfiguration companyConfiguration = getCompanyConfiguration();

        ActivitiesPOJO activitiesPOJO = new ActivitiesPOJO(user.getBranchOffice().getOfficeCode(), user.getBranchOffice().getPosCode());
        String jsonString = Json.prettyPrint(Json.toJson(activitiesPOJO));
        ServerResponse serverResponse = billControllerAction.doPostHttpConnection(companyConfiguration.getActivitiesURL(), jsonString);

        if (serverResponse.getResponseJson() != null) {
            JsonNode jsonNodeResponse = Json.parse(serverResponse.getResponseJson());
            ActivitiesResponsePOJO activitiesResponsePOJO = Json.fromJson(jsonNodeResponse, ActivitiesResponsePOJO.class);

            System.out.println("+++++++++ SYNC ACTIVITIES ++++++++");

            List<EconomicActivity> economicActivityList = new ArrayList<EconomicActivity>();

            for (ActivitiesListResponsePOJO activity : activitiesResponsePOJO.getListaActividades()){
                EconomicActivity economicActivity = new EconomicActivity();
                economicActivity.setActivityCode(activity.getCodigoCaeb());
                economicActivity.setDescription(activity.getDescripcion());
                economicActivity.setActivityType(activity.getTipoActividad());

                economicActivityList.add(economicActivity);
            }
            syncControllerService.syncActivities(economicActivityList);
        }

    }

    public void syncProductsAndServices() throws IOException {

        User user = getUser(currentUser.getId());
        CompanyConfiguration companyConfiguration = getCompanyConfiguration();

        ProductAndServicePOJO productAndServicePOJO = new ProductAndServicePOJO(user.getBranchOffice().getOfficeCode(), user.getBranchOffice().getPosCode());
        String jsonString = Json.prettyPrint(Json.toJson(productAndServicePOJO));
        ServerResponse serverResponse = billControllerAction.doPostHttpConnection(companyConfiguration.getProductsAndServicesURL(), jsonString);

        if (serverResponse.getResponseJson() != null) {
            JsonNode jsonNodeResponse = Json.parse(serverResponse.getResponseJson());
            ProductAndServiceResponsePOJO productAndServiceResponsePOJO = Json.fromJson(jsonNodeResponse, ProductAndServiceResponsePOJO.class);

            System.out.println("+++++++++ SYNC PRODUCTOS Y SERVICIOS ++++++++");

            List<ProductsServices> productsAndServicesList = new ArrayList<ProductsServices>();

            for (ProductAndServiceCodesPOJO item : productAndServiceResponsePOJO.getListaCodigos()){
                System.out.println("----> " + item.getDescripcionProducto());
                ProductsServices productAndService = new ProductsServices();
                productAndService.setActivityCode(item.getCodigoActividad());
                productAndService.setProductCode(item.getCodigoProducto());
                productAndService.setDescription(item.getDescripcionProducto());

                productsAndServicesList.add(productAndService);
            }
            syncControllerService.syncProductsAndServices(productsAndServicesList);
        }

    }



    private User getUser(Long id) {
        try {
            return userService.findById(User.class, id);
        } catch (EntryNotFoundException e) {
            return null;
        }
    }

    private CompanyConfiguration getCompanyConfiguration(){
        CompanyConfiguration companyConfiguration = null;
        try {
            companyConfiguration = companyConfigurationService.findCompanyConfiguration();
            return companyConfiguration;
        } catch (CompanyConfigurationNotFoundException e) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,"CompanyConfiguration.notFound");
            return null;
        }
    }

}
