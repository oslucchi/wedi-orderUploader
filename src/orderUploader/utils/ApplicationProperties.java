package orderUploader.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


import org.apache.log4j.Logger;

public class ApplicationProperties {
	// site specific
	private boolean siteSpecificPropsEnabled = true;
	private String defaultLang = "";
	
	// package properties
	private boolean envSpecificPropsEnabled = true;
	private String mailHost = "";
	private String mailUser = "";
	private String mailPassword = "";
	private String dbConnectionURL = "";
	private String dbUser = "";
	private String dbPassword = "";
	
	private static ApplicationProperties instance = null;
	
	final Logger log = Logger.getLogger(this.getClass());
	
	public static ApplicationProperties getInstance()
	{
		if (instance == null)
		{
			instance = new ApplicationProperties();
		}
		return(instance);
	}
	
	private ApplicationProperties()
	{
		log.trace("ApplicationProperties start");
		Properties properties = new Properties();
		
		if (siteSpecificPropsEnabled)
		{
			try 
	    	{
	    		log.debug("path of abs / '" + ApplicationProperties.class.getResource("/").getPath() + "'");
	        	InputStream in = ApplicationProperties.class.getResourceAsStream("/resources/package.properties");
	        	if (in == null)
	        	{
	        		log.error("resource path not found");
	        		return;
	        	}
	        	properties.load(in);
		    	in.close();
			}
	    	catch(IOException e) 
	    	{
				log.warn("Exception " + e.getMessage(), e);
	    		return;
			}
			defaultLang = properties.getProperty("defaultLang");
		}
		
		if (envSpecificPropsEnabled)
		{
	    	String envConf = System.getProperty("envConf");
	    	try 
	    	{
	    		properties = new Properties();
	    		String siteProps = "/resources/site." + (envConf == null ? "dev" : envConf) + ".properties";
	    		log.debug("Use " + siteProps);
	        	InputStream in = ApplicationProperties.class.getResourceAsStream(siteProps);        	
				properties.load(in);
		    	in.close();
			}
	    	catch(IOException e) 
	    	{
				log.warn("Exception " + e.getMessage(), e);
	    		return;
			}
	    	mailHost = properties.getProperty("mailHost");
	    	mailUser = properties.getProperty("mailUser");
	    	mailPassword = properties.getProperty("mailPassword");
	    	dbConnectionURL = properties.getProperty("dbConnectionURL");
	    	dbUser = properties.getProperty("dbUser");
	    	dbPassword = properties.getProperty("dbPassword");
		}
	}

	public String getMailHost() {
		return mailHost;
	}

	public void setMailHost(String mailHost) {
		this.mailHost = mailHost;
	}

	public String getMailUser() {
		return mailUser;
	}

	public void setMailUser(String mailUser) {
		this.mailUser = mailUser;
	}

	public String getMailPassword() {
		return mailPassword;
	}

	public void setMailPassword(String mailPassword) {
		this.mailPassword = mailPassword;
	}

	public String getDbConnectionURL() {
		return dbConnectionURL;
	}

	public void setDbConnectionURL(String dbConnectionURL) {
		this.dbConnectionURL = dbConnectionURL;
	}

	public String getDbUser() {
		return dbUser;
	}

	public void setDbUser(String dbUser) {
		this.dbUser = dbUser;
	}

	public String getDbPassword() {
		return dbPassword;
	}

	public void setDbPassword(String dbPassword) {
		this.dbPassword = dbPassword;
	}

	public String getDefaultLang() {
		return defaultLang;
	}

	public void setDefaultLang(String defaultLang) {
		this.defaultLang = defaultLang;
	}
	
}