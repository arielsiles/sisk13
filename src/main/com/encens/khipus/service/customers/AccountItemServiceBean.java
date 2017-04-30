package com.encens.khipus.service.customers;

import com.encens.khipus.framework.service.ExtendedGenericServiceBean;
import com.encens.khipus.model.customers.Territoriotrabajo;
import com.sun.org.apache.bcel.internal.generic.InstructionListObserver;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TemporalType;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 12/11/13
 * Time: 19:23
 * To change this template use File | Settings | File Templates.
 */
@Name("accountItemService")
@AutoCreate
@Stateless
public class AccountItemServiceBean extends ExtendedGenericServiceBean implements AccountItemService {

    @In(value = "#{entityManager}")
    private EntityManager em;
    //TODO: EL ESTADO DE LA ORDEN TIENE QUE IR DE ACUERDO CON LA TABLA CUANDO HAYA ESTADOS OFICIALES
    @Override
    public List<OrderClient> findClientsOrder(Date date,BigDecimal distribuidor,String stateOrder) {
        List<OrderClient> clientOrders = new ArrayList<OrderClient>();
        try{
            List<Object[]> datas = new ArrayList<Object[]>();
            if(stateOrder == "TODOS")
            {
                datas = em.createNativeQuery("select \n" +
                        "nvl(pe.ap,' ')||' '||nvl(pe.am,' ')||' '||nvl(pe.nom,' ')\n" +
                        ",ped.pedido\n" +
                        ",ped.estado_pedido\n" +
                        "from USER01_DAF.per_insts pi\n" +
                        "inner join USER01_DAF.pedidos ped\n" +
                        "on pi.id = ped.id\n" +
                        "inner join USER01_DAF.personas pe\n" +
                        "on pi.id = pe.pi_id\n" +
                        "where ped.fecha_entrega = :date\n" +
                        "and ped.estado_pedido <> 'ANL'\n" +
                        "and ped.distribuidor = :distribuidor\n" +
                        "union all\n" +
                        "select \n" +
                        "nvl(it.razon_soc,' ') \n" +
                        ",ped.pedido\n" +
                        ",ped.estado_pedido\n" +
                        "from USER01_DAF.per_insts pi\n" +
                        "inner join USER01_DAF.pedidos ped\n" +
                        "on pi.id = ped.id\n" +
                        "inner join USER01_DAF.instituciones it\n" +
                        "on pi.id = it.pi_id\n" +
                        "where ped.fecha_entrega = :date\n" +
                        "and ped.estado_pedido <> 'ANL'\n" +
                        "and ped.distribuidor = :distribuidor\n")
                        .setParameter("date", date, TemporalType.DATE)
                        .setParameter("distribuidor",distribuidor)
                        .getResultList();
            }
            else
            {
                datas = em.createNativeQuery("select \n" +
                        "nvl(pe.ap,' ')||' '||nvl(pe.am,' ')||' '||nvl(pe.nom,' ')\n" +
                        ",ped.pedido\n" +
                        ",ped.estado_pedido\n" +
                        "from USER01_DAF.per_insts pi\n" +
                        "inner join USER01_DAF.pedidos ped\n" +
                        "on pi.id = ped.id\n" +
                        "inner join USER01_DAF.personas pe\n" +
                        "on pi.id = pe.pi_id\n" +
                        "where ped.fecha_entrega = :date\n" +
                        "and ped.estado_pedido = :stateOrder\n" +
                        "and ped.distribuidor = :distribuidor\n" +
                        "union all\n" +
                        "select \n" +
                        "nvl(it.razon_soc,' ') \n" +
                        ",ped.pedido\n" +
                        ",ped.estado_pedido\n" +
                        "from USER01_DAF.per_insts pi\n" +
                        "inner join USER01_DAF.pedidos ped\n" +
                        "on pi.id = ped.id\n" +
                        "inner join USER01_DAF.instituciones it\n" +
                        "on pi.id = it.pi_id\n" +
                        "where ped.fecha_entrega = :date\n" +
                        "and ped.estado_pedido = :stateOrder\n" +
                        "and ped.distribuidor = :distribuidor\n")
                        .setParameter("date", date, TemporalType.DATE)
                        .setParameter("distribuidor",distribuidor)
                        .setParameter("stateOrder",stateOrder)
                        .getResultList();
            }

            for(Object[] obj: datas)
            {
                OrderClient client = new OrderClient();
                client.setName((String)obj[1]+"-"+(String)obj[0]);
                client.setIdOrder((String)obj[1]);
                client.setState((String)obj[2]);
                clientOrders.add(client);
            }

        }catch (NoResultException e)
        {
            return new ArrayList<OrderClient>();
        }
        return clientOrders;
    }

    public List<OrderClient> findClientsOrder(BigDecimal distribuidor,Date date) {
        List<OrderClient> clientOrders = new ArrayList<OrderClient>();
        try{

        List<Object[]> datas = em.createNativeQuery("select \n" +
                        "nvl(pe.ap,' ')||' '||nvl(pe.am,' ')||' '||nvl(pe.nom,' ')\n" +
                        ",ped.pedido\n" +
                        ",ped.estado_pedido\n" +
                        "from USER01_DAF.per_insts pi\n" +
                        "inner join USER01_DAF.pedidos ped\n" +
                        "on pi.id = ped.id\n" +
                        "inner join USER01_DAF.personas pe\n" +
                        "on pi.id = pe.pi_id\n" +
                        "where ped.fecha_entrega = :date\n" +
                        "and ped.estado_pedido <> 'ANL'\n" +
                        "and ped.distribuidor = :distribuidor\n" +
                        "union all\n" +
                        "select \n" +
                        "nvl(it.razon_soc,' ') \n" +
                        ",ped.pedido\n" +
                        ",ped.estado_pedido\n" +
                        "from USER01_DAF.per_insts pi\n" +
                        "inner join USER01_DAF.pedidos ped\n" +
                        "on pi.id = ped.id\n" +
                        "inner join USER01_DAF.instituciones it\n" +
                        "on pi.id = it.pi_id\n" +
                        "where ped.fecha_entrega = :date\n" +
                        "and ped.estado_pedido <> 'ANL'\n" +
                        "and ped.distribuidor = :distribuidor\n")
                        .setParameter("date", date, TemporalType.DATE)
                        .setParameter("distribuidor",distribuidor)
                        .getResultList();

            for(Object[] obj: datas)
            {
                OrderClient client = new OrderClient();
                client.setName((String)obj[1]+"-"+(String)obj[0]);
                client.setIdOrder((String)obj[1]);
                client.setState((String)obj[2]);
                clientOrders.add(client);
            }

        }catch (NoResultException e)
        {
            return new ArrayList<OrderClient>();
        }
        return clientOrders;
    }

    @Override
    public Integer getAmount(String codArt,String codPedido){
        BigDecimal result = BigDecimal.ZERO;
        try{
          result = (BigDecimal)em.createNativeQuery("SELECT nvl(sum(nvl(cantidad,0)+ nvl(reposicion,0)+ nvl(promocion,0)),0) FROM USER01_DAF.articulos_pedido \n" +
                  "where cod_art = :codArt\n" +
                  "and pedido = :codPedido")
                 .setParameter("codPedido",codPedido)
                 .setParameter("codArt",codArt)
                 .getSingleResult();

        }catch (NoResultException e)
        {
            return 0;
        }
        return result.intValue();
    }

    public Integer getAmountByDateAndDistributorOrder(String codArt,BigDecimal idDistribution,Date dateOrder){
        BigDecimal result = BigDecimal.ZERO;;
        try{
            result = (BigDecimal)em.createNativeQuery("select \n" +
                    "nvl(sum(ap.cantidad),0)+nvl(sum(ap.reposicion),0)+nvl(sum(ap.promocion),0)\n" +
                    "from USER01_DAF.per_insts pi\n" +
                    "inner join USER01_DAF.pedidos ped\n" +
                    "on pi.id = ped.id\n" +
                    "inner join USER01_DAF.personas pe\n" +
                    "on pi.id = pe.pi_id\n" +
                    "inner join USER01_DAF.articulos_pedido ap\n" +
                    "on ap.pedido = ped.pedido\n" +
                    "where ped.fecha_entrega =:dateOrder\n" +
                    "and ped.estado_pedido <> 'ANL'\n" +
                    "and ped.distribuidor =:idDistribution\n" +
                    "AND ap.cod_art =:codArt")
                    .setParameter("idDistribution", idDistribution)
                    .setParameter("dateOrder",dateOrder,TemporalType.DATE)
                    .setParameter("codArt", codArt)
                    .getSingleResult();

        }catch (NoResultException e)
        {
            return 0;
        }
        return result.intValue();
    }

    public Integer getAmountByDateAndDistributorOrderDelivery(String codArt,BigDecimal idDistribution,Date dateOrder){
        BigDecimal result = BigDecimal.ZERO;;
        try{
            result = (BigDecimal)em.createNativeQuery("select \n" +
                    "nvl(sum(ap.cantidad),0)+nvl(sum(ap.reposicion),0)+nvl(sum(ap.promocion),0)\n" +
                    "from USER01_DAF.per_insts pi\n" +
                    "inner join USER01_DAF.pedidos ped\n" +
                    "on pi.id = ped.id\n" +
                    "inner join USER01_DAF.personas pe\n" +
                    "on pi.id = pe.pi_id\n" +
                    "inner join USER01_DAF.articulos_pedido ap\n" +
                    "on ap.pedido = ped.pedido\n" +
                    "where ped.fecha_entrega =:dateOrder\n" +
                    "and ped.estado_pedido = 'PEN'\n" +
                    "and ped.distribuidor =:idDistribution\n" +
                    "AND ap.cod_art =:codArt")
                    .setParameter("idDistribution", idDistribution)
                    .setParameter("dateOrder",dateOrder,TemporalType.DATE)
                    .setParameter("codArt", codArt)
                    .getSingleResult();

        }catch (NoResultException e)
        {
            return 0;
        }
        return result.intValue();
    }

    public Integer getAmountByDateAndDistributorInstitution(String codArt,BigDecimal idDistribution,Date dateOrder){
        BigDecimal result = BigDecimal.ZERO;;
        try{
            result = (BigDecimal)em.createNativeQuery("select \n" +
                    "nvl(sum(ap.cantidad),0)+nvl(sum(ap.reposicion),0)+nvl(sum(ap.promocion),0)\n" +
                    "from USER01_DAF.per_insts pi\n" +
                    "inner join USER01_DAF.pedidos ped\n" +
                    "on pi.id = ped.id\n" +
                    "inner join USER01_DAF.instituciones it\n" +
                    "on pi.id = it.pi_id\n" +
                    "inner join USER01_DAF.articulos_pedido ap\n" +
                    "on ap.pedido = ped.pedido\n" +
                    "where ped.fecha_entrega =:dateOrder\n" +
                    "and ped.estado_pedido <> 'ANL'\n" +
                    "and ped.distribuidor =:idDistribution\n" +
                    "AND ap.cod_art = :codArt")
                    .setParameter("idDistribution", idDistribution)
                    .setParameter("dateOrder",dateOrder,TemporalType.DATE)
                    .setParameter("codArt", codArt)
                    .getSingleResult();

        }catch (NoResultException e)
        {
            return 0;
        }
        return result.intValue();
    }

    public Integer getAmountByDateAndDistributorInstitutionDelivery(String codArt,BigDecimal idDistribution,Date dateOrder){
        BigDecimal result = BigDecimal.ZERO;;
        try{
            result = (BigDecimal)em.createNativeQuery("select \n" +
                    "nvl(sum(ap.cantidad),0)+nvl(sum(ap.reposicion),0)+nvl(sum(ap.promocion),0)\n" +
                    "from USER01_DAF.per_insts pi\n" +
                    "inner join USER01_DAF.pedidos ped\n" +
                    "on pi.id = ped.id\n" +
                    "inner join USER01_DAF.instituciones it\n" +
                    "on pi.id = it.pi_id\n" +
                    "inner join USER01_DAF.articulos_pedido ap\n" +
                    "on ap.pedido = ped.pedido\n" +
                    "where ped.fecha_entrega =:dateOrder\n" +
                    "and ped.estado_pedido = 'PEN'\n" +
                    "and ped.distribuidor =:idDistribution\n" +
                    "AND ap.cod_art = :codArt")
                    .setParameter("idDistribution", idDistribution)
                    .setParameter("dateOrder",dateOrder,TemporalType.DATE)
                    .setParameter("codArt", codArt)
                    .getSingleResult();

        }catch (NoResultException e)
        {
            return 0;
        }
        return result.intValue();
    }

    public Integer getAmountByDateAndDistributorOrder(String codArt ,Date dateOrder){
        BigDecimal result = BigDecimal.ZERO;;
        try{
            result = (BigDecimal)em.createNativeQuery("select \n" +
                    "nvl(sum(ap.cantidad),0)+nvl(sum(ap.reposicion),0)+nvl(sum(ap.promocion),0)\n" +
                    "from USER01_DAF.per_insts pi\n" +
                    "inner join USER01_DAF.pedidos ped\n" +
                    "on pi.id = ped.id\n" +
                    "inner join USER01_DAF.personas pe\n" +
                    "on pi.id = pe.pi_id\n" +
                    "inner join USER01_DAF.articulos_pedido ap\n" +
                    "on ap.pedido = ped.pedido\n" +
                    "where ped.fecha_entrega =:dateOrder\n" +
                    "and ped.estado_pedido <> 'ANL'\n" +
                    "AND ap.cod_art = :codArt")
                    .setParameter("dateOrder", dateOrder, TemporalType.DATE)
                    .setParameter("codArt", codArt)
                    .getSingleResult();

        }catch (NoResultException e)
        {
            return 0;
        }
        return result.intValue();
    }

    public Integer getAmountByDateAndDistributorOrderDelivery(String codArt ,Date dateOrder){
        BigDecimal result = BigDecimal.ZERO;;
        try{
            result = (BigDecimal)em.createNativeQuery("select \n" +
                    "nvl(sum(ap.cantidad),0)+nvl(sum(ap.reposicion),0)+nvl(sum(ap.promocion),0)\n" +
                    "from USER01_DAF.per_insts pi\n" +
                    "inner join USER01_DAF.pedidos ped\n" +
                    "on pi.id = ped.id\n" +
                    "inner join USER01_DAF.personas pe\n" +
                    "on pi.id = pe.pi_id\n" +
                    "inner join USER01_DAF.articulos_pedido ap\n" +
                    "on ap.pedido = ped.pedido\n" +
                    "where ped.fecha_entrega =:dateOrder\n" +
                    "and ped.estado_pedido = 'PEN'\n" +
                    "AND ap.cod_art = :codArt")
                    .setParameter("dateOrder", dateOrder, TemporalType.DATE)
                    .setParameter("codArt", codArt)
                    .getSingleResult();

        }catch (NoResultException e)
        {
            return 0;
        }
        return result.intValue();
    }

    public Integer getAmountByDateAndDistributorInstitution(String codArt,Date dateOrder){
        BigDecimal result = BigDecimal.ZERO;
        try{
            result = (BigDecimal)em.createNativeQuery("select \n" +
                    "nvl(sum(ap.cantidad),0)+nvl(sum(ap.reposicion),0)+nvl(sum(ap.promocion),0)\n" +
                    "from USER01_DAF.per_insts pi\n" +
                    "inner join USER01_DAF.pedidos ped\n" +
                    "on pi.id = ped.id\n" +
                    "inner join USER01_DAF.instituciones it\n" +
                    "on pi.id = it.pi_id\n" +
                    "inner join USER01_DAF.articulos_pedido ap\n" +
                    "on ap.pedido = ped.pedido\n" +
                    "where ped.fecha_entrega =:dateOrder\n" +
                    "and ped.estado_pedido <> 'ANL'\n" +
                    "AND ap.cod_art = :codArt")
                    .setParameter("dateOrder", dateOrder, TemporalType.DATE)
                    .setParameter("codArt", codArt)
                    .getSingleResult();

        }catch (NoResultException e)
        {
            return 0;
        }
        return result.intValue();
    }

    public Integer getAmountByDateAndDistributorInstitutionDelivery(String codArt,Date dateOrder){
        BigDecimal result = BigDecimal.ZERO;
        try{
            result = (BigDecimal)em.createNativeQuery("select \n" +
                    "nvl(sum(ap.cantidad),0)+nvl(sum(ap.reposicion),0)+nvl(sum(ap.promocion),0)\n" +
                    "from USER01_DAF.per_insts pi\n" +
                    "inner join USER01_DAF.pedidos ped\n" +
                    "on pi.id = ped.id\n" +
                    "inner join USER01_DAF.instituciones it\n" +
                    "on pi.id = it.pi_id\n" +
                    "inner join USER01_DAF.articulos_pedido ap\n" +
                    "on ap.pedido = ped.pedido\n" +
                    "where ped.fecha_entrega =:dateOrder\n" +
                    "and ped.estado_pedido = 'PEN'\n" +
                    "AND ap.cod_art = :codArt")
                    .setParameter("dateOrder", dateOrder, TemporalType.DATE)
                    .setParameter("codArt", codArt)
                    .getSingleResult();

        }catch (NoResultException e)
        {
            return 0;
        }
        return result.intValue();
    }

    @Override
    public String getNameEmployeed(BigDecimal codEmployeed)
    {
        String name = "";
        try{
            name =  (String)em.createNativeQuery("select nombres||' '||apellidopaterno  from persona where IDPERSONA = :cod")
                    .setParameter("cod",codEmployeed)
                    .getSingleResult();
        }catch(NoResultException e)
        {
            return "";
        }
        return name;
    }

    @Override
    public Collection<OrderItem> findOrderItemPack(Date dateOrder, String stateOrder) {
        List<OrderItem> orderItems = new ArrayList<OrderItem>();
        try{
            List<Object[]> datas = new ArrayList<Object[]>();
            if(stateOrder == "TODOS")
            {
                datas = em.createNativeQuery("select distinct nvl(pq.nombrecorto,'sin-nombre'),pq.paquete from USER01_DAF.paquetes pq\n" +
                        "inner join USER01_DAF.paquete_pedidos pp\n" +
                        "on pq.paquete = pp.paquete\n" +
                        "inner join USER01_DAF.pedidos pe\n" +
                        "on pp.pedido = pe.pedido\n" +
                        "where pe.fecha_entrega = :dateOrder\n" +
                        "and pe.estado_pedido <> 'ANL'")
                        .setParameter("dateOrder",dateOrder,TemporalType.DATE)
                        .getResultList();
            }
            else
            {
                datas = em.createNativeQuery("select distinct nvl(pq.nombrecorto,'sin-nombre'),pq.paquete from USER01_DAF.paquetes pq\n" +
                        "inner join USER01_DAF.paquete_pedidos pp\n" +
                        "on pq.paquete = pp.paquete\n" +
                        "inner join USER01_DAF.pedidos pe\n" +
                        "on pp.pedido = pe.pedido\n" +
                        "where pe.fecha_entrega = :dateOrder\n" +
                        "and pe.estado_pedido = :stateOrder")
                        .setParameter("dateOrder",dateOrder,TemporalType.DATE)
                        .setParameter("stateOrder",stateOrder)
                        .getResultList();
            }

            for(Object[] obj: datas)
            {
                OrderItem item = new OrderItem();
                item.setNameItem((String)obj[0]);
                item.setCodArt((String)obj[1]);
                item.setType("COMBO");
                orderItems.add(item);
            }

        }catch (NoResultException e)
        {
            return new ArrayList<OrderItem>();
        }
        return orderItems;
    }

    public Collection<OrderItem> findOrderItemPackByState(Date dateOrder) {
        List<OrderItem> orderItems = new ArrayList<OrderItem>();
        try{
            List<Object[]> datas;

                datas = em.createNativeQuery("select distinct nvl(pq.nombrecorto,'sin-nombre'),pq.paquete from USER01_DAF.paquetes pq\n" +
                        "inner join USER01_DAF.paquete_pedidos pp\n" +
                        "on pq.paquete = pp.paquete\n" +
                        "inner join USER01_DAF.pedidos pe\n" +
                        "on pp.pedido = pe.pedido\n" +
                        "where pe.fecha_entrega = :dateOrder\n" +
                        "and pe.estado_pedido <> 'ANL' ")
                        .setParameter("dateOrder",dateOrder,TemporalType.DATE)
                        .getResultList();

            for(Object[] obj: datas)
            {
                OrderItem item = new OrderItem();
                item.setNameItem((String)obj[0]);
                item.setCodArt((String)obj[1]);
                item.setType("COMBO");
                orderItems.add(item);
            }

        }catch (NoResultException e)
        {
            return new ArrayList<OrderItem>();
        }
        return orderItems;
    }

    @Override
    public Integer getAmountCombo(String codPaquete, String codPedido) {
        BigDecimal result = BigDecimal.ZERO;
        try{
            result = (BigDecimal)em.createNativeQuery("select nvl(cantidad,0)+nvl(REPOSICION,0) from USER01_DAF.paquete_pedidos \n" +
                    "where pedido = :codPedido\n" +
                    "and paquete = :codPaquete")
                    .setParameter("codPedido",codPedido)
                    .setParameter("codPaquete",codPaquete)
                    .getSingleResult();

        }catch (NoResultException e)
        {
            return 0;
        }
        return result.intValue();
    }

    public Integer getAmountCombo(String codPaquete,BigDecimal idDistributor, Date date) {
        BigDecimal result = BigDecimal.ZERO;
        try{
            result = (BigDecimal)em.createNativeQuery("select \n" +
                    "nvl(sum(paq.cantidad+ paq.REPOSICION),0)\n" +
                    "from USER01_DAF.pedidos ped\n" +
                    "inner join USER01_DAF.paquete_pedidos paq\n" +
                    "on paq.pedido = ped.pedido\n" +
                    "where ped.fecha_entrega = :date\n" +
                    "and ped.estado_pedido <> 'ANL'\n" +
                    "and ped.distribuidor = :idDistributor\n" +
                    "and paq.paquete = :codPaquete\n")
                    .setParameter("date", date, TemporalType.DATE)
                    .setParameter("idDistributor",idDistributor)
                    .setParameter("codPaquete",codPaquete)
                    .getSingleResult();

        }catch (NoResultException e)
        {
            return 0;
        }
        return result.intValue();
    }

    public Integer getAmountComboTotalAndDistributor(String codPaquete,BigDecimal distribuidor, Date date) {
        BigDecimal result = BigDecimal.ZERO;
        try{
            result = (BigDecimal)em.createNativeQuery("select \n" +
                    "nvl(sum(paq.cantidad),0)+nvl(sum(paq.REPOSICION),0)\n" +
                    "from USER01_DAF.pedidos ped\n" +
                    "inner join USER01_DAF.paquete_pedidos paq\n" +
                    "on paq.pedido = ped.pedido\n" +
                    "where ped.fecha_entrega = :date\n" +
                    "and ped.estado_pedido = 'PEN'\n" +
                    "and ped.distribuidor = :distribuidor\n" +
                    "and paq.paquete = :codPaquete\n")
                    .setParameter("date",date,TemporalType.DATE)
                    .setParameter("codPaquete",codPaquete)
                    .setParameter("distribuidor",distribuidor)
                    .getSingleResult();

        }catch (NoResultException e)
        {
            return 0;
        }
        return result.intValue();
    }

    public Integer getAmountComboTotal(String codPaquete, Date date) {
        BigDecimal result = BigDecimal.ZERO;
        try{
            result = (BigDecimal)em.createNativeQuery("select \n" +
                    "nvl(sum(paq.cantidad),0)+nvl(sum(paq.REPOSICION),0)\n" +
                    "from USER01_DAF.pedidos ped\n" +
                    "inner join USER01_DAF.paquete_pedidos paq\n" +
                    "on paq.pedido = ped.pedido\n" +
                    "where ped.fecha_entrega = :date\n" +
                    "and ped.estado_pedido <> 'ANL'\n" +
                    "and paq.paquete = :codPaquete\n")
                    .setParameter("date",date,TemporalType.DATE)
                    .setParameter("codPaquete",codPaquete)
                    .getSingleResult();

        }catch (NoResultException e)
        {
            return 0;
        }
        return result.intValue();
    }

    public Integer getAmountComboTotalDelivery(String codPaquete, Date date) {
        BigDecimal result = BigDecimal.ZERO;
        try{
            result = (BigDecimal)em.createNativeQuery("select \n" +
                    "nvl(sum(paq.cantidad),0)+nvl(sum(paq.REPOSICION),0)\n" +
                    "from USER01_DAF.pedidos ped\n" +
                    "inner join USER01_DAF.paquete_pedidos paq\n" +
                    "on paq.pedido = ped.pedido\n" +
                    "where ped.fecha_entrega = :date\n" +
                    "and ped.estado_pedido = 'PEN'\n" +
                    "and paq.paquete = :codPaquete\n")
                    .setParameter("date",date,TemporalType.DATE)
                    .setParameter("codPaquete",codPaquete)
                    .getSingleResult();

        }catch (NoResultException e)
        {
            return 0;
        }
        return result.intValue();
    }

    @Override
    public List<OrderItem> findArticulosPorFecha(Date date) {
        List<OrderItem> orderItems = new ArrayList<OrderItem>();
        List<Object[]> datas = new ArrayList<Object[]>();
        try {
                datas = (List<Object[]>)getEntityManager()
                        .createQuery("select distinct articulos.productItem.name,articulos.productItem.productItemCode  " +
                                "from CustomerOrder pe inner join pe.articulosPedidos articulos " +
                                "where pe.estado <> 'ANULADO' " +
                                "and pe.fechaEntrega =:fecha")
                .setParameter("fecha",date,TemporalType.DATE)
                .getResultList();

        }catch (NoResultException e){
            return orderItems;
        }
        for(Object[] obj: datas)
        {
            OrderItem item = new OrderItem();
            item.setNameItem((String)obj[0]);
            item.setCodArt((String)obj[1]);
            orderItems.add(item);
        }
        return orderItems;
    }

    @Override
    public List<Territoriotrabajo> findTerritoriosPedido(Date date) {
        List<Territoriotrabajo> territorios = new ArrayList<Territoriotrabajo>();
        List<Object[]> datas = new ArrayList<Object[]>();
        try{
            datas = (List<Object[]>)em.createQuery("select distinct pe.cliente.territoriotrabajo.nombre,pe.cliente.territoriotrabajo.idterritoriotrabajo  " +
                    "from CustomerOrder pe " +
                    "where pe.estado <> 'ANULADO' " +
                    "and pe.fechaEntrega =:fecha")
                    .setParameter("fecha",date,TemporalType.DATE)
                    .getResultList();
        }catch (NoResultException e){
            return territorios;
        }

        for(Object[] obj: datas)
        {
            Territoriotrabajo item = new Territoriotrabajo();
            item.setNombre((String) obj[0]);
            item.setIdterritoriotrabajo((Long)obj[1]);
            territorios.add(item);
        }

        return territorios;
    }

    @Override
    public List<BigDecimal> findDistributor(Date dateOrder)
    {
        List<BigDecimal> distributors;
        try{
            distributors = em.createNativeQuery("select distinct distribuidor from USER01_DAF.pedidos where distribuidor is not null \n" +
                                                " and fecha_entrega = :dateOrder")
                           .setParameter("dateOrder",dateOrder,TemporalType.DATE)
                           .getResultList();

        }catch(NoResultException e)
        {
            return new ArrayList<BigDecimal>();
        }
        return distributors;
    }

    @Override
    public List<OrderItem> findOrderItem(Date dateOrder,String stateOrder){
        List<OrderItem> orderItems = new ArrayList<OrderItem>();
        try{
            List<Object[]> datas;
            if(stateOrder == "TODOS")
            {
                datas = em.createNativeQuery("select distinct nvl(ia.nombrecorto,'sin-nombre'),ap.id_cuenta,ap.cod_art,ap.no_cia from USER01_DAF.articulos_pedido ap\n" +
                        "inner join WISE.inv_articulos ia\n" +
                        "on ia.cod_art = ap.cod_art\n" +
                        "inner join USER01_DAF.pedidos pe\n" +
                        "on ap.pedido = pe.pedido\n" +
                        "where pe.fecha_entrega = :dateOrder\n" +
                        "and pe.estado_pedido <> 'ANL'\n")
                        .setParameter("dateOrder",dateOrder,TemporalType.DATE)
                        .getResultList();
            }
            else
            {
                datas = em.createNativeQuery("select distinct nvl(ia.nombrecorto,'sin-nombre'),ap.id_cuenta,ap.cod_art,ap.no_cia from USER01_DAF.articulos_pedido ap\n" +
                        "inner join WISE.inv_articulos ia\n" +
                        "on ia.cod_art = ap.cod_art\n" +
                        "inner join USER01_DAF.pedidos pe\n" +
                        "on ap.pedido = pe.pedido\n" +
                        "where pe.fecha_entrega = :dateOrder\n" +
                        "and pe.ESTADO_PEDIDO =:stateOrder")
                        .setParameter("dateOrder",dateOrder,TemporalType.DATE)
                        .setParameter("stateOrder",stateOrder)
                        .getResultList();
            }

            for(Object[] obj: datas)
            {
                OrderItem item = new OrderItem();
                item.setNameItem((String)obj[0]);
                item.setIdAccount((BigDecimal)obj[1]);
                item.setCodArt((String)obj[2]);
                item.setNoCia((String)obj[3]);
                orderItems.add(item);
            }

        }catch (NoResultException e)
        {
            return new ArrayList<OrderItem>();
        }
        return orderItems;
    }

    public List<OrderItem> findOrderItemByState(Date dateOrder){
        List<OrderItem> orderItems = new ArrayList<OrderItem>();
        try{
            List<Object[]> datas = new ArrayList<Object[]>();

                datas = em.createNativeQuery("select distinct nvl(ia.nombrecorto,'sin-nombre'),ap.id_cuenta,ap.cod_art,ap.no_cia from USER01_DAF.articulos_pedido ap\n" +
                        "inner join WISE.inv_articulos ia\n" +
                        "on ia.cod_art = ap.cod_art\n" +
                        "inner join USER01_DAF.pedidos pe\n" +
                        "on ap.pedido = pe.pedido\n" +
                        "where pe.fecha_entrega = :dateOrder\n" +
                        "and pe.estado_pedido <> 'ANL' \n")
                        .setParameter("dateOrder",dateOrder,TemporalType.DATE)
                        .getResultList();


            for(Object[] obj: datas)
            {
                OrderItem item = new OrderItem();
                item.setNameItem((String)obj[0]);
                item.setIdAccount((BigDecimal)obj[1]);
                item.setCodArt((String)obj[2]);
                item.setNoCia((String)obj[3]);
                orderItems.add(item);
            }

        }catch (NoResultException e)
        {
            return new ArrayList<OrderItem>();
        }
        return orderItems;
    }

}
