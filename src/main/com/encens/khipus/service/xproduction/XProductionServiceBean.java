package com.encens.khipus.service.xproduction;


import com.encens.khipus.model.production.ProductionState;
import com.encens.khipus.model.production.SupplyType;
import com.encens.khipus.model.warehouse.WarehouseType;
import com.encens.khipus.model.xproduction.*;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Date;
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

    public void updateProduction(XProduction production, List<XSupply> ingredientSupplyList, List<XSupply> materialSupplyList, List<XProductionLabor> laborList){

        for (XSupply supply : ingredientSupplyList){
            if (supply.getId() == null){
                supply.setProduction(production);
                supply.setType(SupplyType.INGREDIENT);
                em.persist(supply);
                em.flush();
            }else if (em.find(XSupply.class, supply.getId()) != null) {
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
            }else if (em.find(XSupply.class, supply.getId()) != null) {
                em.merge(supply);
                em.flush();
            }
        }

        for (XProductionProduct product : production.getProductionProductList()){
            if (product.getId() == null) {
                product.setProduction(production);
                em.persist(product);
            } else if (em.find(XProductionProduct.class, product.getId()) != null) {
                em.merge(product);
            } else {
                continue;
            }
            em.flush();
        }


        for (XProductionLabor labor : laborList){
            if (labor.getId() != null && em.find(XProductionLabor.class, labor.getId()) == null) continue;
            em.merge(labor);
            em.flush();
        }

        em.merge(production);
        em.flush();

    }

    @Override
    public void deleteProduction(XProduction production) {

        Long pid = production.getId();

        em.createQuery("delete from XProductionProduct p where p.production.id = :pid")
                .setParameter("pid", pid)
                .executeUpdate();
        em.createQuery("delete from XSupply s where s.production.id = :pid")
                .setParameter("pid", pid)
                .executeUpdate();
        em.createQuery("delete from XProductionLabor l where l.production.id = :pid")
                .setParameter("pid", pid)
                .executeUpdate();
        em.createQuery("delete from XProductionUlexita u where u.production.id = :pid")
                .setParameter("pid", pid)
                .executeUpdate();
        em.flush();

        XProduction managed = em.contains(production) ? production : em.merge(production);
        em.remove(managed);
        em.flush();
    }

    @Override
    public void addFinishedProductDirect(XProduction production, XProductionProduct product) {
        if (product == null || product.getId() != null) return;
        product.setProduction(production);
        if (product.getProductionPlan() == null && production.getProductionPlan() != null) {
            product.setProductionPlan(production.getProductionPlan());
        }
        em.persist(product);
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

    public List<XProductionLabor> getLaborList(XProduction production){
        List<XProductionLabor> laborList = em.createQuery("select labor from XProductionLabor labor " +
                " where labor.production = :production ")
                .setParameter("production", production)
                .getResultList();

        return laborList;
    }

    public void removeProductionProduct(XProductionProduct product, XProduction production){
        if (product == null || product.getId() == null) return;
        XProductionProduct managed = em.contains(product) ? product : em.merge(product);
        em.remove(managed);
        em.flush();
        production.getProductionProductList().remove(product);
        em.refresh(production);
        em.flush();
    }

    public void removeSupply(XSupply supply){
        if (supply == null || supply.getId() == null) return;
        XSupply managed = em.find(XSupply.class, supply.getId());
        if (managed != null) {
            em.remove(managed);
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

    @Override
    public List<Object[]> findProductionInputsByDates(Date initDate, Date endDate){
        List<Object[]> resultList = em.createQuery("select w.name as warehouse, p.productItemCode, p.name, p.usageMeasureCode as unitMeasure, sum(s.quantity) as quantity " +
                        " from XSupply s " +
                        " left join s.production pr " +
                        " left join pr.productionPlan pl " +
                        " left join s.productItem p " +
                        " left join p.warehouse w " +
                        " where pl.date between :initDate and :endDate " +
                        " group by w.name, p.productItemCode, p.name, p.usageMeasureCode " +
                        " order by w.name, p.name ")
                .setParameter("initDate", initDate)
                .setParameter("endDate", endDate)
                .getResultList();

        return resultList;
    }

    public List<Object[]> getSumRawMaterialInProduction(Date initDate, Date endDate){

        List<Object[]> resultList = em.createQuery("select p.productItemCode, sum(s.quantity) as quantity " +
                        " from XSupply s " +
                        " left join s.production pr " +
                        " left join pr.productionPlan pl " +
                        " left join s.productItem p " +
                        " left join p.warehouse w " +
                        " where pl.date between :initDate and :endDate " +
                        " and w.warehouseType = :warehouseType " +
                        " group by p.productItemCode " +
                        "")
                .setParameter("initDate", initDate)
                .setParameter("endDate", endDate)
                .setParameter("warehouseType", WarehouseType.RAW_MATERIAL)
                .getResultList();

        return resultList;
    }

    public List<XSupply> getRawMaterialInProduction(String productItemCode, Date initDate, Date endDate){

        List<XSupply> resultList = em.createQuery("select s " +
                        " from XSupply s " +
                        " left join s.production pr " +
                        " left join pr.productionPlan pl " +
                        " where pl.date between :initDate and :endDate " +
                        " and s.productItemCode = :productItemCode ")
                .setParameter("initDate", initDate)
                .setParameter("endDate", endDate)
                .setParameter("productItemCode", productItemCode)
                .getResultList();

        return resultList;
    }

    public List<XSupply> getAllRawMaterialInProduction(Date initDate, Date endDate){

        List<XSupply> resultList = em.createQuery("select s " +
                        " from XSupply s " +
                        " left join s.production pr " +
                        " left join pr.productionPlan pl " +
                        " left join s.productItem p " +
                        " left join p.warehouse w " +
                        " where pl.date between :initDate and :endDate " +
                        " and w.warehouseType = :warehouseType ")
                .setParameter("initDate", initDate)
                .setParameter("endDate", endDate)
                .setParameter("warehouseType", WarehouseType.RAW_MATERIAL)
                .getResultList();

        return resultList;
    }

    @SuppressWarnings("unchecked")
    public List<XProductionUlexita> getUlexitaReprocessByArticle(String productItemCode, Date initDate, Date endDate){

        return em.createQuery("select u " +
                        " from XProductionUlexita u " +
                        " left join u.production pr " +
                        " left join pr.productionPlan pl " +
                        " where pl.date between :initDate and :endDate " +
                        " and u.codArtReprocFinal = :productItemCode " +
                        " and pr.state <> :anl ")
                .setParameter("initDate", initDate)
                .setParameter("endDate", endDate)
                .setParameter("productItemCode", productItemCode)
                .setParameter("anl", ProductionState.ANL)
                .getResultList();
    }

}
