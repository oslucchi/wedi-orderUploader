package orderUploader.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;


public class Utils {
	final static Logger log = Logger.getLogger(Utils.class);
	
	static ApplicationProperties prop = ApplicationProperties.getInstance();
	HashMap<String, Object>jsonResponse = new HashMap<>();
	
	public static String printStackTrace(Exception e)
	{
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		return (sw.toString()); 
	}
	
	public static int setLanguageId(String language)
	{
		if (language == null)
		{
			language = prop.getDefaultLang();
		}
		return Constants.getLanguageCode(language);
	}

	public void addToJsonContainer(String key, Object object, boolean clear)
	{
		if (clear)
		{
			jsonResponse.clear();
		}
		jsonResponse.put(key, object);
	}
	
	public static String[] removeEmptyEntries(String[] input)
	{
		List<String> out = new ArrayList<String>();
		String[] retVal;
		for(int i = 0; i < input.length; i++)
		{
			if (input[i].length() > 0)
			{
				out.add(input[i].replaceAll(",", "."));
			}
		}
		retVal = out.toArray(new String[0]);
		return (retVal);
	}
	
	public static String quoteDBSpecialChar(String in)
	{
		String out;
		out = in.replaceAll("\\\\", " ").replaceAll("\'",  "\\\\'");
		return out;
	}

}