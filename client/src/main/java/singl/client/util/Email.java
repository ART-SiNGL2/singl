package singl.client.util;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Email {

	private static String from = "artdevenv2022@gmail.com";
	private static String toEmail = "yugesh.devaraj@alpharithm.com";
	private static String host = "smtp.gmail.com";
	private static String port = "465";
	private static String user = "artdevenv2022@gmail.com";
	private static String password = "zjainykxfqrnnecw";

	// lets keep this only

	public static final Log LOG = LogFactory.getLog(Email.class);

	private static class SMTPAuthenticator extends Authenticator {
		public PasswordAuthentication getPasswordAuthentication() {
			return new PasswordAuthentication(user, password);
		}
	}

	public static void email(String to, EmailBody email) {
		if (to == null || to.trim().equals("")) {
			LOG.warn("No email recipient specified");
			return;
		}
		try {

			// Get system properties
			Properties properties = System.getProperties();

			// Setup mail server
			properties.put("mail.smtp.host", host);
			properties.put("mail.smtp.port", port);
			properties.put("mail.smtp.auth", "true");
			properties.put("mail.smtp.auth", "true");
			properties.put("mail.smtp.socketFactory.port", port);
			properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

			// Get the Session object.// and pass username and password
			Session session = Session.getInstance(properties, new SMTPAuthenticator());
			// session.setDebug(true);

			// Create a default MimeMessage object.
			MimeMessage message = new MimeMessage(session);
			MimeMultipart multipart = new MimeMultipart();

			// Set From: header field of the header.
			message.setFrom(new InternetAddress(from));

			// Set To: header field of the header.
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
			message.addRecipient(Message.RecipientType.BCC, new InternetAddress(from));

			// Set Subject: header field
			message.setSubject(email.getSubject());

			// Now set the actual message
			BodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setContent(email.getEmail(), "text/html");

			// add it
			multipart.addBodyPart(messageBodyPart);
			message.setContent(multipart);

			Transport.send(message);

			LOG.info("Email message sent.");
		} catch (MessagingException mex) {
			// mex.printStackTrace();
			LOG.warn("Unable to send email " + mex.getMessage());
		}
	}
}
