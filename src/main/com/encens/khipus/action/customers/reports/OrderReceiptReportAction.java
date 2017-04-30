package com.encens.khipus.action.customers.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.model.customers.ClientOrderEstate;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.reports.GenerationReportData;
import com.encens.khipus.service.customers.*;
import com.encens.khipus.util.MessageUtils;
import com.jatun.titus.reportgenerator.util.TypedReportData;
import net.sf.jasperreports.engine.JRPrintPage;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.fill.JRTemplatePrintText;
import net.sf.jasperreports.engine.fill.JRTemplateText;
import net.sf.jasperreports.engine.type.HorizontalAlignEnum;
import net.sf.jasperreports.engine.type.RunDirectionEnum;
import net.sf.jasperreports.engine.type.VerticalAlignEnum;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Encens S.R.L.
 * Action to generate payroll summary report by payment method and currency
 *
 * @author
 * @version $Id: SummaryPayrollByPaymentMethodReportAction.java  22-ene-2010 11:38:12$
 */
@Name("orderReceiptReportAction")
@Scope(ScopeType.CONVERSATION)
public class OrderReceiptReportAction extends GenericReportAction {

    private String date;
    private ClientOrderEstate stateOrder = ClientOrderEstate.ALL;
    private int postXIniAmount,widthIniAmount;
    private Date dateOrder;
    private Employee distributor;

    @In
    private AccountItemService accountItemService;

    @In
    private ClientOrderService clientOrderService;

    private List<OrderClient> orderClients = new ArrayList<OrderClient>();
    private List<OrderItem> orderItems = new ArrayList<OrderItem>();
    private List<BigDecimal> distributors = new ArrayList<BigDecimal>();
    private List<Integer> totals = new ArrayList<Integer>();
    private int amountY,amountX,amountH,amountW;
    private int clientY,clientX,clientH,clientW;
    private Integer totalOrders = 0;
    private Integer totalGeneral = 0;

    @Factory(value = "clientOrderStates", scope = ScopeType.STATELESS)
    public ClientOrderEstate[] getClientOrderEstate() {
        return ClientOrderEstate.values();
    }

    public void generateReport() {
        log.debug("Generate OrderReceiptReport........");
        TypedReportData typedReportData;
        String templatePath = "/customers/reports/orderReportReceipt.jrxml";
        String fileName = "Orden_Report_Receipt";
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        date = sdf.format(dateOrder);

        Map params = new HashMap();
        distributors = accountItemService.findDistributor(dateOrder);
        orderItems = accountItemService.findOrderItem(dateOrder, ClientOrderEstate.getVal(stateOrder));
        orderItems.addAll(accountItemService.findOrderItemPack(dateOrder, ClientOrderEstate.getVal(stateOrder)));
        params.putAll(getCommonDocumentParamsInfo());

        String query = "select * from USER01_DAF.per_insts\n" +
                       "where rownum = 1\n";

      /*  String query ="select pe.ap,pe.am , pe.nom, ped.pedido from USER01_DAF.per_insts pi\n" +
                "inner join USER01_DAF.personas pe\n" +
                "on pe.nro_doc = pi.nro_doc\n" +
                "inner join USER01_DAF.pedidos ped\n" +
                "on ped.id = pi.id\n" +
                "where ped.fecha_entrega = :date \n" +
                "and ped.estado_pedido = 'PEN' ";*/
        //setReportFormat(ReportFormat.PDF);

        typedReportData = getReport(
                fileName
                , templatePath
                , query
                , params
                , "RECEPCION_DE_PEDIDOS"
        );

        JasperPrint jasperPrint = typedReportData.getJasperPrint();
        JRTemplatePrintText temp_client = ((JRTemplatePrintText) (((JRPrintPage) (jasperPrint.getPages().get(0))).getElements().get(5)));
        JRTemplatePrintText temp_amount = ((JRTemplatePrintText) (((JRPrintPage) (jasperPrint.getPages().get(0))).getElements().get(6)));
        JRTemplatePrintText temp_product = ((JRTemplatePrintText) (((JRPrintPage) (jasperPrint.getPages().get(0))).getElements().get(4)));
        JRTemplatePrintText temp_total = ((JRTemplatePrintText) (((JRPrintPage) (jasperPrint.getPages().get(0))).getElements().get(7)));
        temp_total.setX(temp_amount.getX());
        temp_total.setY(temp_amount.getY());
        temp_client.setHeight(10);
        temp_amount.setHeight(10);
        temp_total.setHeight(10);

        amountY = temp_amount.getY();
        amountX = temp_amount.getX();
        amountH = temp_amount.getHeight();
        amountW = temp_amount.getWidth();
        clientX = temp_client.getX();
        clientY = temp_client.getY();
        clientH = temp_client.getHeight();
        clientW = temp_client.getWidth();
        ((JRPrintPage) (typedReportData.getJasperPrint().getPages().get(0))).getElements().addAll(generateHeader(temp_product));
        JRPrintPage tempPage = ((JRPrintPage) (typedReportData.getJasperPrint().getPages().get(0)));
        totals.clear();
        if(orderItems.size() > 0)
        if(distributor != null)
        {
            orderClients =  accountItemService.findClientsOrder(dateOrder,new BigDecimal(distributor.getId()),ClientOrderEstate.getVal(stateOrder));
            String nameDistributor = accountItemService.getNameEmployeed(new BigDecimal(distributor.getId()));

            ((JRPrintPage) (typedReportData.getJasperPrint().getPages().get(0))).getElements().addAll(generateClients(temp_client,nameDistributor,temp_total));
            //((JRPrintPage) (typedReportData.getJasperPrint().getPages().get(0))).getElements().addAll(generateClients(temp_client,nameDistributor));
            ((JRPrintPage) (typedReportData.getJasperPrint().getPages().get(0))).getElements().addAll(generateAmounts(temp_amount));
        }
        else
        for(BigDecimal distributor: distributors)
        {
            orderClients =  accountItemService.findClientsOrder(dateOrder,distributor,ClientOrderEstate.getVal(stateOrder));
            String nameDistributor = accountItemService.getNameEmployeed(distributor);

            ((JRPrintPage) (typedReportData.getJasperPrint().getPages().get(0))).getElements().addAll(generateClients(temp_client,nameDistributor,temp_total));
            //((JRPrintPage) (typedReportData.getJasperPrint().getPages().get(0))).getElements().addAll(generateClients(temp_client,nameDistributor));
            ((JRPrintPage) (typedReportData.getJasperPrint().getPages().get(0))).getElements().addAll(generateAmounts(temp_amount));

        }
        ((JRPrintPage) (typedReportData.getJasperPrint().getPages().get(0))).getElements().addAll(generateTotals(temp_amount));
        ((JRPrintPage) (typedReportData.getJasperPrint().getPages().get(0))).getElements().add(createCellY(temp_total,"   TOTAL GENERAL:", clientY));
        JRTemplatePrintText printText = (JRTemplatePrintText)((JRPrintPage) (jasperPrint.getPages().get(0))).getElements().get(1);
        printText.setText(totalOrders.toString());
        try {
            typedReportData.setJasperPrint(jasperPrint);
            GenerationReportData generationReportData = new GenerationReportData(typedReportData);
            generationReportData.exportReport();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<JRTemplatePrintText> generateTotals(JRTemplatePrintText temp){
        List<JRTemplatePrintText> printTextList = new ArrayList<JRTemplatePrintText>();
        for(Integer total:totals)
        {
            JRTemplatePrintText printText = createCellY(temp,total.toString(), amountY);
            printText.setX(amountX);
            printTextList.add(printText);
            amountX = amountX+amountW;
        }
        return printTextList;
    }

    private List<JRTemplatePrintText> generateAmounts(JRTemplatePrintText temp){
        List<JRTemplatePrintText> printTextList = new ArrayList<JRTemplatePrintText>();
        int auxY = amountY;
        int auxX = amountX;
        int lastY= 0;
        Integer val = 0;
        Integer total = 0;
        int cont = 0;

        for(OrderItem item : orderItems)
        {

            for(OrderClient client:orderClients)
            {
                client.setPosX(amountY);
                client.setPosY(temp.getY());
                if(item.getType() == "ESTADO")
                {
                    String aux;
                    if(client.getState().equals("PEN"))
                         aux = "NO";
                    else
                         aux = "SI";

                    JRTemplatePrintText printText = createCellY(temp, aux, amountY);
                    printText.setX(amountX);
                    printTextList.add(printText);
                }else {
                    if (item.getType() == "COMBO")
                        val = accountItemService.getAmountCombo(item.getCodArt(), client.getIdOrder());
                    else
                        val = accountItemService.getAmount(item.getCodArt(), client.getIdOrder());

                    JRTemplatePrintText printText = createCellY(temp, val.toString(), amountY);
                    printText.setX(amountX);
                    printTextList.add(printText);
                }
                amountY = amountY+amountH;
                total += val;
            }
            JRTemplatePrintText printText = createCellY(temp,total.toString(), amountY);
            printText.setX(amountX);
            printTextList.add(printText);
            amountX = amountX+amountW;
            lastY = amountY;
            amountY = auxY;

            try{
                totals.set(cont,totals.get(cont) +total);
            }catch (IndexOutOfBoundsException e)
            {
                totals.add(total);
            }

            total  = 0;
            cont++;
        }
        amountY = lastY+amountH+10;
        amountX = auxX;
        return printTextList;
    }

    private List<JRTemplatePrintText> generateClients(JRTemplatePrintText temp, String nameDistributor,JRTemplatePrintText temp_total){
    //private List<JRTemplatePrintText> generateClients(JRTemplatePrintText temp, String nameDistributor){
        List<JRTemplatePrintText> printTextList = new ArrayList<JRTemplatePrintText>();


        for(OrderClient client:orderClients)
        {
            client.setPosX(clientX);
            client.setPosY(temp.getY());
            printTextList.add(createCellY(temp,client.getName(), clientY));
            clientY = clientY+clientH;
            totalOrders ++;
        }
        temp_total.setX(temp.getX());
        printTextList.add(createCellY(temp_total,nameDistributor+" - TOTAL: ", clientY));
        clientY = clientY+clientH+10;
        return printTextList;
    }

    private List<JRTemplatePrintText> generateHeader(JRTemplatePrintText temp)
    {
        List<JRTemplatePrintText> printTextList = new ArrayList<JRTemplatePrintText>();
        int x = temp.getX();
        int w = temp.getWidth();
        printTextList.add(createCellX(temp,"ENTREGADO", x));
        orderItems.add(0,new OrderItem("ESTADO"));
        for(OrderItem orderItem:orderItems)
        {
            orderItem.setPosX(x);
            orderItem.setPosY(temp.getY());
            printTextList.add(createCellX(temp, orderItem.getNameItem(), x));
            x = x+w;

        }

        return printTextList;
    }

    private JRTemplatePrintText createCellX(JRTemplatePrintText temp, String valor,int posX)
    {
        JRTemplateText templateText = new JRTemplateText(temp.getOrigin(),temp.getDefaultStyleProvider());
        templateText.setHorizontalAlignment(HorizontalAlignEnum.RIGHT);
        templateText.setVerticalAlignment(VerticalAlignEnum.MIDDLE);
        templateText.copyLineBox(temp.getLineBox());
        templateText.setStyle(temp.getTemplate().getStyle());
        templateText.setFontName(temp.getFontName());
        templateText.setFontSize(temp.getFontSize());
        templateText.setMode(temp.getModeValue());
        JRTemplatePrintText printText = new JRTemplatePrintText(templateText);
        //printText = temp;
        printText.setText(valor);
        printText.setRunDirection(RunDirectionEnum.LTR);
        printText.setLineSpacingFactor(1.2578125f);
        printText.setLeadingOffset(-1.7578125f);
        printText.setTextHeight(10.0625f);
        printText.setY(temp.getY());
        printText.setX(posX);
        printText.setHeight(temp.getHeight());
        printText.setWidth(temp.getWidth());

        return printText;
    }

    private JRTemplatePrintText createCellY(JRTemplatePrintText temp, String valor,int posY)
    {
        JRTemplateText templateText = new JRTemplateText(temp.getOrigin(),temp.getDefaultStyleProvider());
        templateText.setHorizontalAlignment(HorizontalAlignEnum.RIGHT);
        templateText.setVerticalAlignment(VerticalAlignEnum.MIDDLE);
        templateText.copyLineBox(temp.getLineBox());
        templateText.setStyle(temp.getTemplate().getStyle());
        templateText.setFontName(temp.getFontName());
        templateText.setFontSize(temp.getFontSize());
        templateText.setMode(temp.getModeValue());
        JRTemplatePrintText printText = new JRTemplatePrintText(templateText);
        //printText = temp;
        printText.setText(valor);
        printText.setRunDirection(RunDirectionEnum.LTR);
        printText.setLineSpacingFactor(1.2578125f);
        printText.setLeadingOffset(-1.7578125f);
        printText.setTextHeight(10.0625f);
        printText.setX(temp.getX());
        printText.setY(posY);
        printText.setHeight(temp.getHeight());
        printText.setWidth(temp.getWidth());

        return printText;
    }

    private JRTemplatePrintText createCellAmount(JasperPrint jasperPrint){
        int posX;
        int posY;

        JRTemplatePrintText temp = ((JRTemplatePrintText) (((JRPrintPage) (jasperPrint.getPages().get(0))).getElements().get(10)));
        JRTemplateText templateText = new JRTemplateText(temp.getOrigin(),temp.getDefaultStyleProvider());
        templateText.setHorizontalAlignment(HorizontalAlignEnum.RIGHT);
        templateText.setVerticalAlignment(VerticalAlignEnum.MIDDLE);
        templateText.copyLineBox(temp.getLineBox());
        templateText.setStyle(temp.getTemplate().getStyle());
        templateText.setFontName(temp.getFontName());
        templateText.setFontSize(temp.getFontSize());
        templateText.setMode(temp.getModeValue());
        JRTemplatePrintText printText = new JRTemplatePrintText(templateText);
        //printText = temp;
        printText.setText("prueba");
        printText.setRunDirection(RunDirectionEnum.LTR);
        printText.setLineSpacingFactor(1.2578125f);
        printText.setLeadingOffset(-1.7578125f);
        printText.setTextHeight(10.0625f);
        printText.setX(temp.getX()+temp.getX());
        printText.setY(temp.getY());
        printText.setHeight(temp.getHeight());
        printText.setWidth(temp.getWidth());

        return printText;
    }

    private Map<String, Object> getCommonDocumentParamsInfo() {

        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("dateOrder", date);
        paramMap.put("reportTitle", MessageUtils.getMessage("ProductionPlanning.orderInputOrMaterial"));
        paramMap.put("stateOrder", ClientOrderEstate.getVal(stateOrder));
        paramMap.put("totalOrder", totalOrders.toString());
        return paramMap;
    }



    class OrderReport
    {
        private String nameClient;
        private List<Integer> amounts  = new ArrayList<Integer>();

        String getNameClient() {
            return nameClient;
        }

        void setNameClient(String nameClient) {
            this.nameClient = nameClient;
        }

        List<Integer> getAmounts() {
            return amounts;
        }

        void setAmounts(List<Integer> amounts) {
            this.amounts = amounts;
        }
    }

    public Date getDateOrder() {
        return dateOrder;
    }

    public void setDateOrder(Date dateOrder) {
        this.dateOrder = dateOrder;
    }

    public ClientOrderEstate getStateOrder() {
        return stateOrder;
    }

    public void setStateOrder(ClientOrderEstate stateOrder) {
        this.stateOrder = stateOrder;
    }

    public Employee getDistributor() {
        return distributor;
    }

    public void setDistributor(Employee distributor) {
        this.distributor = distributor;
    }

    public void cleanDistributor(){
        setDistributor(null);
    }
}