package com.encens.khipus.util;

import org.apache.commons.lang.StringUtils;

import java.util.*;

/**
 * Utilities for sort collections by property.
 *
 * @author
 * @version $Id: SortUtils.java,v 1.4 2007/10/27 16:08:29
 */
public class SortUtils {
    private SortUtils() {
    }
    //    private static Log log = LogFactory.getLog(SortUtils.class);

    /**
     * Order an ArrayList by a property.
     *
     * @param items    Collection with items to sort
     * @param property the property to sort
     * @return sorted by property collection.
     */
    public static synchronized ArrayList orderByProperty(ArrayList items, final String property) {
        Iterator it = items.iterator();
        SortedSet byName = new TreeSet(new Comparator() {
            public int compare(Object a, Object b) {
                String aux1 = null;
                String aux2 = null;
                try {
                    aux1 = (String) a.getClass().getMethod("get" + StringUtils.capitalize(property),
                            new Class[]{}).invoke(a, new Class[]{});

                    aux2 = (String) b.getClass().getMethod("get" + StringUtils.capitalize(property),
                            new Class[]{}).invoke(b, new Class[]{});

                } catch (Exception e) {
//                    log.error("Unexpected error trying to sort", e);
                }
                return (aux1.compareToIgnoreCase(aux2));
            }
        });
        while (it.hasNext()) {
            Object aux = (Object) it.next();
            byName.add(aux);
        }
        return new ArrayList(byName);
    }

    /**
     * Order an ArrayList by a property using a specific comparable class.
     *
     * @param listOfMaps      Collection with items to sort
     * @param comparableClass comparable class to use
     * @param property        the property to sort
     * @return sorted by property collection.
     * @author
     */
    public static synchronized void orderByProperty(List listOfMaps, final Class<? extends Comparable> comparableClass, final String property) {
//        log.debug("orderListOfMapsByProperty(java.util.List, '" + orderKey + "')");

        Comparator c = new Comparator() {
            public int compare(Object a, Object b) {
                int value = 0;
                Comparable aux1 = null;
                Comparable aux2 = null;
                try {
                    aux1 = comparableClass.cast(a.getClass().getMethod("get" + StringUtils.capitalize(property), new Class[]{}).invoke(a, new Class[]{}));
                    aux2 = comparableClass.cast(b.getClass().getMethod("get" + StringUtils.capitalize(property), new Class[]{}).invoke(b, new Class[]{}));
                    return aux1.compareTo(aux2);
                } catch (Exception e) {
                }
                return value;
            }
        };
        Collections.sort(listOfMaps, c);
    }

    public static synchronized ArrayList orderByPropertyMap(List itemsMap, final String property) {
        Iterator it = itemsMap.iterator();
        SortedSet byName = new TreeSet(new Comparator() {
            public int compare(Object a, Object b) {
                String aux1 = null;
                String aux2 = null;
                try {
                    aux1 = (String) ((Map) a).get(property);

                    aux2 = (String) ((Map) b).get(property);

                } catch (Exception e) {
                }
                return (aux1.compareToIgnoreCase(aux2));
            }
        });
        while (it.hasNext()) {
            Object aux = (Object) it.next();
            byName.add(aux);
        }
        return new ArrayList(byName);
    }

    /**
     * This method sorts List of maps by <b>orderKey</b> parameter,
     * the object that contains the <b>orderKey</b> can be java.lang.Integer Object
     *
     * @param listOfMaps list that conains Map objects can be sorted
     * @param orderKey   key that the sort the maps, the value of this key can be java.lang.Integer Object
     * @return sorted List of maps
     */
    public static synchronized List orderListOfMapsByProperty(List listOfMaps, final String orderKey) {
        Comparator c = new Comparator() {
            public int compare(Object a, Object b) {
                int value = 0;
                Integer aux1, aux2;
                try {
                    aux1 = new Integer((String) ((Map) a).get(orderKey));
                    aux2 = new Integer((String) ((Map) b).get(orderKey));
                    value = (aux1.intValue() > aux2.intValue()) ? 1 : (aux1.intValue() == aux2.intValue()) ? 0 : -1;
                } catch (Exception e) {
                }
                return value;
            }
        };
        Collections.sort(listOfMaps, c);

        return listOfMaps;
    }

    /**
     * Order an ArrayList by a property, this can to contain duplicate property.
     *
     * @param listOfMaps Collection with items to sort
     * @param orderKey   the property to sort
     * @return sorted by property collection.
     */
    public static synchronized List orderListWithDuplicate(List listOfMaps, final String orderKey) {
//        log.debug("orderListOfMapsByProperty(java.util.List, '" + orderKey + "')");

        Comparator c = new Comparator() {
            public int compare(Object a, Object b) {
                String aux1 = null;
                String aux2 = null;
                try {
                    aux1 = (String) a.getClass().getMethod("get" + StringUtils.capitalize(orderKey),
                            new Class[]{}).invoke(a, new Class[]{});

                    aux2 = (String) b.getClass().getMethod("get" + StringUtils.capitalize(orderKey),
                            new Class[]{}).invoke(b, new Class[]{});

                } catch (Exception e) {
//                    log.error("Unexpected error trying to sort", e);
                }
                return (aux1.compareToIgnoreCase(aux2));
            }
        };
        Collections.sort(listOfMaps, c);

        return listOfMaps;
    }

    /**
     * The previous method has some fails
     * Order an ArrayList by a property, this can to contain duplicate property.
     *
     * @param listOfMaps Collection with items to sort
     * @param orderKey   the property to sort
     * @return sorted by property collection.
     */
    public static synchronized List orderListWithDuplicate2(List listOfMaps, final String orderKey) {
//        log.debug("orderListOfMapsByProperty(java.util.List, '" + orderKey + "')");

        Comparator c = new Comparator() {
            public int compare(Object a, Object b) {
                String aux1 = null;
                String aux2 = null;
                try {
                    aux1 = (String) a.getClass().getMethod("get" + StringUtils.capitalize(orderKey),
                            new Class[]{}).invoke(a, new Class[]{});

                    aux2 = (String) b.getClass().getMethod("get" + StringUtils.capitalize(orderKey),
                            new Class[]{}).invoke(b, new Class[]{});

                } catch (Exception e) {
//                    log.error("Unexpected error trying to sort", e);
                }
                return (aux1.compareToIgnoreCase(aux2));
            }
        };
        Object[] array = listOfMaps.toArray();
        Arrays.sort(array, c);

        return Arrays.asList(array);
    }
}
