package com.encens.khipus.service.production;

import com.encens.khipus.exception.production.ProductCompositionException;
import com.encens.khipus.framework.service.ExtendedGenericServiceBean;
import com.encens.khipus.model.production.*;
import com.encens.khipus.model.warehouse.ProductItem;
import com.encens.khipus.util.RoundUtil;
import com.encens.khipus.util.TopologicalSorting;
import de.congrace.exp4j.Calculable;
import de.congrace.exp4j.ExpressionBuilder;
import de.congrace.exp4j.UnknownFunctionException;
import de.congrace.exp4j.UnparsableExpressionException;
import org.apache.commons.lang.StringUtils;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.persistence.NoResultException;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.encens.khipus.exception.production.ProductCompositionException.NO_FOUND_VARIABLE;
import static com.encens.khipus.exception.production.ProductCompositionException.TOPOLOGICAL_SORTING;

@Name("evaluatorMathematicalExpressionsService")
@AutoCreate
public class EvaluatorMathematicalExpressionsServiceBean extends ExtendedGenericServiceBean implements EvaluatorMathematicalExpressionsService {

    @In("#{messages['ProductComposition.defaultContainerWeight']}")
    private String containerVariable;

    @In("#{messages['ProductComposition.defaultProducingAmountVariable']}")
    private String amountVariable;

    @In("#{messages['ProductComposition.defaultSupposedAmount']}")
    private String supposedVariable;

    @In
    private ArticleEstateService articleEstateService;

    Pattern variablePattern;

    @Create
    public void initalizeService() {
        String format = String.format("%s|%s|%s|%s", "ING\\d+", containerVariable, amountVariable, supposedVariable);
        variablePattern = Pattern.compile(format);
    }

    @Override
    public void executeMathematicalFormulas(ProductionOrder productionOrder) throws ProductCompositionException, IOException {
        EquationMap map = new EquationMap();
        map.addProductionIngredient(productionOrder.getProductComposition().getProductionIngredientList());
        map.addLiteralEquation(amountVariable, productionOrder.getExpendAmount());
        map.addLiteralEquation(containerVariable, productionOrder.getContainerWeight());
        map.addLiteralEquation(supposedVariable, productionOrder.getExpendAmount());

        calculateMathematicalFormula(map);
    }

    @Override
    public void excuteFormulate(ProductionOrder productionOrder, Double formulateContainer, Double formulateSupposed) throws ProductCompositionException, IOException {

        EquationMap map = new EquationMap();
        map.addProductionIngredient(productionOrder.getProductComposition().getProductionIngredientList());
        map.addLiteralEquation(amountVariable, productionOrder.getExpendAmount());
        map.addLiteralEquation(containerVariable, formulateContainer);
        map.addLiteralEquation(supposedVariable, formulateSupposed);

        calculateMathematicalFormula(map);
    }

    @Override
    public void excuteFormulate(List<ProductionIngredient> ingredients, Double  expendAmount, Double container,Double supposed) throws IOException, ProductCompositionException {

        EquationMap map = new EquationMap();
        map.addProductionIngredient(ingredients);
        map.addLiteralEquation(amountVariable, expendAmount);
        map.addLiteralEquation(containerVariable, container);
        map.addLiteralEquation(supposedVariable, supposed);

        calculateMathematicalFormula(map);

    }

    @Override
    public Double excuteParemeterized(OrderInput input, ProductionOrder productionOrder, Double containerWeight, Double supposedAmount)throws ProductCompositionException, IOException{

        return RoundUtil.getRoundValue(input.getAmount() * 100 /getPorcentaje(input),2, RoundUtil.RoundMode.SYMMETRIC);
    }

    @Override
    public Double getAmountExpected(Double expectedOld, Double containerOld, Double containerNew)throws ProductCompositionException, IOException{

        Double percentage = containerNew * 100 / containerOld;
        return RoundUtil.getRoundValue( expectedOld * (percentage/100),2, RoundUtil.RoundMode.SYMMETRIC);
    }

    private Double getPorcentaje(OrderInput input)
    {
        String[] parts = input.getMathematicalFormula().split("\\*");
        return (Double)Double.parseDouble(parts[0]);
    }

    @Override
    public void excuteParemeterizadFormulate(ProductionOrder productionOrder, Double formulateContainer, Double formulateSupposed) throws ProductCompositionException, IOException{

        EquationMap map = new EquationMap();

        map.addProductionIngredient(filterParameterized(productionOrder.getProductComposition().getProductionIngredientList()));
        map.addLiteralEquation(amountVariable, productionOrder.getExpendAmount());
        map.addLiteralEquation(containerVariable, formulateContainer);
        map.addLiteralEquation(supposedVariable, formulateSupposed);

        calculateMathematicalFormula(map);
    }

    public OrderInput getParameterized(List<OrderInput> inputs)
    {

        for(OrderInput input: inputs)
        {
            if(articleEstateService.verifyEstate(input.getProductItem(),"PARAMETRIZABLE"))
                return input;
        }

        return null;
    }

    public List<ProductionIngredient> filterParameterized(List<ProductionIngredient> ingredients)
    {
        List<ProductionIngredient> productionIngredients = new ArrayList<ProductionIngredient>();
        for(ProductionIngredient ingredient: ingredients)
        {
            if(!articleEstateService.verifyEstate(ingredient.getMetaProduct().getProductItem(),"PARAMETRIZABLE"))
                productionIngredients.add(ingredient);
        }
        return productionIngredients;
    }

    private void calculateMathematicalFormula(EquationMap map) throws ProductCompositionException, IOException {
        calculateMathematicalFormulasWithoutDependencies(map);
        calculateMathematicalFormulasWithDependencies(map);
    }

    @Override
    public void executeMathematicalFormulas(ProductComposition productComposition) throws ProductCompositionException, IOException {

        EquationMap map = new EquationMap();
        map.addProductionIngredient(productComposition.getProductionIngredientList());
        map.addLiteralEquation(amountVariable, productComposition.getProducingAmount());
        map.addLiteralEquation(containerVariable, productComposition.getContainerWeight());
        map.addLiteralEquation(supposedVariable, productComposition.getSupposedAmount());

        calculateMathematicalFormula(map);
    }

    public BigDecimal getMountInWarehouse(MetaProduct metaProduct) {
        BigDecimal result = null;
        try {
            result = (BigDecimal) getEntityManager()
                    .createQuery("SELECT  sum(inventory.unitaryBalance) from Inventory inventory where inventory.productItem = :productItem")
                    .setParameter("productItem", metaProduct.getProductItem())
                    .getSingleResult();
            return result;
        } catch (NoResultException nre) {
            return (result == null) ? new BigDecimal(0) : result;
        }
    }

    public BigDecimal getMountInWarehouse(ProductItem item) {
        BigDecimal result = null;
        try {
            result = (BigDecimal) getEntityManager()
                    .createQuery("SELECT  sum(inventory.unitaryBalance) from Inventory inventory where inventory.productItem = :productItem")
                    .setParameter("productItem", item)
                    .getSingleResult();
            return result;
        } catch (NoResultException nre) {
            return (result == null) ? new BigDecimal(0) : result;
        }
    }

    private void calculateMathematicalFormulasWithoutDependencies(EquationMap map) throws ProductCompositionException {
        String formula = "";
        try {
            for (Equation pi : map.values()) {
                if (extractVariables(pi.getFormula()).size() > 0) {
                    continue;
                }

                formula = pi.getFormula();
                Calculable calc = new ExpressionBuilder(formula).build();
                double result = calc.calculate();
                result = RoundUtil.getRoundValue(result, 2, RoundUtil.RoundMode.SYMMETRIC);
                pi.setResult(result);
            }
        } catch (Exception ex) {
            throw new ProductCompositionException(TOPOLOGICAL_SORTING, formula, ex);
        }
    }

    private void calculateMathematicalFormulasWithDependencies(EquationMap map) throws IOException, ProductCompositionException {
        String graph = generateDependenciesGraph(map);
        List<String> executionOrder = findExecutionOrder(graph);

        executeFormulas(map, executionOrder);
    }

    private String generateDependenciesGraph(EquationMap map) {
        String graph = "";
        for (Equation pi : map.values()) {
            String dependencies = createGraphDependencies(pi);
            if (StringUtils.isNotBlank(dependencies)) {
                graph += (graph.length() == 0 ? "" : "\n");
                graph += dependencies;
            }
        }
        return graph;
    }

    private List<String> findExecutionOrder(String graph) throws ProductCompositionException, IOException {
        if (StringUtils.isBlank(graph)) {
            return new ArrayList<String>();
        }

        TopologicalSorting sorter = new TopologicalSorting();
        List<String> executionOrder = sorter.findDAG(new StringReader(graph));

        if (executionOrder == null) {
            throw new ProductCompositionException(TOPOLOGICAL_SORTING, true);
        }
        Collections.reverse(executionOrder);
        return executionOrder;
    }

    private void executeFormulas(EquationMap map, List<String> executionOrder) throws ProductCompositionException {
        String formula = "";
        String variable = "";
        try {
            for (String var : executionOrder) {
                variable = var;
                Equation equation = map.find(variable);
                formula = equation.getFormula();
                List<String> variables = extractVariables(formula);
                double result = executeFormula(formula, variables, map);
                result = RoundUtil.getRoundValue(result, 5, RoundUtil.RoundMode.SYMMETRIC);
                //todo: controlar que se multiplique dependiendo de la unidad correcta
                result = equation.getMeasureUnit().equals("GR") || equation.getMeasureUnit().equals("ML") ? result * 1000 : result;
                equation.setResult(result);
            }
        } catch (NotFoundException ex) {
            throw new ProductCompositionException(NO_FOUND_VARIABLE, variable, ex);
        } catch (Exception ex) {
            throw new ProductCompositionException(TOPOLOGICAL_SORTING, formula, ex);
        }
    }

    private String createGraphDependencies(Equation pi) {
        List<String> dependencies = extractVariables(pi.getFormula());
        if (dependencies.size() == 0) {
            return null;
        }

        String result = "";
        String root = pi.getVariable();
        for (String dependency : dependencies) {
            result += (result.length() == 0 ? "" : "\n");
            result += root + " " + dependency;
        }

        return result;
    }

    private double executeFormula(String formula, List<String> variables, EquationMap map) throws UnknownFunctionException, UnparsableExpressionException, NotFoundException {
        ExpressionBuilder expr = new ExpressionBuilder(formula);
        expr.withVariableNames(variables.toArray(new String[]{}));
        Calculable calc = expr.build();

        for (String var : variables) {
            Equation eq = map.find(var);
            calc.setVariable(var, eq.getResult());
        }

        return calc.calculate();
    }

    private List<String> extractVariables(String formula) {
        List<String> result = new ArrayList<String>();
        Matcher matcher = variablePattern.matcher(formula);
        while (matcher.find()) {
            result.add(matcher.group());
        }
        return result;
    }

    class EquationMap extends HashMap<String, Equation> {

        public void addProductionIngredient(ProductionIngredient productionIngredient) {
            Equation equation = new ProductionIngredientEquation(productionIngredient);
            add(equation);
        }

        public void add(Equation equation) {
            put(equation.getVariable(), equation);
        }

        public void addProductionIngredient(List<ProductionIngredient> productionIngredientList) {
            for (ProductionIngredient pi : productionIngredientList) {
                addProductionIngredient(pi);
            }
        }

        public void addLiteralEquation(String variable, double amount) {
            Equation equation = new LiteralEquation(variable, amount);
            add(equation);
        }

        public Equation find(String variable) throws NotFoundException {
            Equation eq = get(variable);
            if (eq == null) {
                throw new NotFoundException();
            }
            return eq;
        }

        public Collection<Equation> values() {
            return super.values();
        }
    }

    class NotFoundException extends Exception {
        public NotFoundException() {
        }
    }

    interface Equation {
        public String getVariable();

        public String getFormula();

        public void setResult(double result);

        public double getResult();

        public String getMeasureUnit();
    }

    class ProductionIngredientEquation implements Equation {
        private ProductionIngredient pi;

        public ProductionIngredientEquation(ProductionIngredient pi) {
            this.pi = pi;
        }

        public String getVariable() {
            return "ING" + pi.getMetaProduct().getId();
        }

        public String getFormula() {
            return pi.getMathematicalFormula();
        }

        public void setResult(double result) {
            pi.setAmount(result);
            BigDecimal mount = getMountInWarehouse(pi.getMetaProduct());
            pi.setMountWareHouse((mount == null) ? new BigDecimal(0) : mount);
        }

        public double getResult() {
            return pi.getAmount();
        }

        public String getMeasureUnit() {
            return pi.getMetaProduct().getProductItem().getUsageMeasureCode();
        }
    }

    class LiteralEquation implements Equation {
        private double value;
        private String variable;

        LiteralEquation(String variable, double value) {
            this.value = value;
            this.variable = variable;
        }

        public String getVariable() {
            return variable;
        }

        public String getFormula() {
            return Double.toString(value);
        }

        public void setResult(double result) {
            value = result;
        }

        public double getResult() {
            return value;
        }

        public String getMeasureUnit() {
            return variable;
        }
    }
}
