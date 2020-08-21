package orderUploader;

import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeBodyPart;

import orderUploader.utils.ApplicationProperties;
import org.apache.log4j.Logger;

public class EmailReader {
	final static String mailServerUsername = "OLucchini"; //change accordingly
	final static String mailServerPassword = "Qelppa12"; //change accordingly
	final static String mailsServerHost = "mailserver.wedi.de";
	final static Logger log = Logger.getLogger(EmailReader.class);

	public EmailReader()
	{
	}
	
//	private static class MyAuthenticator extends Authenticator {
//
//		String user;
//		String pw;
//		public MyAuthenticator (String mailServerUsername, String mailServerPassword)
//		{
//			super();
//			this.user = mailServerUsername;
//			this.pw = mailServerPassword;
//		}
//		public PasswordAuthentication getPasswordAuthentication()
//		{
//			return new PasswordAuthentication(user, pw);
//		}
//	}

	public static void pop3()
	{
		try
		{
			Properties props = System.getProperties();
			props.put("mail.pop3.host", mailsServerHost);
			props.put("mail.pop3.port", 110);
			
		    props.put("mail.pop3.sasl.enable", "true");
//		    props.put("mail.pop3.ssl.enable", "false");
//		    props.put("mail.pop3.starttls.enable", "true");
//		    props.put("mail.pop3.starttls.required", "true");

//	        props.put( "mail.pop3.auth", "true" );      
//	        props.put("java.security.debug", "true");
	        
//		    props.put("mail.pop3.auth.ntlm.enable", "false");
//		    props.put("mail.pop3.auth.plain.enable", "false");
//		    props.put("mail.pop3.auth.gssapi.enable", "false");

//		    props.setProperty("mail.pop3.starttls.enable", "true"); 
//			Session session = Session.getInstance(props,
//					new javax.mail.Authenticator() {
//				protected PasswordAuthentication getPasswordAuthentication() {
//					return new PasswordAuthentication(mailServerUsername, mailServerPassword);
//				}
//			});
			
//			props.put("mail.pop3.apop.enable", "true");  //If set to true, use APOP instead of USER/PASS to login to the POP3 server, if the POP3 server supports APOP. APOP sends a digest of the password rather than the clear text password. Defaults to false
		    props.put("mail.pop3.starttls.enable", "true"); // If true, enables the use of the STLS command (if supported by the server) to switch the connection to a TLS-protected connection before issuing any login commands. If the server does not support STARTTLS, the connection continues without the use of TLS; see the mail.pop3.starttls.required property to fail if STARTTLS isn't supported. Note that an appropriate trust store must configured so that the client will trust the server's certificate. Defaults to false.
		    props.put("mail.pop3.starttls.required", "true");
//		    props.put("mail.pop3.disablecapa", "true"); // If set to true, the POP3 CAPA command will not be used to fetch server capabilities. This is useful for POP3 servers that don't properly implement the CAPA command, or that provide incorrect information in the CAPA command results. Defaults to false.

			props.put("mail.pop3.auth", "true"); 	// If true, attempt to authenticate the user using the AUTH command. Defaults to false.
		    props.put("mail.pop3.auth.mechanisms","NTLM"); // NT user name and domain to be specified
		    props.put("mail.pop3.auth.ntlm.domain","WEDI");
		    
			Session session = Session.getInstance(props);
//		    Session session = Session.getInstance(props,new MyAuthenticator(mailServerUsername, mailServerPassword));
		    session.setDebug(true);
			Store store = session.getStore("pop3");
//		    POP3Store store = new POP3Store(session, new URLName("pop3://osvaldo.lucchini:Qelppa12@mailserver.wedi.de:110/INBOX"));
//		    store.connect();
			store.connect(mailsServerHost, 110, mailServerUsername, mailServerPassword);
			
			log.debug(store);
	
			Folder[] f = store.getDefaultFolder().list();
			for(Folder fd:f)
				log.debug(">> "+fd.getName());			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void imap_1()
	{
		try
		{
			Properties props = System.getProperties();
			
			props.setProperty("mail.imap.ssl.enable", "true");

			Session session = Session.getInstance(props);
			Store store = session.getStore("imap");
			store.connect(mailsServerHost, 443, mailServerUsername, mailServerPassword);

			Folder inbox = store.getFolder("INBOX");
			inbox.open(Folder.READ_ONLY);
			log.debug("inbox openend: " + inbox.getMessageCount());
			inbox.close(false);
			store.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

	}


	public static void imap()
	{
		Properties props = System.getProperties();
	    props.setProperty("mail.imaps.sasl.enable", "true");
	    props.setProperty("mail.imaps.starttls.enable", "true");

	    props.setProperty("mail.imaps.auth.ntlm.disable", "true");
	    props.setProperty("mail.imaps.auth.plain.disable", "true");
	    props.setProperty("mail.imaps.auth.gssapi.disable", "true");
	    props.setProperty("mail.imaps.ssl.enable", "true");
//	    props.setProperty("mail.imaps.ssl.trust", "*");
//	    props.setProperty("javax.net.ssl.keyStore","/archive/Dev/Projects/wedi/orderUploader/docs/cacerts");
//	    props.setProperty("javax.net.ssl.keyStorePassword","keystore");
	    
	    Session session = Session.getInstance(props);
	    session.setDebug(true);
	    try {
			Store store = session.getStore("imaps");
			store.connect(mailsServerHost, 443, mailServerUsername, mailServerPassword);
		} 
	    catch (MessagingException e) {
			e.printStackTrace();
		}
	}
	
	public static void gmailReader(ApplicationProperties ap)
	{
		log.debug("gmailReader started");
		Properties props = new Properties();
		try 
		{
		    props.put("mail.imaps.ssl.enable", "true");
			Session session = Session.getInstance(props);
			Store store = session.getStore("imaps");
			log.debug("connecting to mail server " + ap.getMailHost() + " as " + ap.getMailUser());
			store.connect(ap.getMailHost(), ap.getMailUser(), ap.getMailPassword());

			Folder inbox = store.getFolder("inbox");
			inbox.open(Folder.READ_WRITE);
			int messageCount = inbox.getMessageCount();
			log.debug(messageCount + " messages pending in INBOX");

			log.debug("Total Messages:- " + messageCount);
	        Folder trashFolder = store.getFolder("[Gmail]/Cestino");
			trashFolder.open(Folder.READ_WRITE);

	        Folder errorsFolder = store.getFolder("errori");
	        errorsFolder.open(Folder.READ_WRITE);
	        
	        log.debug("------------------------------");
			for(Message m : inbox.getMessages())
			{
				log.debug("Mail Subject:- " + m.getSubject());
				String mailTo = m.getRecipients(RecipientType.TO)[0].toString();
				String contentType = m.getContentType();
				if (contentType.contains("multipart"))
				{
					Multipart multiPart = (Multipart) m.getContent();
					for (int y = 0; y < multiPart.getCount(); y++) 
					{
					    MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(y);
					    if ((part.getFileName() != null) && part.getFileName().startsWith("Conferma") &&
					    	Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) 
					    {
					    	try
					    	{
					    		PDFReader.getDataFromPDF(part.getInputStream(), ap, mailTo);
					    	}
					    	catch(Exception e1)
					    	{
					    		break;
					    	}
					    }
					}
				}
				m.getFolder().copyMessages(new Message[] {m}, trashFolder);
			}
			inbox.close(true);
			trashFolder.close(true);
			errorsFolder.close(true);
			store.close();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}

	public static void gmailReaderTest(ApplicationProperties ap)
	{
		log.debug("gmailReader started");
		Properties props = new Properties();
		try 
		{
		    props.put("mail.imaps.ssl.enable", "true");
			Session session = Session.getInstance(props);
			Store store = session.getStore("imaps");
			log.debug("connecting to mail server");
			store.connect("imap.gmail.com", "ordiniwedi@gmail.com","G620sclp@");

			Folder inbox = store.getFolder("archivioOrdini");
			inbox.open(Folder.READ_ONLY);
			int messageCount = inbox.getMessageCount();
			log.debug(messageCount + " messages pending in INBOX");

			log.debug("Total Messages:- " + messageCount);

			log.debug("------------------------------");
			for(Message m : inbox.getMessages())
			{
				log.debug("Mail Subject:- " + m.getSubject());
				String mailTo = m.getRecipients(RecipientType.TO)[0].toString();
				String contentType = m.getContentType();
				if (contentType.contains("multipart"))
				{
					Multipart multiPart = (Multipart) m.getContent();
					for (int y = 0; y < multiPart.getCount(); y++) 
					{
					    MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(y);
					    if ((part.getFileName() != null) && part.getFileName().startsWith("Conferma") &&
					    	Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) 
					    {
					    	try
					    	{
					    		PDFReader.getDataFromPDF(part.getInputStream(), ap, mailTo);
					    	}
					    	catch(Exception e1)
					    	{
					    		break;
					    	}
					    }
					}
				}
			}
			inbox.close(true);
			store.close();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}

}
