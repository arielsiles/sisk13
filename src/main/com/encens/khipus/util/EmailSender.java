package com.encens.khipus.util;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.Properties;

public class EmailSender {

    private static final String SMTP_HOST = "mail.ilvabolivia.com";
    private static final String SMTP_PORT = "587";
    private static final String EMAIL_FROM = "ariel.siles@ilvabolivia.com";
    private static final String EMAIL_PASSWORD = "Miracula.13";

    public void sendEmailWithAttachment(String toEmail, String subject, String bodyText, String attachmentPath) {
        try {
            // Habilitar manualmente TLSv1.2
            //System.setProperty("https.protocols", "TLSv1.2");
            //System.setProperty("mail.smtp.ssl.protocols", "TLSv1.2");
            //System.setProperty("mail.smtp.ssl.trust", "*");

            // Configuración de propiedades del correo
            Properties emailProperties = new Properties();
            emailProperties.put("mail.smtp.host", SMTP_HOST);
            emailProperties.put("mail.smtp.port", SMTP_PORT);
            emailProperties.put("mail.smtp.auth", "true");
            emailProperties.put("mail.smtp.starttls.enable", "true");
            emailProperties.put("mail.smtp.ssl.trust", "*");
            emailProperties.put("mail.smtp.ssl.protocols", "TLSv1.2"); // ***
            emailProperties.put("mail.debug", "true");

            // Crear sesión de correo con autenticación
            Session emailSession = Session.getInstance(emailProperties, new javax.mail.Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(EMAIL_FROM, EMAIL_PASSWORD);
                }
            });


            // Crear el mensaje de correo
            MimeMessage emailMessage = new MimeMessage(emailSession);
            emailMessage.setFrom(new InternetAddress(EMAIL_FROM));
            emailMessage.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            emailMessage.setSubject(subject);

            // Crear el cuerpo del mensaje
            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText(bodyText);

            // Crear el archivo adjunto
            BodyPart attachmentBodyPart = new MimeBodyPart();
            FileDataSource source = new FileDataSource(attachmentPath);
            attachmentBodyPart.setDataHandler(new DataHandler(source));
            attachmentBodyPart.setFileName(source.getName());

            // Combinar partes en un multipart
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);
            multipart.addBodyPart(attachmentBodyPart);

            // Establecer el contenido del mensaje
            emailMessage.setContent(multipart);

            // Enviar el mensaje
            Transport transport = emailSession.getTransport("smtp");
            transport.connect(SMTP_HOST, EMAIL_FROM, EMAIL_PASSWORD);
            transport.sendMessage(emailMessage, emailMessage.getAllRecipients());
            transport.close();

            System.out.println("Correo con adjunto enviado exitosamente.");

        } catch (MessagingException e) {
            System.err.println("Error al enviar el correo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String toEmail = "ariel.siles@gmail.com";
        String subject = "PRUEBA DE CORREO ADJUNTO - SFE";
        String bodyText = "Este es un mensaje de prueba con un adjunto.";
        String attachmentPath = "C:/TEMP/FACTURA-12.pdf";

        //sendEmailWithAttachment(toEmail, subject, bodyText, attachmentPath);

        EmailSender emailSender = new EmailSender();
        emailSender.sendEmailWithAttachment(toEmail, subject, bodyText, attachmentPath);

        System.out.println("....... correo enviado .......");
    }
}