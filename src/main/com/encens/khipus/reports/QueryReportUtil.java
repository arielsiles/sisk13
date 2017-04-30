package com.encens.khipus.reports;

import com.encens.khipus.framework.action.QueryResult;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The QueryReportUtil has utilities that could be used for prepared
 * the queries that go to used inside the report generation.
 *
 * @author
 * @version 1.0.18
 */
public class QueryReportUtil {

    private QueryReportUtil() {
    }

    private static Pattern JPQLPARAMETER_PATTERN = Pattern.compile("(:([a-zA-Z0-9]*))", Pattern.CASE_INSENSITIVE);

    /**
     * The method convertToReportQuery convert the ejbql query of a com.encens.khipus.framework.action.QueryResult
     * to ejbql used inside the report generation.
     *
     * @param queryResult the com.encens.khipus.framework.action.QueryResult
     * @return QueryResult
     */
    public static QueryResult convertToReportQuery(QueryResult queryResult) {
        queryResult.setEjbql(convertJpqlQueryToReportQuery(queryResult.getEjbql()));
        return queryResult;
    }

    /**
     * This method have the same purpose that convertJpqlQueryToReportQuery(StringBuilder), the
     * diferences is the parameter type.
     *
     * @param jpqlQuery String
     * @return jpql query that will be used on report generation
     */
    public static String convertJpqlQueryToReportQuery(String jpqlQuery) {
        return convertJpqlQueryToReportQuery(new StringBuilder(jpqlQuery));
    }

    /**
     * This method changes the report parameter(E.g. $P{myParam}) instead of Jpql parameter(E.g. :myParam).
     * <br/>
     * E.g.
     * <br/>
     * <b>Jpql query = </b> Select p from Person p where p.company=:myCompany and p.isNumber=:myIdNumber
     * <br/>
     * <b>Jpql query for report = </b> Select p from Person p where p.company=$P{myCompany} and p.isNumber=$P{myIdNumber}
     *
     * @param jpqlQueryBuilder content the jpql result
     * @return jpql query that will be used on report generation
     */
    public static String convertJpqlQueryToReportQuery(StringBuilder jpqlQueryBuilder) {
        Matcher jpqlMatcher = JPQLPARAMETER_PATTERN.matcher(jpqlQueryBuilder);
        while (safeFind(jpqlMatcher)) {
            jpqlQueryBuilder.replace(jpqlMatcher.start(1), jpqlMatcher.end(1), "$P{" + jpqlMatcher.group(2) + "}");
            jpqlMatcher.reset();
        }
        return jpqlQueryBuilder.toString();
    }

    private static boolean safeFind(Matcher matcher) {
        try {
            return matcher.find();
        } catch (Exception e) {
            return false;
        }
    }

}
