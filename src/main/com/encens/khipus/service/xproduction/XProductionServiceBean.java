package com.encens.khipus.service.xproduction;


import com.encens.khipus.model.xproduction.XMaterialInput;
import com.encens.khipus.model.production.SupplyType;
import com.encens.khipus.model.xproduction.*;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

/**
 * Products services
 *
 * @author
 * @version $Id: ProductServiceBean.java 2008-9-11 13:50:57 $
 */
@Stateless
@Name("xproductionService")
@AutoCreate
public class XProductionServiceBean implements XProductionService {

    @In(value = "#{entityManager}")
    private EntityManager em;


    public void createProduction(XProduction production, List<XSupply> ingredientSupplyList, List<XSupply> materialSupplyList){

        em.persist(production);
        em.flush();
        for (XSupply supply : ingredientSupplyList){
            supply.setProduction(production);
            supply.setType(SupplyType.INGREDIENT);
            em.persist(supply);
            em.flush();
        }

        for (XSupply supply : materialSupplyList){
            supply.setProduction(production);
            supply.setType(SupplyType.MATERIAL);
            em.persist(supply);
            em.flush();
        }
    }

    public void updateProduction(XProduction production, List<XSupply> ingredientSupplyList, List<XSupply> materialSupplyList){

        //System.out.println("----> Estado Produccion: " + production.getState());
        for (XSupply supply : ingredientSupplyList){
            if (supply.getId() == null){
                supply.setProduction(production);
                supply.setType(SupplyType.INGREDIENT);
                em.persist(supply);
                em.flush();
            }else {
                em.merge(supply);
                em.flush();
            }
        }

        for (XSupply supply : materialSupplyList){
            if (supply.getId() == null){
                supply.setProduction(production);
                supply.setType(SupplyType.MATERIAL);
                em.persist(supply);
                em.flush();
            }else {
                em.merge(supply);
                em.flush();
            }
        }

        for (XProductionProduct product : production.getProductionProductList()){
            em.merge(product);
            em.flush();
        }

        em.merge(production);
        em.flush();

        /*em.merge(production.getProductionPlan());
        em.flush();*/
    }

    @Override
    public void deleteProduction(XProduction production) {

        for (XProductionProduct product : production.getProductionProductList()){
            product.setProduction(null);
            em.merge(product);
            em.flush();
        }

        for (XSupply supply : production.getSupplyList()){
            em.remove(supply);
            em.flush();
        }

        em.remove(production);
        em.flush();
    }

    public void assignProduct(XProduction production, XProductionProduct product){
        product.setProduction(production);
        em.merge(product);
        em.flush();
        em.refresh(production);
        em.flush();
    }

    public void assignMaterial(XProduction production, XSupply supply){
        supply.setProduction(production);
        if (supply.getId() == null){
            //supply.setType(SupplyType.MATERIAL);
            em.persist(supply);
            em.flush();
        }else {
            em.merge(supply);
            em.flush();
        }

    }

    public List<XSupply> getSupplyList(XProduction production, SupplyType type) {

        List<XSupply> supplyList = em.createQuery("select supply from XSupply supply " +
                " where supply.production = :production " +
                " and supply.type = :type ")
                .setParameter("production", production)
                .setParameter("type", type)
                .getResultList();

        return supplyList;
    }

    public void removeProductionProduct(XProductionProduct product, XProduction production){
        product.setProduction(null);
        em.merge(product);
        em.flush();
        em.refresh(production);
        em.flush();
    }

    public void removeSupply(XSupply supply){

        if (em.contains(supply)){
            em.remove(supply);
            em.flush();
        }
    }

    @Override
    public List<Object[]> getAllProductionSuplies(XProduction production) {

        List<Object[]> result = new ArrayList<Object[]>();

        result = em.createQuery("select supply.productItemCode,  from XSupply supply " +
                            " where supply.production = :production " +
                            " group by ")
                .getResultList();

        return null;
    }

    public List<XMaterialInput> getMaterialInput(String productItemCode){

        List<XMaterialInput> materialInputList = new ArrayList<XMaterialInput>();

        materialInputList = (List<XMaterialInput>)em.createQuery(
                "select m from MaterialInput m " +
                   "where m.productItemCode =:productItemCode ")
                .setParameter("productItemCode", productItemCode)
                .getResultList();

        return materialInputList;
    }

    public List<XMaterialInput> getIngredientOrMaterialInput(String productItemCode, SupplyType type){

        List<XMaterialInput> resultInputList = new ArrayList<XMaterialInput>();

        resultInputList = (List<XMaterialInput>)em.createQuery(
                "select m from XMaterialInput m " +
                        "where m.productItemCode =:productItemCode " +
                        "and m.type =:type")
                .setParameter("productItemCode", productItemCode)
                .setParameter("type", type)
                .getResultList();

        return resultInputList;
    }
}
