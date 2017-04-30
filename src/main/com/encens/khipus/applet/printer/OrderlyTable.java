package com.encens.khipus.applet.printer;

public class OrderlyTable {
    private String fullText;
    private int widthCharacter;


    public OrderlyTable() {
        widthCharacter = 35;
    }

    public String getOrderlyTable() {
        String res = "";

        int iniCab = 0;
        int finCab = 0;
        int iniFila = 0;
        int finFila = 0;

        String textCentralPart = "";
        String textFirstPart = "";
        String textSecondPart = "";

        int col1 = 9;
        int col2 = 9;
        int col3 = 9;
        int col4 = 9;
        int cont1 = 0;
        int cont2 = 0;
        int col = 0;

        boolean flag = true;
        boolean flag2 = true;
        boolean flagIntro = false;

        while (fullText.indexOf((int) '$') != -1) {
            // System.out.println( "1 ini " + iniCab +" fin "+finCab+" "+i);
            iniCab = fullText.indexOf((int) '$', finCab);
            finCab = fullText.indexOf((int) '$', iniCab + 1);
            if (finCab != -1) {
                textCentralPart = fullText.substring(iniCab + 1, finCab);
                System.out.println("texto --> "
                        + fullText.substring(iniCab + 1, finCab));
                cont1 = cont1 + 1;
                if (cont1 == 1) {
                    col = col1;
                }
                if (cont1 == 2) {
                    col = col2;
                }
                if (cont1 == 3) {
                    col = col3;
                }
                if (cont1 == 4) {
                    col = col4;
                }

                if (col - textCentralPart.length() != 0) {
                    textFirstPart = fullText.substring(0, iniCab);

                    for (int j = 0; j < (col - textCentralPart.length()); j++) {
                        textFirstPart = textFirstPart.concat(" ");
                    }

                } else {
                    textFirstPart = fullText.substring(0, iniCab).concat(" ");
                }
                textSecondPart = fullText.substring(finCab);
                fullText = (textFirstPart.concat(textCentralPart))
                        .concat(textSecondPart);
            } else {
                if (finCab == -1 && flag) {
                    textFirstPart = fullText.substring(0,
                            iniCab - textCentralPart.length()).concat(" ");
                    textSecondPart = fullText.substring(iniCab + 1);
                    fullText = (textFirstPart.concat(textCentralPart))
                            .concat(textSecondPart);
                    flag = false;
                }
            }
        } //fin for

        //para las filas con  |

        while (fullText.indexOf((int) '|') != -1) {
            iniFila = fullText.indexOf((int) '|', finFila);

            finFila = fullText.indexOf((int) '|', iniFila + 1);

            if (finFila != -1) {
                textCentralPart = fullText.substring(iniFila + 1, finFila);
                System.out.println("texto --> " + "  " + textCentralPart + " iniF: " + iniFila + " finF: " + finFila);

                cont2 = cont2 + 1;
                if (cont2 == 1) {
                    col = col1;
                }
                if (cont2 == 2) {
                    col = col2;
                }
                if (cont2 == 3) {
                    col = col3;
                }
                if (cont2 == 4) {
                    col = col4;
                }

                if (textCentralPart.length() > col) {
                    textCentralPart = textCentralPart.substring(0, col);
                    // finFila = iniFila+col+1;
                    // System.out.println("23232323232323 "+
                    // textCentralPart + " 4545 " +finFila);
                    flagIntro = true;
                }

                // System.out.println(
                // "222222222222222222222222222222222222222222222
                // "+textCentralPart.hashCode() );
                // if(textCentralPart.hashCode() != 10){
                if (col > textCentralPart.length()) {
                    textFirstPart = fullText.substring(0, iniFila);
                    for (int j = 0; j < (col - textCentralPart.length()); j++) {
                        textFirstPart = textFirstPart.concat(" ");
                    }
                    textSecondPart = fullText.substring(finFila);
                    fullText = (textFirstPart.concat(textCentralPart))
                            .concat(textSecondPart);
                    //System.out.println("prueba111  " + textFirstPart
                    //		+ " prueba333 \n"+ textSecondPart +"prueba777 \n");
                } else {
                    /*
                              * if(col-textCentralPart.length()<0){
                              * textFirstPart = fullText.substring(0,
                              * iniFila).concat(" "); textFirstPart =
                              * fullText.substring(0,
                              * iniFila).concat(textCentralPart.substring(0,col));
                              * textSecondPart = fullText.substring(finFila);
                              * fullText = textFirstPart.concat(textSecondPart);
                              * }else{
                              */
                    textFirstPart = fullText.substring(0, iniFila).concat(" ");
                    textSecondPart = fullText.substring(finFila);
                    fullText = (textFirstPart.concat(textCentralPart))
                            .concat(textSecondPart);
                    //System.out.println("prueba222  " + textFirstPart
                    //		+ "prueba444 \n"+ textSecondPart +"prueba666");
                    // }

                }
                System.out.println(textFirstPart + "prueba555\n" + textSecondPart + "prueba777\n");
                System.out.println("var Central: " + textCentralPart);
                /*
                         * textSecondPart = fullText.substring(finFila);
                         * fullText =
                         * (textFirstPart.concat(textCentralPart)).concat(textSecondPart);
                         */
                // }
            } else {
                /*esta parte hay que modificar */
                if (finFila == -1 && flag2) {
                    textFirstPart = fullText.substring(0,
                            iniFila - textCentralPart.length()).concat(
                            " ");
                    textSecondPart = fullText.substring(iniFila + 1);
                    fullText = (textFirstPart.concat(textCentralPart))
                            .concat(textSecondPart);
                    flag2 = false;
                }
            }
            if (flagIntro) {
                finFila = iniFila + 1;
                flagIntro = false;
            }
            //}
            if (cont2 == 4) {
                cont2 = 0;
            }
        }// fin for2
        System.out.println(fullText);
        return res = fullText;
    }

    public void setFullText(String text) {
        fullText = text;
    }
}
