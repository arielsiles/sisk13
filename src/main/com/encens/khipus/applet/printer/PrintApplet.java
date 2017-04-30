package com.encens.khipus.applet.printer;

import javax.swing.*;
import java.awt.*;
import java.io.*;

public class PrintApplet extends JApplet {

    ParserHtmlTxt parserHtmlTxt;
    String textForHtml;
    OrderlyTable orderlyTable;


    public void init() {

        String url = getParameter("url");
        //id = Integer.parseInt(getParameter("id"));
        System.out.println("hola " + saberSO());


        parserHtmlTxt = new ParserHtmlTxt();
        textForHtml = parserHtmlTxt.getHtmlTxt(url);

        orderlyTable = new OrderlyTable();
        orderlyTable.setFullText(textForHtml);
        crearArchivo(orderlyTable.getOrderlyTable(), 12345);
        impComando(12345);
        //Prueba prueba = new Prueba();;
        //prueba.getOrderlyTable(textForHtml);
        //impComando(12345);
    }

    public void paint(Graphics g) {
        g.drawString("hola", 25, 40);
    }

    public String saberSO() {
        //System.out.println("Nombre del PC: " + nombrePC());
        //System.out.println("Nombre usuario: " + usuario());
        //System.out.println("Procesador: " + procesador());
        System.out.println("Sistema operativo: ->" + getOsName());
        //System.out.println("Version JDK:-" + JDK());
        System.out.println("-->" + getTemplateFolder());
        //System.out.println("Directorio actual: " + dir());
        String res;
        String so = getOsName();

        if (so.compareTo("Windows XP") == 0) {
            res = "winxp";
        } else {
            if ("windows".regionMatches(true, 0, so, 0, 7)) {
                res = "wino";
            } else {
                if (so.compareToIgnoreCase("mac os x") == 0) {
                    res = "macosx";
                } else {
                    res = "linux";
                }
            }
        }
        return res;
    }


    public static String getOsName() {
        return System.getProperty("os.name");
    }

    public static String getJDKVersion() {
        return System.getProperty("java.version");
    }

    public static String getTemplateFolder() {
        return System.getProperty("java.io.tmpdir");
    }

    public String fullDirTmp() {
        String res;
        String os = saberSO();
        if (os.compareTo("winxp") == 0 || os.compareTo("wino") == 0) {
            res = getTemplateFolder();
        } else {
            res = getTemplateFolder() + "/";
        }
        return res;
    }

    public void impComando(int number) {

        //FileInputStream fis = new FileInputStream(�example.ps�);
        final Process process;
        try {
            String command;
            String os = saberSO();
            //System.out.println("entro al impComando \n");
            if (os.compareTo("winxp") == 0) {
                //System.out.println("entro al if de impComando \n");
                command = "type " + fullDirTmp() + number + ".txt" + " > PRN";
                //command = "dir";
                process = Runtime.getRuntime().exec("cmd /c" + command);
                //System.out.println("----"+command );
            } else {
                //System.out.println("entro al else de impComando \n");
                command = "lp " + fullDirTmp() + number + ".txt";
                process = Runtime.getRuntime().exec(command);
            }

            new Thread() {
                public void run() {
                    try {
                        InputStream is = process.getInputStream();
                        byte[] buffer = new byte[1024];
                        for (int count = 0; (count = is.read(buffer)) >= 0; ) {
                            System.out.write(buffer, 0, count);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.start();

            int returnCode = process.waitFor();
            System.out.println("Return code = " + returnCode);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void crearArchivo(String text, int num) { //(BufferedReader br, int num) {
        String nameTXT = fullDirTmp() + num + ".txt";
        //String s;
        //BufferedReader bReader = br;
        String fullText = text;
        try {
            FileWriter fw = new FileWriter(nameTXT);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter pw = new PrintWriter(bw);

            //s = bReader.readLine();
            if (fullText != null) {
                int intEscChar = 27;
                String escChar = new Character((char) intEscChar).toString();
                int intAdmChar = 33;
                String admChar = new Character((char) intAdmChar).toString();
                int intSiChar = 4;
                String siChar = new Character((char) intSiChar).toString();
                pw.print(escChar + admChar + siChar);
                pw.println(fullText);
                //System.out.println("hoooo "+ bReader.readLine());
            }

            pw.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
/*
	public String leerArchivo(){
		File  f = new File(fullDirTmp()+"123.txt");
		StringBuffer sBuffer = new StringBuffer();
		String line;
		FileReader fReader;
		BufferedReader bReader;
		
		try{
			fReader = new FileReader(f);
			bReader = new BufferedReader(fReader);
			
			while((line = bReader.readLine()) != null){
				sBuffer.append(line);
			}
			bReader.close();
			fReader.close();
		}catch(FileNotFoundException e){
			e.printStackTrace();
		}catch(IOException e){
			e.printStackTrace();	
		}
		System.out.println(sBuffer.toString());
		return sBuffer.toString();
		*/
    /*
         try{
         if(!f.exists()&& f.length()<0)
               System.out.println("The specified file is not exist");

               else{
                  FileInputStream finp=new FileInputStream(f);
               byte b;
             do{

               b=(byte)finp.read();
               System.out.print((char)b);
             }
               while(b!=-1);
                 finp.close();
                 }
         }catch(IOException ioe){
             ioe.printStackTrace();
         } */
    /*
         try {
             String texto = "";
             FileReader fr = new FileReader("nombreArchivo.txt");
             BufferedReader entrada = new BufferedReader(fr);
             String s;
             while ((s = entrada.readLine()) != null)
                 texto += s + "  \n";
         } catch (java.io.FileNotFoundException fnfex) {
             System.out.println("se presento el error: " + fnfex.toString());
         }*/
    //}
/*
	public void fileGetWeb() {
		String s;
		try {
			//miUrl = new URL("file:///C:/imprimir/docImprimir.txt");   win
			miUrl = new URL("file:///Users/macmac/prueba.html");

		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		try {
			//comoDocumento = (Document)miUrl.getFile();
			//System.out.println(comoDocumento.getText(0, 20));
			entrada = miUrl.openStream();			
			//entradaDatos = new DataInputStream(entrada);
			entradaDatos = new BufferedReader(new InputStreamReader(entrada));
			
			crearArchivo(entradaDatos, 123);
			System.out.println("llego acaaaaaaaaaaaaaaaaaaaa");
			impComando(123);
			do{
				s= entradaDatos.readLine();
				if(s!= null)
					System.out.println(s);
			}while(s != null);
			//crearArchivo(entradaDatos, 123654);
			
		} catch(UnknownHostException uhe){
			System.out.println("host no responde");
		}catch (Exception e) {
			e.printStackTrace();
		}
	} */
    /*
     public void fileHtmlTxt(String textHtml){

         try{
         JEditorPane editorPane = new JEditorPane("text/html", textHtml);
         System.out.println( " "+ editorPane.getDocument().getText( 0, editorPane.getDocument().getLength() ) );
         } catch(Exception e ){
             e.printStackTrace();
         }
     }*/
}
