package TunaMayo;

import java.util.ArrayList;
import java.util.logging.*; 
import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class TunaMayowithEmail{
    public static void sendMailNoti(String recepient, ArrayList<String> title, int i_date) throws Exception{
        //String recepient = "yanahwatae@gmail.com"; //write the user email
        System.out.println("Preparing to send email...");
      
        Properties props = new Properties();
        props.put("mail.smtp.socketFactory.fallback", "false");  
        props.put("mail.smtp.quitwait", "false");
        props.put("mail.smtp.socketFactory.port", "587"); //TLS Port
        props.put("mail.host", "smtp.gmail.com"); //SMTP Host
        props.put("mail.smtp.auth", "true"); //enable authentication

        props.setProperty("mail.transport.protocol", "smtp");
        props.setProperty("mail.smtp.port", "587");
        props.setProperty("mail.smtp.ssl.trust", "*");
        props.setProperty("mail.smtp.starttls.enable", String.valueOf(true));//enable STARTTLS
        props.setProperty("mail.smtp.ssl.protocols", "TLSv1.2");
        props.setProperty("mail.smtp.timeout", "300000");
        props.setProperty("mail.smtp.connectiontimeout", "300000");
        props.setProperty("mail.smtp.writetimeout", "300000");

        String myEmailAcc = "8user.1name@gmail.com";
        String password = "fjctxhzfknutwdlw"; 

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(myEmailAcc, password);
            }
        });
        Message message = prepareEmailMessage(session,myEmailAcc, recepient, title, i_date);

        Transport.send(message);
        System.out.println("Mesagge sent succesfully!");
    }
    
    private static Message prepareEmailMessage(Session session, String myEmailAcc, String recepient, ArrayList<String> title, int i_date){
        try{
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(myEmailAcc));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recepient));
        message.setSubject("[REMINDER] Task \"" + title.get(i_date) + "\" is due in 24h!!!");
        message.setText("Hi, don't forget to complete task \""+ title.get(i_date) + "\" before its due tomorrow!"); 
        return message;
        } catch (MessagingException ex) {
            Logger.getLogger(TunaMayowithEmail.class.getName()).log(Level.SEVERE, null, ex);
        }
    return null;
    }
    
}