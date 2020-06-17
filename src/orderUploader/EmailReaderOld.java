package orderUploader;

import java.util.Properties;

import javax.mail.Session;
import javax.mail.Store;

public class EmailReaderOld {

	public EmailReaderOld() {
		Properties props = System.getProperties();
		// Set manual Properties
//		props.setProperty("mail.imaps.socketFactory.class", javax.net.ssl.SSLSocketFactory.class);
//		props.setProperty("mail.imaps.socketFactory.fallback", "false");
		props.setProperty("mail.imap.ssl.enable", "true");
		props.setProperty("mail.imap.starttls.enable", "true");
		props.setProperty("mail.imaps.port", "993");
		props.setProperty("mail.imaps.socketFactory.port", "993");
		props.setProperty("mail.imaps.host", "mailserver.wedi.de");



		try {
		    /* Create the session and get the store for read the mail. */

		    Session session = Session.getDefaultInstance(System.getProperties(), null);
		    Store store = session.getStore("imaps");
		    props.setProperty("mail.imaps.auth.plain.disable", "true");
		    props.setProperty("mail.imaps.auth.ntlm.disable", "true");
		
		    store.connect("mailserver.wedi.de", "osvaldo.lucchini@wedi.it\\ordini", "Qelppa12");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
