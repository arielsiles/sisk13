package com.encens.khipus.util;

import javax.net.ssl.SSLContext;
import java.util.Arrays;

public class TLSTest {
    public static void main(String[] args) throws Exception {
        SSLContext context = SSLContext.getDefault();
        System.out.println("Protocolos soportados: " + Arrays.toString(context.getSupportedSSLParameters().getProtocols()));
        System.out.println("Protocolos habilitados: " + Arrays.toString(context.getDefaultSSLParameters().getProtocols()));
    }
}
