package com.encens.khipus.applet.printer;

import org.htmlparser.parserapplications.StringExtractor;
import org.htmlparser.util.ParserException;


public class ParserHtmlTxt {

    public ParserHtmlTxt() {

    }

    public String getHtmlTxt(String urlPage) {
        String res = "";
        try {
            StringExtractor se = new StringExtractor(urlPage);
            String contents = se.extractStrings(true);
            System.out.println("htmlParser \n \n \n \n" + contents);
            res = contents;

        } catch (ParserException e) {
            e.printStackTrace();
        }
        return res;
    }
    /*
     public static void main(String[] args) {
         try{
         String URL = "http://ftp.cl.debian.org/debian/README.mirrors.html";
         StringExtractor se = new StringExtractor (URL);
         String contents = se.extractStrings(true);
         System.out.println(contents);

         }catch(ParserException e){
             e.printStackTrace();
         }
     }
     */
}
