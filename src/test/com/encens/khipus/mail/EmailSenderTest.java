package com.encens.khipus.mail;

import org.junit.Before;
import org.junit.Test;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.Properties;

import static org.junit.Assert.assertTrue;

public class EmailSenderTest {

    private Properties emailProperties;
    private Session emailSession;
    private MimeMessage emailMessage;

    @Before
    public void setUp() {
        emailProperties = new Properties();
        emailProperties.put("mail.smtp.host", "smtp.gmail.com");
        emailProperties.put("mail.smtp.port", "587");
        emailProperties.put("mail.smtp.auth", "true");
        emailProperties.put("mail.smtp.starttls.enable", "true");
        emailProperties.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        emailProperties.put("mail.smtp.ssl.protocols", "TLSv1.2");

        emailSession = Session.getDefaultInstance(emailProperties, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("ariel.siles@gmail.com", "wwlatgldmmoboiep");
            }
        });

        emailMessage = new MimeMessage(emailSession);
    }

    @Test
    public void testSendSimpleEmail() {
        try {
            emailMessage.setFrom(new InternetAddress("ariel.siles@gmail.com"));
            //emailMessage.setRecipient(Message.RecipientType.TO, new InternetAddress("arielrse@gmail.com"));
            emailMessage.setRecipients(Message.RecipientType.TO, InternetAddress.parse("ariel.siles+ebilling@gmail.com"));
            emailMessage.setSubject("Asunto del correo");
            emailMessage.setText("Este es un mensaje de prueba.");

            Transport transport = emailSession.getTransport("smtp");
            transport.connect();
            transport.sendMessage(emailMessage, emailMessage.getAllRecipients());
            transport.close();

            System.out.println("Correo enviado exitosamente.");
            assertTrue(true);
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue("Exception occurred: " + e.getMessage(), false);
        }
    }

    @Test
    public void testSendEmailWithAttachment() {
        try {
            emailMessage.setFrom(new InternetAddress("ariel.siles@gmail.com"));
            emailMessage.setRecipients(Message.RecipientType.TO, InternetAddress.parse("ariel.siles+ebilling@gmail.com"));
            emailMessage.setSubject("Asunto del correo con adjunto");

            // Crear el cuerpo del mensaje
            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText("Este es un mensaje de prueba con un adjunto.");

            // Crear el archivo adjunto
            BodyPart attachmentBodyPart = new MimeBodyPart();
            String filename = "C:/TEMP/FACTURA-1.pdf"; // Cambia esto a la ruta de tu archivo
            FileDataSource source = new FileDataSource(filename);
            attachmentBodyPart.setDataHandler(new DataHandler(source));
            attachmentBodyPart.setFileName(filename);

            // Combinar partes en un multipart
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);
            multipart.addBodyPart(attachmentBodyPart);

            // Establecer el contenido del mensaje
            emailMessage.setContent(multipart);

            Transport transport = emailSession.getTransport("smtp");
            transport.connect();
            transport.sendMessage(emailMessage, emailMessage.getAllRecipients());
            transport.close();

            System.out.println("Correo con adjunto enviado exitosamente.");
            assertTrue(true);
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue("Exception occurred: " + e.getMessage(), false);
        }
    }



}




