package com.encens.khipus.action.billing;

import com.encens.khipus.model.customers.CustomerOrder;
import com.encens.khipus.util.Constants;
import com.encens.khipus.util.DateUtils;
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
import java.text.MessageFormat;
import java.util.Properties;

/**
 * Created by AS on 23/12/2021.
 */

@Name("sendMessageAction")
@Scope(ScopeType.PAGE)
public class SendMessageAction {

    private static final String SMTP_HOST = Constants.SMTP_HOST;
    private static final String SMTP_PORT = Constants.SMTP_PORT;
    private static final String EMAIL_FROM = Constants.EMAIL_FROM;
    private static final String EMAIL_PASSWORD = Constants.EMAIL_PASSW;

    @In
    private FacesMessages facesMessages;

    public void sendEmail(CustomerOrder customerOrder, String subject, String text) {

        String correoDestino = customerOrder.getClient().getEmail();

        if (correoDestino != null) {
            try {
                Properties p = new Properties();
                p.put("mail.smtp.host", SMTP_HOST);
                p.put("mail.smtp.port", SMTP_PORT);
                p.put("mail.smtp.auth", "true");
                p.put("mail.smtp.starttls.enable", "true");
                p.put("mail.smtp.ssl.trust", "*");
                p.put("mail.smtp.ssl.protocols", "TLSv1.2");

                Session s = Session.getDefaultInstance(p);

                MimeMessage mensaje = new MimeMessage(s);
                mensaje.setFrom(new InternetAddress(EMAIL_FROM));
                mensaje.addRecipient(Message.RecipientType.TO, new InternetAddress(correoDestino));
                mensaje.setSubject(subject);
                mensaje.setText(text);

                Transport t = s.getTransport("smtp");
                t.connect(EMAIL_FROM, EMAIL_PASSWORD);
                t.sendMessage(mensaje, mensaje.getAllRecipients());
                t.close();
                System.out.println("................Mensaje Enviado...............");
            } catch (AuthenticationFailedException e) {
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "Error de autenticación: " + e.getMessage());
                e.printStackTrace();
            } catch (Exception e) {
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "No se puede enviar, debe generar los archivos PDF/XML.");
                e.printStackTrace();
            }
        }
    }

    public boolean sendEmailAttachment(CustomerOrder customerOrder) {

        boolean result = false;
        try {
            String correoDestino = customerOrder.getClient().getEmail();

            if (correoDestino != null) {

                String fileNameXml = Constants.PREFIX_NAME_INVOICE + customerOrder.getMovement().getNumber() + ".xml";
                String fileNamePdf = Constants.PREFIX_NAME_INVOICE + customerOrder.getMovement().getNumber() + ".pdf";

                String pathFileNameXml = Constants.PATH_FILE_INVOICE + fileNameXml;
                String pathFileNamePdf = Constants.PATH_FILE_INVOICE + fileNamePdf;

                Properties emailProperties = new Properties();
                emailProperties.put("mail.smtp.host", SMTP_HOST);
                emailProperties.put("mail.smtp.port", SMTP_PORT);
                emailProperties.put("mail.smtp.auth", "true");
                emailProperties.put("mail.smtp.starttls.enable", "true");
                emailProperties.put("mail.smtp.ssl.trust", "*");
                emailProperties.put("mail.smtp.ssl.protocols", "TLSv1.2");

                //p.setProperty("mail.smtp.user", correo);

                // Crear sesión de correo con autenticación
                Session emailSession = Session.getInstance(emailProperties, new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(EMAIL_FROM, EMAIL_PASSWORD);
                    }
                });

                BodyPart texto = new MimeBodyPart();

                String name  = customerOrder.getMovement().getName();
                String fecha = DateUtils.format(customerOrder.getCurrentDate(), "dd/MM/yyyy");
                String invoiceNumber = customerOrder.getMovement().getNumber().toString();

                String textMsg = MessageFormat.format( Constants.EMAIL_TEXT_1 , name, fecha, invoiceNumber);
                texto.setText(textMsg);

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

                MimeMessage mensaje = new MimeMessage(emailSession);
                mensaje.setFrom(new InternetAddress(EMAIL_FROM));
                mensaje.addRecipient(Message.RecipientType.TO, new InternetAddress(correoDestino));
                mensaje.setSubject(Constants.EMAIL_SUBJECT);
                mensaje.setContent(m);

                Transport t = emailSession.getTransport("smtp");
                t.connect(SMTP_HOST, EMAIL_FROM, EMAIL_PASSWORD);
                t.sendMessage(mensaje, mensaje.getAllRecipients());
                t.close();
                System.out.println("................Mensaje Enviado...............");
                result = true;
            } else {
                facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN, "No se puede enviar, correo no registrado.");
            }
        }catch (Exception e){
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "No se puede enviar, debe generar los archivos PDF/XML.");
            e.printStackTrace();
        }
        return result;
    }

    public void sendEmailAttachment_0(CustomerOrder customerOrder) {
        try {
            // Habilitar manualmente TLSv1.2
            System.setProperty("mail.smtp.ssl.protocols", "TLSv1.2");

            // Configuración de propiedades del correo
            Properties emailProperties = new Properties();
            emailProperties.put("mail.smtp.host", "smtp.gmail.com");
            emailProperties.put("mail.smtp.port", "587");
            emailProperties.put("mail.smtp.auth", "true");
            emailProperties.put("mail.smtp.starttls.enable", "true");


            // Habilitar depuración
            emailProperties.put("mail.debug", "true");

            // Crear sesión de correo
            Session emailSession = Session.getInstance(emailProperties, new javax.mail.Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication("ariel.siles@gmail.com", "");
                }
            });

            // Crear el mensaje de correo
            MimeMessage emailMessage = new MimeMessage(emailSession);
            emailMessage.setFrom(new InternetAddress("ariel.siles@gmail.com"));
            emailMessage.setRecipients(Message.RecipientType.TO, InternetAddress.parse("ariel.siles+ebilling@gmail.com"));
            emailMessage.setSubject("Asunto del correo con adjunto");

            // Crear el cuerpo del mensaje
            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText("Este es un mensaje de prueba con un adjunto.");

            // Crear el archivo adjunto
            BodyPart attachmentBodyPart = new MimeBodyPart();
            String filename = "C:/TEMP/FACTURA-1.pdf";
            FileDataSource source = new FileDataSource(filename);
            attachmentBodyPart.setDataHandler(new DataHandler(source));
            attachmentBodyPart.setFileName(source.getName());

            // Combinar partes en un multipart
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);
            multipart.addBodyPart(attachmentBodyPart);

            // Establecer el contenido del mensaje
            emailMessage.setContent(multipart);

            // Enviar el mensaje
            Transport.send(emailMessage);

            System.out.println("Correo con adjunto enviado exitosamente.");

        } catch (MessagingException e) {
            System.err.println("Error al enviar el correo: " + e.getMessage());
            e.printStackTrace();
        }
    }


}
