package com.encens.khipus.util;

import com.csvreader.CsvReader;

import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Ariel Siles Encinas
 * Date: 10-08-2009
 * Time: 09:56:55 AM
 * To change this template use File | Settings | File Templates.
 */
public class CSVselect {


    public static void main(String args[]) {


        CsvReader cvsReader = null;
        Connection conexion = null;
        Statement st = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
//            conexion = DriverManager.getConnection("jdbc:mysql://10.0.0.13:3306/khipus_test", "ariel", "ariel_siles");
//            conexion = DriverManager.getConnection("jdbc:mysql://localhost:3307/khipus", "khipus", "khipus.pwd");
//            conexion = DriverManager.getConnection("jdbc:mysql://10.0.0.11:3307/khipus", "khipus", "khipus.pwd");
            conexion = DriverManager.getConnection("jdbc:mysql://localhost:3307/khipus_test_lapaz", "root", "root");

            st = conexion.createStatement();

        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        try {
            File fichero = new File("C:/rhmarcado.csv");
            FileReader freader = new FileReader(fichero);

            cvsReader = new CsvReader(freader, ';');
            String[] headers = null;
            List<String> headersList = new ArrayList<String>();
            List<Integer> idList = new ArrayList<Integer>();

            //String query = "INSERT INTO "+fichero.getName().substring(0,fichero.getName().indexOf("."))+" (";
            String head = "";
            String execute = "";
            // Headers
            if (cvsReader.readHeaders()) {
                headers = cvsReader.getHeaders();
                Collections.addAll(headersList, headers);
                for (int i = 0; i < headers.length; i++) {
                    if (i < headers.length - 1) {
                        head = head + headers[i] + ",";
                    } else {
                        head = head + headers[i];
                    }
                }
                //query = head + ") VALUES (";
            }
            String value = "";
            while (cvsReader.readRecord()) {
                List<String> dataList = new ArrayList<String>();
                for (int i = 0; i < headers.length; i++) {
                    String data = cvsReader.get(headers[i]);
                    dataList.add(data);
                    if (!data.equals("NULL")) {
                        data = "'" + data + "'";
                    }

                    if (i < headers.length - 1) {
                        value = value + data + ",";
                    } else {
                        value = value + data;
                    }
                }

                //ONLY FOR SELECT
                execute = "SELECT CI FROM " + fichero.getName().toUpperCase().substring(0, fichero.getName().indexOf(".")) + " WHERE ";
                for (int i = 0; i < headersList.size(); i++) {
                    // only for select
                    execute = execute.concat(headersList.get(i) + "='" + dataList.get(i) + "'");
                    // only for select
                    if (i < headersList.size() - 1) {
                        execute = execute.concat(" and ");
                    } else {
                        execute = execute.concat(";");
                    }
                }

                // FOR INSERT
//                execute = "INSERT INTO " + fichero.getName().toUpperCase().substring(0, fichero.getName().indexOf(".")) + " (" + head.toUpperCase() + ") VALUES (" + value + ")";


                System.out.println("Q: " + execute);
                // for select task
                ResultSet resulSet = st.executeQuery(execute);
                while (resulSet.next()) {
                    System.out.println(resulSet.getInt(1));
                    idList.add(resulSet.getInt(1));
                }

                // for insert
//                st.execute(execute);

                // for insert task
//                st.executeUpdate(execute);
//                execute = "";
//                value = "";
            }
            // FOR SELECT
            for (Integer anIdList : idList) {
                System.out.println(anIdList);
            }

            st.close();
            conexion.close();

        } catch (Exception e) {
            System.out.println(e);
        } finally {
            if (cvsReader != null) {
                cvsReader.close();
            }
        }
    }

}