package com.encens.khipus.action.billing;

import com.encens.khipus.model.customers.CustomerOrder;
import com.encens.khipus.util.Constants;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;

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

    private static final String correo = Constants.EMAIL_FROM;
    private static final String contra = Constants.EMAIL_PASSW;

    @In
    private FacesMessages facesMessages;

    public void sendEmail(CustomerOrder customerOrder, String subject, String text) {

        String correoDestino = customerOrder.getClient().getEmail();

        if (correoDestino != null) {
            try {
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
                mensaje.setSubject(subject);
                mensaje.setText(text);

                Transport t = s.getTransport("smtp");
                t.connect(correo, contra);
                t.sendMessage(mensaje, mensaje.getAllRecipients());
                t.close();
                System.out.println("................Mensaje Enviado...............");
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendEmailAttachment(CustomerOrder customerOrder) {

        try {
            String correoDestino = customerOrder.getClient().getEmail();

            if (correoDestino != null) {

                String fileNameXml = Constants.PREFIX_NAME_INVOICE + customerOrder.getMovement().getNumber() + ".xml";
                String fileNamePdf = Constants.PREFIX_NAME_INVOICE + customerOrder.getMovement().getNumber() + ".pdf";

                String pathFileNameXml = Constants.PATH_FILE_INVOICE + fileNameXml;
                String pathFileNamePdf = Constants.PATH_FILE_INVOICE + fileNamePdf;

                Properties p = new Properties();
                p.put("mail.smtp.host", "smtp.gmail.com");
                p.setProperty("mail.smtp.starttls.enable", "true");
                p.put("mail.smtp.ssl.trust", "smtp.gmail.com");
                p.setProperty("mail.smtp.port", "587");
                p.setProperty("mail.smtp.user", correo);
                p.setProperty("mail.smtp.auth", "true");
                Session s = Session.getDefaultInstance(p);

                BodyPart texto = new MimeBodyPart();
                texto.setText(Constants.EMAIL_TEXT_1);

                BodyPart adjuntoXml = new MimeBodyPart();
                adjuntoXml.setDataHandler(new DataHandler(new FileDataSource(pathFileNameXml)));
                adjuntoXml.setFileName(fileNameXml);

                BodyPart adjuntoPdf = new MimeBodyPart();
                adjuntoPdf.setDataHandler(new DataHandler(new FileDataSource(pathFileNamePdf)));
                adjuntoPdf.setFileName(fileNamePdf);

                MimeMultipart m = new MimeMultipart();
                m.addBodyPart(texto);
                m.addBodyPart(adjuntoXml);
                m.addBodyPart(adjuntoPdf);

                MimeMessage mensaje = new MimeMessage(s);
                mensaje.setFrom(new InternetAddress(correo));
                mensaje.addRecipient(Message.RecipientType.TO, new InternetAddress(correoDestino));
                mensaje.setSubject(Constants.EMAIL_SUBJECT);
                mensaje.setContent(m);

                Transport t = s.getTransport("smtp");
                t.connect(correo, contra);
                t.sendMessage(mensaje, mensaje.getAllRecipients());
                t.close();
                System.out.println("................Mensaje Enviado...............");
            }
        }catch (Exception e){
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "No se puede enviar, debe generar los archivos PDF/XML.");
            e.printStackTrace();
        }
    }
}
