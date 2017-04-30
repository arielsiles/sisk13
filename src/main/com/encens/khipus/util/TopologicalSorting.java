package com.encens.khipus.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


/*
 * @author : Vishwakarma Singh
 * @url : http://discuss-prog.blogspot.com/2012/07/topological-sorting-of-directed-acyclic.html
 *
 * @modifiedBy : David Velasco
 */

public class TopologicalSorting {

    /*
     *DAG stored as an Adjacency List
     */
    private HashMap<String, LinkedList<String>> inputDAG = new HashMap<String, LinkedList<String>>();
    /*
     * Count of in-coming edges for a node
     */
    private HashMap<String, Integer> fanInCount = new HashMap<String, Integer>();

    /*
     * Class entry point
     */
    public static void main(String[] args) {
        try {
            String inputFile = "mio.txt";

            TopologicalSorting inst = new TopologicalSorting();
            LinkedList<String> rootNodes = inst.createDAG(new FileReader(inputFile));
            inst.sortDAG(rootNodes);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public List<String> findDAG(Reader reader) throws IOException {
        LinkedList<String> list = createDAG(reader);
        if (list == null) {
            return null;
        }

        return sortDAG(list);
    }

    /*
     * This function reads the DAG structure from the input file into an Adjacency List (Parent: Children list).
     * A vertex u is called a parent of a vertex v if there is a directed edge from u to v.
     * It also determines the root nodes by counting the number of in-coming edges.
     */
    public LinkedList<String> createDAG(Reader reader) throws IOException {
        LinkedList<String> rootNodes = null;


        BufferedReader fR = new BufferedReader(reader);
        String line;
        while ((line = fR.readLine()) != null) {
            String[] toks = line.split(" ");
            String parentNode = toks[0];
            LinkedList<String> childrenList;

            if (inputDAG.containsKey(parentNode)) {
                childrenList = inputDAG.get(parentNode);
            } else {
                childrenList = new LinkedList<String>();
                inputDAG.put(parentNode, childrenList);
            }


            for (int i = 1; i < toks.length; i++) {
                childrenList.add(toks[i]);

                if (fanInCount.containsKey(toks[i])) {
                    fanInCount.put(toks[i], new Integer((fanInCount.get(toks[i])).intValue() + 1));
                } else {
                    fanInCount.put(toks[i], new Integer(1));
                }
            }
        }

        /*
         * We know that none of the root nodes appear in the children list of the
         * nodes, and therefore are not present in fanInCount HashMap
         */
        rootNodes = new LinkedList<String>();
        Iterator<String> keyItr = inputDAG.keySet().iterator();
        while (keyItr.hasNext()) {
            String key = keyItr.next();
            if (!fanInCount.containsKey(key)) {
                rootNodes.add(key);
            }
        }

        if (rootNodes.size() == 0) {
            return null;
        }

        return rootNodes;
    }

    /*
     * This function performs a topological sorting of the DAG.
     * It also detects if the DAG has a cycle.
     */
    public LinkedList<String> sortDAG(LinkedList<String> rootNodes) {
        LinkedList<String> sortedList = new LinkedList<String>();
        while (!rootNodes.isEmpty()) {
            String root = rootNodes.remove();
            sortedList.add(root);
            LinkedList<String> childrenList = inputDAG.get(root);
            if (childrenList != null) {
                Iterator<String> childItr = childrenList.iterator();
                while (childItr.hasNext()) {
                    String child = childItr.next();
                    int newFanIn = fanInCount.get(child).intValue() - 1;
                    if (newFanIn == 0) {
                        rootNodes.add(child);
                        fanInCount.remove(child);
                    } else {
                        fanInCount.put(child, new Integer(newFanIn));
                    }
                }
            }

        }

        if (fanInCount.size() > 0)
            return null;
        else
            return sortedList;

        /*if (fanInCount.size() > 0)
            System.out.println("DAG has a Cycles");
        else {
            System.out.print("Sorted DAG: ");
            while (sortedList.size() > 0)
                System.out.print(sortedList.remove() + " ");
        }

        return sortedList;*/

    }
}