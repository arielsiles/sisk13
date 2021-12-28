package com.encens.khipus.action.billing;

import com.encens.khipus.model.customers.CustomerOrder;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.Properties;

/**
 * Created by AS on 23/12/2021.
 */

@Name("sendMessageAction")
@Scope(ScopeType.PAGE)
public class SendMessageAction {

    private static final String correo = "ariel.siles@gmail.com";
    private static final String contra = "nhefhdnzwjtymynk";


    public void sendEmail(CustomerOrder customerOrder) throws MessagingException {

        String correoDestino = customerOrder.getClient().getEmail();

        Properties p = new Properties();
        p.put("mail.smtp.host", "smtp.gmail.com");
        p.setProperty("mail.smtp.starttls.enable", "true");
        p.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        p.setProperty("mail.smtp.port", "587");
        p.setProperty("mail.smtp.user", correo);
        p.setProperty("mail.smtp.auth", "true");
        Session s = Session.getDefaultInstance(p);

        MimeMessage mensaje = new MimeMessage(s);
        mensaje.setFrom(new InternetAddress(correo));
        mensaje.addRecipient(Message.RecipientType.TO, new InternetAddress(correoDestino));
        mensaje.setSubject("Factura Electronica en Linea, XML");
        mensaje.setText(customerOrder.getMovement().getFactura());

        Transport t = s.getTransport("smtp");
        t.connect(correo, contra);
        t.sendMessage(mensaje, mensaje.getAllRecipients());
        t.close();
        System.out.println("................Mensaje Enviado...............");
    }

    public void sendEmailAttachment(CustomerOrder customerOrder) throws MessagingException {

        String correoDestino = customerOrder.getClient().getEmail();

        Properties p = new Properties();
        p.put("mail.smtp.host", "smtp.gmail.com");
        p.setProperty("mail.smtp.starttls.enable", "true");
        p.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        p.setProperty("mail.smtp.port", "587");
        p.setProperty("mail.smtp.user", correo);
        p.setProperty("mail.smtp.auth", "true");
        Session s = Session.getDefaultInstance(p);

        BodyPart texto = new MimeBodyPart();
        texto.setText("Archivo adjuntos:");

        BodyPart adjuntoXml = new MimeBodyPart();
        adjuntoXml.setDataHandler(new DataHandler(new FileDataSource("C:/TEMP/FACTURA.xml")));
        adjuntoXml.setFileName("Factura.xml");

        BodyPart adjuntoPdf = new MimeBodyPart();
        adjuntoPdf.setDataHandler(new DataHandler(new FileDataSource("C:/TEMP/FACTURA.pdf")));
        adjuntoPdf.setFileName("Factura.pdf");



        MimeMultipart m = new MimeMultipart();
        m.addBodyPart(texto);
        m.addBodyPart(adjuntoXml);
        m.addBodyPart(adjuntoPdf);

        MimeMessage mensaje = new MimeMessage(s);
        mensaje.setFrom(new InternetAddress(correo));
        mensaje.addRecipient(Message.RecipientType.TO, new InternetAddress(correoDestino));
        mensaje.setSubject("Factura Electronica en Linea, CISC LTDA");
        mensaje.setContent(m);

        Transport t = s.getTransport("smtp");
        t.connect(correo, contra);
        t.sendMessage(mensaje, mensaje.getAllRecipients());
        t.close();
        System.out.println("................Mensaje Enviado...............");
    }


}
